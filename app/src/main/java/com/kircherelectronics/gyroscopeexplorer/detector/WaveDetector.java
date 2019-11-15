package com.kircherelectronics.gyroscopeexplorer.detector;

import android.util.Log;

/**
 * Created by finnfu on 2017/6/3.
 * 步行或者跑步
 */

public class WaveDetector {

    // 连续上升次数
    int CONTINUE_UP_LIMIT;
    // 峰值范围最小值
    float PEAK_MIN;
    float PEAK_MAX;
    float VALLEY_MIN;
    float VALLEY_MAX;

    //用于存放计算阈值的波峰波谷差值
    int TEMP_NUM;
    float[] tempValue;
    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    float INITIAL_VALUE;
    //初始阈值
    float highValue;
    float lowValue;



    //波峰波谷时间差
    int timeInterval;

    int tempCount = 0;
    //是否上升的标志位
    boolean isDirectionUp = false;
    //持续上升次数
    int continueUpCount = 0;
    // 持续下降次数
    int continueDownCount = 0;
    //上一点的持续上升的次数，为了记录波峰的上升次数
    int continueUpFormerCount = 0;
    //上一点的状态，上升还是下降
    boolean lastStatus = false;
    //波峰值
    float peakOfWave = 0;
    //波谷值
    float valleyOfWave = 0;
    //此次波峰的时间
    long timeOfThisPeak = 0;
    //上次波峰的时间
    long timeOfLastPeak = 0;
    //当前的时间
    long timeOfNow = 0;

    //上次传感器的值
    float valueOld = 0;

    int count = 0;

    public int getCount() {
        return count * 2;
    }

    /*
     * 检测步子，并开始计步
     * 1.传入sersor中的数据
     * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
     * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
     * */
    public void detectorNew(float value) {
        if (valueOld == 0) {
            valueOld = value;
        } else {
            timeOfNow = System.currentTimeMillis();
            if (detectorPeak(value, valueOld)) {
                timeOfLastPeak = timeOfThisPeak;

                // 第一个波峰
                long dt = timeOfNow - timeOfLastPeak;
                float dw = peakOfWave - valleyOfWave;

                Log.d("check", String.format("%d, %f", dt, dw));
                if (dt >= timeInterval && dw >= lowValue && dw <= highValue) {
                    timeOfThisPeak = timeOfNow;

                    /*
                     一次波形识别
                     * */
                    countOne();
                    if(dt > 10000L) {
                        resSomeValue();
                    }
                }
                /*if (dt >= timeInterval  && dw <= INITIAL_VALUE) {
                    timeOfThisPeak = timeOfNow;
                    threadValue = peakValleyThread(dw);
                }*/

            }
        }
        valueOld = value;
    }


    public void countOne() {

        count++;

        /*timeOfLastStep = timeOfThisStep;
        timeOfThisStep = System.currentTimeMillis();
        long diffValue = timeOfThisStep - timeOfLastStep;
        if (diffValue <= 6000L) {
            averageTimeOfEveryStep += diffValue;

            if (stepCount == 9) {
                averageTimeOfEveryStep = averageTimeOfEveryStep / 10;
                Log.i("result", "averageTimeOfEveryStep: " + averageTimeOfEveryStep);
            }
        } else {//超时
            resSomeValue();
        }*/
    }

    private void resSomeValue() {
        continueUpCount = 0;
        continueUpFormerCount = 0;
        lastStatus = false;
        isDirectionUp = false;
    }


    /*
     * 检测波峰
     * 以下四个条件判断为波峰：
     * 1.目前点为下降的趋势：isDirectionUp为false
     * 2.之前的点为上升的趋势：lastStatus为true
     * 3.到波峰为止，持续上升大于等于2次
     * 4.波峰值大于20
     * 记录波谷值
     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
     * */
    public boolean detectorPeak(float newValue, float oldValue) {

        lastStatus = isDirectionUp;
        if (newValue >= oldValue) {
            isDirectionUp = true;
            continueUpCount++;
            continueDownCount = 0;
        } else if (newValue < oldValue){
            continueDownCount++;
            if(continueDownCount == 1) {
                tempValue[0] = oldValue;
            }
        }

        if(continueDownCount >= 1) {
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
            isDirectionUp = false;
        }

        Log.d("detectorPeak", String.format("%b, %b, %d, %f, %f", isDirectionUp, lastStatus, continueUpFormerCount, newValue, Math.abs(newValue - oldValue)));

        if (!isDirectionUp && lastStatus
                && continueUpFormerCount >= CONTINUE_UP_LIMIT
                && tempValue[0] >= PEAK_MIN && tempValue[0] < PEAK_MAX) {
            Log.d("peakOfWave", String.format("%f", tempValue[0]));
            peakOfWave = tempValue[0];
            return true;
        } else if (!lastStatus && isDirectionUp
                && oldValue >= VALLEY_MIN && oldValue < VALLEY_MAX) {
            Log.d("valleyOfWave", String.format("%f", oldValue));
            valleyOfWave = oldValue;
            return false;
        } else {
            return false;
        }

    }

    /*
     * 阈值的计算
     * 1.通过波峰波谷的差值计算阈值
     * 2.记录4个值，存入tempValue[]数组中
     * 3.在将数组传入函数averageValue中计算阈值
     * */
    public float peakValleyThread(float value) {
        float tempThread = highValue;
        if (tempCount < TEMP_NUM) {
            tempValue[tempCount] = value;
            tempCount++;
        } else {
            tempThread = averageValue(tempValue, TEMP_NUM);
            for (int i = 1; i < TEMP_NUM; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[TEMP_NUM - 1] = value;
        }
        return tempThread;
    }

    /*
     * 梯度化阈值
     * 1.计算数组的均值
     * 2.通过均值将阈值梯度化在一个范围里
     * */
    public float averageValue(float value[], int n) {
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave / TEMP_NUM;
        return ave;
    }

}