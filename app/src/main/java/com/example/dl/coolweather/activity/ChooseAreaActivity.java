package com.example.dl.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dl.coolweather.R;
import com.example.dl.coolweather.db.CoolWeatherDB;
import com.example.dl.coolweather.model.City;
import com.example.dl.coolweather.model.County;
import com.example.dl.coolweather.model.Province;
import com.example.dl.coolweather.util.HttpUtil;
import com.example.dl.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends Activity {

    //省市县对应的等级
    private static final int LEAVER_PROVINCE=0;
    private static final int LEAVER_CITY=1;
    private static final int LEAVER_COUNTY=0;

    //布局中用到的控件
    private ProgressDialog progressDialog;
    private ListView listView;
    private TextView textView;

    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private ArrayList<String> dataList=new ArrayList<String>();

    //省市县各级列表
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    //当前选择的省、市和当前的等级
    private Province currentProvince;
    private City currentCity;
    private int currentLeaver;

    private boolean isFromActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isFromActivity=getIntent().getBooleanExtra("from_weather_activity",false);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean city_selected=preferences.getBoolean("city_selected",false);
        //首先判断是否是从WeatherActivity跳转过来的，如果不是，并且已经选择过城市就直接跳转到天气显示界面
        if(!isFromActivity&&city_selected){
            Intent intent =new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_choose_area);
        initView();//初始化布局
        coolWeatherDB=CoolWeatherDB.getInstance(this);//获取db的实例

        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentLeaver==LEAVER_PROVINCE){
                    currentProvince=provinceList.get(i);
                    queryCities();
                }else if (currentLeaver==LEAVER_CITY){
                    currentCity=cityList.get(i);
                    queryCounties();
                }else if(currentLeaver==LEAVER_COUNTY){
                    County county = countyList.get(i);
                    String countyCode = county.getCountyCode();
                    Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("countyCode",countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProivinces();//第一次进来查询省份信息
    }


    /**
     * 初始化布局
     */
    private void initView() {
        listView= (ListView) findViewById(R.id.list_view);
        textView= (TextView) findViewById(R.id.title_text);
    }

    /**
     * 查询省份信息,优先从数据库中取，取不到则从网络上获取
     */
    private void queryProivinces() {
        provinceList=coolWeatherDB.loadProvinces();
        if(provinceList!=null&&provinceList.size()>0){
            dataList.clear();//清空datalist中的数据，否则会出现错乱
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//默认选中第一条数据
            textView.setText("中国");
            currentLeaver=LEAVER_PROVINCE;
        }else{
            queryFromServer(null,"province");//从网络上获取数据
        }
    }



    /**
     * 查询城市信息,优先从数据库中取，取不到则从网络上获取
     */
    private void queryCities() {
        cityList=coolWeatherDB.loadCitys();
        if(cityList!=null&&cityList.size()>0){
            dataList.clear();//清空datalist中的数据，否则会出现错乱
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//默认选中第一条数据
            textView.setText(currentProvince.getProvinceName());
            currentLeaver=LEAVER_CITY;
        }else{
            queryFromServer(currentProvince.getProvinceCode(),"city");//从网络上获取数据
        }
    }

    /**
     * 查询县级信息,优先从数据库中取，取不到则从网络上获取
     */
    private void queryCounties() {
        countyList=coolWeatherDB.loadCounty();
        if(countyList!=null&&countyList.size()>0){
            dataList.clear();//清空datalist中的数据，否则会出现错乱
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//默认选中第一条数据
            textView.setText(currentCity.getCityName());
            currentLeaver=LEAVER_COUNTY;
        }else{
            queryFromServer(currentCity.getCityCode(),"county");//从网络上获取数据
        }
    }

    /**
     * 根据代号和类型从网络上获取数据
     */
    private void queryFromServer(String code, final String type) {

        String address;

        if (TextUtils.isEmpty(code)){//省份信息不需要传入code
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }else{
            address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
        }
        showProgressDialog();//显示进度对话框
        HttpUtil.sendHttpRequest(address, new HttpUtil.HttpCallbackListener() {
            @Override
            public void onFinish(String data) {//把从网络上返回的数据存储到数据库中
                boolean result=false;
                if("province".equals(type)){
                    result=Utility.handleProvincesResponse(coolWeatherDB,data);
                }else if ("city".equals(type)){
                    result=Utility.handleCityResponse(coolWeatherDB,data,currentProvince.getId());
                }else if ("county".equals(type)){
                    result=Utility.handleCountyResponse(coolWeatherDB,data,currentCity.getId());
                }
                if(result){
                    runOnUiThread(new Runnable() {//回到主线程去执行
                        @Override
                        public void run() {
                            closeProgressDialog();//关闭进度条对话框
                            if ("province".equals(type)){//重新加载数据
                                queryProivinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }

            @Override
            public void onError(Exception e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();//关闭进度条对话框
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if(progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("正在加载请稍后...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 捕获back键，根据当前显示的级别去判断是回到省份，城市，县级等信息还是直接退出
     */
    @Override
    public void onBackPressed() {
        if(currentLeaver==LEAVER_CITY){
            queryCounties();
        }else if(currentLeaver==LEAVER_CITY){
            queryProivinces();
        }else{
            if(isFromActivity){//如果是从WeatherActivity跳转过来的，则返回WeatherActivity
                Intent intent=new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }

    }
}
