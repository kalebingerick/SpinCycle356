package com.example.zach.spincycle;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    public final static String SPIN_COUNT = "spins";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setMenuButtons();
    }

    private void setMenuButtons() {
        setMenuButton1();
        setMenuButton2();
        setGPSButton();
    }

    private void setMenuButton1(){
        Button testButton = (Button) findViewById(R.id.test_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.second_page);
                setBackButton();
                playSound();
            }
        });
    }

    private void setMenuButton2(){
        Button testButton = (Button) findViewById(R.id.test_button2);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Spin_Display.class);
                intent.putExtra(SPIN_COUNT,2);
                startActivity(intent);
//                setBackButton();
            }
        });
    }

    private void setGPSButton(){
        Button gpsButton = (Button) findViewById(R.id.gps_button);
        gpsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GPS_Display.class);
                startActivity(intent);
            }
        });
    }

    private void setBackButton(){
        Button backButton = (Button) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_main);
                setMenuButtons();
            }
        });
    }

    public void playSound(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onBackPressed(){
        setContentView(R.layout.activity_main);
        setMenuButtons();
    }

}
