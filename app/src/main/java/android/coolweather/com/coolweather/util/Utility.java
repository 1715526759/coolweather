package android.coolweather.com.coolweather.util;

import android.coolweather.com.coolweather.db.City;
import android.coolweather.com.coolweather.db.County;
import android.coolweather.com.coolweather.db.Province;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    private static final String TAG = "Utility";
    /*
    解析服务器返回的省数据
     */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray=new JSONArray(response);
                Log.i(TAG, "handleProvinceResponse: 数组长度为："+jsonArray.length());
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    Log.i(TAG, "handleProvinceResponse: 名称为"+jsonObject.getString("name"));
                    Log.i(TAG, "handleProvinceResponse: id为："+jsonObject.getInt("id"));
                    Province province=new Province();
                    province.setProvinceName(jsonObject.getString("name"));
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    解析服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response,int provinceId){
       if(!TextUtils.isEmpty(response)){
           try {
               JSONArray jsonArray=new JSONArray(response);
               for(int i=0;i<jsonArray.length();i++){
                   JSONObject jsonObject=jsonArray.getJSONObject(i);
                   City city=new City();
                   city.setCityName(jsonObject.getString("name"));
                   city.setCityCode(jsonObject.getInt("id"));
                   city.setProvinceId(provinceId);
                   city.save();
               }
               return true;
           } catch (JSONException e) {
               e.printStackTrace();
           }
       }
        return false;
    }
    public static boolean handleCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray=new JSONArray(response);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    County county=new County();
                    county.setCountyName(jsonObject.getString("name"));
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
