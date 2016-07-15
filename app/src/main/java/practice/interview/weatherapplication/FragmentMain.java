package practice.interview.weatherapplication;

import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import practice.interview.weatherapplication.data.WeatherContract;


/**
 * Displays weather data in fragment
 * Uses a loader to load data in the background from the content provider
 */

public class FragmentMain extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = FragmentMain.class.getName();
    private static final double DEFAULT_SUNNY_TEMP = 20.0;
    private static final String EMPTY_TEXT = "";
    private static final int WEATHER_LOADER = 0;
    private static final String BUNDLE_LOCATION_TYPE = "bundleLocationType";
    private static final String BUNDLE_LOCATION = "bundleLocation";

    private static final String[] WEATHER_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_CITY_ID,
            WeatherContract.WeatherEntry.COLUMN_CITY_NAME,
            WeatherContract.WeatherEntry.COLUMN_ZIP,
            WeatherContract.WeatherEntry.COLUMN_DESCRIPTION,
            WeatherContract.WeatherEntry.COLUMN_LAT,
            WeatherContract.WeatherEntry.COLUMN_LON,
            WeatherContract.WeatherEntry.COLUMN_TEMP_MIN,
            WeatherContract.WeatherEntry.COLUMN_TEMP_MAX,
            WeatherContract.WeatherEntry.COLUMN_TEMP

    };

    // These indicies have to map to WEATHER_COLUMNS and should be updated if WEATHER_COLUMNS changes
    static final int COL_ID = 0;
    static final int COL_DATE = 1;
    static final int COL_CITY_ID = 2;
    static final int COL_CITY_NAME = 3;
    static final int COL_ZIP = 4;
    static final int COL_DESC = 5;
    static final int COL_COORD_LAT = 6;
    static final int COL_COORD_LON = 7;
    static final int COL_WEATHER_MIN_TEMP = 8;
    static final int COL_WEATHER_MAX_TEMP = 9;
    static final int COL_WEATHER_TEMP = 10;


    private TextView mCityNameTextView;
    private TextView mTemperatureDescriptionTextView;
    private TextView mTemperatureTextView;
    private TextView mTemperatureMaxTextView;
    private TextView mTemperatureMinTextView;
    private ImageView mWeatherImageView;


    public FragmentMain() {
    }


    public static FragmentMain newInstance(String locationType, String location) {
        FragmentMain f = new FragmentMain();
        Bundle args = new Bundle();
        args.putString(BUNDLE_LOCATION_TYPE, locationType);
        args.putString(BUNDLE_LOCATION, location);
        f.setArguments(args);
        return f;
    }


    public String getBundleLocationType() {
        return getArguments().getString(BUNDLE_LOCATION_TYPE, Utility.getDefaultLocationType(getActivity()));
    }

    public String getBundleLocation() {
        return getArguments().getString(BUNDLE_LOCATION, Utility.getDefaultLocationType(getActivity()));
    }

    public void updateLoader(String locationType, String location) {
        Log.d(TAG, "updateLoader locationType: " + locationType + " location: " + location);
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_LOCATION_TYPE, locationType);
        bundle.putString(BUNDLE_LOCATION, location);
        getLoaderManager().restartLoader(WEATHER_LOADER, bundle, this);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated()");
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_LOCATION_TYPE, getBundleLocationType());
        bundle.putString(BUNDLE_LOCATION, getBundleLocation());
        getLoaderManager().initLoader(WEATHER_LOADER, bundle, this);
        super.onActivityCreated(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mCityNameTextView = (TextView) rootView.findViewById(R.id.fragment_city_name);
        mTemperatureDescriptionTextView = (TextView) rootView.findViewById(R.id.fragment_description);
        mTemperatureTextView = (TextView) rootView.findViewById(R.id.fragment_temperature);
        mTemperatureMaxTextView = (TextView) rootView.findViewById(R.id.fragment_temperature_max);
        mTemperatureMinTextView = (TextView) rootView.findViewById(R.id.fragment_temperature_min);
        mWeatherImageView = (ImageView) rootView.findViewById(R.id.fragment_image);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d("onCreateLoader", "onCreateLoader");
        String locationType = bundle.getString(BUNDLE_LOCATION_TYPE);
        String location = bundle.getString(BUNDLE_LOCATION);
        Uri weatherForLocationUri = null;
        if(locationType.equals(EMPTY_TEXT)) {
            weatherForLocationUri =  WeatherContract.WeatherEntry.buildWeatherUri(Long.parseLong(location));
        }
        else {
            weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocation(locationType, location);
        }
        Log.d("onCreateLoader", "weatherForLocationUri: " + weatherForLocationUri);
        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                WEATHER_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // if using an adapter, swapCursor here
        // only display the most recently updated item
        Log.d("onLoadFinished", "data.getCount: " + data.getCount());
        if (data.moveToLast()) {
            Log.d(TAG, " onLoadFinished(), city name: " + data.getString(COL_CITY_NAME));
            updateView(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // if using an adapter, swapCursor here
    }

    private void updateView(Cursor data) {
        String temperature = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_TEMP));
        String maxTemp = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP));
        String minTemp = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP));
        mCityNameTextView.setText(data.getString(COL_CITY_NAME));
        mTemperatureDescriptionTextView.setText(data.getString(COL_DESC));
        mTemperatureTextView.setText(temperature);
        mTemperatureMaxTextView.setText(maxTemp);
        mTemperatureMinTextView.setText(minTemp);

        if(data.getDouble(COL_WEATHER_TEMP) > DEFAULT_SUNNY_TEMP){
            // image from clipartpanda.com
            mWeatherImageView.setImageResource(R.drawable.sun_happy);
        }else{
            // image from 123rf.com
            mWeatherImageView.setImageResource(R.drawable.sun_sad);
        }
        // TODO: update imageview
    }


    public class WeatherObserver extends ContentObserver {

        public WeatherObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            long rowId = ContentUris.parseId(uri);
            FragmentMain.this.updateLoader(EMPTY_TEXT, rowId+EMPTY_TEXT);
        }
    }

}
