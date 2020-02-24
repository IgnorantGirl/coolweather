package com.example.wanghui.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.preference.PreferenceManager;

import com.example.wanghui.coolweather.WeatherActivity;
import com.example.wanghui.coolweather.gson.Weather;
import com.example.wanghui.coolweather.util.HttpUtil;
import com.example.wanghui.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
           return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //更新天气
        updateWeather();
        //更新背景图片
        updateBingPic();
        //获取一个AlarmManager的实例
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int  anHour = 8*60*60*1000;//这是8小时的毫秒数
        //定时任务的触发时间，以毫秒为单位
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i =new  Intent(this,AutoUpdateService.class);
        // 使用PendingIntent指定处理定时任务的服务为LongRunningService
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        //调用AlarmManager的set()方法就可以设置一个定时任务
        //ELAPSED_REALTIME_WAKEUP ：表示让定时任务的触发时间从系统开机开始算起，但会唤醒CPU
        //定时任务的触发时间，以毫秒为单位

        //PendingIntent这里一般会调用getService()方法或者getBroadcast()方法来获取一个能够执行服务或者广播的PendingIntent。
        // 这样当定时任务被触发的时候，服务的onStartCommand()方法或广播接收器的onReceive()方法就会得到执行
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return  super.onStartCommand(intent,flags,startId);
    }

    /*
    * 更新天气信息
    * */
    private void  updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if(weatherString!=null){
            //有缓存数据直接用
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl  = "http://guolin.tech/api/weather?cityid=" +
                    weatherId + "&key=0a7a13b80f3b4e6ebbb475ceba7579c5";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
              String responseText = response.body().string();
              Weather weather = Utility.handleWeatherResponse(responseText);
                    if(weather != null && "ok".equals(weather.status)){
                        //调用SharedPreferences对象的edit()方法来得到SharedPreferences.Editor对象
                        SharedPreferences.Editor editor = PreferenceManager.
                                getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        //向SharedPreferences.Editor添加数据
                        //字符串类型用putString()
                        editor.putString("weather",responseText);
                        //调用apply()提交，完成保存操作
                        editor.apply();

                    }
                }
            });
        }
    }

    /*
    * 更新必应每日一图
    * */
    private void updateBingPic() {
      String requestBingPic = "http://guolin.tech/api/bing_pic";
      HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {
              e.printStackTrace();
          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            String bingPic = response.body().string();
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(AutoUpdateService.this).edit();
             editor.putString("bing_pic",bingPic);
             editor.apply();
          }
      });

    }
}
