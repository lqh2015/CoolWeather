package com.example.dl.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.dl.coolweather.receiver.AutoUpdateReceiver;
import com.example.dl.coolweather.util.HttpUtil;
import com.example.dl.coolweather.util.Utility;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updataWeatherInfo();
            }
        }).start();

        AlarmManager am= (AlarmManager) getSystemService(ALARM_SERVICE);
        int anhour=60*60*1000;//一小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anhour;
        Intent i=new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pending=PendingIntent.getBroadcast(this,0,i,0);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pending);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     *更新天气信息
     */
    private void updataWeatherInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = preferences.getString("weather_code", "");
        String address = "http://www.weather.com.cn/data/cityinfo/" +
                weatherCode + ".html";
        HttpUtil.sendHttpRequest(address, new HttpUtil.HttpCallbackListener() {
            @Override
            public void onFinish(String data) {
                Utility.handleWeatherResponse(AutoUpdateService.this,data);//更新天气信息到preferences中
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
