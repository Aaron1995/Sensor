package moe.hanawa.sensor;

import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import java.math.BigDecimal;

import broadcast.Receiver;
import service.LocationService;

public class MySensorTest extends AppCompatActivity {

    private TextView accelerometerView;
    private TextView orientationView;
    private SensorManager sensorManager;
    private MySensorEventListener sensorEventListener;
    private LocalBroadcastManager localBroadcastManager;
    private Intent locationService;
    private IntentFilter intentFilter;
    private Receiver receiver;
    private TextView gps;
    private Button stop;
    private Button start;
    private EditText timeInterval;
    private double lastLat = 0;
    private double lastLong = 0;
    private int status = 1;
    private int time = 3000;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_sensor_test);

        findView();
        init();

        tvShowDateTime();
    }

    private void thread(final int t) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (1 == status) {
                    try {
                        tvShowDateTime();
                        Thread.sleep(t);
                        Log.d("msg", "运行了thread函数");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void findView() {
        accelerometerView = (TextView) findViewById(R.id.accelerometerView);
        orientationView = (TextView) findViewById(R.id.orientationView);
        gps = (TextView) findViewById(R.id.gps);
        stop = (Button) findViewById(R.id.time_stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = 0;
            }
        });
        start = (Button) findViewById(R.id.time_start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int t = Integer.valueOf(timeInterval.getText().toString()) * 1000;
                status = 1;
                Log.d("msg", "输入的时间间隔为：" + t + " ms  status=" + status);
                thread(t);
            }
        });
        timeInterval = (EditText) findViewById(R.id.time_interval);
    }

    private void init() {

        if (null == sensorEventListener)
            sensorEventListener = new MySensorEventListener();
        //获取感应器管理器
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    private void tvShowDateTime() {
        if (null == locationService) {
            locationService = new Intent(MySensorTest.this, LocationService.class);
            startService(locationService);
        }
        intentFilter = new IntentFilter();
        receiver = new Receiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter.addAction("获取定位经纬度值");
        localBroadcastManager.registerReceiver(receiver, intentFilter);
        receiver.setOnReceivveLatLongListner(new Receiver.LatLongListner() {
            @Override
            public void OnReceived(double latitude, double longitude, String dateTime) {
                //latitude为定位纬度  longitude为定位经度   index为将StaticValues.VI重新排序后的新序列下标
                Log.d("msg", "获取到的定位经纬度为：" + latitude + " " + longitude + " " + dateTime);
                if (0 == lastLat && 0 == lastLong) {
                    lastLat = latitude;
                    lastLong = longitude;
                    gps.setText("gps纬度latitude为 — \n" + latitude + " \n经度longitude为 — " + longitude + " \n获取时间为 — "
                            + dateTime);
                } else {
                    BigDecimal bigDecimal = new BigDecimal(AMapUtils.calculateLineDistance(new LatLng(lastLat, lastLong)
                            , new LatLng(latitude, longitude)));
                    gps.setText("gps纬度latitude为 — " + latitude + " \ngps经度longitude为 — " + longitude + " \n获取时间为 — "
                            + dateTime + " \n距离上一次定位的直线距离为 — " + bigDecimal.toPlainString());
                    lastLat = latitude;
                    lastLong = longitude;
                }
                if (receiver != null) {
                    intentFilter = null;
                    receiver = null;
                    LocalBroadcastManager.getInstance(MySensorTest.this).unregisterReceiver(receiver);
                    localBroadcastManager = null;
                    Log.d("msg", "destroy broadcast success!");
                    if (locationService != null) {
                        stopService(locationService);
                        locationService = null;
                    }
                }
            }
        });

    }


    @Override
    protected void onResume() {
        if (null != locationService)
            startService(locationService);
        //获取方向传感器
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(sensorEventListener, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //获取加速度传感器
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    private final class MySensorEventListener implements SensorEventListener {
        //可以得到传感器实时测量出来的变化值
        @Override
        public void onSensorChanged(SensorEvent event) {
            //得到方向的值
            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                float x = event.values[SensorManager.DATA_X];
                float y = event.values[SensorManager.DATA_Y];
                float z = event.values[SensorManager.DATA_Z];

                orientationView.setText("方向传感器的x,y,z： " + Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z - 9.8));
            }
            //得到加速度的值
            else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[SensorManager.DATA_X];
                float y = event.values[SensorManager.DATA_Y];
                float z = event.values[SensorManager.DATA_Z];
                accelerometerView.setText("加速度传感器的x,y,z： " + Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z));
            }

        }

        //重写变化
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    //暂停传感器的捕获
    @Override
    protected void onPause() {
        sensorManager.unregisterListener(sensorEventListener);
        if (locationService != null)
            stopService(locationService);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (receiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }
}
