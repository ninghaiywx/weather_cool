package com.example.ywx.weathercool;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ywx.weathercool.database.City;
import com.example.ywx.weathercool.database.Country;
import com.example.ywx.weathercool.database.Province;
import com.example.ywx.weathercool.gson.Weather;
import com.example.ywx.weathercool.util.HttpUtil;
import com.example.ywx.weathercool.util.Utility;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by ywx on 2017/4/5.
 * 用于选择地区的碎片
 */
public class ChooseAreaFragment extends Fragment {
    private static final String TAG="ChooseAreaFragment";
    private static final int LEVEL_PROVINCE=0;
    private static final int LEVEL_CITY=1;
    private static final int LEVEL_COUNTRY=2;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> datalist=new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    private Province selectedProvince;
    private City selectedCity;
    private int selectedLevel;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView)view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
            adapter=new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,datalist);
        listView.setAdapter(adapter);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(selectedLevel==LEVEL_PROVINCE)
                {
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(selectedLevel==LEVEL_CITY)
                {
                    selectedCity=cityList.get(position);
                    queryCountries();
                }else if(selectedLevel==LEVEL_COUNTRY)
                {
                    String weatherId=countryList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    else if(getActivity() instanceof WeatherActivity)
                    {
                        WeatherActivity activity=(WeatherActivity)getActivity();
                        activity.getDrawerLayout().closeDrawers();
                        activity.getRefreshLayout().setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedLevel==LEVEL_COUNTRY)
                {
                    queryCities();
                }else if(selectedLevel==LEVEL_CITY)
                {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    private void queryCities()
    {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",selectedProvince.getId()+"").find(City.class);
        if(cityList.size()>0)
        {
            Log.d(TAG,"数据库有数据");
            datalist.clear();
            for(City city:cityList)
            {
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            selectedLevel=LEVEL_CITY;
        }
        else
        {
            Log.d(TAG,"数据库无数据");
            int provinceCode=selectedProvince.getProvinceCode();
            Log.d(TAG,provinceCode+"");
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }
    private void queryProvinces()
    {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0)
        {
            datalist.clear();
            for(Province province:provinceList)
            {
                datalist.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            selectedLevel=LEVEL_PROVINCE;
        }
        else
        {
            Log.d(TAG,"数据库空");
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    private void queryCountries()
    {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countryList=DataSupport.where("cityid=?",selectedCity.getId()+"").find(Country.class);
        if(countryList.size()>0)
        {
            datalist.clear();
            for(Country country:countryList)
            {
                    datalist.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            selectedLevel=LEVEL_COUNTRY;
        }
        else
        {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"country");
        }
    }
    private void queryFromServer(String address, final String type)
    {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean flag=false;
                if("province".equals(type))
                {
                    flag=Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type))
                {
                    flag=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("country".equals(type))
                {
                    flag=Utility.handleCountryResponse(responseText,selectedCity.getId());
                }
                if(flag)
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if("province".equals(type))
                            {
                                queryProvinces();
                            }else if("city".equals(type))
                            {
                                queryCities();
                            }else if("country".equals(type))
                            {
                                queryCountries();
                            }
                        }
                    });
                }
            }
        });
    }
}
