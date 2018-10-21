package com.zbm.dainty.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zbm.dainty.R;
import com.zbm.dainty.bean.QueryItemBean;

import java.util.ArrayList;

/**
 * Created by Zbm阿铭 on 2018/1/25.
 */

public class QueryListAdapter extends BaseAdapter {

    public final static int TYPE_HEAD=0;
    public final static int TYPE_CONTENT=1;

    private ArrayList<QueryItemBean> data;
    private LayoutInflater mInflater;
    private OnFillingClickListener onFillingClickListener;
    private OnHeadClickListener onHeadClickListener;

    public QueryListAdapter(Context context, ArrayList<QueryItemBean> data){
        this.mInflater=LayoutInflater.from(context);
        this.data=data;
    }
    @Override
    public int getCount() {
        return data.size()+1;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0)
            return TYPE_HEAD;
        else
            return TYPE_CONTENT;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        int currentType = getItemViewType(i);//当前类型
        if (currentType==TYPE_CONTENT) {
            ContentHolder holder;
            if (view == null) {
                holder = new ContentHolder();
                view = mInflater.inflate(R.layout.query_list_item_content, viewGroup, false);
                holder.typeImage = view.findViewById(R.id.type_image);
                holder.queryTitle = view.findViewById(R.id.query_title);
                holder.copy = view.findViewById(R.id.copy_button);
                view.setTag(holder);
            } else {
                holder = (ContentHolder) view.getTag();
            }
            if (data.get(i-1).getQueryTYPE().equals("url")) {
                holder.typeImage.setImageResource(R.drawable.enter_url);
            } else {
                holder.typeImage.setImageResource(R.drawable.enter_word);
            }
            holder.queryTitle.setText(data.get(i-1).getQueryNAME());
            holder.copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onFillingClickListener.onFilling(data.get(i-1).getQueryNAME());
                }
            });
        }else {
            HeadHolder holder;
            if (view==null){
                holder = new HeadHolder();
                view = mInflater.inflate(R.layout.query_list_item_head, viewGroup, false);
                holder.clear = view.findViewById(R.id.clear_history);
                view.setTag(holder);
            }else
                holder = (HeadHolder) view.getTag();

            holder.clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onHeadClickListener.onClick();
                }
            });
        }
        return view;
    }

    public void setOnFillingClickListener(OnFillingClickListener onFillingClickListener) {
        this.onFillingClickListener = onFillingClickListener;
    }

    public interface OnFillingClickListener{
        void onFilling(String text);
    }

    public void setOnHeadClickListener(OnHeadClickListener onHeadClickListener) {
        this.onHeadClickListener = onHeadClickListener;
    }

    public interface OnHeadClickListener{
        void onClick();
    }

    private static class ContentHolder{
        ImageView typeImage;
        TextView queryTitle;
        Button copy;
    }

    private static class HeadHolder{
        Button clear;
    }
}
