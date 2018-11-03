package cn.edu.pku.ss.yinguojian.myminiweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import cn.edu.pku.ss.yinguojian.bean.TodayWeather;
import cn.edu.pku.ss.yinguojian.util.NetUtil;

/**
 * Created by yilen on 2018/10/6.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    //设置信息常量，用于检测信息内容
    private static final int UPDATE_TODAY_WEATHER = 1;

    private ImageView mUpdateBtn;
    private ImageView mCitySelect;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, currTemperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    //处理消息队列中的请求
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
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

        initView();
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
        if (v.getId() == R.id.title_update_btn) {
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
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
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

}
