package pl.edu.uj.gpstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class GPSTracker extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback  {


    private static final String BINDING_STATUS = "pl.edu.uj.gpstracker.BINDING_STATUS";
    MyService mService;
    boolean mBound = false;

    private static final String TAG = "Tracking Service";
    private static final int  PERMISSION_REQUEST_LOCATION=0;

    Button buttonStart, buttonStop;
    TextView gpsData;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpstracker);

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        gpsData = (TextView) findViewById(R.id.gpsData);

        if(savedInstanceState != null) {
            mBound = savedInstanceState.getBoolean(BINDING_STATUS);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mBound) {
            Log.d(TAG, "Binding to service");
            Intent intent = new Intent(this, MyService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(mBound) {
            Log.d(TAG, "Unbinding from service");
            unbindService(connection);
        }
    }

    /**
     * Requests the {@link android.Manifest.permission#CAMERA} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG,"App dialog was gone, callback launch");
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            // Request for camera permission.
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Log.d(TAG, "All permisions granted");

            } else {
                // Permission request was denied.
                Log.d(TAG, "Not all permisions granted");

            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    public void onClick(View src) {
        requestLocationPermission();
        switch (src.getId()) {
            case R.id.buttonStart:
                Log.d(TAG, "onClick: starting srvice");
                //startService(new Intent(this, MyService.class));
                Intent intent = new Intent(this, MyService.class);
                bindService(intent, connection, Context.BIND_AUTO_CREATE);
                break;
            case R.id.buttonStop:
                Log.d(TAG, "onClick: stopping srvice");
                //stopService(new Intent(this, MyService.class));
                unbindService(connection);
                mBound = false;
                break;

        }
    }

    public void refreshLocation(View view){
        if(mBound){
            Location location = mService.getCurrentLocation();
            if(location != null) {
                gpsData.setText("Lat: " + location.getLatitude() + " Lon: " + location.getLongitude());
            }else{
                gpsData.setText("No location updates yet.");
            }
        }
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the answer
        outState.putBoolean(BINDING_STATUS, mBound);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}