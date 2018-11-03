package cn.edu.pku.ss.yinguojian.myminiweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.ss.yinguojian.adapter.CityAdapter;
import cn.edu.pku.ss.yinguojian.app.MyApplication;
import cn.edu.pku.ss.yinguojian.bean.City;
import cn.edu.pku.ss.yinguojian.view.ClearEditText;

/**
 * Created by yilen on 2018/10/16.
 */

public class SelectCity extends Activity implements View.OnClickListener {
    private ImageView mBackBtn;
    private TextView mTodayCity;
    private ListView mList;
    private List<City> cityList;
    private CityAdapter mAdapter;
    private String cityCode;
    private ClearEditText mClearEditText;
    private List<City> filterDataList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        initViews();
        initEditText();
    }

    /* 设置点击事件 */
    @Override
    public void onClick(View v) {
       switch (v.getId()){
           /* 根据点击的城市传回城市编码，默认是北京 */
           case R.id.title_back:
               Intent i = new Intent();
               i.putExtra("cityCode", "101160101");
               if (cityCode == null) {
                   i.putExtra("cityCode", "101010100");
               } else {
                   i.putExtra("cityCode", cityCode);
               }
               /* 为回调函数提供检测值 */
               setResult(RESULT_OK, i);
               finish();
               break;
           default:
               break;
       }
    }

    /* 初始化界面 */
    private void initViews() {
        Intent intent = getIntent();
        mTodayCity = (TextView)findViewById(R.id.title_name);
        mTodayCity.setText("当前城市：" + intent.getStringExtra("city"));
        //  为返回键声明监听事件
        /* 为返回键声明监听事件 */
        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        mList = (ListView)findViewById(R.id.title_list);
        /* 获取城市列表 */
        MyApplication myApplication = (MyApplication)getApplication();
        cityList = myApplication.getCityList();
        mAdapter = new CityAdapter(SelectCity.this, cityList);
        filterDataList = new ArrayList<City>();
        for (City city : cityList) {
            filterDataList.add(city);
        }
        /* 为Adapter设置上下文和传递城市列表 */
        mAdapter = new CityAdapter(SelectCity.this, filterDataList);
        /* 为ListView设置adapter */
        mList.setAdapter(mAdapter);

        /* 为ListView设置点击事件 */
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                City city = filterDataList.get(i);
                mTodayCity.setText("当前城市：" + city.getCity());
                cityCode = city.getNumber();
            }
        });
    }
    private void initEditText() {
        mClearEditText = (ClearEditText)findViewById(R.id.search_city);
         /* 根据输入框的改变来过滤搜索 */
        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /* 当输出框里面的值为空，更新为原来的列表，否则未过滤数据列表 */
                filterData(charSequence.toString());
                mList.setAdapter(mAdapter);
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
    /**
     * 根据输入框中的值来过滤数据并更新ListView
     * @param filterStr
     */
    private void filterData(String filterStr) {
        //filterDataList = new ArrayList<City>();
        Log.d("Filter", filterStr);
        /* 判断搜索框是否为空，为空则显示全部数据 */
        if (TextUtils.isEmpty(filterStr)) {
            filterDataList.clear();
            for (City city : cityList) {
                filterDataList.add(city);
            }
        } else {
            /* 字符串不为空，则比较搜索框内容和数据库对象的属性进行性检索 */
            filterDataList.clear();
            for (City city : cityList) {
                /* 排除大小写的影响 */
                String allFP_lower = city.getAllFirstPY().toLowerCase();
                String FP_lower = city.getFirstPY().toLowerCase();
                String AP_lower = city.getAllPY().toLowerCase();
                if (city.getAllFirstPY().indexOf(filterStr.toString()) == 0 || city.getFirstPY().indexOf(filterStr.toString()) == 0 || city.getAllPY().indexOf(filterStr.toString()) == 0
                        || city.getNumber().indexOf(filterStr.toString()) == 0 || city.getCity().indexOf(filterStr.toString()) != -1 || allFP_lower.indexOf(filterStr.toString()) == 0
                        || FP_lower.indexOf(filterStr.toString()) == 0 || AP_lower.indexOf(filterStr.toString()) == 0) {
                    filterDataList.add(city);
                }
            }
        }
        //根据a-z进行排序
        //Collections sort(filterDataList, pinyinComparator)
        mAdapter.notifyDataSetChanged();
    }
}
