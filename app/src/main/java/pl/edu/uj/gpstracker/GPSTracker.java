package pl.edu.uj.gpstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class GPSTracker extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback  {

    public class MyBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MyBroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            double lat = intent.getDoubleExtra("LAT", 0);
            double lon = intent.getDoubleExtra("LON", 0);
            gpsData.setText("Latitude: "+lat+" Longitude: "+lon);
        }

    }

    private static final String TAG = "Tracking Service";
    private static final int  PERMISSION_REQUEST_LOCATION=0;
    BroadcastReceiver br = new MyBroadcastReceiver();

    Button buttonStart, buttonStop;
    TextView gpsData;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpstracker);

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        gpsData = (TextView) findViewById(R.id.gpsData);

        IntentFilter filter = new IntentFilter(MyService.LOCATION_BROADCAST_ACTION);
        this.registerReceiver(br, filter);

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
                startService(new Intent(this, MyService.class));
                break;
            case R.id.buttonStop:
                Log.d(TAG, "onClick: stopping srvice");
                stopService(new Intent(this, MyService.class));
                break;

        }
    }
}