package com.kircherelectronics.gyroscopeexplorer.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.kircherelectronics.fsensor.filter.gyroscope.OrientationGyroscope;
import com.kircherelectronics.gyroscopeexplorer.R;
import com.kircherelectronics.gyroscopeexplorer.listener.Simulator;

import org.apache.commons.math3.complex.Quaternion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class DebugActivity extends AppCompatActivity {

    private static int SMOOTHING_WINDOW_SIZE = 20;

    public static SensorManager mSensorManager;
    public static Sensor mSensorCount, mSensorAcc;
    private float mRawAccelValues[] = new float[3];

    // smoothing accelerometer signal variables
    private float mAccelValueHistory[][] = new float[3][SMOOTHING_WINDOW_SIZE];
    private float mRunningAccelTotal[] = new float[3];
    private float mCurAccelAvg[] = new float[3];
    private int mCurReadIndex = 0;

    public static float mStepCounter = 0;
    public static float mStepCounterAndroid = 0;
    public static float mInitialStepCount = 0;

    private double mGraph1LastXValue = 0d;
    private double mGraph2LastXValue = 0d;

    private LineGraphSeries<DataPoint> mSeries11;
    private LineGraphSeries<DataPoint> mSeries12;
    private LineGraphSeries<DataPoint> mSeries13;
    private LineGraphSeries<DataPoint> mSeries21;
    private LineGraphSeries<DataPoint> mSeries22;
    private LineGraphSeries<DataPoint> mSeries23;

    private double lastMag = 0d;
    private double avgMag = 0d;
    private double netMag = 0d;

    //peak detection variables
    private double lastXPoint = 1d;
    double stepThreshold = 1.0d;
    double noiseThreshold = 6d;
    private int windowSize = 20;

    private static OrientationGyroscope orientationGyroscope = new OrientationGyroscope();
    private float[] orientation = new float[3];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_main);

        // mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // mSensorCount = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        // mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // mSensorManager.registerListener(this, mSensorCount, SensorManager.SENSOR_DELAY_UI);
        // mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_UI);


        //Graph for showing raw acceleration magnitude signal
        GraphView graph = (GraphView) this.findViewById(R.id.graph);

        mSeries11 = new LineGraphSeries<>();
        mSeries11.setColor(Color.YELLOW);
        graph.addSeries(mSeries11);
        mSeries12 = new LineGraphSeries<>();
        mSeries12.setColor(Color.BLUE);
        graph.addSeries(mSeries12);
        mSeries13 = new LineGraphSeries<>();
        mSeries13.setColor(Color.RED);
        graph.addSeries(mSeries13);

        graph.setTitle("Accelerator Signal");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Signal Value");
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(50);

        //Graph for showing smoothed acceleration magnitude signal
        GraphView graph2 = (GraphView) this.findViewById(R.id.graph2);
        mSeries21 = new LineGraphSeries<>();
        graph2.addSeries(mSeries21);
        mSeries22 = new LineGraphSeries<>();
        graph2.addSeries(mSeries22);
        mSeries23 = new LineGraphSeries<>();
        graph2.addSeries(mSeries23);

        graph2.setTitle("Smoothed Signal");
        graph2.getGridLabelRenderer().setVerticalAxisTitle("Signal Value");
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(50);

        Simulator.registListener(this, "data2.txt", (event)-> onSensorChanged(event));
    }

    //Button to link home view from debug view
    public void onClickBtn(View v)
    {
        Intent i = new Intent(this, GyroscopeActivity.class);
        this.startActivity(i);
    }

    // @Override
    public void onSensorChanged (Simulator.Event e) {
        float[] values = e.getData();
        switch (e.getType()) {
            case Sensor.TYPE_STEP_COUNTER:
                if(mInitialStepCount == 0.0){
                    mInitialStepCount = values[0];
                }
                mStepCounterAndroid = values[0];
                break;
            case Sensor.TYPE_ACCELEROMETER:

                /*if(!orientationGyroscope.isBaseOrientationSet()) {
                    orientationGyroscope.setBaseOrientation(Quaternion.IDENTITY);
                } else {
                    orientation = orientationGyroscope.calculateOrientation(values, e.getTimestamp());
                }*/

                // Log.e("orientation", Arrays.toString(orientation));

                mRawAccelValues[0] = values[0];
                mRawAccelValues[1] = values[1];
                mRawAccelValues[2] = values[2];

                lastMag = Math.sqrt(Math.pow(mRawAccelValues[0], 2) + Math.pow(mRawAccelValues[1], 2) + Math.pow(mRawAccelValues[2], 2));

                //Source: https://github.com/jonfroehlich/CSE590Sp2018
                for (int i = 0; i < 3; i++) {
                    mRunningAccelTotal[i] = mRunningAccelTotal[i] - mAccelValueHistory[i][mCurReadIndex];
                    mAccelValueHistory[i][mCurReadIndex] = mRawAccelValues[i];
                    mRunningAccelTotal[i] = mRunningAccelTotal[i] + mAccelValueHistory[i][mCurReadIndex];
                    mCurAccelAvg[i] = mRunningAccelTotal[i] / SMOOTHING_WINDOW_SIZE;
                }
                mCurReadIndex++;
                if(mCurReadIndex >= SMOOTHING_WINDOW_SIZE){
                    mCurReadIndex = 0;
                }

                avgMag = Math.sqrt(Math.pow(mCurAccelAvg[0], 2) + Math.pow(mCurAccelAvg[1], 2) + Math.pow(mCurAccelAvg[2], 2));

                netMag = lastMag - avgMag; //removes gravity effect

                //update graph data points
                mGraph1LastXValue += 1d;
                mSeries11.appendData(new DataPoint(mGraph1LastXValue, lastMag), true, 50);
                // mSeries12.appendData(new DataPoint(mGraph1LastXValue, mRawAccelValues[1]), true, 50);
                // mSeries13.appendData(new DataPoint(mGraph1LastXValue, mRawAccelValues[2]), true, 50);

                mGraph2LastXValue += 1d;
                mSeries21.appendData(new DataPoint(mGraph2LastXValue, netMag), true, 50);
                // mSeries22.appendData(new DataPoint(mGraph2LastXValue, lastMag), true, 50);
                // mSeries23.appendData(new DataPoint(mGraph2LastXValue, lastMag), true, 160);
        }

        TextView calculatedStep = (TextView) this.findViewById(R.id.tv1);
        TextView androidStep = (TextView) this.findViewById(R.id.tv2);

        peakDetection();

        calculatedStep.setText(new String("Steps Tracked: " + (int)mStepCounter));
        //android always returns total steps since reboot so subtract all steps recorded before the app started
        androidStep.setText(new String("Android Steps Tracked: " + (int)(mStepCounterAndroid - mInitialStepCount)));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void peakDetection(){

        /* Peak detection algorithm derived from: A Step Counter Service for Java-Enabled Devices Using a Built-In Accelerometer, Mladenov et al.
            *Threshold, stepThreshold was derived by observing people's step graph
            * ASSUMPTIONS:
            * Phone is held vertically in portrait orientation for better results
         */

        double highestValX = mSeries21.getHighestValueX();

        if(highestValX - lastXPoint < windowSize){
            return;
        }

        Iterator<DataPoint> valuesInWindow = mSeries21.getValues(lastXPoint,highestValX);

        lastXPoint = highestValX;

        double forwardSlope = 0d;
        double downwardSlope = 0d;

        List<DataPoint> dataPointList = new ArrayList<DataPoint>();
        valuesInWindow.forEachRemaining(dataPointList::add); //This requires API 24 or higher

        for(int i = 0; i<dataPointList.size(); i++){
            if(i == 0) continue;
            else if(i < dataPointList.size() - 1){
                forwardSlope = dataPointList.get(i+1).getY() - dataPointList.get(i).getY();
                downwardSlope = dataPointList.get(i).getY() - dataPointList.get(i - 1).getY();
                Log.e("step", String.format("%f, %f, %f", forwardSlope, downwardSlope, dataPointList.get(i).getY()));
                if(forwardSlope < 0 && downwardSlope > 0 && dataPointList.get(i).getY() > stepThreshold && dataPointList.get(i).getY() < noiseThreshold){
                    mStepCounter+=1;
                }
            }
        }
    }

    // @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}

