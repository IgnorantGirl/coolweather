package com.example.wanghui.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {


    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){

        //创建一个OkHttpClient实例
        OkHttpClient client = new OkHttpClient();
        //发送一条HTTP请求，需要创建一个Request对象，同时增加其他方法丰富Request对象
        Request request = new Request.Builder().url(address).build();
        //调用enqueue方法，把okhttp3.Callback参数传入，enqueue方法的内部已经帮我们开好子线程
//然后会在子线程中去执行HTTP请求，并将最终的请求结果回调到okhttp3.Callback当中
        client.newCall(request).enqueue(callback);
    }
}
