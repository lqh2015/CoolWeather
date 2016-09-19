package com.example.dl.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dl.coolweather.model.City;
import com.example.dl.coolweather.model.County;
import com.example.dl.coolweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liqinghai on 2016/9/19.
 */
public class CoolWeatherDB {
    /**
     * 数据库名称
     */
    private static final String DB_NAME = "cool_weather";
    /**
     * 数据库版本
     */
    private static final int  DB_VERSION = 1;

    private static CoolWeatherDB coolWeatherDB;//采用单利模式，保证整个项目只有一个数据库引用

    private SQLiteDatabase db;

    /**
     * 将构造方法私有化
     * @param context
     */
    private CoolWeatherDB(Context context){
        CoolWeatherOpenHelper helper=new CoolWeatherOpenHelper(context,DB_NAME,null,DB_VERSION);
        db=helper.getWritableDatabase();
    }

    /**
     * 使用单利模式，当使用多线程操作的时候可能会出现创建多个实例的情况所有使用synchronized关键字
     * @param context
     * @return
     */
    public synchronized static CoolWeatherDB getInstance(Context context){
        if(coolWeatherDB==null){
            coolWeatherDB=new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }

    /**
     * 往数据库中插入省份信息
     * @param province
     */
    public void saveProvince(Province province){
        if(province!=null){
            ContentValues values=new ContentValues();
            values.put("provinceName",province.getProvinceName());
            values.put("provinceCode",province.getProvinceCode());
            db.insert("Province",null,values);
        }
    }

    /**
     * 从数据库中查询所有省份信息
     * @return
     */
    public List<Province> loadProvinces(){
        List<Province> list = new ArrayList<Province>();
        Cursor cursor=db.query("Province",null,null,null,null,null,null);

        while (cursor.moveToNext()){
           Province province=new Province();
            String provinceName=cursor.getString(cursor.getColumnIndex("provinceName"));
            String provinceCode=cursor.getString(cursor.getColumnIndex("provinceCode"));
            province.setProvinceName(provinceName);
            province.setProvinceName(provinceCode);
            list.add(province);
        }
        cursor.close();
        return list;
    }

    /**
     * 往数据库中插入城市信息
     * @param city
     */
    public void saveCity(City city){
        if(city!=null){
            ContentValues values=new ContentValues();
            values.put("cityName",city.getCityName());
            values.put("cityCode",city.getCityCode());
            values.put("provinceId",city.getProvinceId());
            db.insert("City",null,values);
        }
    }

    /**
     * 从数据库中查询所有城市信息
     * @return
     */
    public List<City> loadCitys(){
        List<City> list = new ArrayList<City>();
        Cursor cursor=db.query("City",null,null,null,null,null,null);

        while (cursor.moveToNext()){
            City city=new City();
            String cityName=cursor.getString(cursor.getColumnIndex("cityName"));
            String cityCode=cursor.getString(cursor.getColumnIndex("ciryCode"));
            int provinceId=cursor.getInt(cursor.getColumnIndex("provinceId"));
            city.setCityName(cityName);
            city.setCityCode(cityCode);
            city.setProvinceId(provinceId);
            list.add(city);
        }
        cursor.close();
        return list;
    }

    /**
     * 往数据库中插入县信息
     * @param county
     */
    public void saveCounty(County county){
        if(county!=null){
            ContentValues values=new ContentValues();
            values.put("countyName",county.getCountyName());
            values.put("countyCode",county.getCountyCode());
            values.put("cityId",county.getCityId());
            db.insert("City",null,values);
        }
    }

    /**
     * 从数据库中查询所有县信息
     * @return
     */
    public List<County> loadCounty(){
        List<County> list = new ArrayList<County>();
        Cursor cursor=db.query("County",null,null,null,null,null,null);

        while (cursor.moveToNext()){
            County county=new County();
            String countyName=cursor.getString(cursor.getColumnIndex("countyName"));
            String countyCode=cursor.getString(cursor.getColumnIndex("countyCode"));
            int cityId=cursor.getInt(cursor.getColumnIndex("cityId"));
            county.setCountyName(countyName);
            county.setCountyCode(countyCode);
            county.setCityId(cityId);
            list.add(county);
        }
        cursor.close();
        return list;
    }
}
