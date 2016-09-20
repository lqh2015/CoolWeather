package com.example.dl.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.dl.coolweather.db.CoolWeatherDB;
import com.example.dl.coolweather.model.City;
import com.example.dl.coolweather.model.County;
import com.example.dl.coolweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by liqinghai on 2016/9/19.
 * 用于解析网络返回的数据
 */
public class Utility {

    /**
     * 解析从网络返回的省份信息，并存入到数据库中
     * @param db
     * @param response
     * @return
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB db,String response){
        if(!TextUtils.isEmpty(response)){
            String[] allProvinces=response.split(",");
            if(allProvinces!=null && allProvinces.length>0){
                for(String p : allProvinces){
                    String[] array=p.split("\\|");
                    Province province=new Province();
                    province.setProvinceName(array[1]);
                    province.setProvinceCode(array[0]);
                    db.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析从网络返回的县信息，并存入到数据库中
     * @param db
     * @param response
     * @return
     */
    public synchronized static boolean handleCityResponse(CoolWeatherDB db,String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            String[] allCitys=response.split(",");
            if(allCitys!=null && allCitys.length>0){
                for(String c : allCitys){
                    String[] array=c.split("\\|");
                    City city=new City();
                    city.setCityName(array[1]);
                    city.setCityCode(array[0]);
                    city.setProvinceId(provinceId);
                    db.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析从网络返回的城市信息，并存入到数据库中
     * @param db
     * @param response
     * @return
     */
    public synchronized static boolean handleCountyResponse(CoolWeatherDB db,String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            String[] allCountys=response.split(",");
            if(allCountys!=null && allCountys.length>0){
                for(String c : allCountys){
                    String[] array=c.split("\\|");
                    County county=new County();
                    county.setCountyName(array[1]);
                    county.setCountyCode(array[0]);
                    county.setCityId(cityId);
                    db.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean handleWeatherResponse(Context context,String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONObject jsonObject=new JSONObject(response);
                JSONObject weatherinfo = jsonObject.getJSONObject("weatherinfo");
                String city = weatherinfo.getString("city");
                String weatherCode = weatherinfo.getString("cityid");
                String temp1 = weatherinfo.getString("temp1");
                String temp2 = weatherinfo.getString("temp2");
                String weather = weatherinfo.getString("weather");
                String ptime = weatherinfo.getString("ptime");

                saveWeatherInfo(context,city,weatherCode,temp1,temp2,weather,ptime);//把天气信息存储到
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static void saveWeatherInfo(Context context, String city,String weatherCode, String temp1, String temp2, String weather, String ptime) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("cityName",city);
        editor.putString("weather_code",weatherCode);
        editor.putString("temp1",temp1);
        editor.putString("temp2",temp2);
        editor.putString("weather_desp",weather);
        editor.putString("publish_time",ptime);
        editor.putString("current_date",sdf.format(new Date()));

    }


}
