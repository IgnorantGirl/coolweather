package com.example.wanghui.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * * AQI中的具体内容
 * "aqi":{
 *  "city":{
 *     "aqi":"44",
 *      "pm25":"13"
 *         }
 * }
 * */
public class AQI {

    public  AQICity city;

    public  class AQICity{
        @SerializedName("aqi")
        public String aqi;
        @SerializedName("pm25")
        public  String pm25;
    }
}
