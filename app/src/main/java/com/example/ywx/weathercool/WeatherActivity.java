package com.example.ywx.weathercool;

import android.app.Fragment;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ywx.weathercool.gson.Forecast;
import com.example.ywx.weathercool.gson.Weather;
import com.example.ywx.weathercool.service.AutoUpdateService;
import com.example.ywx.weathercool.util.HttpUtil;
import com.example.ywx.weathercool.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private Button navButton;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherinfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    private LinearLayout aqiLayout;
    private SwipeRefreshLayout refreshLayout;
    private String mweatherId;
    private IntentFilter filter;
    private NetworkChangeReceiver networkChangeReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkChangeReceiver=new NetworkChangeReceiver();
        filter=new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver,filter);
        if(Build.VERSION.SDK_INT>=21)
        {
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        refreshLayout=(SwipeRefreshLayout)findViewById(R.id.refresh);
        aqiLayout=(LinearLayout)findViewById(R.id.aqi_layout);
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        navButton=(Button)findViewById(R.id.nav_button);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherinfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mweatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        } else
        {
            mweatherId=getIntent().getStringExtra("weather_id");
            requestWeather(mweatherId);
        }
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                navButton.setVisibility(View.INVISIBLE);
                ChooseAreaFragment fragment=(ChooseAreaFragment)getFragmentManager().findFragmentById(R.id.choose_area_fragment);
                fragment.backToMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                navButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mweatherId);
            }
        });
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });
    }
    private void loadBingPic()
    {
        String address="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
    public DrawerLayout getDrawerLayout()
    {
        return drawerLayout;
    }
    public SwipeRefreshLayout getRefreshLayout()
    {
        return refreshLayout;
    }
    public void requestWeather(final String weatherId)
    {
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=35f26d28c8eb49928bea3ea792ce24ed";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败.",Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status))
                        {
                           SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mweatherId=weather.basic.weatherId;
                            showWeatherInfo(weather);
                            refreshLayout.setRefreshing(false);
                        }else
                        {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败.",Toast.LENGTH_SHORT).show();
                            refreshLayout.setRefreshing(false);
                        }
                    }
                });
            }
        });
    }
    private void showWeatherInfo(Weather weather)
    {
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime;
        String degree=weather.now.temperature;
        String weatherInfo=weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText("天气网更新: "+updateTime);
        degreeText.setText(degree+"℃");
        weatherinfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList)
        {
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateTime=(TextView)view.findViewById(R.id.data_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            dateTime.setText(forecast.date);
            infoText.setText(forecast.more.info);
            minText.setText(forecast.temperature.min+"℃");
            maxText.setText(forecast.temperature.max+"℃");
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null)
        {
            aqiLayout.setVisibility(View.VISIBLE);
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        else
        {
            aqiLayout.setVisibility(View.GONE);
        }
        String comfort="舒适度: "+weather.suggestion.comfort.info;
        String carWash="洗车指数: "+weather.suggestion.carWash.info;
        String sport="运动建议: "+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
    class NetworkChangeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
            if(networkInfo!=null&&networkInfo.isAvailable())
            {
                Toast.makeText(context,"网络已连接",Toast.LENGTH_SHORT).show();
                loadBingPic();
                refreshLayout.setRefreshing(true);
                requestWeather(mweatherId);
            }
            else
            {
                Toast.makeText(context,"当前网络不可用",Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }
}
