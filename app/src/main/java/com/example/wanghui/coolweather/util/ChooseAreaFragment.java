package com.example.wanghui.coolweather.util;

import com.example.wanghui.coolweather.*;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.wanghui.coolweather.MainActivity;
import com.example.wanghui.coolweather.R;
import com.example.wanghui.coolweather.WeatherActivity;
import com.example.wanghui.coolweather.db.City;
import com.example.wanghui.coolweather.db.County;
import com.example.wanghui.coolweather.db.Province;
import com.example.wanghui.coolweather.gson.Weather;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 遍历省市县数据的碎片
 * */
public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;


    private TextView titleText;
    private Button backButton;

    private ListView listView;
    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     * */
    private List<Province> provinceList;
    /**
     * 市列表
     * */
    private List<City> cityList;
    /**
     * 县列表
     * */
    private List<County> countyList;

    /**
     * 选中的省份
     * */
    private Province selectedProvince;
    /**
     * 选中的城市
     * */
    private City selectedCity;
    /**
     * 当前选择的级别
     * */
    private int currentLevel;

    /**
     * onCreateView是碎片的生命周期中的一种状态，在为碎片创建视图（加载布局）时调用
     * 用于获取一些控件的实例
     * 各个参数的含义以及作用：
     *LayoutInflater inflater：作用类似于findViewById（），findViewById（）用来寻找xml布局下的具体的控件（Button、TextView等），LayoutInflater inflater（）用来找res/layout/下的xml布局文件
     *ViewGroup container：表示容器，View放在里面
     *Bundle savedInstanceState：保存当前的状态，在活动的生命周期中，只要离开了可见阶段，活动很可能就会被进程终止，这种机制能保存当时的状态

     *
     * */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       /**
        * 参数分析：
        * arg1是当前布局的id   布局文件。
        * arg2是root
        * arg3是实例化以后的view是否添加到root中
        * */
        View view = inflater.inflate(R.layout.choose_area,container,false);
       titleText =(TextView)view.findViewById(R.id.title_text);
       backButton = (Button)view.findViewById(R.id.back_button);
       listView =(ListView)view.findViewById(R.id.list_view);
       //初始化ArrayAdapter，并将它设置为ListView的适配器
       adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
       listView.setAdapter(adapter);
       return view;
    }

    /**
     * 给ListView 和Button设置点击事件
     * */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            /**
             * 参数分析：
             * arg1是当前item的view，通过它可以获得该项中的各个组件。
             * 例如:TextView text = view.findViewById(R.id.text);
             * arg2是当前mineTravelList的positioin。这个id根据你在适配器中的写法可以自己定义。
             * 例如：MineTravelList travel = mineTravel.get(position);
             * arg3是当前的item在listView中的相对位置id！
             * */
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    //加载县级数据
                    queryCounties();

                }
                else if(currentLevel ==LEVEL_COUNTY){
                    /**
                     * 新加判断条件，如果当前级别是LEVEL_COUNTY，就启动WeatherActivity，
                     * 并把当前选中县的天气id传递过去
                     * */
                    String  weatherId = countyList.get(position).getWeatherId();

                    //使用instanceof关键字可以要拿过来判断一个对象是否属于某个类的实例
                    if(getActivity() instanceof MainActivity){
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        //如果是在WeatherActivity当中，那就关闭滑动功能，显示下拉刷新进度条，然后请求新城市的天气信息
                        WeatherActivity activity = (WeatherActivity)getActivity();

                        //对，改的就是这！

                        SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(getContext())
                                        .edit();
                        editor.putString("weather_id", weatherId);
                        editor.apply();

                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);

                    }


                }

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel ==LEVEL_COUNTY){
                    queryCities();
                }else  if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有省，优先从数据库查询，如果没查询到，再去服务器上查询
     * */
    private  void  queryProvinces(){
        //将标题设置为中国
        titleText.setText("中国");
        //隐藏返回按钮
        backButton.setVisibility(View.GONE);
        //调用LitePal的查询接口来从数据库读取省级数据
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            //如果读到了，就遍历省份，将数据直接显示到界面上
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            //选中第一条数据
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            //如果没有读到，就从服务器上查询数据
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没查询到，再去服务器上查询
     * */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
    cityList = DataSupport.where("provinceid=?"
            ,String.valueOf(selectedProvince.getId())).find(City.class);
    if(cityList.size()>0){
        dataList.clear();
        for(City city:cityList){
            dataList.add(city.getCityName());
        }
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
        currentLevel=LEVEL_CITY;
    }else {
        int provinceCode = selectedProvince.getProvinceCode();
        String address = "http://guolin.tech/api/china/" +provinceCode;
        queryFromServer(address,"city");
    }

    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没查询到，再去服务器上查询
     * */
    private void  queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?"
                ,String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" +provinceCode+"/"+ cityCode;
            queryFromServer(address,"county");
        }

    }


    /**
     * 根据传入的地址和类型从服务器上查询省市县的数据
     * */
    private void queryFromServer(String address, final  String type) {
        showProgressDialog();
        //调用sendOkHttpRequest()方法向服务器发送请求，相应数据会回调到onResponse()方法中
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                 String responseText = response.body().string();
                 boolean result = false;
                 if("province" .equals(type)){
                     //调用Utility的handleProvinceResponse()方法来解析和处理服务器返回的数据，并存到数据库当中
                      result = Utility.handleProvinceResponse(responseText);
                 } else if ("city".equals(type)) {
                   result = Utility.handleCityResponse(responseText,selectedProvince.getId());

                 }else  if("county".equals(type)){
                     result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                 }
                 if(result){
                     //再次调用queryProvinces()方法来重新加载省级数据
                     // 由于queryProvinces()方法牵扯到UI操作，因此必须在主线程中调用
                     //这里借助runOnUiThread()方法来实现：子线程切换到主线程

                     getActivity().runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             closeProgressDialog();
                             if("provice".equals(type)){
                                 queryProvinces();
                             }else  if("city".equals(type)){
                                 queryCities();

                             }else if("county".equals(type)){
                                 queryCounties();
                             }
                         }
                     });
                 }
            }
        });
    }

    /**
     * 显示进度对话框
     * */
    private void showProgressDialog() {
        if(progressDialog==null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
//            progressBar
//            progressDialog.setMessage();
//            progressBar.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        progressDialog.show();
    }
    /**
     * 关闭进度对话框
     * */
    private  void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
