package practice.interview.weatherapplication;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import practice.interview.weatherapplication.data.WeatherContract;
import practice.interview.weatherapplication.sync.WeatherSyncAdapter;


/**
 * This is the home activity which allows you to add a weather location via pressing the add button to type a city or zip,
 * or just pressing the map button to grab the current gps coordinates.
 *
 */
public class ActivityMain extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    private static final String TAG = ActivityMain.class.getName();
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_main_add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchAddLocationActivity();
            }
        });

        FloatingActionButton fabLocation = (FloatingActionButton) findViewById(R.id.activity_location_fab);
        fabLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.update_weather_location, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (mLastLocation != null) {
                    Log.d(TAG, "fabLocation click, lat: " + mLastLocation.getLatitude() + " lon: " + mLastLocation.getLongitude());
                    String loc = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
                    queryGpsLocation(loc);
                }
            }
        });

        WeatherSyncAdapter.initializeSyncAdapter(this);
        buildGoogleApiClient();
    }


    @Override
    protected void onResume() {
        FragmentMain mainActivityFragment = (FragmentMain) getSupportFragmentManager().findFragmentById(R.id.fragment_parent);
        if (mainActivityFragment == null) {
            addMainFragment(mainActivityFragment);
        } else {
            mainActivityFragment.updateLoader(Utility.getDefaultLocationType(this), Utility.getDefaultLocation(this));
        }
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    private void addMainFragment(FragmentMain mainActivityFragment) {
        if (mainActivityFragment == null) {
            mainActivityFragment = FragmentMain.newInstance(Utility.getDefaultLocationType(this), Utility.getDefaultLocation(this));
        }

        FragmentMain.WeatherObserver observer = mainActivityFragment.new WeatherObserver(new Handler());
        getContentResolver().registerContentObserver(WeatherContract.WeatherEntry.CONTENT_URI, true, observer);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_parent, mainActivityFragment);
        ft.commit();
    }

    private void launchAddLocationActivity() {
        Intent intent = new Intent(this, ActivityAddLocation.class);
        startActivity(intent);
    }

    public void queryGpsLocation(String gpsLocation) {
        WeatherSyncAdapter.syncImmediately(this, Utility.LOCATION_TYPE_GPS, gpsLocation);
    }

    // Methods below get GPS coordinates
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d(TAG, "onConnected() lat: " + mLastLocation.getLatitude() + " lon: " + mLastLocation.getLongitude());
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


}
