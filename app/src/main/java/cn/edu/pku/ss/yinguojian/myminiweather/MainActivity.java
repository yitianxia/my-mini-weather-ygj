package cn.edu.pku.ss.yinguojian.myminiweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


import cn.edu.pku.ss.yinguojian.adapter.ViewPagerAdapter;
import cn.edu.pku.ss.yinguojian.bean.TodayWeather;
import cn.edu.pku.ss.yinguojian.listener.MyLocationListener;
import cn.edu.pku.ss.yinguojian.util.NetUtil;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by yilen on 2018/10/6.
 */
public class MainActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    //设置信息常量，用于检测信息内容
    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int GET_LOCATION = 2;

    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private ImageView mLocation;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, currTemperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    private ViewPager vp;
    private ViewPagerAdapter vpAdapter;
    private List<View> views;
    private TextView futureDay1, futureTem1, futureWeather1, futureWind1;
    private TextView futureDay2, futureTem2, futureWeather2, futureWind2;
    private TextView futureDay3, futureTem3, futureWeather3, futureWind3;
    private TextView futureDay4, futureTem4, futureWeather4, futureWind4;
    private TextView futureDay5, futureTem5, futureWeather5, futureWind5;
    private TextView futureDay6, futureTem6, futureWeather6, futureWind6;
    private ImageView futureImg1, futureImg2, futureImg3, futureImg4, futureImg5, futureImg6;
    private ImageView[] dots;
    private int[] ids = {R.id.indicator1, R.id.indicator2};
    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    //处理消息队列中的请求
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                case GET_LOCATION:
                    if (msg.obj != null) {
                        if (NetUtil.getNetworkState(MainActivity.this) != NetUtil.NETWORN_NONE) {
                            Log.d("myWeather", "网络OK");
                            queryWeatherCode(myListener.cityCode);
                        } else {
                            Log.d("myWeather", "网络挂了");
                            Toast.makeText(MainActivity.this, "网络挂了!", Toast.LENGTH_LONG).show();
                        }
                    }
                    myListener.cityCode = null;
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);
        /* 获取控件，设置监听事件 */
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        mCitySelect = (ImageView)findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        /* 获取控件，设置监听事件 */
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            Toast.makeText(MainActivity.this, "网络OK!", Toast.LENGTH_LONG).show();
        } else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了!", Toast.LENGTH_LONG).show();
        }

        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        mLocation = (ImageView) findViewById(R.id.title_location);
        mLocation.setOnClickListener(this);
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());

        /* 检测网络 */
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            initView();
        }


    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        option.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(1000);
        //可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址描述
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否使用GPS
        option.setOpenGps(true);
        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setLocationNotify(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(true);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(false);
        mLocationClient.setLocOption(option);
    }

    /* 初始化界面 */
    void initView() {
        /* 获取控件 */
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        currTemperatureTv = (TextView) findViewById(R.id.current_temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        currTemperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<>();
        views.add(inflater.inflate(R.layout.future_weather, null));
        views.add(inflater.inflate(R.layout.future_weather2, null));
        vpAdapter = new ViewPagerAdapter(views, this);
        vp = (ViewPager)findViewById(R.id.mViewPager);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);
        futureDay1 = (TextView)views.get(0).findViewById(R.id.future1_day);
        futureTem1 = (TextView)views.get(0).findViewById(R.id.future1_temperature);
        futureWeather1 = (TextView)views.get(0).findViewById(R.id.future1_weather);
        futureWind1 = (TextView)views.get(0).findViewById(R.id.future1_wind);
        futureImg1 = (ImageView)views.get(0).findViewById(R.id.future1_image);
        futureDay2 = (TextView)views.get(0).findViewById(R.id.future2_day);
        futureTem2 = (TextView)views.get(0).findViewById(R.id.future2_temperature);
        futureWeather2 = (TextView)views.get(0).findViewById(R.id.future2_weather);
        futureWind2 = (TextView)views.get(0).findViewById(R.id.future2_wind);
        futureImg2 = (ImageView)views.get(0).findViewById(R.id.future2_image);
        futureDay3 = (TextView)views.get(0).findViewById(R.id.future3_day);
        futureTem3 = (TextView)views.get(0).findViewById(R.id.future3_temperature);
        futureWeather3 = (TextView)views.get(0).findViewById(R.id.future3_weather);
        futureWind3 = (TextView)views.get(0).findViewById(R.id.future3_wind);
        futureImg3 = (ImageView)views.get(0).findViewById(R.id.future3_image);
        futureDay4 = (TextView)views.get(1).findViewById(R.id.future4_day);
        futureTem4 = (TextView)views.get(1).findViewById(R.id.future4_temperature);
        futureWeather4 = (TextView)views.get(1).findViewById(R.id.future4_weather);
        futureWind4 = (TextView)views.get(1).findViewById(R.id.future4_wind);
        futureImg4 = (ImageView)views.get(1).findViewById(R.id.future4_image);
        futureDay5 = (TextView)views.get(1).findViewById(R.id.future5_day);
        futureTem5 = (TextView)views.get(1).findViewById(R.id.future5_temperature);
        futureWeather5 = (TextView)views.get(1).findViewById(R.id.future5_weather);
        futureWind5 = (TextView)views.get(1).findViewById(R.id.future5_wind);
        futureImg5 = (ImageView)views.get(1).findViewById(R.id.future5_image);
        futureDay6 = (TextView)views.get(1).findViewById(R.id.future6_day);
        futureTem6 = (TextView)views.get(1).findViewById(R.id.future6_temperature);
        futureWeather6 = (TextView)views.get(1).findViewById(R.id.future6_weather);
        futureWind6 = (TextView)views.get(1).findViewById(R.id.future6_wind);
        futureImg6 = (ImageView)views.get(1).findViewById(R.id.future6_image);
        dots = new ImageView[views.size()];
        for (int i = 0; i < views.size(); i++) {
            dots[i] = (ImageView) findViewById(ids[i]);
        }
    }

    /* 设置点击事件 */
    @Override
    public void onClick(View v) {
        /* 跳转页面事件 */
        if (v.getId() == R.id.title_city_manager){
            Intent i = new Intent(this,SelectCity.class);
            i.putExtra("city",cityTv.getText());
//            startActivity(i);
            /* 设置异步事件，用于传回城市编码，发出请求 */
            startActivityForResult(i,1);
        }
        /* 更新按钮，直接使用给定的城市编码发出请求 */
        else if (v.getId() == R.id.title_update_btn) {
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            Log.d("myWeather", cityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(cityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
                /* 定位按钮 */
        else if (v.getId() == R.id.title_location) {
            if (mLocationClient.isStarted()) {
                mLocationClient.stop();
            }
            mLocationClient.registerLocationListener(myListener);
            initLocation();
            mLocationClient.start();
            setUpdateProgress();
        }
    }
    /* 使用传回的城市编码请求天气数据 */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");
            Log.d("myWeather", "选择的城市代码为"+newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setUpdateProgress() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("11111", myListener.cityCode);
                    while (myListener.cityCode == null) {
                        Thread.sleep(2000);
                        Log.d("11111", "run: ");
                    }
                    Message msg = new Message();
                    msg.what = GET_LOCATION;
                    msg.obj = myListener.cityCode;
                    Log.d("11111", myListener.cityCode);
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /* 请求天气数据 */
    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        /* 开启子线程请求网络数据，避免主线程阻塞 */
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try {
                    /* 获取数据 */
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    /* 将传回的二进制流数据转化为字符串 */
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);
//                    parseXML(responseStr);
                    /* 解析XML，把数据赋值给记录天气数据的类 */
                    todayWeather = parseXML(responseStr);
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());
                        /* 把数据传递给消息队列，供主线程使用 */
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    /* 解析XML数据 */
    private TodayWeather parseXML(String xmldata) {
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp"
                        )) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli")) {
                                eventType = xmlPullParser.next();
                                if (fengliCount == 0) {
                                    todayWeather.setFengli(xmlPullParser.getText());
                                }
                                else if (fengliCount == 1) {
                                    todayWeather.setFuture1Fengli(xmlPullParser.getText());
                                }
                                else if (fengliCount == 2) {
                                    todayWeather.setFuture2Fengli(xmlPullParser.getText());
                                }
                                else if (fengliCount == 3) {
                                    todayWeather.setFuture3Fengli(xmlPullParser.getText());
                                }
                                else if (fengliCount == 4) {
                                    todayWeather.setFuture4Fengli(xmlPullParser.getText());
                                }
                                else if (fengliCount == 5) {
                                    todayWeather.setFuture5Fengli(xmlPullParser.getText());
                                }
                                else if (fengliCount == 6) {
                                    todayWeather.setFuture6Fengli(xmlPullParser.getText());
                                }
                                fengliCount++;

                            } else if (xmlPullParser.getName().equals("date")) {
                                eventType = xmlPullParser.next();
                                if (dateCount == 0) {
                                    todayWeather.setDate(xmlPullParser.getText());
                                }
                                else if (dateCount == 1) {
                                    todayWeather.setFuture1Day(xmlPullParser.getText());
                                }
                                else if (dateCount == 2) {
                                    todayWeather.setFuture2Day(xmlPullParser.getText());
                                }
                                else if (dateCount == 3) {
                                    todayWeather.setFuture3Day(xmlPullParser.getText());
                                }
                                else if (dateCount == 4) {
                                    todayWeather.setFuture4Day(xmlPullParser.getText());
                                }
                                else if (dateCount == 5) {
                                    todayWeather.setFuture5Day(xmlPullParser.getText());
                                }
                                else if (dateCount == 6) {
                                    todayWeather.setFuture6Day(xmlPullParser.getText());
                                }
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high")) {
                                eventType = xmlPullParser.next();
                                if (highCount == 0) {
                                    todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                }
                                else if (highCount == 1) {
                                    todayWeather.setFuture1High(xmlPullParser.getText().substring(2).trim());
                                }
                                else if (highCount == 2) {
                                    todayWeather.setFuture2High(xmlPullParser.getText().substring(2).trim());
                                }
                                else if (highCount == 3) {
                                    todayWeather.setFuture3High(xmlPullParser.getText().substring(2).trim());
                                }
                                else if (highCount == 4) {
                                    todayWeather.setFuture4High(xmlPullParser.getText().substring(2).trim());
                                }
                                else if (highCount == 5) {
                                    todayWeather.setFuture5High(xmlPullParser.getText().substring(2).trim());
                                }
                                else if (highCount == 6) {
                                    todayWeather.setFuture6High(xmlPullParser.getText().substring(2).trim());
                                }

                                highCount++;
                            } else if (xmlPullParser.getName().equals("low")) {
                                eventType = xmlPullParser.next();

                                if (lowCount == 0) {
                                    todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                }
                                else if (lowCount == 1) {
                                    todayWeather.setFuture1Low(xmlPullParser.getText().substring(2).trim());
                                }
                                else if (lowCount == 2) {
                                    todayWeather.setFuture2Low(xmlPullParser.getText().substring(2).trim());
                                }
                                else if (lowCount == 3) {
                                    todayWeather.setFuture3Low(xmlPullParser.getText().substring(2).trim());
                                }
                                else if (lowCount == 4) {
                                    todayWeather.setFuture4Low(xmlPullParser.getText().substring(2).trim());
                                }
                                else if (lowCount == 5) {
                                    todayWeather.setFuture5Low(xmlPullParser.getText().substring(2).trim());
                                }
                                else if (lowCount == 6) {
                                    todayWeather.setFuture6Low(xmlPullParser.getText().substring(2).trim());
                                }
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type")) {
                                eventType = xmlPullParser.next();
                                if (typeCount == 0) {
                                    todayWeather.setType(xmlPullParser.getText());
                                }
                                else if (typeCount == 1) {
                                    todayWeather.setFuture1Type(xmlPullParser.getText());
                                }
                                else if (typeCount == 2) {
                                    todayWeather.setFuture2Type(xmlPullParser.getText());
                                }
                                else if (typeCount == 3) {
                                    todayWeather.setFuture3Type(xmlPullParser.getText());
                                }
                                else if (typeCount == 4) {
                                    todayWeather.setFuture4Type(xmlPullParser.getText());
                                }
                                else if (typeCount == 5) {
                                    todayWeather.setFuture5Type(xmlPullParser.getText());
                                }
                                else if (typeCount == 6) {
                                    todayWeather.setFuture6Type(xmlPullParser.getText());
                                }
                                typeCount++;
                            }
                        }
                        break;
                    // 判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }

    private void parseXML2(String xmldata) {
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("city ")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "city:    " + xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("updatetime")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "updatetime:    " + xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("shidu")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "shidu:    " + xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("wendu")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "wendu:    " + xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("pm25")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "pm25:    " + xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("quality")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "quality:    " + xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "fengxiang:    " + xmlPullParser.getText());
                            fengxiangCount++;
                        } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "fengli:    " + xmlPullParser.getText());
                            fengliCount++;
                        } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "date:    " + xmlPullParser.getText());
                            dateCount++;
                        } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "high:    " + xmlPullParser.getText());
                            highCount++;
                        } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "low:    " + xmlPullParser.getText());
                            lowCount++;
                        } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "type:    " + xmlPullParser.getText());
                            typeCount++;
                        }
                        break;
                    // 判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* 由主线程利用消息队列中的数据更新界面 */
    void updateTodayWeather(TodayWeather todayWeather) {
        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        currTemperatureTv.setText("当前温度：" + todayWeather.getWendu()+"℃");
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:" + todayWeather.getFengli());

        futureDay1.setText(todayWeather.getFuture1Day());
        futureWeather1.setText(todayWeather.getFuture1Type());
        futureTem1.setText(todayWeather.getFuture1High() + "~" + todayWeather.getFuture1Low());
        futureWind1.setText(todayWeather.getFuture1Fengli());
        futureDay2.setText(todayWeather.getFuture2Day());
        futureWeather2.setText(todayWeather.getFuture2Type());
        futureTem2.setText(todayWeather.getFuture2High() + "~" + todayWeather.getFuture2Low());
        futureWind2.setText(todayWeather.getFuture2Fengli());
        futureDay3.setText(todayWeather.getFuture3Day());
        futureWeather3.setText(todayWeather.getFuture3Type());
        futureTem3.setText(todayWeather.getFuture3High() + "~" + todayWeather.getFuture3Low());
        futureWind3.setText(todayWeather.getFuture3Fengli());
        futureDay4.setText(todayWeather.getFuture4Day());
        futureWeather4.setText(todayWeather.getFuture4Type());
        futureTem4.setText(todayWeather.getFuture4High() + "~" + todayWeather.getFuture4Low());
        futureWind4.setText(todayWeather.getFuture4Fengli());
        //        futureDay5.setText(todayWeather.getFuture5Day());
        //        futureTem5.setText(todayWeather.getFuture5High() + "~" + todayWeather.getFuture5Low());
        //        futureWind5.setText(todayWeather.getFuture5Fengli());
                //        futureDay6.setText(todayWeather.getFuture6Day());
        //        futureTem6.setText(todayWeather.getFuture6High() + "~" + todayWeather.getFuture6Low());
        //        futureWind6.setText(todayWeather.getFuture6Fengli());
        String[] type = {todayWeather.getFuture1Type(), todayWeather.getFuture2Type(), todayWeather.getFuture3Type(),
                todayWeather.getFuture4Type(), todayWeather.getFuture5Type(), todayWeather.getFuture6Type()};
        ImageView[] image = {futureImg1, futureImg2, futureImg3, futureImg4, futureImg5, futureImg6};
        for (int i = 0; i < 4; i++) {
            if (type[i].equals("晴")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_qing);
            }
            else if (type[i].equals("暴雪")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_baoxue);
            }
            else if (type[i].equals("暴雨")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_baoyu);
            }
            else if (type[i].equals("大暴雨")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
            }
            else if (type[i].equals("大雪")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_daxue);
            }
            else if (type[i].equals("大雨")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_dayu);
            }
            else if (type[i].equals("多云")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_duoyun);
            }
            else if (type[i].equals("雷阵雨")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
            }
            else if (type[i].equals("雷阵雨冰雹")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
            }
            else if (type[i].equals("沙尘暴")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_shachenbao);
            }
            else if (type[i].equals("特大暴雨")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
            }
            else if (type[i].equals("雾")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_wu);
            }
            else if (type[i].equals("小雪")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
            }
            else if (type[i].equals("小雨")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
            }
            else if (type[i].equals("阴")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_yin);
            }
            else if (type[i].equals("雨夹雪")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
            }
            else if (type[i].equals("阵雪")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_zhenxue);
            }
            else if (type[i].equals("阵雨")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_zhenyu);
            }
            else if (type[i].equals("中雪")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_zhongxue);
            }
            else if (type[i].equals("中雨")) {
                image[i].setImageResource(R.drawable.biz_plugin_weather_zhongyu);
            }
        }
         /* 更新PM2.5和天气图片 */
        updateImage(todayWeather);
        Toast.makeText(MainActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
    }
    /* 更新PM2.5和天气图片 */
    void updateImage(TodayWeather todayWeather) {
        int pm = 0;
        if (todayWeather.getPm25()!=null){
            pm = Integer.parseInt(todayWeather.getPm25());
        }
        String type = todayWeather.getType();
        if(pm>=0&&pm<51) {
            pmImg.setBackgroundResource(R.drawable.biz_plugin_weather_0_50);
        }else if (pm<101){
            pmImg.setBackgroundResource(R.drawable.biz_plugin_weather_51_100);
        }else if (pm<151){
            pmImg.setBackgroundResource(R.drawable.biz_plugin_weather_101_150);
        }else if (pm<201){
            pmImg.setBackgroundResource(R.drawable.biz_plugin_weather_151_200);
        }else if (pm<301){
            pmImg.setBackgroundResource(R.drawable.biz_plugin_weather_201_300);
        }else {
            pmImg.setBackgroundResource(R.drawable.biz_plugin_weather_greater_300);
        }
        if ("暴雪".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_baoxue);
        }else if ("暴雨".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_baoyu);
        }else if ("大暴雨".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_dabaoyu);
        }else if ("大雪".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_daxue);
        }else if ("大雨".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_dayu);
        }else if ("多云".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_duoyun);
        }else if ("雷阵雨".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_leizhenyu);
        }else if ("雷阵雨冰雹".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
        }else if ("晴".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_qing);
        }else if ("沙尘暴".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_shachenbao);
        }else if ("特大暴雨".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_tedabaoyu);
        }else if ("雾".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_wu);
        }else if ("小雪".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_xiaoxue);
        }else if ("小雨".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_xiaoyu);
        }else if ("阴".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_yin);
        }else if ("雨夹雪".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_yujiaxue);
        }else if ("阵雪".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_zhenxue);
        }else if ("阵雨".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_zhenyu);
        }else if ("中雪".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_zhongxue);
        }else if ("中雨".equals(type)){
            weatherImg.setBackgroundResource(R.drawable.biz_plugin_weather_zhongyu);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < ids.length; i++) {
            if (i == position) {
                dots[i].setImageResource(R.drawable.page_indicator_focused);
            } else {
                dots[i].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }
    @Override
    public void onPageScrollStateChanged(int state) {
    }

}
