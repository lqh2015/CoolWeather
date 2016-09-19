package com.example.dl.coolweather.util;

import android.text.TextUtils;

import com.example.dl.coolweather.db.CoolWeatherDB;
import com.example.dl.coolweather.model.City;
import com.example.dl.coolweather.model.County;
import com.example.dl.coolweather.model.Province;

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
}
