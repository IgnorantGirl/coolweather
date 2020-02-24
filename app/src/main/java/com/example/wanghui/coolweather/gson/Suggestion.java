package com.example.wanghui.coolweather.gson;

import com.google.gson.annotations.SerializedName;


/**
 * suggestion中的具体内容
 * "suggestion":{
 *      "comf":{
 *               "txt":"白天天气太热...."
 *         },
 *      "cw":{
 *               "txt":"不宜洗车...."
 *         },
 *      "sport":{
 *               "txt":"户外运动，注意避暑...."
 *         }
 * }
 * */
public class Suggestion {

    @SerializedName("comf")
    public  Comfort comfort;
    @SerializedName("cw")
    public  CarWash carWash;
    @SerializedName("sport")
    public Sport sport;

  public class Comfort{

      @SerializedName("txt")
      public String info;
  }
  public class CarWash{
      @SerializedName("txt")
      public String info;
  }
  public class  Sport{
      @SerializedName("txt")
      public String info;
  }
}
