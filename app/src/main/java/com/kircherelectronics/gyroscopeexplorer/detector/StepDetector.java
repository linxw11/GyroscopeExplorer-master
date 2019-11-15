package com.kircherelectronics.gyroscopeexplorer.detector;

public class StepDetector extends WaveDetector {

    public StepDetector() {
        // 连续上升次数
        CONTINUE_UP_LIMIT = 1;
        // 峰值范围最小值
        PEAK_MIN = 0.001f;
        //用于存放计算阈值的波峰波谷差值
        TEMP_NUM = 4;
        tempValue = new float[TEMP_NUM];

        //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
        INITIAL_VALUE = 0.01f;
        highValue = 0.2f;
        lowValue = 0.1f;
        noise = 0.05f;
        //波峰波谷时间差
        timeInterval = 800;

    }

}
