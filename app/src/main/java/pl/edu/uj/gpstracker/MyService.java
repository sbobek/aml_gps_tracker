package pl.edu.uj.gpstracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MyService extends Service implements LocationListener {

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        MyService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private static final String TAG = "Tracking Service";
    private static final int ONE_MINUTE = 1000 * 60;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int RECORD_FREQ = ONE_MINUTE / 60;


    private LocationManager mLocationManager;
    private Location currentLocation;


    @Override
    public void onCreate() {
        Toast.makeText(this, "Tracking Service Created", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onCreate");
        registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        currentLocation = null;

        Toast.makeText(this, "Tracking Service Started", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStart");

        Location gpsLocation = null;
        Location networkLocation = null;
        // Request updates from both fine (gps) and coarse (network) providers.
        gpsLocation = requestUpdatesFromProvider(
                LocationManager.GPS_PROVIDER,RECORD_FREQ);
        networkLocation = requestUpdatesFromProvider(
                LocationManager.NETWORK_PROVIDER,RECORD_FREQ);

        if (gpsLocation != null && networkLocation != null) {
            currentLocation =  getBetterLocation(gpsLocation, networkLocation);
        } else if (gpsLocation != null) {
            currentLocation= gpsLocation;
        } else if (networkLocation != null) {
            currentLocation = networkLocation;
        }
    }


    /** Determines whether one Location reading is better than the current Location fix.
     * Code taken from
     * http://developer.android.com/guide/topics/location/obtaining-user-location.html
     *
     * @param newLocation  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new
     *        one
     * @return The better Location object based on recency and accuracy.
     */
    protected Location getBetterLocation(Location newLocation, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return newLocation;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return newLocation;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return currentBestLocation;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return newLocation;
        } else if (isNewer && !isLessAccurate) {
            return newLocation;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return newLocation;
        }
        return currentBestLocation;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Method to register location updates with a desired location provider.  If the requested
     * provider is not available on the device, the app displays a Toast with a message referenced
     * by a resource id.
     *
     * @param provider Name of the requested provider.
     * @return A previously returned {@link android.location.Location} from the requested provider,
     *         if exists.
     */
    @SuppressLint("MissingPermission")
    private Location requestUpdatesFromProvider(final String provider, int frequency) {
        Location location = null;

        if (mLocationManager.isProviderEnabled(provider)) {
                mLocationManager.requestLocationUpdates(provider, frequency, 0, this);
                location = mLocationManager.getLastKnownLocation(provider);

        } else {
            //onProviderNotExists();
        }
        return location;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Tracking Service Stopped", Toast.LENGTH_LONG).show();
        unregisterReceiver(batteryInfoReceiver);
        Log.d(TAG, "onDestroy");
        mLocationManager.removeUpdates(this);
    }


    @Override
    synchronized public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        currentLocation = location;
        Log.d(TAG,location.toString());
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }


    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        synchronized public void onReceive(Context context, Intent intent) {
            int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            if(batteryLevel < 30)
                Toast.makeText(context,"Battery level low",Toast.LENGTH_LONG).show();
        }
    };
}