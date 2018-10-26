package cn.edu.pku.ss.yinguojian.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.List;

import cn.edu.pku.ss.yinguojian.bean.City;
import cn.edu.pku.ss.yinguojian.myminiweather.R;

/**
 * Created by yilen on 2018/10/26.
 */

public class CityAdapter extends BaseAdapter {
    private Context context;
    private List<City> list;

    public CityAdapter(Context _context, List<City> _list) {
        context = _context;
        list = _list;
    }

    @Override
    public int getCount() {
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        if (list == null) {
            return null;
        }
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        View convertView;
        if (view == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.c_detail, null);
            viewHolder = new ViewHolder();
            viewHolder.City_name = (TextView) convertView.findViewById(R.id.list_city_name);
            viewHolder.City_code = (TextView) convertView.findViewById(R.id.list_city_code);
            convertView.setTag(viewHolder);
        } else {
            convertView = view;
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.City_name.setText(list.get(i).getCity());
        viewHolder.City_code.setText(list.get(i).getNumber());

        return convertView;
    }

    private class ViewHolder {
        public TextView City_name;
        public TextView City_code;
    }
}
