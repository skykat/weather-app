package practice.interview.weatherapplication.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import static practice.interview.weatherapplication.data.WeatherContract.WeatherEntry.TABLE_NAME;

/**
 * Created by karen
 */

public class WeatherProvider extends ContentProvider {
    static final int WEATHER = 100;
    static final int WEATHER_WITH_ZIP = 101;
    static final int WEATHER_WITH_CITY = 102;
    static final int WEATHER_WITH_ID = 103;


    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WeatherDbHelper mOpenHelper;
    private static final String mLocationZipSelection = WeatherContract.WeatherEntry.COLUMN_ZIP + " = ? ";
    private static final String mLocationCitySelection = WeatherContract.WeatherEntry.COLUMN_CITY_NAME + " = ? ";
    private static final String mLocationLatLonSelection = WeatherContract.WeatherEntry.COLUMN_LAT + " = ? and " + WeatherContract.WeatherEntry.COLUMN_LON + " = ? " ;

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor = null;
        switch (sUriMatcher.match(uri)) {
            case WEATHER_WITH_ZIP: {
                String locationSetting = WeatherContract.WeatherEntry.getLocationFromUri(uri);
                selection = mLocationZipSelection;
                Log.d("WEATHER_WITH_ZIP", "query, uri: " + uri);
                selectionArgs = new String[]{locationSetting};
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case WEATHER_WITH_CITY: {
                String locationSetting = WeatherContract.WeatherEntry.getLocationFromUri(uri);
                selection = mLocationCitySelection;
                Log.d("WEATHER_WITH_CITY", "query, uri: " + uri);
                selectionArgs = new String[]{locationSetting};
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case WEATHER_WITH_ID:
                String rowId = WeatherContract.WeatherEntry.getLocationFromUri(uri);
                selection = "_ID = ?";
                selectionArgs = new String[]{rowId};
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case WEATHER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
        }
        Log.d("provider", "query uri: " + uri);
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_ZIP:
                return WeatherContract.WeatherEntry.CONTENT_TYPE_ZIP;
            case WEATHER_WITH_CITY:
                return WeatherContract.WeatherEntry.CONTENT_TYPE_CITY;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri = null;
        long _id = db.insert(TABLE_NAME, null, values);
        if (_id > 0) {
            returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
            Log.d("INSERT", "uri: " + returnUri);
            getContext().getContentResolver().notifyChange(returnUri, null, true);
        }

        //getContext().getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null, true);

        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                //getContext().getContentResolver().notifyChange(uri, null);
                Log.d("Provider", " uri: " + uri);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/#", WEATHER_WITH_ID);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/" + WeatherContract.PATH_ZIP + "/*", WEATHER_WITH_ZIP);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/" + WeatherContract.PATH_CITY + "/*", WEATHER_WITH_CITY);
        return matcher;
    }
}
