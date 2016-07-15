package practice.interview.weatherapplication.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Taken from https://developer.android.com/training/sync-adapters/creating-authenticator.html
 * as a stub authenticator
 * <p>
 * Passes data between the authenticator and the framework
 */

public class WeatherAuthenticatorService extends Service {


    private WeatherAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new WeatherAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
