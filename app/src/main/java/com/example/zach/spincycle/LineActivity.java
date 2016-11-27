package com.example.zach.spincycle;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class LineActivity extends AppCompatActivity implements SensorEventListener{

    public final int numSteps = 10;

    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    private Sensor mOrient;
    private TextView status;
    private TextView accelText;
    private TextView stepText;
    private float   mLimit = 1;
    private float   mLastValues[] = new float[3*2];
    private float   mScale[] = new float[2];
    private float   mYOffset;

    private float   mLastDirections[] = new float[3*2];
    private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
    private float   mLastDiff[] = new float[3*2];
    private int     mLastMatch = -1;
    private int pointsRecorded = 0;
    private int iOrient = 0;
    private float[] orientations = new float[numSteps];
    private float[] curOrient = new float[3];           //used to smooth out data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrient = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        status = (TextView) findViewById(R.id.status);
        accelText = (TextView) findViewById(R.id.accel);
        stepText = (TextView) findViewById(R.id.stepCount);
        int h = 480;
        mYOffset = h * 0.5f;
        mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));

        if(mSensorManager.registerListener(this, mOrient, SensorManager.SENSOR_DELAY_UI)){
            status.setText("Listeners registered");
        }

        mSensorListener = new SensorEventListener() {
            int steps = 0;
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {

                float vSum = 0;
                for (int i=0 ; i<3 ; i++) {
                    final float v = mYOffset + event.values[i] * mScale[1];
                    vSum += v;
                }
                int k = 0;
                float v = vSum / 3;

                float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
                if (direction == - mLastDirections[k]) {
                    // Direction changed
                    int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
                    mLastExtremes[extType][k] = mLastValues[k];
                    float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                    if (diff > mLimit) {

                        boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k]*2/3);
                        boolean isPreviousLargeEnough = mLastDiff[k] > (diff/3);
                        boolean isNotContra = (mLastMatch != 1 - extType);

                        if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                            steps++;
                            if(steps > numSteps){
                                mSensorManager.unregisterListener(this);
                                return;
                            }
                            stepText.setText("Steps: " + steps);
                            iOrient = 0;
                            mLastMatch = extType;
                        }
                        else {
                            mLastMatch = -1;
                        }
                    }
                    mLastDiff[k] = diff;
                }
                mLastDirections[k] = direction;
                mLastValues[k] = v;
            }
        };
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Called when there is a new sensor event.  Note that "on changed"
     * is somewhat of a misnomer, as this will also be called if we have a
     * new reading from a sensor with the exact same sensor values (but a
     * newer timestamp).
     * <p>
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     * <p>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        accelText.setText("Angle: " + Math.abs(180-event.values[0]));
        if(iOrient > -1){
            if(pointsRecorded >= numSteps){
                float sd = 0f;
                float sum = 0f;
                mSensorManager.unregisterListener(this);
                for(float f : orientations)
                    sum += f;
                float average = sum / pointsRecorded;
                //status.setText("Average orientation = " + average);
                for(float f : orientations)
                    sd += Math.pow(f - average,2) / pointsRecorded;
                Double stdDev = Math.sqrt(sd);
                accelText.setText("Std dev = " + stdDev);
                int score = stdDev < 2 ? 100 : (int)(102 - stdDev);
                status.setText("Score = " + score);
                return;
            }
            orientations[pointsRecorded++] = Math.abs( 180 - event.values[0] );
            iOrient = -1;
        }
        status.setText("Yaw: " + event.values[0]);
    }

    /**
     * Called when the accuracy of the registered sensor has changed.  Unlike
     * onSensorChanged(), this is only called when this accuracy value changes.
     * <p>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mOrient, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
