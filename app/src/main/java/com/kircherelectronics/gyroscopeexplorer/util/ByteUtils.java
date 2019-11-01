package com.kircherelectronics.gyroscopeexplorer.util;

/**
 * Created by linxw on 2019/9/29.
 */

public class ByteUtils {

    //System.arraycopy()方法
    public static byte[] merger(byte[]... bt) {
        int len = 0;
        for(int i=0; i< bt.length; i++) {
            len += bt[i].length;
        }
        byte[] result = new byte[len];
        int curIndex = 0;
        for(int i=0; i< bt.length; i++) {
            System.arraycopy(bt[i], 0, result, curIndex, bt[i].length);
            curIndex += bt[i].length;
        }
        return result;
    }

    /**
     * byte[]转short
     * @param bytes 需要转换成int的数组
     * @return int值
     */
    public static short bytesToShort(byte[] bytes) {
        short value = 0;
        for(int i = 0; i < 2; i++) {
            int shift= (1-i) * 8;
            value += (bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    /**
     * int 转 byte[]
     * 高位在前，低位在后
     * @param digit
     * @return
     */
    public static byte[] intToBytes(int digit) {
        byte[] length = new byte[2];
        for (int i = 0; (i < 4) && (i < 2); i++) {
            length[1 - i] = (byte) (digit >>> 8 * i);
        }
        return length;
    }

    /**
     * byte[]转int
     * @param bytes 需要转换成int的数组
     * @return int值
     */
    public static int bytesToInt(byte[] bytes) {
        int value = 0;
        for(int i = 0; i < 4; i++) {
            int shift= (3-i) * 8;
            value += (bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    public static byte dataSumCheck(byte[] data) {
        byte sum = 0;
        for(byte d: data) {
            sum = (byte) (sum + d);
        }
        return (byte) (sum & 0xFF);
    }


}
