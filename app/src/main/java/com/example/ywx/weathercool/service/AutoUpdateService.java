package com.example.ywx.weathercool.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;

import com.example.ywx.weathercool.MainActivity;
import com.example.ywx.weathercool.R;
import com.example.ywx.weathercool.WeatherActivity;
import com.example.ywx.weathercool.gson.Weather;
import com.example.ywx.weathercool.util.HttpUtil;
import com.example.ywx.weathercool.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    private Notification noti;
    private Weather weather;
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null)
        {
            weather=Utility.handleWeatherResponse(weatherString);
            Intent intent=new Intent(this,WeatherActivity.class);
            PendingIntent pi=PendingIntent.getActivity(this,0,intent,0);
            Notification notification = new NotificationCompat.Builder(this).setContentTitle(weather.basic.cityName+": "+weather.now.temperature + "℃").setSmallIcon(R.mipmap.ic_launcher).setStyle(new NotificationCompat.BigTextStyle().bigText(weather.suggestion.comfort.info)).setContentIntent(pi).build();
            startForeground(1, notification);
            noti=notification;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        AlarmManager manager=(AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour=60*60*1000;
        long updateTime= SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pendingIntent=PendingIntent.getService(this,0,i,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,updateTime,pendingIntent);
        return super.onStartCommand(intent,flags,startId);
    }

    private void updateWeather() {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null)
        {
            weather= Utility.handleWeatherResponse(weatherString);
            String weatherId=weather.basic.weatherId;
            String url="http://guolin.tech/api/weather?cityid="+weatherId+"&key=35f26d28c8eb49928bea3ea792ce24ed";
            HttpUtil.sendOkHttpRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText=response.body().string();
                    weather=Utility.handleWeatherResponse(responseText);
                    if(weather!=null&&weather.status.equals("ok"))
                    {
                        getNotificationManager().notify(1,getNotification());
                        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification() {
        Intent intent=new Intent(this,WeatherActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setContentTitle(weather.basic.cityName+": "+weather.now.temperature + "℃").setStyle(new NotificationCompat.BigTextStyle().bigText(weather.suggestion.comfort.info)).setSmallIcon(R.mipmap.ic_launcher).setContentText(weather.suggestion.comfort.info).setContentIntent(pi);
        return builder.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
