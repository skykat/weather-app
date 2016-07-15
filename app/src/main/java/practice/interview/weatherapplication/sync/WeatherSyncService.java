package practice.interview.weatherapplication.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service to kick off the weather sync adapter
 */
public class WeatherSyncService extends Service {

    // Storage for an instance of the sync adapter
    private static WeatherSyncAdapter sSyncAdapter = null;
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new WeatherSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
