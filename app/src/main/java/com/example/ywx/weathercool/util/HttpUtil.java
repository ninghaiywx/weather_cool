package com.example.ywx.weathercool.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ywx on 2017/4/4.
 * 工具类
 * 用于发送Http请求
 */
public class HttpUtil {
    public static void sendOkHttpRequest(String address, Callback callback)
    {
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
