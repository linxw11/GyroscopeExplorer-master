package com.kircherelectronics.gyroscopeexplorer.detector;

public class BeadDetector extends WaveDetector {

    public BeadDetector() {
        // 连续上升次数
        CONTINUE_UP_LIMIT = 3;
        // 峰值范围最小值
        PEAK_MIN = 1f;
        //用于存放计算阈值的波峰波谷差值
        TEMP_NUM = 4;
        tempValue = new float[TEMP_NUM];

        //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
        INITIAL_VALUE = 0.5f;
        //初始阈值
        highValue = 2f;
        lowValue = 1f;
        noise = 0.05f;
        //波峰波谷时间差
        timeInterval = 1200;

    }

}
