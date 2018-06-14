package com.zbm.dainty.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.zbm.dainty.R;
import com.zbm.dainty.util.PictureUtil;
import com.zbm.dainty.widget.CircleImageView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by zbm阿铭 on 2018/3/11.
 */

public class CollectionListAdapter extends BaseAdapter {
    private List<Map<String, Object>> data;
    private LayoutInflater mInflater;
    private boolean canSelectMore;
    private boolean restoreCheckBox=false;
    private OnCheckChangedListener onCheckChangedListener;

    public CollectionListAdapter(Context context, List<Map<String, Object>> data) {
        this.data= data;
        mInflater=LayoutInflater.from(context);
    }

    public boolean isRestoreCheckBox() {
        return restoreCheckBox;
    }

    public void setRestoreCheckBox(boolean restoreCheckBox) {
        this.restoreCheckBox = restoreCheckBox;
    }

    public void setOnCheckChangedListener(OnCheckChangedListener onCheckChangedListener) {
        this.onCheckChangedListener = onCheckChangedListener;
    }




    public boolean isCanSelectMore() {
        return canSelectMore;
    }

    public void setCanSelectMore(boolean canSelectMore) {
        this.canSelectMore = canSelectMore;
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView==null){
            holder=new ViewHolder();
            convertView=mInflater.inflate(R.layout.collection_list_item,null);
            holder.icon=convertView.findViewById(R.id.collection_icon);
            holder.title=convertView.findViewById(R.id.collection_title);
            holder.url=convertView.findViewById(R.id.collection_url);
            holder.checkBox=convertView.findViewById(R.id.collection_delete_checkbox);
            convertView.setTag(holder);
        }else{
            holder= (ViewHolder) convertView.getTag();
        }
        holder.icon.setImageBitmap(PictureUtil.bytesToBitmap((byte[]) data.get(position).get("icon")));
        holder.title.setText((String)data.get(position).get("name"));
        String host;
        try {
            URL url=new URL((String) data.get(position).get("url"));
            host=url.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            host="";
        }
        holder.url.setText(host);
        if(canSelectMore){
            holder.checkBox.setVisibility(View.VISIBLE);
        }else {
            holder.checkBox.setVisibility(View.INVISIBLE);
        }
        if (restoreCheckBox)holder.checkBox.setChecked(false);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onCheckChangedListener.onCheckChanged((int) compoundButton.getTag(R.id.collection_delete_checkbox),b);
            }
        });
        holder.checkBox.setTag(R.id.collection_delete_checkbox,position);
        convertView.setTag(R.id.collection_title, position);
        return convertView;
    }

    public interface OnCheckChangedListener{
        void onCheckChanged(int position,boolean checked);
    }
    private final class ViewHolder{
        CircleImageView icon;
        TextView title;
        TextView url;
        CheckBox checkBox;
    }
}
