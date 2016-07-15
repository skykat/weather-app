package practice.interview.weatherapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper methods
 */

public class Utility {

    public static String LOCATION_TYPE_ZIP = "zip";
    public static String LOCATION_TYPE_CITY = "city";
    public static String LOCATION_TYPE_GPS = "gps";
    public static String PREF_DEFAULT_LOCATION = "prefDefaultLocation";
    public static String PREF_DEFAULT_LOCATION_TYPE = "prefDefaultLocationType";


    public static void setDefaultLocation(Context context, String locationType, String location) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_DEFAULT_LOCATION_TYPE, locationType);
        editor.putString(PREF_DEFAULT_LOCATION, location);
        editor.commit();
    }

    public static String getDefaultLocationType(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_DEFAULT_LOCATION_TYPE, LOCATION_TYPE_ZIP);
    }

    public static String getDefaultLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_DEFAULT_LOCATION, context.getString(R.string.default_location_zip));
    }

    public static String formatTemperature(Context context, double temperature) {
        temperature = (temperature * 1.8) + 32;
        return String.format(context.getString(R.string.format_temperature), temperature);
    }

}
