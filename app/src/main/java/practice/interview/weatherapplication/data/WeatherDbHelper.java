package practice.interview.weatherapplication.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import practice.interview.weatherapplication.data.WeatherContract.WeatherEntry;


public class WeatherDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "interview.weather.db";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_CITY_ID + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_CITY_NAME + " TEXT, " +
                WeatherEntry.COLUMN_ZIP + " TEXT, " +
                WeatherEntry.COLUMN_DESCRIPTION + " TEXT, " +
                WeatherEntry.COLUMN_LAT + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_LON + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_TEMP_MIN + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_TEMP_MAX + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_TEMP + " REAL NOT NULL, " +
                " UNIQUE (" + WeatherEntry.COLUMN_DATE + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
