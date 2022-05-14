package com.example.surfacescannerpro;

import androidx.appcompat.app.AppCompatActivity;

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

public class MovingActivity extends AppCompatActivity {

    TextView txt_accel, txt_prevAccel, txt_currentAccel;
    ProgressBar prog_shakeMeter;

    TextView txt_gyro, txt_prevGyro, txt_currentGyro;


    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor nGyroscope;

    private double accelerationCurrentValue;
    private double accelerationPrevValue;

    private double gyroCurrentValue, gyroPrevValue;

    private int pointsPlotted = 15;
    private int graphPointsInterval = 0;

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
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                accelerationCurrentValue = Math.sqrt(x * x + y * y + z * z);

                double defPrevCurrentAcceleration = Math.abs(accelerationCurrentValue - accelerationPrevValue);
                accelerationPrevValue = accelerationCurrentValue;

                txt_currentAccel.setText("Current acceleration = " + (int) accelerationCurrentValue + " - "+ (int) gyroCurrentValue);
                txt_prevAccel.setText("Previous acceleration = " + (int) accelerationPrevValue + " - " + (int) gyroPrevValue);
                txt_accel.setText("Acceleration change = " + (int) defPrevCurrentAcceleration);

                prog_shakeMeter.setProgress((int) defPrevCurrentAcceleration);

                pointsPlotted++;
                series.appendData(new DataPoint(pointsPlotted, defPrevCurrentAcceleration), true, pointsPlotted);
                viewport.setMaxX(pointsPlotted);
                viewport.setMinX(pointsPlotted - 200);
            }
            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                System.out.println("salaaaaaam!");
                float xteta = event.values[0];
                float yteta = event.values[1];
                float zteta = event.values[2];

                gyroCurrentValue = 100* (Math.sin(xteta) + Math.sin(yteta) + Math.sin(zteta));

                double defPrevCurrentGyro = Math.abs(gyroCurrentValue - gyroPrevValue);
                gyroPrevValue = gyroCurrentValue;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_accel = findViewById(R.id.txt_accel);
        txt_currentAccel = findViewById(R.id.txt_currentAccel);
        txt_prevAccel = findViewById(R.id.txt_prevAccel);

        prog_shakeMeter = findViewById(R.id.prog_shakeMeter);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        nGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        viewport = graph.getViewport();
        viewport.setScrollable(true);
        viewport.setXAxisBoundsManual(true);
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