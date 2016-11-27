package com.example.zach.spincycle;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.location.LocationProvider.OUT_OF_SERVICE;
import static android.location.LocationProvider.TEMPORARILY_UNAVAILABLE;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class GPS_Display extends AppCompatActivity implements LocationListener {
    private TextView lat;
    private TextView lon;
    private TextView status;
    private ArrayList<Coords> linePts = new ArrayList<>();
    long startTime = 0;
    LocationManager lm;

    @Override
    public void onLocationChanged(Location location) {
        if(startTime == 0)
            startTime = System.nanoTime();
        status.setText("Location changed");
        /*
        Log.i("TEST", Double.toString(location.getLatitude()));
        Log.i("TEST", Double.toString(location.getLongitude())); 
        */
        lat.setText("Lat: " + location.getLatitude());
        lon.setText("Long: " + location.getLongitude());
        linePts.add(new Coords(location.getLatitude(), location.getLongitude()));
        //if((System.nanoTime() - startTime) > 10000){
        if(linePts.size() > 9){
            GPS_Display.this.killGPS();
            GPS_Display.this.processPoints();
        }
        //Just show it every time there's an update, figure out why its so slow
        TextView coordsView = (TextView) findViewById(R.id.coordsView);
        coordsView.setText(linePts.toString());
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
        setButtons();
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
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            status.setText("Location updates requested");
        } else {
            status.setText("GPS_PROVIDER is not enabled!");
            return;
        }
        Toast.makeText(this, "Location not null", Toast.LENGTH_SHORT).show();
    }

    public void killGPS(){
        if (ActivityCompat.checkSelfPermission(GPS_Display.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(GPS_Display.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.removeUpdates(GPS_Display.this);
        status.setText("Unsubscribed from GPS updates");
    }
    public void processPoints(){
        /**
         * Get difference between first and last points, split into x segments, compare end points with each point in array
         * Do timed, but factor in distance
         */
        Double totalScore = 100.0;  //total score to subtract from. Probably cast as int before display
        Double sumScore = 0.0;
        Double pointScore = (totalScore/(double)linePts.size()); //score per point
        ArrayList<Coords> idealPoints = getIdealCoords();
        for (int i=0; i<linePts.size(); i++){
            Double thisDist = distance(linePts.get(i), idealPoints.get(i));
            Double thisPoint = thisDist > pointScore ? 0 : pointScore - thisDist;
            sumScore += thisPoint;
            //totalScore -= pointScore / distance(linePts.get(i), idealPoints.get(i));
        }
        status.setText("score is " + sumScore);
    }

    private ArrayList<Coords> getIdealCoords() {
        double initLat = linePts.get(0).getLat();
        double initLon = linePts.get(0).getLon();
        double finalLat = linePts.get(linePts.size() - 1).getLat();
        double finalLon = linePts.get(linePts.size() - 1).getLon();
        double latDifference = (finalLat - initLat) / linePts.size();
        double lonDifference = (finalLon - initLon) / linePts.size();

        ArrayList<Coords> idealCoords = new ArrayList<Coords>();
        idealCoords.add(linePts.get(0));
        for (int numPoints = 0; numPoints < linePts.size(); numPoints++) {
            idealCoords.add(new Coords(initLat + ((latDifference) * (numPoints + 1)), initLon + ((lonDifference) * (numPoints + 1))));
        }
        idealCoords.add(linePts.get(linePts.size() - 1));
        return idealCoords;
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

    private void setButtons() {
        setStartButton();
        setStopButton();
        setMainMenuButton();
    }

    /**
     * Store the user's movement
     */
    private void setStartButton() {
        /**Pseudo-code
        1) Get initial lat/long (try, catch (can't get vals --> tell user to go outside or something)
        2) onLocationChanged, or at every x interval of time, store user's new locations
         */
    }

    /**
     * This is where we'll calculate the score
     */
    private void setStopButton() {
        /**Pseudo-code
         1) for lat/lon in lat/long array from setStartButton, compare score to ideal lat/long,
         change score accordingly (e.g., start score @ 100, for every x std. dev., subtract (y /
         number of lat/long values from user's movement) from score

         TODO: How to get ideal endpoint? Once we have it, algorithm to get ideal midpoints (as a
         function of the number of lat/long pairs collected while user was walking) should be simple.
         Will compare each "ideal" midpoint to each actual midpoint to calculate std. dev./how much
         to subtract from starting score (100).
         */
    }

    private void setMainMenuButton() {
        Button backButton = (Button) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GPS_Display.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    /*
    * Calculate distance between two points in latitude and longitude.
    *
    * lat1, lon1 Start point lat2, lon2 End point
    * @returns Distance in Meters
    */
    public static double distance(Coords start, Coords end){
        double lat1 = start.getLat();
        double lat2 = end.getLat();
        double lon1 = start.getLon();
        double lon2 = end.getLon();
        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

    class Coords{
        private double lat, lon;
        public Coords(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
        public double getLat() {return lat;}
        public double getLon() {return lon;}
        @Override
        public String toString() { return "(" + lat + ", " + lon + ')'; }
    }

}