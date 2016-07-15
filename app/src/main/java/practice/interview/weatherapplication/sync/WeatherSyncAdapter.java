package practice.interview.weatherapplication.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import practice.interview.weatherapplication.R;
import practice.interview.weatherapplication.Utility;
import practice.interview.weatherapplication.data.WeatherContract;

import static practice.interview.weatherapplication.data.WeatherContract.CONTENT_AUTHORITY;


/**
 * Sync adapter to transfer data from server to the content provider
 */

public class WeatherSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOGTAG = WeatherSyncAdapter.class.getName();

    private static final String SYNC_EXTRAS_LOCATION_TYPE = "sync_location_type";
    private static final String SYNC_EXTRAS_LOCATION = "sync_location";
    private static final String WEATHER = "weather";
    private static final String WEATHER_LIST = "list";
    private static final String WEATHER_CITY = "city";
    private static final String WEATHER_CITY_ID = "id";
    private static final String WEATHER_CITY_NAME = "name";
    private static final String WEATHER_COORD = "coord";
    private static final String WEATHER_LAT = "lat";
    private static final String WEATHER_LON = "lon";
    private static final String WEATHER_DESCRIPTION = "description";
    private static final String WEATHER_TEMP = "temp";
    private static final String WEATHER_TEMP_MIN = "temp_min";
    private static final String WEATHER_TEMP_MAX = "temp_max";
    private static final String WEATHER_MAIN = "main";
    private static final String WEATHER_DATE = "dt";
    private static final String WEATHER_ICON = "icon";


    public static final int SYNC_INTERVAL = 60 * 180;

    private static String WEATHER_BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
    private static String PARAM_QUERY = "q";
    private static String PARAM_LAT = "lat";
    private static String PARAM_LON = "lon";
    private static String PARAM_ZIP = "zip";
    private static String PARAM_APPID = "APPID";
    private static String PARAM_UNITS = "units";
    private static String PARAM_METRIC = "metric"; // Celsius


    ContentResolver mContentResolver;

    /**
     * Set up the sync adapter
     *
     * @param context
     * @param autoInitialize
     */
    public WeatherSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }


    /**
     * Set up the sync adapter and maintain compatiability with Android 3.0
     *
     * @param context
     * @param autoInitialize
     * @param allowParallelSyncs
     */
    public WeatherSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOGTAG, "onPerformSync");
        String locationType = extras.getString(SYNC_EXTRAS_LOCATION_TYPE) != null ? extras.getString(SYNC_EXTRAS_LOCATION_TYPE) : "";
        String location = extras.getString(SYNC_EXTRAS_LOCATION);
        downloadWeatherData(locationType, location);
        Log.d(LOGTAG, "Network syncing complete...");
    }


    private void downloadWeatherData(String locationType, String location) {

        Log.d(LOGTAG, "downloadWeatherData()");
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonString = null;
        String cityUserInput = null;
        String zip = null;

        try {
            Context context = getContext();

            String appId = "<REPLACE_WITH_OWN_ID_HERE>";
            Uri.Builder uriBuilder = Uri.parse(WEATHER_BASE_URL).buildUpon();

            if (location == null) {
                location = Utility.getDefaultLocation(context);
            }

            if (locationType != null && locationType.equals(Utility.LOCATION_TYPE_CITY)) {
                cityUserInput = location;
                uriBuilder.appendQueryParameter(PARAM_QUERY, location);
            } else if (locationType != null && locationType.equals(Utility.LOCATION_TYPE_GPS)) {
                String[] parts = location.split(",");
                uriBuilder.appendQueryParameter(PARAM_LAT, parts[0]);
                uriBuilder.appendQueryParameter(PARAM_LON, parts[1]);
            } else {
                zip = location;
                uriBuilder.appendQueryParameter(PARAM_ZIP, zip);
            }
            uriBuilder.appendQueryParameter(PARAM_UNITS, PARAM_METRIC);

            Uri builtUri = uriBuilder.appendQueryParameter(PARAM_APPID, appId).build();
            Log.d(LOGTAG, "builtUri: " + builtUri.toString());
            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                Log.d(LOGTAG, "Stream is empty!");
                return;
            }
            jsonString = buffer.toString();
            getWeatherData(jsonString, cityUserInput, zip);
        } catch (IOException e) {
            Log.e(LOGTAG, e.getMessage(), e);
        } catch (JSONException e) {
            Log.e(LOGTAG, e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOGTAG, "Error: ", e);
                }
            }
        }
    }

    private void getWeatherData(String weatherJsonStr, String userInputCity, String zip) throws JSONException {

        try {
            // TODO: check for errors in response and handle error response codes
            Log.d(LOGTAG, "getWeatherData(),  weatherJsonStr: " + weatherJsonStr);
            JSONObject weatherJson = new JSONObject(weatherJsonStr);
            JSONObject coordinates = weatherJson.getJSONObject(WEATHER_COORD);
            double longitude = coordinates.getDouble(WEATHER_LON);
            double latitude = coordinates.getDouble(WEATHER_LAT);
            JSONObject main = weatherJson.getJSONObject(WEATHER_MAIN);
            double temperature = main.getDouble(WEATHER_TEMP);
            double temperatureHigh = main.getDouble(WEATHER_TEMP_MAX);
            double temperatureLow = main.getDouble(WEATHER_TEMP_MIN);
            JSONArray weatherArray = weatherJson.getJSONArray(WEATHER);
            String description = weatherArray.getJSONObject(0).getString(WEATHER_DESCRIPTION);
            int cityId = weatherJson.getInt(WEATHER_CITY_ID);
            int date = weatherJson.getInt(WEATHER_DATE);
            String cityName = userInputCity == null ? weatherJson.getString(WEATHER_CITY_NAME) : userInputCity;

            // set default values
            if (zip != null) {
                Utility.setDefaultLocation(getContext(), Utility.LOCATION_TYPE_ZIP, zip);
            } else {
                Log.d(LOGTAG, " setting default location to cityName: " + cityName);
                Utility.setDefaultLocation(getContext(), Utility.LOCATION_TYPE_CITY, cityName);
            }

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_CITY_ID, cityId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, date);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_CITY_NAME, cityName);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_ZIP, zip);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LAT, latitude);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LON, longitude);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_TEMP, temperature);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_TEMP_MAX, temperatureHigh);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_TEMP_MIN, temperatureLow);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DESCRIPTION, description);
            Log.d(LOGTAG, " cityName: " + weatherValues.get(WeatherContract.WeatherEntry.COLUMN_CITY_NAME));
            getContext().getContentResolver().insert(WeatherContract.WeatherEntry.CONTENT_URI, weatherValues);

            //TODO: delete old data so we don't build up an endless history

        } catch (JSONException e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
    }


    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        Log.d(LOGTAG, "getSyncAccount()");
        if (null == accountManager.getPassword(newAccount)) {
            // Add account, account type, no password or user data
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                Log.d(LOGTAG, "getSyncAccount() addAccountExplicitly");
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            Log.d(LOGTAG, "getSyncAccount() getPassword is null ");
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        Log.d(LOGTAG, "onAccountCreated " + newAccount.name);
        ContentResolver.setIsSyncable(newAccount, CONTENT_AUTHORITY, 1);
        //WeatherSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        //syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        Log.d("syncadapter", "initializeSyncAdapter");
        getSyncAccount(context);
    }

    /**
     * Helper to schedule periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Log.d("configurePeriodicSync", "configurePeriodicSync");
        Account account = getSyncAccount(context);

        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * TODO: this should only be synced once a day if the location is stored in the db
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context, String locationType, String location) {
        Log.d(LOGTAG, "syncImmediately, location: " + location);
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putString(SYNC_EXTRAS_LOCATION_TYPE, locationType);
        bundle.putString(SYNC_EXTRAS_LOCATION, location);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }


}
