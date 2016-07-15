package practice.interview.weatherapplication;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import practice.interview.weatherapplication.sync.WeatherSyncAdapter;

/**
 * Activity to add a location to get the current weather.
 * Add a city or a zip to see the updated weather.
 * If both are filled out, zip will be used.
 */
public class ActivityAddLocation extends AppCompatActivity {


    private static String TAG = ActivityAddLocation.class.getName();
    private EditText mCityEditText;
    private EditText mZipEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        mCityEditText = (EditText) findViewById(R.id.activity_add_city_edit_text);
        mZipEditText = (EditText) findViewById(R.id.activity_add_zip_edit_text);
        Button mAddLocationButton = (Button) findViewById(R.id.activity_add_location_button);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAddLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isZipEmpty = TextUtils.isEmpty(mZipEditText.getText().toString());
                boolean isCityEmpty = TextUtils.isEmpty(mCityEditText.getText().toString());
                if (isZipEmpty && isCityEmpty) {
                    Snackbar.make(v, R.string.add_city_or_zip_title, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    addLocation();
                }

            }
        });
    }

    private void addLocation() {
        Log.d(TAG, "addLocation()");
        String locationType = Utility.LOCATION_TYPE_ZIP;
        String location = mZipEditText.getText() != null ? mZipEditText.getText().toString() : Utility.getDefaultLocation(getApplicationContext());
        if (!TextUtils.isEmpty(mCityEditText.getText())) {
            locationType = Utility.LOCATION_TYPE_CITY;
            location = mCityEditText.getText().toString();
        }
        Utility.setDefaultLocation(this, locationType, location);
        WeatherSyncAdapter.syncImmediately(this, locationType, location);
        finish();
    }


}
