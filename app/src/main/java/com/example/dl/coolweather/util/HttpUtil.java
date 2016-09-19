package com.example.dl.coolweather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by liqinghai on 2016/9/19.
 */
public class HttpUtil {
    /**
     * 发送Http请求去获取网络数据
     * @param address 数据地址
     * @param listener 回掉接口
     */
    public  static void sendHttpRequest(final String address, final HttpCallbackListener listener){

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection= (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response=new StringBuilder();
                    String line="";
                    while ((line=reader.readLine())!=null){
                        response.append(line);
                    }
                    //获取数据成功回调接口中的onFinish方法
                    if(listener!=null){
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //获取数据失败回调接口中的方法
                    if(listener!=null){
                        listener.onError(e);
                    }
                }
            }
        }).start();
    }

    public interface HttpCallbackListener {
        void onFinish(String data);
        void onError(Exception e);
    }
}
