package com.example.surfacescannerpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.sql.Timestamp;

public class MovingActivity extends AppCompatActivity {

    TextView txt_accel, txt_prevAccel, txt_currentAccel;

    TextView txt_gyro, txt_prevGyro, txt_currentGyro;

    static final double EPSILON_GYRO = 0.05;
    static final double EPSILON_ACCEL = 0.01;
    static final double alpha = 0.8;
    private double[] gravity = new double[]{0,0,0};

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor nGyroscope;

    private double accelerationCurrentValue = 0;
    private double accelerationPrevValue;

    private double gyroCurrentValue, gyroPrevValue;
    private double currentTime;

    private double xOmega, yOmega, zOmega = 0;
    private double omegaMagnitude = 0;
    private double aX, aY, aZ = 0;
    private double aD, aH = 0;
    private double tetha = 0;
    double sinThetaOverTwo, cosThetaOverTwo = 0;
    private double currentVx, currentVy = 0;
    private double xValue, yValue = 0;

    private static final float NS2S = 1.0f / 1000000000.0f;




    private int pointsPlotted = 15;


    private Viewport viewport;

    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
            /*new DataPoint(0, 1),
            new DataPoint(1, 5),
            new DataPoint(2, 3),
            new DataPoint(3, 2),
            new DataPoint(4, 6)*/
    });


    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                boolean wait_for_normalise = true;

                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                double aXtemp = event.values[0] - gravity[0];
                double aYtemp = event.values[1] - gravity[1];
                double aZtemp = event.values[2] - gravity[2];

                if (aZtemp < 0.01){wait_for_normalise = false;}
                double accelerationCurrentValueTemp = Math.sqrt(aXtemp * aXtemp + aYtemp * aYtemp + aZtemp * aZtemp);
                if (accelerationCurrentValueTemp > EPSILON_ACCEL && !wait_for_normalise){
                    accelerationCurrentValue = accelerationCurrentValueTemp;
                    aX = aXtemp;
                    aY = aYtemp;
                    aZ = aZtemp;
                }
                else{
                    accelerationCurrentValue = 0;
                    aX = 0;
                    aY = 0;
                    aZ = 0;
                }

                /*
                double aXtemp = event.values[0];
                double aYtemp = event.values[1];
                double aZtemp = event.values[2];
                double accelerationCurrentValueTemp = Math.sqrt(aXtemp * aXtemp + aYtemp  * aYtemp  + aZtemp * aZtemp);
                if(accelerationCurrentValueTemp > EPSILON_ACCEL){
                    accelerationCurrentValue = accelerationCurrentValueTemp;
                    aX = aXtemp;
                    aY = aYtemp;
                    aZ = aZtemp;
                }
                else{
                    accelerationCurrentValue = 0;
                    aX = 0;
                    aY = 0;
                    aZ = 0;

                }*/
            }
            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                System.out.println("salaaaaaam!");
                double xOmegaTemp = event.values[0];
                double yOmegaTemp = event.values[1];
                double zOmegaTemp = event.values[2];

                double omegaMagnitudeTemp = Math.sqrt(xOmegaTemp*xOmegaTemp + yOmegaTemp*yOmegaTemp + zOmegaTemp*zOmegaTemp);
                if (omegaMagnitudeTemp > EPSILON_GYRO) {
                    omegaMagnitude = omegaMagnitudeTemp;
                    xOmega = xOmegaTemp/omegaMagnitude;
                    yOmega = yOmegaTemp/omegaMagnitude;
                    zOmega = zOmegaTemp/omegaMagnitude;
                }
                else {
                    omegaMagnitude = 0;
                    xOmega = 0;
                    yOmega = 0;
                    zOmega = 0;
                }
            }

            // get dT which is a very small proportion of time.
            double dT = (event.timestamp - currentTime) * NS2S;

            // updating tetha for upcoming calculations.
            if (dT > 0.05){
                tetha = omegaMagnitude * dT / 2.0f;
                sinThetaOverTwo = Math.sin(tetha);
                cosThetaOverTwo = Math.cos(tetha);
            }


            // update acceleration in z and x directions.
            aD = aZ * cosThetaOverTwo - aX * sinThetaOverTwo;
            aH = aX * cosThetaOverTwo - aZ * sinThetaOverTwo;

            // update velocity in x and y directions.
            if(aX+aY == 0) {
                currentVx = aX * dT + currentVx;
                currentVy = aY * dT + currentVx;
            }
            else{
                currentVx = 0;
                currentVy = 0;
            }
            // update x and y values. we ignore xValue in the diagram
            xValue =  currentVx * dT + xValue;
            yValue =  currentVy * dT + yValue;


            //double theta2 = tetha*10000;
            txt_currentAccel.setText("aY = " + aY);
            txt_prevAccel.setText("aX = " + aX);
            txt_accel.setText("currentTime= " + currentTime);

            txt_currentGyro.setText("xValue = " + (xValue));
            txt_prevGyro.setText("yValue = " + yValue);
            txt_gyro.setText("Current Vx and Vy " + currentVx + " - " + currentVy);

            pointsPlotted++;
            series.appendData(new DataPoint(pointsPlotted, accelerationCurrentValue), true, pointsPlotted);
            viewport.setMaxX(pointsPlotted);
            viewport.setMinX(pointsPlotted - 100);

            // update current time for next calculations
            currentTime = event.timestamp;

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moving);

        Intent intent = getIntent();

        txt_accel = findViewById(R.id.txt_accel);
        txt_currentAccel = findViewById(R.id.txt_currentAccel);
        txt_prevAccel = findViewById(R.id.txt_prevAccel);

        txt_gyro=findViewById(R.id.txt_gyro);
        txt_currentGyro = findViewById(R.id.txt_currentGyro);
        txt_prevGyro = findViewById(R.id.txt_prevGyro);


        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        nGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        viewport = graph.getViewport();
        viewport.setScrollable(true);
        viewport.setXAxisBoundsManual(true);
        //viewport.setYAxisBoundsManual(true);
        //viewport.setMaxY(10);
        graph.addSeries(series);

    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(sensorEventListener, nGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(sensorEventListener);
    }

}