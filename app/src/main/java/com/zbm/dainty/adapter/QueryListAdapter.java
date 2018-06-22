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
    private ArrayList<QueryItemBean> data;
    private LayoutInflater mInflater;
    private OnFillingClickListener onFillingClickListener;
    public QueryListAdapter(Context context, ArrayList<QueryItemBean> data){
        this.mInflater=LayoutInflater.from(context);
        this.data=data;
    }
    @Override
    public int getCount() {
        return data.size();
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder ;
        if(view==null){
            holder=new ViewHolder();
            view=mInflater.inflate(R.layout.query_list_item,viewGroup,false);
            holder.typeImage=view.findViewById(R.id.type_image);
            holder.queryTitle=view.findViewById(R.id.query_title);
            holder.copy=view.findViewById(R.id.copy_button);
            view.setTag(holder);
        }else {
            holder=(ViewHolder)view.getTag();
        }
        if(data.get(i).getQueryTYPE().equals("url")){
            holder.typeImage.setImageResource(R.drawable.enter_url);
        }else {
            holder.typeImage.setImageResource(R.drawable.enter_word);
        }
        holder.queryTitle.setText(data.get(i).getQueryNAME());
        holder.copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFillingClickListener.onFilling(data.get(i).getQueryNAME());
            }
        });
        return view;
    }

    public void setOnFillingClickListener(OnFillingClickListener onFillingClickListener) {
        this.onFillingClickListener = onFillingClickListener;
    }

    public interface OnFillingClickListener{
        void onFilling(String text);
    }
    private static class ViewHolder{
        ImageView typeImage;
        TextView queryTitle;
        Button copy;
    }
}
