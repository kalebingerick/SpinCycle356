package com.example.zach.spincycle;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static android.location.LocationProvider.OUT_OF_SERVICE;
import static android.location.LocationProvider.TEMPORARILY_UNAVAILABLE;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class GPS_Display extends AppCompatActivity implements LocationListener {
    private TextView lat;
    private TextView lon;
    private TextView status;
    LocationManager lm;

    @Override
    public void onLocationChanged(Location location) {
        status.setText("Location changed");
        Log.i("TEST", Double.toString(location.getLatitude()));
        Log.i("TEST", Double.toString(location.getLongitude()));
        lat.setText("Lat: " + location.getLatitude());
        //String.format("%10f", location.getLatitude());
        lon.setText("Long: " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle b) {
        if (i == OUT_OF_SERVICE)
            status.setText("Location out of service");
        if (i == TEMPORARILY_UNAVAILABLE)
            status.setText("Location temporarily unavailable");
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps__display);
        lat = (TextView) findViewById(R.id.lat);
        lon = (TextView) findViewById(R.id.lon);
        status = (TextView) findViewById(R.id.status);
        status.setText("Views created");

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Register for location updates --  currently throws a NPE (suspected from "this")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            status.setText("No GPS permissions!");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
            status.setText("Location updates requested");
        } else {
            status.setText("GPS_PROVIDER is not enabled!");
            return;
        }
        Toast.makeText(this, "Location not null" , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PERMISSION_GRANTED)
            status.setText("Permission granted");
        else {
            status.setText("Permission denied!");
            return;
        }
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
            status.setText("Location updates requested");
        }
        else{
            status.setText("GPS_PROVIDER is not enabled!");
        }
    }
}