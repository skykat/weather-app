package practice.interview.weatherapplication.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by karen on 6/24/16.
 */

public class WeatherContract {

    public static final String CONTENT_AUTHORITY = "practice.interview.weatherapplication";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WEATHER = "weather";
    public static final String PATH_ZIP = "zip";
    public static final String PATH_CITY = "city";

    public static final class WeatherEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        public static final String CONTENT_TYPE_CITY = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER + "/" + PATH_CITY;
        public static final String CONTENT_TYPE_ZIP = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER + "/" + PATH_ZIP;
                ;

        public static final String TABLE_NAME = "weather";
        public static final String COLUMN_LON = "lon";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_TEMP = "temp";
        public static final String COLUMN_TEMP_MIN = "temp_min";
        public static final String COLUMN_TEMP_MAX = "temp_max";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_CITY_NAME = "city";
        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_ZIP = "zip";
        public static final String COLUMN_DATE = "dt";
        public static final String COLUMN_CITY_ID = "city_id";

        public static Uri buildWeatherUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


        public static String getLocationFromUri(Uri uri) {
            //return uri.getPathSegments().get(2);
            return uri.getLastPathSegment();
        }

        public static Uri buildWeatherLocation(String locationType, String location) {
            return CONTENT_URI.buildUpon().appendPath(locationType).appendEncodedPath(location).build();
        }

    }
}
