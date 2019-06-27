package com.example.walkdetector;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import org.w3c.dom.Text;

/*
* The objective of this algorithm is to detect when the user stops walking
* and how many seconds he has spent walking since he opened the app.
*
* It will be used to perform the running speed test of an SPPB test. For this,
* it returns the time during which the user has been walking, which will allow
* to determine the speed in the tests of 4 and 5 meters away.
*
* @Author José Luis López Sánchez
* */
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final int TIME_THRSHOLD = 2000; // Miliseconds

    private float max_sqrt = 0;
    private long iniTime;
    private long lastChangeTime;
    private double walkingTime = 0;
    private boolean end_of_test = false;

    // Accelerometer variables
    private SensorManager sensorManager;
    private Sensor sensorAcc;

    // Layout variables
    private TextView tv_sqrt;
    private TextView tv_walking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Keeps screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Sensor declaration. We use 1Hz frequency to get smoother measurements.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAcc = sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION).get(0);
        sensorManager.registerListener(this, sensorAcc, 1000000);

        tv_sqrt = (TextView) findViewById(R.id.tv_sqrt);
        tv_walking = (TextView) findViewById(R.id.tv_walking);

        // Save the initial time
        iniTime = System.currentTimeMillis();
        lastChangeTime = iniTime;

        tv_walking.setTextColor(Color.RED);
        tv_walking.setText("\nSTILL WALKING");
    }

    @Override
    public void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }

    // Pause sensor listener to save battery and memory.
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Resume sensor listener
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorAcc, 1000000);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(!end_of_test){
            // Values measured on each axis.
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate the length of the movement vector, avoiding dependence
            // on the position of the phone.
            long curTime = System.currentTimeMillis();
            float raiz_q = (float) Math.sqrt(x*x + y*y + z*z);

            // Each time the vector exceeds one third of the maximum registered historical
            // value, the varaible LastChangeTime is updated. The meaning of this variable
            // is the moment in time when the last "step" occurred.
            if (raiz_q > max_sqrt/3){
                lastChangeTime = curTime;
                if(raiz_q > max_sqrt) {
                    max_sqrt = raiz_q;
                }
            }

            long diffChanges = curTime - lastChangeTime;

            // If there is no update of the LastChangeTime variable in the last TIME_THRSHOLD
            // seconds, assume the user is not walking any more, and calculate the time spent walking
            if (diffChanges > TIME_THRSHOLD){
                walkingTime = lastChangeTime - iniTime;
                walkingTime = (double)walkingTime/1000;
                end_of_test = true;
            }

            // Show registered values
            tv_sqrt.setText("\nAcc. values: " + raiz_q);
        } else {
            tv_walking.setTextColor(Color.GREEN);
            tv_walking.setText("\nWALKING TIME: " + walkingTime + " (s).");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
