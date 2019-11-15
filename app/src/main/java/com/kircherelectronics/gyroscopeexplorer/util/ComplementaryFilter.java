package com.kircherelectronics.gyroscopeexplorer.util;

/**
 * 互补滤波
 *
 * 由于加速度计有高频噪声，陀螺仪有低频噪声，可以通过互补滤波融合得到较可靠的角度值，公式如下：
 * angle = (1 - R) * angle_g + R * angle_a
 * angle 为融合后的角度，
 * angle_g 为陀螺仪计算的角度，
 * angle_a 为加速度计计算的角度，
 * R为滤波器系数，一般取较小的数，接近0。
 * 相当于对陀螺仪数据高通滤波，对加速度计数据低通滤波。
 * 基本思想为使用加速度计的数据修正陀螺仪的漂移。
 * R取值过大，角度收敛慢，动态性能降低，R过小，角度波动较大，滤波效果降低。
 */

public class ComplementaryFilter {

    private final float fRad2Deg = 57.295779513f; //弧度换算角度乘的系数
    private final float dt = 0.5f; //时间周期
    private float[] angle = new float[3];
    private float R = 0.1f;
    private float[] angleLast = new float[3];

    public float[] filter(float[] acc, float[] gyr) { //计算角度
        float[] temp = new float[3];
        temp[0] = (float) Math.sqrt(acc[1] * acc[1] + acc[2] * acc[2]);
        temp[1] = (float) Math.sqrt(acc[0] * acc[0] + acc[2] * acc[2]);

        // pitch and roll
        for(int i = 0; i < 2; i++) {
            angle[i] = (float) (R * (angleLast[i] + gyr[i]* dt / fRad2Deg)
                    + (1 - R) * Math.atan(acc[i] / temp[i])); // * fRad2Deg
            angleLast[i] = angle[i];
        }
        //yaw
        angle[2] = angleLast[2] + gyr[2] * dt / fRad2Deg;
        angleLast[2] = angle[2];

        return angle;
    }
}
