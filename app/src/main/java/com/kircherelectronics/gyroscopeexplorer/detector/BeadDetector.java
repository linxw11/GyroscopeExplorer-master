package com.kircherelectronics.gyroscopeexplorer.detector;

public class BeadDetector extends WaveDetector {

    public BeadDetector() {
        // 连续上升次数
        CONTINUE_UP_LIMIT = 3;
        // 峰值范围最小值
        PEAK_MIN = 0.9f;
        PEAK_MAX = 1.8f;
        VALLEY_MIN = -1.5f;
        VALLEY_MAX = -0.9f;
        //用于存放计算阈值的波峰波谷差值
        TEMP_NUM = 4;
        tempValue = new float[TEMP_NUM];

        //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
        INITIAL_VALUE = 0.5f;
        //初始阈值
        highValue = 3.1f;
        lowValue = 1f;
        //波峰波谷时间差
        timeInterval = 3000;

    }

    @Override
    public int getCount() {
        return count * 12;
    }
}
