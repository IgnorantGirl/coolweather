package com.example.wanghui.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/*
* daily_forecast包含的是一个数组，数组中每一项代表这未来一天的天气信息
 *"daily_forecast":[
 * {
 *       "date":"2020-02-02",
 *        "cond":{
 *           "txt_d":"阵雨"
 *               },
 *        "tmp":{
 *            "max":"34"，
 *            "min":"27"，
      }
 * },
 * {
  *       "date":"2020-02-02",
 *        "cond":{
 *           "txt_d":"阵雨"
 *               },
 *        "tmp":{
 *            "max":"34"，
 *            "min":"27"，
      }
 * },{
 * },
 *  ...
 * }
 * 针对该情况，
 * 只需要定义出单日天气的实体类即可，
 * 然后再声明实体类引用的时候使用集合类型来进行声明
* */
public class Forecast {

public String date;
@SerializedName("tmp")
public Temperature temperature;

@SerializedName("cond")
public More more;

public class  Temperature{
    public String max;
    public String min;
}

public class  More{
    @SerializedName("txt_d")
    public  String info;
}
}
