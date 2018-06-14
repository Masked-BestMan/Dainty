package com.zbm.dainty.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zbm.dainty.R;
import com.zbm.dainty.util.MyUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by zbm阿铭 on 2018/3/18.
 */

public class DownloadRecordAdapter extends BaseAdapter {

    private List<Map<String,Object>> data;
    private LayoutInflater mInflater;
    private boolean canSelectMore;
    private boolean restoreCheckBox=false;
    private OnCheckChangedListener onCheckChangedListener;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private Context context;
    public DownloadRecordAdapter(Context context, List<Map<String, Object>> data){
        this.context=context;
        this.data=data;
        mInflater=LayoutInflater.from(context);
        DateFormat.getInstance();
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

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView==null){
            holder=new ViewHolder();
            convertView=mInflater.inflate(R.layout.download_record_list_item,null);
            holder.icon=convertView.findViewById(R.id.download_record_icon);
            holder.name=convertView.findViewById(R.id.download_record_name);
            holder.describe=convertView.findViewById(R.id.download_record_modified_date_and_size);
            holder.checkBox=convertView.findViewById(R.id.download_record_delete_checkbox);
            convertView.setTag(holder);
        }else{
            holder= (ViewHolder) convertView.getTag();
        }
        holder.icon.setImageResource(R.mipmap.ic_launcher);
        holder.name.setText((String) data.get(position).get("file_name"));
        holder.describe.setText(format.format(new Date((Long) data.get(position).get("last_modified")))+"   "+data.get(position).get("file_size"));
        if(canSelectMore){
            holder.checkBox.setVisibility(View.VISIBLE);
        }else {
            holder.checkBox.setVisibility(View.INVISIBLE);
        }
        if (restoreCheckBox)holder.checkBox.setChecked(false);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onCheckChangedListener.onCheckChanged((int) compoundButton.getTag(R.id.download_record_delete_checkbox),b);
            }
        });
        holder.checkBox.setTag(R.id.download_record_delete_checkbox,position);
        ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, MyUtil.dip2px(context,60));//设置宽度和高度
        convertView.setLayoutParams(params);
        return convertView;
    }
    public interface OnCheckChangedListener{
        void onCheckChanged(int position,boolean checked);
    }

    private final class ViewHolder{
        ImageView icon;
        TextView name;
        TextView describe;
        CheckBox checkBox;
    }
}
