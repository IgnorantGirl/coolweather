package com.example.wanghui.coolweather.gson;


import com.google.gson.annotations.SerializedName;

/**
 * basic中的具体内容
 * "basic":{
 *      "city":"苏州",
 *      "id":"CN101190401",
 *      "update":{
 *               "loc":"2016-08-08 21：58"
 *         }
 * }
 * city：城市名，id：城市对应的天气id，update：表示天气的更新时间
 *
 * 由于JSON中的一些字段不太适合直接作为Java字段来命名，
 * 因此这里使用了@SerializedName注解的方式来让JSON字段和Java字段之间建立映射关系。
 * */
public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public  String weatherId;

    public  Update update;

    public  class  Update{

        @SerializedName("loc")
        public String updateTime;
    }
}
