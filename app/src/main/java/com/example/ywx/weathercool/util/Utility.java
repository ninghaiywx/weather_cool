package com.example.ywx.weathercool.util;

import android.text.TextUtils;

import com.example.ywx.weathercool.database.City;
import com.example.ywx.weathercool.database.Country;
import com.example.ywx.weathercool.database.Province;
import com.example.ywx.weathercool.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ywx on 2017/4/4.
 * 用于解析各种json
 * 数据
 */

public class Utility {
    public static boolean handleProvinceResponse(String response)
    {
        if(!TextUtils.isEmpty(response))
        {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject object = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(object.getString("name"));
                    province.setProvinceCode(object.getInt("id"));
                    province.save();
                }
                return true;
            }
            catch(JSONException e)
                {
                    e.printStackTrace();
                }
            }
            return false;
        }
        public static boolean handleCityResponse(String response,int provinceId)
        {
            if(!TextUtils.isEmpty(response))
            {
                try {
                    JSONArray allCities=new JSONArray(response);
                    for(int i=0;i<allCities.length();i++)
                    {
                        JSONObject object=allCities.getJSONObject(i);
                        City city=new City();
                        city.setCityName(object.getString("name"));
                        city.setCityCode(object.getInt("id"));
                        city.setProvinceId(provinceId);
                        city.save();
                    }
                    return true;
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
        public static boolean handleCountryResponse(String response,int cityId)
        {
            if(!TextUtils.isEmpty(response))
            {
                try {
                    JSONArray allCountries=new JSONArray(response);
                    for(int i=0;i<allCountries.length();i++)
                    {
                        JSONObject object=allCountries.getJSONObject(i);
                        Country country=new Country();
                        country.setCountryName(object.getString("name"));
                        country.setWeatherId(object.getString("weather_id"));
                        country.setCityId(cityId);
                        country.save();
                    }
                    return true;
                }catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            return false;
        }
        public static Weather handleWeatherResponse(String response)
        {
            try {
                JSONObject object=new JSONObject(response);
                JSONArray jsonArray=object.getJSONArray("HeWeather");
                String weatherContent=jsonArray.getJSONObject(0).toString();
                return new Gson().fromJson(weatherContent,Weather.class);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
}
