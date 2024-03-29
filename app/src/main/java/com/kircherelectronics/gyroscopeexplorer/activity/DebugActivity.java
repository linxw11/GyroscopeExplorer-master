package com.kircherelectronics.gyroscopeexplorer.activity;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
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
import com.kircherelectronics.gyroscopeexplorer.detector.BeadDetector;
import com.kircherelectronics.gyroscopeexplorer.detector.StepDetector;
import com.kircherelectronics.gyroscopeexplorer.listener.Simulator;
import com.kircherelectronics.gyroscopeexplorer.util.ComplementaryFilter;
import com.kircherelectronics.gyroscopeexplorer.util.DynamicAverage;


import java.util.ArrayList;
import java.util.Arrays;
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

    private BeadDetector beadDetector = new BeadDetector();
    private StepDetector stepDetector = new StepDetector();
    private ComplementaryFilter complementaryFilter = new ComplementaryFilter();
    private DynamicAverage dynamicAverageGyr = new DynamicAverage();

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
    double stepThreshold = 5d;
    double noiseThreshold = 15d;
    private int windowSize = 20;

    private static OrientationGyroscope orientationGyroscope = new OrientationGyroscope();
    private float[] orientation = new float[3];

    private long num;
    private float[] lastQ = new float[4];
    private float[] rotation = new float[3];
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];

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
        graph.getViewport().setMaxX(40);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-2);
        graph.getViewport().setMaxY(2);

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
        graph2.getViewport().setMaxX(20);

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
        long timestamp = e.getTimestamp();
        int type = e.getType();

        float[] acc = new float[]{values[0], values[1], values[2]};

        float[] gyr = new float[]{values[3], values[4], values[5]};

        gyr = dynamicAverageGyr.removeAverage(gyr);

        float[] angle = complementaryFilter.filter(acc, gyr);

        float sum = 0;
        for(float w: angle) {
            sum += (w * w);
        }
        beadDetector.detectorNew(angle[0]);
        stepDetector.detectorNew(angle[0]);
        mStepCounter = beadDetector.getCount();
        mStepCounterAndroid = stepDetector.getCount();
        // stepDetector.detectorNew(angle[0]);

        mGraph1LastXValue += 1d;
        mSeries11.appendData(new DataPoint(mGraph1LastXValue, angle[0]), true, 40);
        mSeries12.appendData(new DataPoint(mGraph1LastXValue, Math.sqrt(sum)), true, 40);

        /*switch (type) {
            case Sensor.TYPE_STEP_COUNTER:
                if(mInitialStepCount == 0.0){
                    mInitialStepCount = values[0];
                }
                mStepCounterAndroid = values[0];
                break;
            case Sensor.TYPE_GYROSCOPE:
                *//*System.arraycopy(values, 0, rotation, 0, values.length);
                if(!orientationGyroscope.isBaseOrientationSet()) {
                    orientationGyroscope.setBaseOrientation(Quaternion.IDENTITY);
                    return;
                } else {
                    orientation = orientationGyroscope.calculateOrientation(rotation, timestamp);
                }
*//*
                *//*float[] degrees = new float[3];
                degrees[0] = (float) (Math.toDegrees(orientation[0]) + 360) % 360;
                degrees[1] = (float) (Math.toDegrees(orientation[1]) + 360) % 360;
                degrees[2] = (float) (Math.toDegrees(orientation[2]) + 360) % 360;*//*

                // Log.e("degrees", Arrays.toString(orientation));

                *//*float[] QAC = new float[4];
                getQuaternionFromVector(QAC, orientation);

                if(num >= 3){

                    double[] QBC = new double[4];

                    QBC[0] = Math.cos(orientation[0]/2)*Math.cos(orientation[1]/2)*Math.cos(orientation[2]/2)
                        + Math.sin(orientation[0]/2)*Math.sin(orientation[1]/2)*Math.sin(orientation[2]/2);
                    QBC[1] = Math.sin(orientation[0]/2)*Math.cos(orientation[1]/2)*Math.cos(orientation[2]/2)
                            - Math.cos(orientation[0]/2)*Math.sin(orientation[1]/2)*Math.sin(orientation[2]/2);
                    QBC[2] = Math.cos(orientation[0]/2)*Math.sin(orientation[1]/2)*Math.cos(orientation[2]/2)
                        + Math.sin(orientation[0]/2)*Math.cos(orientation[1]/2)*Math.sin(orientation[2]/2);
                    QBC[3] = Math.cos(orientation[0]/2)*Math.cos(orientation[1]/2)*Math.sin(orientation[2]/2)
                        - Math.sin(orientation[0]/2)*Math.sin(orientation[1]/2)*Math.cos(orientation[2]/2);


                    *//**//*float[] QBA = new float[4];
                    QBA[0] = lastQ[0];
                    QBA[1] = -lastQ[1];
                    QBA[2] = -lastQ[2];
                    QBA[3] = -lastQ[3];
                    // QBA = QBA * QAC
                    float[] QBC = new float[4];
                    QBC[0] = QAC[0]*QBA[0] - QAC[1]*QBA[1] - QAC[2]*QBA[2] -QAC[3]*QBA[3];
                    QBC[1] = QAC[0]*QBA[1] + QAC[1]*QBA[0] + QAC[2]*QBA[3] -QAC[3]*QBA[2];
                    QBC[2] = QAC[0]*QBA[2] - QAC[1]*QBA[3] + QAC[2]*QBA[0] +QAC[3]*QBA[1];
                    QBC[3] = QAC[0]*QBA[3] + QAC[1]*QBA[2] - QAC[2]*QBA[1] +QAC[3]*QBA[0];*//**//*

                    //偏向Z轴的位移
                    double z = Math.atan2(2*QBC[1]*QBC[2] - 2*QBC[0]*QBC[3]
                            , 2*QBC[0]*QBC[0] + 2*QBC[1]*QBC[1]-1);
                    //偏向X轴的位移
                    double x = -Math.asin(2*QBC[1]*QBC[3] + 2*QBC[0]*QBC[2]);
                    //偏向Y轴的位移
                    double y = Math.atan2(2*QBC[2]*QBC[3] - 2*QBC[0]*QBC[1]
                            , 2*QBC[0]*QBC[0] + 2*QBC[3]*QBC[3]-1);

                    java.text.DecimalFormat df = new java.text.DecimalFormat("#0.000");
                    // Log.i("Sensor","z=" +  df.format(z) + " x=" + df.format(x)  + " y=" +df.format(y) );

                    lastQ = QAC;
                    lastMag = Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2);

                    Log.e("d", lastMag+"");
                    // Math.sqrt();
                    mRawAccelValues[0] = (float) Math.abs(x);
                    mRawAccelValues[1] = (float) Math.abs(y);
                    mRawAccelValues[2] = (float) Math.abs(z);

                } else {
                    num++;
                    lastQ = QAC;
                }
*//*

                Log.e("degrees", Arrays.toString(values));
                *//*for (int i=0; i<3; i++) {
                    values[i] = (float) (values[i] * Math.PI / 180);
                    mRawAccelValues[i] += (values[i] * 0.5);
                }*//*

                float dT = 0.5f;
                // Axis of the rotation sample, not normalized yet.
                float axisX = values[0];
                float axisY = values[1];
                float axisZ = values[2];

                // Calculate the angular speed of the sample
                float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

                // Normalize the rotation vector if it's big enough to get the axis
                if (omegaMagnitude !=0 ) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }

                // Integrate around this axis with the angular speed by the time step
                // in order to get a delta rotation from this sample over the time step
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);

                float[] deltaRotationVector = new float[4];
                deltaRotationVector[0] = sinThetaOverTwo * axisX;
                deltaRotationVector[1] = sinThetaOverTwo * axisY;
                deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                deltaRotationVector[3] = cosThetaOverTwo;

                float[] deltaRotationMatrix = new float[9];
                SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);

                getOrientation(deltaRotationMatrix, mRawAccelValues);
                // User code should concatenate the delta rotation we computed with the current
                // rotation in order to get the updated rotation.
                // rotationCurrent = rotationCurrent * deltaRotationMatrix;

                // lastMag = Math.sqrt(Math.pow(mRawAccelValues[0], 2) + Math.pow(mRawAccelValues[1], 2) + Math.pow(mRawAccelValues[2], 2));

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

                avgMag = Math.pow(mCurAccelAvg[0], 2) + Math.pow(mCurAccelAvg[1], 2) + Math.pow(mCurAccelAvg[2], 2);
                // Math.sqrt();
                netMag = lastMag - avgMag; //removes gravity effect
                // Log.e("d", netMag + "");
                //update graph data points
                mGraph1LastXValue += 1d;
                mSeries11.appendData(new DataPoint(mGraph1LastXValue, mRawAccelValues[0]), true, 20);
                mSeries12.appendData(new DataPoint(mGraph1LastXValue, mRawAccelValues[1]), true, 20);
                mSeries13.appendData(new DataPoint(mGraph1LastXValue, mRawAccelValues[2]), true, 20);

                mGraph2LastXValue += 1d;
                // mSeries21.appendData(new DataPoint(mGraph2LastXValue, lastMag), true, 20);
                // mSeries22.appendData(new DataPoint(mGraph2LastXValue, lastMag), true, 50);
                // mSeries23.appendData(new DataPoint(mGraph2LastXValue, lastMag), true, 160);
                break;
            case Sensor.TYPE_ACCELEROMETER: {
                // In this example, alpha is calculated as t / (t + dT),
                // where t is the low-pass filter's time-constant and
                // dT is the event delivery rate.
<<<<<<< HEAD
                float deltaT = 0.5f;
                float freqCut = 15;
                float alpha = (float) (deltaT/(deltaT+1/(2*Math.PI*freqCut)));
=======

                float deltaT = 0.5f;
                float freqCut = 10;
                float t = (float) (1.0f/(2*Math.PI*freqCut));
                float alpha = t /(deltaT + t);
>>>>>>> dae7416a02c789d8343a34650b192b7608874995

                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = alpha * gravity[0] + (1 - alpha) * values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * values[2];

                // Remove the gravity contribution with the high-pass filter.
                linear_acceleration[0] = values[0] - gravity[0];
                linear_acceleration[1] = values[1] - gravity[1];
                linear_acceleration[2] = values[2] - gravity[2];

                double[] degress = new double[]{
                        Math.atan(Math.sqrt(Math.pow(linear_acceleration[0], 2)
                          + Math.pow(linear_acceleration[1], 2))/linear_acceleration[2]),
                        Math.atan(linear_acceleration[0]/Math.sqrt(Math.pow(linear_acceleration[1], 2)
                                + Math.pow(linear_acceleration[2], 2))),
                        Math.atan(linear_acceleration[1]/Math.sqrt(Math.pow(linear_acceleration[0], 2)
                                + Math.pow(linear_acceleration[2], 2)))
                };

                Log.d("acceleration", Arrays.toString(degress));

                // mSeries11.appendData(new DataPoint(mGraph1LastXValue, Math.abs(linear_acceleration[0])), true, 20);
                // mSeries12.appendData(new DataPoint(mGraph1LastXValue, Math.abs(linear_acceleration[1])), true, 20);
                //.appendData(new DataPoint(mGraph1LastXValue, Math.abs(linear_acceleration[2])), true, 20);

                lastMag = Math.sqrt(Math.pow(linear_acceleration[0], 2) + Math.pow(linear_acceleration[1], 2) + Math.pow(linear_acceleration[2], 2));

                // mSeries21.appendData(new DataPoint(mGraph2LastXValue, degress[0]), true, 20);
                break;
            }
        }
*/
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

        Iterator<DataPoint> valuesInWindow = mSeries21.getValues(lastXPoint, highestValX);

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
                // Log.e("step", String.format("%f, %f, %f", forwardSlope, downwardSlope, dataPointList.get(i).getY()));
                /*if(forwardSlope < 0 && downwardSlope > 0 && dataPointList.get(i).getY() > stepThreshold && dataPointList.get(i).getY() < noiseThreshold){
                    mStepCounter+=1;
                }*/
                if(forwardSlope < 0 && downwardSlope > 0){
                    mStepCounter+=1;
                }
            }
        }
    }

    // @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * 获取一个四元素
     * @param Q
     * @param rv
     */
    public static void getQuaternionFromVector(float[] Q, float[] rv) {
        if (rv.length >= 4) {
            Q[0] = rv[3];
        } else {
            Q[0] = 1 - rv[0]*rv[0] - rv[1]*rv[1] - rv[2]*rv[2];
            Q[0] = (Q[0] > 0) ? (float) Math.sqrt(Q[0]) : 0;
        }
        Q[1] = -rv[0];
        Q[2] = -rv[1];
        Q[3] = -rv[2];
    }

    public static float[] getOrientation(float[] R, float values[]) {
        /*
         * 4x4 (length=16) case:
         *   /  R[ 0]   R[ 1]   R[ 2]   0  \
         *   |  R[ 4]   R[ 5]   R[ 6]   0  |
         *   |  R[ 8]   R[ 9]   R[10]   0  |
         *   \      0       0       0   1  /
         *
         * 3x3 (length=9) case:
         *   /  R[ 0]   R[ 1]   R[ 2]  \
         *   |  R[ 3]   R[ 4]   R[ 5]  |
         *   \  R[ 6]   R[ 7]   R[ 8]  /
         *
         */
        if (R.length == 9) {
            values[0] = (float)Math.atan2(R[1], R[4]);
            values[1] = (float)Math.asin(-R[7]);
            values[2] = (float)Math.atan2(-R[6], R[8]);
        } else {
            values[0] = (float)Math.atan2(R[1], R[5]);
            values[1] = (float)Math.asin(-R[9]);
            values[2] = (float)Math.atan2(-R[8], R[10]);
        }
        return values;
    }
}

