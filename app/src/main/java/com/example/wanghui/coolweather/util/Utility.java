package com.example.wanghui.coolweather.util;

import android.text.TextUtils;

import com.example.wanghui.coolweather.db.City;
import com.example.wanghui.coolweather.db.County;
import com.example.wanghui.coolweather.db.Province;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     * */

    public static boolean handleProvinceResponse(String response){

        if(!TextUtils.isEmpty(response)){
            try{
                //将服务器返回的数据传入到了一个JSONArray对象中
                JSONArray allProvinces = new JSONArray(response);
                for(int i = 0;i<allProvinces.length();i++){
                    /**
                     *然后循环遍历这个JSONArray，从中取出的每一个元素都是一个JSONObject对象，
                     * 每个JSONObject对象中又会包含id、name这些数据;取出来组装成实体类对象
                     * 最后调用save()方法将数据存储到数据库当中
                     * */
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return  true;

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     * */

    public static boolean handleCityResponse(String response,int provinceId){

        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities = new JSONArray(response);
                for(int i = 0;i<allCities.length();i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return  true;

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 解析和处理服务器返回的县级数据
     * */

    public static boolean handleCountyResponse(String response,int cityId){

        if(!TextUtils.isEmpty(response)){
            try{
                ////将服务器返回的数据传入到了一个JSONArray对象中
                JSONArray allCounties = new JSONArray(response);
                for(int i = 0;i<allCounties.length();i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return  true;

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
