package com.example.dl.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dl.coolweather.R;
import com.example.dl.coolweather.service.AutoUpdateService;
import com.example.dl.coolweather.util.HttpUtil;
import com.example.dl.coolweather.util.Utility;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener{
    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名
     第一行代码——Android
     514
     */
    private TextView cityNameText;
    /**
     * 用于显示发布时间
     */
    private TextView publishText;
    /**
     * 用于显示天气描述信息
     */
    private TextView weatherDespText;
    /**
     * 用于显示气温1
     */
    private TextView temp1Text;
    /**
     * 用于显示气温2
     */
    private TextView temp2Text;
    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;
    /**
     * 切换城市按钮
     */
    private Button switchCity;
    /**
     * 更新天气按钮
     */
    private Button refreshWeather;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_weather);
        getSupportActionBar().hide();

        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);

        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        String countyCode=getIntent().getStringExtra("county_code");
        if(!TextUtils.isEmpty(countyCode)){
            publishText.setText("正在加载中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else{
            showWeather();//没有传递进来weatherCode就显示本地天气信息
        }
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
    }

    /**
     * 显示本地天气信息
     */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText( prefs.getString("city_name", ""));
        temp1Text.setText(prefs.getString("temp1", ""));
        temp2Text.setText(prefs.getString("temp2", ""));
        weatherDespText.setText(prefs.getString("weather_desp", ""));
        publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
        currentDateText.setText(prefs.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);

        Intent intent=new Intent(this, AutoUpdateService.class);//当展示完天气信息的时候就去开启一个服务
        startService(intent);
    }

    /**
     * 根据传进来的countyCode去加载天气信息
     * @param countyCode
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" +
                countyCode + ".xml";
        queryFromServer(address,"countyCode");
    }

    /**
     * 根据天气编号去加载天气信息
     * @param weatherCode
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" +
                weatherCode + ".html";
        queryFromServer(address,"weatherCode");
    }

    /**
     * 根据传递进来的参数和类型去查询天气信息，还是先查询weatherCode在去查询天气信息
     * @param address
     * @param type
     */
    private void queryFromServer(final String address, final String type) {
        if (!TextUtils.isEmpty(type)) {
            HttpUtil.sendHttpRequest(address, new HttpUtil.HttpCallbackListener() {
                @Override
                public void onFinish(String data) {//如果传递进来的是城市编号就先用城市编号获取对应的天气编号再去查询天气信息
                    if ("countyCode".equals(type)){
                        String[] array=data.split("\\|");
                        if(array!=null&&array.length==2){
                            String weratherCode=array[1];
                            queryWeatherInfo(weratherCode);
                        }
                    }else if("weatherCode".equals(type)){//如果是天气编号就直接解析从服务器获取到的数据
                        boolean result=Utility.handleWeatherResponse(WeatherActivity.this,data);
                        if(result){
                            showWeather();//解析成功去显示信息
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WeatherActivity.this,"加载失败！",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.switch_city:
                Intent intent=new Intent(this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity",true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather://取出weather_Code去加载天气信息
                publishText.setText("正在刷新...");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String weather_code = preferences.getString("weather_code", "");
                if(TextUtils.isEmpty(weather_code)){
                    queryWeatherInfo(weather_code);
                }
                break;
        }

    }
}
