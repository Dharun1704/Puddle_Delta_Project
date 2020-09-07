package com.example.puddle;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SensorActivity extends AppCompatActivity implements SensorEventListener{

    TextView mDate, mYear, mTemp, mHum, mLight, mPressure, uTemp, uPress, uHum;
    TextView[] mAcc, uAcc, mGyro, mGravity, uGravity, mGmf;
    Sensor sPressure, sHumidity, sLight, sTemperature, sAccelerometer, sGyroscope, sGravity, sGMF;
    SensorManager manager;

    private BroadcastReceiver mBattery = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level",0);
            TextView battery = findViewById(R.id.batteryLvl);
            battery.setText(level + "%");
        }
    };

    @Override
    @SuppressLint("SimpleDateFormat")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mDate = findViewById(R.id.sensorDate);
        mYear = findViewById(R.id.sensorYear);
        mTemp = findViewById(R.id.sensorTemp);
        mHum = findViewById(R.id.sensorHumidity);
        mLight = findViewById(R.id.sensorLight);
        mPressure = findViewById(R.id.sensorPressure);

        uTemp = findViewById(R.id.tempUnit);
        uPress = findViewById(R.id.pressUnit);
        uHum = findViewById(R.id.humUnit);

        mAcc = new TextView[3];
        mAcc[0] = findViewById(R.id.acc_x);
        mAcc[1] = findViewById(R.id.acc_y);
        mAcc[2] = findViewById(R.id.acc_z);

        uAcc = new TextView[3];
        uAcc[0] = findViewById(R.id.acc_xUnit);
        uAcc[1] = findViewById(R.id.acc_yUnit);
        uAcc[2] = findViewById(R.id.acc_zUnit);
        for (int i = 0; i < 3; i++) {
            uAcc[i].setText(Html.fromHtml("m/s<sup>2</sup>"));
        }

        mGyro = new TextView[3];
        mGyro[0] = findViewById(R.id.gyro_x);
        mGyro[1] = findViewById(R.id.gyro_y);
        mGyro[2] = findViewById(R.id.gyro_z);

        mGravity = new TextView[3];
        mGravity[0] = findViewById(R.id.grv_x);
        mGravity[1] = findViewById(R.id.grv_y);
        mGravity[2] = findViewById(R.id.grv_z);

        uGravity = new TextView[3];
        uGravity[0] = findViewById(R.id.grv_xUnit);
        uGravity[1] = findViewById(R.id.grv_yUnit);
        uGravity[2] = findViewById(R.id.grv_zUnit);
        for (int i = 0; i < 3; i++) {
            uGravity[i].setText(Html.fromHtml("m/s<sup>2</sup>"));
        }

        mGmf = new TextView[3];
        mGmf[0] = findViewById(R.id.gmf_x);
        mGmf[1] = findViewById(R.id.gmf_y);
        mGmf[2] = findViewById(R.id.gmf_z);


        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());
        SimpleDateFormat format1 = new SimpleDateFormat("dd MMMM");
        SimpleDateFormat format2 = new SimpleDateFormat("YYYY");
        mDate.setText(format1.format(ts));
        mYear.setText(format2.format(ts));

        registerReceiver(mBattery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        setSensors();


    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            mTemp.setText(String.valueOf((int) event.values[0]));
        }

        if (sensor.getType() == Sensor.TYPE_PRESSURE) {
            mPressure.setText(String.valueOf((int) event.values[0]));
        }

        if (sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            mHum.setText(String.valueOf((int) event.values[0]));
        }

        if (sensor.getType() == Sensor.TYPE_LIGHT) {
            mLight.setText(String.format("%.1f", event.values[0]));
        }

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAcc[0].setText(String.format("%.5f", event.values[0]) + " ");
            mAcc[1].setText(String.format("%.5f", event.values[1]) + " ");
            mAcc[2].setText(String.format("%.5f", event.values[2]) + " ");
        }

        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            mGyro[0].setText(String.format("%.5f", event.values[0]) + " rad/s");
            mGyro[1].setText(String.format("%.5f", event.values[1]) + " rad/s");
            mGyro[2].setText(String.format("%.5f", event.values[2]) + " rad/s");
        }

        if (sensor.getType() == Sensor.TYPE_GRAVITY) {
            mGravity[0].setText(String.format("%.4f", event.values[0]) + " ");
            mGravity[1].setText(String.format("%.4f", event.values[1]) + " ");
            mGravity[2].setText(String.format("%.4f", event.values[2]) + " ");
        }

        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGmf[0].setText(String.format("%.5f", event.values[0]) + " µT");
            mGmf[1].setText(String.format("%.5f", event.values[1]) + " µT");
            mGmf[2].setText(String.format("%.5f", event.values[2]) + " µT");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setSensors() {

        sTemperature = manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (sTemperature != null) {
            manager.registerListener(SensorActivity.this, sTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            mTemp.setText(R.string.sensorError);
            mTemp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            mTemp.setTextColor(Color.RED);
            uTemp.setVisibility(View.GONE);
        }

        sPressure = manager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (sPressure != null) {
            manager.registerListener(SensorActivity.this, sPressure, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            mPressure.setText(R.string.sensorError);
            mPressure.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            mPressure.setTextColor(Color.RED);
            uPress.setVisibility(View.GONE);
        }

        sLight = manager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (sLight != null) {
            manager.registerListener(SensorActivity.this, sLight, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            mLight.setText(R.string.sensorError);
            mLight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            mLight.setTextColor(Color.RED);
        }

        sHumidity = manager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if (sHumidity != null) {
            manager.registerListener(SensorActivity.this, sHumidity, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            mHum.setText(R.string.sensorError);
            mHum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            mHum.setTextColor(Color.RED);
            uHum.setVisibility(View.GONE);
        }

        sAccelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sAccelerometer != null) {
            manager.registerListener(SensorActivity.this, sAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            mAcc[0].setText(R.string.sensorError);
            mAcc[0].setTextColor(Color.RED);
            uAcc[0].setVisibility(View.GONE);
            for (int i = 1; i < 3; i++) {
                mAcc[i].setVisibility(View.GONE);
                uAcc[i].setVisibility(View.GONE);
            }
        }

        sGyroscope = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (sGyroscope != null) {
            manager.registerListener(SensorActivity.this, sGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            mGyro[0].setText(R.string.sensorError);
            mGyro[0].setTextColor(Color.RED);
            mGyro[1].setVisibility(View.GONE);
            mGyro[2].setVisibility(View.GONE);
        }

        sGravity = manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (sGravity != null) {
            manager.registerListener(SensorActivity.this, sGravity, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            mGravity[0].setText(R.string.sensorError);
            mGravity[0].setTextColor(Color.RED);
            uGravity[0].setVisibility(View.GONE);
            for (int i = 1; i < 3; i++) {
                mGravity[i].setVisibility(View.GONE);
                uGravity[i].setVisibility(View.GONE);
            }
        }

        sGMF = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (sGMF != null) {
            manager.registerListener(SensorActivity.this, sGMF, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            mGmf[0].setText(R.string.sensorError);
            mGmf[0].setTextColor(Color.RED);
            mGmf[1].setVisibility(View.GONE);
            mGmf[2].setVisibility(View.GONE);
        }
    }
}
