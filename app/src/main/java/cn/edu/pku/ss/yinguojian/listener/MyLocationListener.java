package cn.edu.pku.ss.yinguojian.listener;

import android.util.Log;

import java.util.List;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import cn.edu.pku.ss.yinguojian.app.MyApplication;
import cn.edu.pku.ss.yinguojian.bean.City;


/**
 * Created by yilen on 2018/11/19.
 */

public class MyLocationListener extends BDAbstractLocationListener {
    public String recity;
    public String cityCode;
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        String city = bdLocation.getCity();
        Log.d("location_city", city);
        recity = city.replace("市", "");
        List<City> mCityList;
        MyApplication myApplication = MyApplication.getInstance();
        mCityList = myApplication.getmCityList();
        for (City temp : mCityList) {
            if (temp.getCity().equals(recity)) {
                cityCode = temp.getNumber();
                Log.d("location_code", cityCode);
            }
        }
    }
}