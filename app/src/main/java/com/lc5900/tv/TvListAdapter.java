package com.lc5900.tv;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


/**
 * Created by liuchun on 2017/11/29.
 */

public class TvListAdapter<TvMenu> extends BaseAdapter {
    private final Context mContext;
    List<TvMenu> arrayList;
    private LayoutInflater mInflater;

    public TvListAdapter(Context context, List<TvMenu> arrayList) {
        this.mInflater = LayoutInflater.from(context);
        this.arrayList = arrayList;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public TvMenu getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final View view;
        final TextView text;

        if (convertView == null) {
            view = mInflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
        } else {
            view = convertView;
        }
        text = (TextView) view;
        com.lc5900.tv.model.TvMenu item = (com.lc5900.tv.model.TvMenu) getItem(position);

        text.setText(item.getId() + "." + item.getName());


        return view;
    }
}
