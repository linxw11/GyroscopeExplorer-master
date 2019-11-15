package com.kircherelectronics.gyroscopeexplorer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class DynamicAverage {

    private List<Queue<Float>> queues = null;
    private float[] av = null;

    public float[] getAverage(float[] value) {

        if(queues == null) {
            queues = new ArrayList<>();
            av = new float[value.length];
        }
        for (int i=0; i<value.length; i++) {
            if(queues.size()<i+1) {
                queues.add(new ArrayBlockingQueue<>(12));
            }
            if(!queues.get(i).offer(value[i])) {
                queues.get(i).poll();
                queues.get(i).offer(value[i]);
            }
            float sum = 0;
            for (float f: queues.get(i)) {
                sum += f;
            }
            av[i] = sum / queues.get(i).size();
        }
        return av;
    }

    public float[] removeAverage(float[] value) {
        getAverage(value);
        for (int i=0; i<value.length; i++) {
            value[i] = value[i] - av[i];
        }
        return value;
    }
}
