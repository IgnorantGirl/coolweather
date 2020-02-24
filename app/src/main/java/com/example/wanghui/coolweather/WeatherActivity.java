package com.example.wanghui.coolweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wanghui.coolweather.gson.Forecast;
import com.example.wanghui.coolweather.gson.Weather;
import com.example.wanghui.coolweather.util.HttpUtil;
import com.example.wanghui.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public  class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    private Button navButton;

    public SwipeRefreshLayout swipeRefresh;
    private ScrollView weatherLayout;

    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;

    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;


    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * 通过调用getWindow().getDecorView()方法获得当前活动的DecorView
         * DecorView为整个Window界面的最顶层View
         * 然后再调用setSystemUiVisibility()方法来改变系统UI的显示
         * View.SYSTEM_UI_FLAG_LAYOUT_STABLE：表示活动的布局会显示在状态栏上面
         * 最后调用一下setStatusBarColor()方法将状态栏设置为透明色
         * */
        if(Build.VERSION.SDK_INT >=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //获取一些控件的实例
        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText =(TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.comfort_text);
        sportText = (TextView)findViewById(R.id.sport_text);

        //初始化SwipeRefreshLayout实例
        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        //调用setColorSchemeResources()方法设置下拉刷新进度条的颜色
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        //定义一个weatherId变量，用于记录城市的天气id
        final  String weatherId ;
        //获取DrawerLayout和Button实例
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navButton = (Button)findViewById(R.id.nav_button);
        //获取SharedPreferences实例
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //初始化ImageView控件
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        String bingPic = prefs.getString("bing_pic",null);

        String weatherString = prefs.getString("weather",null);

        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

        if(weatherString !=null){
            //有缓存时 直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
//            wetherid= weather.basic.
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            //无缓存时去服务器查询天气
            //无缓冲时，从Intent中取出天气id，并调用requestWeather()方法来从服务器请求天气数据
             weatherId = getIntent().getStringExtra("weather_id");
            //请求数据时，先将ScrollView进行隐藏
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        //调用setOnRefreshListener()方法来设置一个下拉刷新的监听器，当触发下拉刷新时，
        //就会回调这个监听器的onRefresh()方法，这里调用requestWeather()方法请求天气信息就可以了
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this);
                String temp = prefs.getString("weather_id", weatherId);
                requestWeather(temp);
//                requestWeather(weatherId);
            }
        });
        //在navButton的点击事件中调用DrawerLayout的openDrawer()方法打开滑动菜单
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 加载必应每日一图
     * */
    private void loadBingPic() {

        String requestBingPic = "http://guolin.tech/api/bing_pic";
        //调用sendOkHttpRequest()方法获取到必应背景图的连接
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //将获得到的链接缓存到SharedPreferences当中，接着将当前线程切换到主线程
            final String bingPic = response.body().string();
            SharedPreferences.Editor editor = PreferenceManager.
                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString("bing_pic",bingPic);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //最后使用Glide来加载这张图片
                            //在requestWeather()方法最后也需要调用一下loadBingPic()方法，这样每次请求天气信息时同时也会刷新背景图片
                            Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                        }
                    });
            }
        });
    }

    /*
    * 根据天气id 请求城市天气信息
    * requestWeather()方法中传入了天气id,并加上之前甚好的APIKey拼装出一个接口地址
    * 接着调用sendOkHttpRequest()方法来向该地址发出请求，服务器会将相应城市的天气信息以JSON格式返回
    * 然后在onResponse()方法回调中先调用了handleWeatherResponse()方法将返回的JSON数据转换成Weather对象
    * 接着利用runOnUiThread()方法将当前线程切换到主线程，判断服务器返回的status状态是否为ok
    * ok表示成功，将数据缓存到SharedPreferences当中，并调用showWeatherInfo()方法来进行内容显示
    *     * */
    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                  weatherId + "&key=0a7a13b80f3b4e6ebbb475ceba7579c5";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }

                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            //调用SharedPreferences对象的edit()方法来得到SharedPreferences.Editor对象
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                           //向SharedPreferences.Editor添加数据
                            //字符串类型用putString()
                            editor.putString("weather",responseText);
                            //调用apply()提交，完成保存操作
                            editor.apply();
                            showWeatherInfo(weather);
                            loadBingPic();
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        //刷新的请求接收后，调用setRefreshing()方法并传入false，用于表示刷新事件结束，并隐藏刷新进度条
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    /*
    * 处理并展示Weather实体类中的数据
    * 从Weather对象中获取数据，然后显示到相应的控件上
    * */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature +"℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        //在处理未来几天天气预报的部分，使用了一个for循环来处理每天的天气信息
        //在循环中动态加载forecast_item.xml布局并设置相应的数据，然后添加到相应的父布局当中
        for(Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度："+weather.suggestion.comfort.info;
        String carWash = "洗车指数：" +weather.suggestion.carWash.info;
        String sport = "运动建议：" +weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        //设置完所有数据以后，要将ScrollView重新变成可见
        weatherLayout.setVisibility(View.VISIBLE);

    }


}
