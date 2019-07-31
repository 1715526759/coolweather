package android.coolweather.com.coolweather.util;


import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.coolweather.com.coolweather.db.City;
import android.coolweather.com.coolweather.db.County;
import android.coolweather.com.coolweather.db.Province;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.coolweather.com.coolweather.R;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */

public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";
    public static final int PROVINCE = 0;
    public static final int CITY = 1;
    public static final int COUNTY = 2;
    TextView titleText;
    Button backbutton;
    ListView listView;
    Province CURRENT_PROVINCE;
    City CURRENT_CITY;
    County CURRNET_COUNTY;
    private ArrayAdapter<String> adapter;
    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private List<County> countyList = new ArrayList<>();
    private List<String> dataList = new ArrayList<String>();
    int CURRENTLEVEL;
    ProgressDialog progressDialog;

    public ChooseAreaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = view.findViewById(R.id.title_text);
        backbutton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryProvince();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (CURRENTLEVEL == PROVINCE) {
                    Province province = provinceList.get(position);
                    CURRENT_PROVINCE = province;
                    queryCity();
                } else if (CURRENTLEVEL == CITY) {
                    City city = cityList.get(position);
                    CURRENT_CITY = city;
                    queryCounty();
                }
            }
        });

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CURRENTLEVEL == COUNTY) {
                    queryCity();
                } else if (CURRENTLEVEL == CITY) {
                    queryProvince();
                }
            }
        });
    }

    private void queryProvince() {

        titleText.setText("中国");
        backbutton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            CURRENTLEVEL = PROVINCE;
        } else {
            //在服务器查询
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryCity() {
        titleText.setText(CURRENT_PROVINCE.getProvinceName());
        backbutton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId=?", String.valueOf(CURRENT_PROVINCE.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {

                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            CURRENTLEVEL = CITY;
        } else {
            int provinceCode = CURRENT_PROVINCE.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryCounty() {
        titleText.setText(CURRENT_CITY.getCityName());
        backbutton.setVisibility(View.VISIBLE);
        //先从数据库查询
        countyList = DataSupport.where("cityId=?", String.valueOf(CURRENT_CITY.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            CURRENTLEVEL = COUNTY;
        } else {
            int provinceCode = CURRENT_PROVINCE.getProvinceCode();
            int cityCode = CURRENT_CITY.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        Log.i(TAG, "queryFromServer: 发送请求之前");
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Log.i(TAG, "onResponse: 请求结果为："+responseText);
                boolean result = false;
                if ("province".equals(type)) {
                    Log.i(TAG, "onResponse: 查询省份");
                    result = Utility.handleProvinceResponse(responseText);
                    Log.i(TAG, "onResponse: 查询省份结果:"+result);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, CURRENT_PROVINCE.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, CURRENT_CITY.getId());
                }
                if (result) {
                    Log.i(TAG, "onResponse: 执行关闭进度条");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvince();
                            } else if ("city".equals(type)) {
                                queryCity();
                            } else if ("county".equals(type)) {
                                queryCounty();
                            }
                        }
                    });
                }
            }

        });
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("正在加载");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    public void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
