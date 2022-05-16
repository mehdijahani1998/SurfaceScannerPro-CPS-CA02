package com.example.surfacescannerpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MovingActivity extends AppCompatActivity {

    TextView txt_accel, txt_prevAccel, txt_currentAccel;

    TextView txt_gyro, txt_prevGyro, txt_currentGyro;

    static final double EPSILON_GYRO = 0.4;
    static final double EPSILON_ACCEL = 0.7;
    static final double EPSILON_A = 0.3;


    private double[] deltaRotationVector = new double[4];
    private List<Double> xValues = new ArrayList<>();
    private List<Double> zValues = new ArrayList<>();


    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor nGyroscope;

    private double currentTime = 0;

    private double xOmega, yOmega, zOmega = 0;
    private double xOmegaTemp, yOmegaTemp, zOmegaTemp = 0;
    private double omegaMagnitude = 0;

    private double aX, aY, aZ = 0;
    private double accelerationCurrentValue = 0;

    private double aD, aH = 0;
    private double aDTemp, aHTemp = 0;

    private double tetha = 0;
    private double tetha_temp = 0;
    double sinThetaOverTwo, cosThetaOverTwo = 0;

    private double currentVx, currentVy, currentVz = 0;
    private double xValue, yValue, zValue = 0;
    private double xValueTemp, yValueTemp, zValueTemp = 0;


    private static final float NS2S = 1.0f / 1000000000.0f;


    private int pointsPlotted = 15;
    private Viewport viewport;

    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
    });

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

                aX = (Math.abs(event.values[0]) > EPSILON_ACCEL) ? event.values[0] : 0;
                aY = (Math.abs(event.values[1]) > EPSILON_ACCEL) ? event.values[1] : 0;
                aZ = (Math.abs(event.values[2]) > EPSILON_ACCEL) ? event.values[2] : 0;

                double accelerationCurrentValue = Math.sqrt(aX * aX + aY * aY + aZ * aZ);
            }
            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){


                xOmegaTemp = (Math.abs(event.values[0]) > EPSILON_GYRO) ? event.values[0] : 0;
                yOmegaTemp = (Math.abs(event.values[1]) > EPSILON_GYRO) ? event.values[1] : 0;
                zOmegaTemp = (Math.abs(event.values[2]) > EPSILON_GYRO) ? event.values[2] : 0;

                omegaMagnitude = Math.sqrt(xOmegaTemp*xOmegaTemp + yOmegaTemp*yOmegaTemp + zOmegaTemp*zOmegaTemp);
                System.out.println(xValue*1000);

                xOmega = xOmegaTemp/omegaMagnitude;
                yOmega = yOmegaTemp/omegaMagnitude;
                zOmega = zOmegaTemp/omegaMagnitude;

            }

            // get dT which is a very small proportion of time.
            double dT = (event.timestamp - currentTime) * NS2S;

            // updating tetha for upcoming calculations.
            double thetaOverTwo  = omegaMagnitude * dT / 2.0f;

            sinThetaOverTwo = Math.sin(thetaOverTwo);
            cosThetaOverTwo = Math.cos(thetaOverTwo);

            deltaRotationVector[0] = sinThetaOverTwo * xOmega;
            deltaRotationVector[1] = sinThetaOverTwo * yOmega;
            deltaRotationVector[2] = sinThetaOverTwo * zOmega;
            deltaRotationVector[3] = cosThetaOverTwo;

            tetha_temp = yOmegaTemp*dT + tetha_temp;
            tetha = (Math.abs(tetha_temp) < 0.05) ? 0 : tetha_temp;


            // update acceleration in z and x directions.
            aH = (Math.abs(aX * Math.cos(tetha) - aZ * Math.sin(tetha)) > EPSILON_A) ? aX * Math.cos(tetha) - aZ * Math.sin(tetha) : 0;
            aD = (Math.abs(aZ * Math.cos(tetha) + aX * Math.sin(tetha)) > EPSILON_A) ? aZ * Math.cos(tetha) + aX * Math.sin(tetha) : 0;



            // update velocity in x and y directions.
            currentVx = Math.abs(aH * dT) + currentVx;
            currentVz = aD * dT + currentVz;
            currentVx /= 2;
            currentVz /= 2;



            // update x and y values. we ignore xValue in the diagram

            xValue =  (Math.abs(aH) > EPSILON_A) ? currentVx * dT + xValue : xValue;
            zValue =  (Math.abs(aD) > EPSILON_A) ? currentVz * dT + zValue : zValue;

            double xValueCM = xValue * 1000;
            double zValueCM = zValue * 1000;

            xValues.add(xValueCM);
            zValues.add(zValueCM);


            //double theta2 = tetha*10000;
            txt_currentAccel.setText("Horizontal Acceleration = " + aH);
            txt_prevAccel.setText("Vertical Acceleration = " + aD);
            txt_accel.setText("Estimated distance covered = " + xValueCM);

            txt_currentGyro.setText("Turning = " + yOmegaTemp);
            txt_prevGyro.setText("Group = Jahani - Mohammad Hashemi - Shaayegh - Kamkaar");
            txt_gyro.setText("Gyro average magnitude " + omegaMagnitude);


            pointsPlotted = (currentVx > 0) ? pointsPlotted + 1 : pointsPlotted;
            series.appendData(new DataPoint(pointsPlotted , zValueCM), true, pointsPlotted);
            viewport.setMaxX(pointsPlotted);
            viewport.setMinX(pointsPlotted - 700);

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
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        nGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        GraphView graph = (GraphView) findViewById(R.id.graph);
        viewport = graph.getViewport();
        viewport.setScrollable(true);
        viewport.setXAxisBoundsManual(true);
        //viewport.setYAxisBoundsManual(true);
        //viewport.setMaxY(5);
        //viewport.setMinY(-5);
        graph.addSeries(series);



    }


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(sensorEventListener, nGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(sensorEventListener);
    }

}