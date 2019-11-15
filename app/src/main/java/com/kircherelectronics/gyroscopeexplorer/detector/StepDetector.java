package com.kircherelectronics.gyroscopeexplorer.detector;

public class StepDetector extends WaveDetector {

    private boolean isStart = false;
    private long time = 0;
    private int tempCount = 0;

    public StepDetector() {
        // 连续上升次数
        CONTINUE_UP_LIMIT = 1;
        // 峰值范围最小值
        PEAK_MIN = 0.01f;
        PEAK_MAX = 3f;
        VALLEY_MIN = -3f;
        VALLEY_MAX = -0.01f;
        //用于存放计算阈值的波峰波谷差值
        TEMP_NUM = 4;
        tempValue = new float[TEMP_NUM];

        //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
        INITIAL_VALUE = 0.01f;
        highValue = 3f;
        lowValue = 0.1f;
        //波峰波谷时间差
        timeInterval = 800;

    }
    @Override
    public int getCount() {
        return (int) Math.round(count * 1.45);
    }

    @Override
    public void countOne() {

        if (isStart) {
            count++;
            return;
        }
        // 5s内发生3次，判定为进入走路模式
        tempCount++;
        if(tempCount == 1) {
            time = System.currentTimeMillis();
        } else if(tempCount == 3) {
            long dt = System.currentTimeMillis() - time;
            if(dt < 5000) {
                count = tempCount;
                isStart = true;
            } else {
                isStart = false;
            }
        }

    }
}
