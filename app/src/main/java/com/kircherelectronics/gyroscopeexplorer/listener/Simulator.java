package com.kircherelectronics.gyroscopeexplorer.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.util.Log;

import com.kircherelectronics.gyroscopeexplorer.activity.GyroscopeActivity;
import com.kircherelectronics.gyroscopeexplorer.datalogger.OriginalDataReader;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Simulator  {

    public static void registListener(Context context, String fileName, Listener listener) {
        try {
            Observable<Event> observable = Observable.create(new ObservableOnSubscribe<Event>() {
                @Override
                public void subscribe(ObservableEmitter<Event> emitter) {
                    List<float[]> dataList = OriginalDataReader.getFromAssets(context, fileName);
                    int line = dataList.size();
                    boolean first = true;
                    // while (true) {
                    // Log.e("xxx", "...");
                        /*if(first) {
                            first = false;
                            startDataLog();
                        } else {
                            stopDataLog();
                        }*/

                    for (int i = 0; i < line; i++) {

                        float[] data = dataList.get(i);

                        // float[] acc = new float[]{data[0], data[1], data[2]};
                        Event event = new Event();
                        event.setTimestamp(System.nanoTime());
                        event.setData(data);
                        event.setType(Sensor.TYPE_ACCELEROMETER);
                        emitter.onNext(event);
                        /*try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        float[] gyr = new float[]{data[3], data[4], data[5]};
                        event = new Event();
                        event.setTimestamp(System.nanoTime());
                        event.setData(gyr);
                        event.setType(Sensor.TYPE_GYROSCOPE);
                        emitter.onNext(event);*/
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // }
                }
            });
            Log.e("xxxx", "xxx");
            observable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Event>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }
                        @Override
                        public void onNext(Event event) {
                            listener.onSensorChanged(event);
                        }
                        @Override
                        public void onComplete() {
                        }
                        @Override
                        public void onError(Throwable e) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static class Event {
        private long timestamp;
        private int type = 0;
        private float[] data;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public float[] getData() {
            return data;
        }

        public void setData(float[] data) {
            this.data = data;
        }
    }

    public interface Listener {
        void onSensorChanged(Event event);
    }
}
