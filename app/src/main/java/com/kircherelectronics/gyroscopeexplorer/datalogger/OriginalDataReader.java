package com.kircherelectronics.gyroscopeexplorer.datalogger;

import android.content.Context;
import android.util.Log;

import com.clj.fastble.utils.HexUtil;
import com.kircherelectronics.gyroscopeexplorer.util.ByteUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OriginalDataReader {

    private static float gyr_accuracy = 2000.0f/32768;
    private static float acc_accuracy = 9.8f/(32768/2);

    public static List<float[]> getFromAssets(Context context, String fileName) {

        try {

            List<float[]> dataList = new ArrayList<>();
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
            String str;
            // 按行读取字符串
            while ((str = bf.readLine()) != null) {
                float[] floats = new float[6];
                String line = str.replace(" ", "")
                        .replaceAll("<", "")
                        .replaceAll(">", "")
                        .substring(16);
                byte[] bytes = HexUtil.hexStringToBytes(line);
                if(bytes.length==12) {
                    for(int i=0; i<6; i++) {
                        byte[] d = new byte[2];
                        System.arraycopy(bytes, i*2, d, 0, d.length);
                        short s = ByteUtils.bytesToShort(d);
                        if(i<3) {
                            floats[i] = (float) s * acc_accuracy;

                        } else {
                            floats[i] = (float) s * gyr_accuracy;
                        }
                    }
                    // Log.e("acc", String.format("%f,%f,%f", floats[0],floats[1],floats[2]));
                    // Log.e("gyr", String.format("%f,%f,%f", floats[3],floats[4],floats[5]));

                }
                dataList.add(floats);
            }
            bf.close();
            inputStream.close();
            return dataList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
