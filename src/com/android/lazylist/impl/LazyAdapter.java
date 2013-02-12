package com.android.lazylist.impl;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.lazylist.ImageLoader;

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private String[] data;
    
    public LazyAdapter(Activity a, String[] d) {
        activity = a;
        data=d;
        
    }

    public int getCount() {
        return data.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        MyItemView view = null;
        if(convertView == null) {
        	view = new MyItemView(activity);
        }else {
        	view = (MyItemView)convertView;
        }
        view.setPosition(position);
        ImageLoader.getInstance().displayImage(data[position], view);
        return view;
    }

	
}