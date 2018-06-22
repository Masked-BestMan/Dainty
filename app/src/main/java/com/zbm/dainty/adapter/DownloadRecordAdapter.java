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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zbm.dainty.R;
import com.zbm.dainty.bean.FileDownloadBean;
import com.zbm.dainty.util.DownloadHelper;
import com.zbm.dainty.util.MyUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by zbm阿铭 on 2018/3/18.
 */

public class DownloadRecordAdapter extends BaseAdapter {

    private List<FileDownloadBean> data;
    private LayoutInflater mInflater;
    private boolean canSelectMore;
    private boolean restoreCheckBox=false;
    private OnCheckChangedListener onCheckChangedListener;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private Context context;
    public DownloadRecordAdapter(Context context, List<FileDownloadBean> data){
        this.context=context;
        this.data=data;
        mInflater=LayoutInflater.from(context);
        DateFormat.getInstance();
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
    public FileDownloadBean getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).isDownloading())
            return 0;
        else
            return 1;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null){
            if (getItemViewType(position)==0) {
                DownloadingHolder holder=new DownloadingHolder();
                convertView = mInflater.inflate(R.layout.downloading_list_item, parent,false);
                holder.icon = convertView.findViewById(R.id.downloading_icon);
                holder.name = convertView.findViewById(R.id.downloading_filename);
                holder.speed = convertView.findViewById(R.id.download_speed);
                holder.progressBar = convertView.findViewById(R.id.download_progress);
                holder.checkBox = convertView.findViewById(R.id.download_record_delete_checkbox);
                convertView.setTag(holder);
            }else {
                ViewHolder holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.download_record_list_item, parent,false);
                holder.icon = convertView.findViewById(R.id.download_record_icon);
                holder.name = convertView.findViewById(R.id.download_record_name);
                holder.describe = convertView.findViewById(R.id.download_record_modified_date_and_size);
                holder.checkBox = convertView.findViewById(R.id.download_record_delete_checkbox);
                convertView.setTag(holder);
            }

        }

        if (getItemViewType(position)==0){
            DownloadingHolder holder= (DownloadingHolder) convertView.getTag();
            holder.icon.setImageResource(R.mipmap.ic_launcher);
            holder.name.setText(data.get(position).getFileName());
            holder.progressBar.setMax(DownloadHelper.downloadList.get(position).getFileSize());
            if(canSelectMore){
                holder.checkBox.setVisibility(View.VISIBLE);
            }else {
                holder.checkBox.setVisibility(View.GONE);
            }
            if (restoreCheckBox)holder.checkBox.setChecked(false);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    onCheckChangedListener.onCheckChanged((int) compoundButton.getTag(R.id.download_record_delete_checkbox),b);
                }
            });
            holder.checkBox.setTag(R.id.download_record_delete_checkbox,position);
        }else {
            ViewHolder holder= (ViewHolder) convertView.getTag();
            holder.icon.setImageResource(R.mipmap.ic_launcher);
            holder.name.setText(data.get(position).getFileName());
            holder.describe.setText(format.format(new Date(data.get(position).getLastModified()))+"   "+data.get(position).getFileSize());
            if(canSelectMore){
                holder.checkBox.setVisibility(View.VISIBLE);
            }else {
                holder.checkBox.setVisibility(View.GONE);
            }
            if (restoreCheckBox)holder.checkBox.setChecked(false);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    onCheckChangedListener.onCheckChanged((int) compoundButton.getTag(R.id.download_record_delete_checkbox),b);
                }
            });
            holder.checkBox.setTag(R.id.download_record_delete_checkbox,position);
        }

        ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, MyUtil.dip2px(context,70));//设置宽度和高度
        convertView.setLayoutParams(params);
        return convertView;
    }
    public interface OnCheckChangedListener{
        void onCheckChanged(int position,boolean checked);
    }

    private static class ViewHolder{
        ImageView icon;
        TextView name;
        TextView describe;
        CheckBox checkBox;
    }

    private static class DownloadingHolder{
        ImageView icon;
        TextView name;
        TextView speed;
        ProgressBar progressBar;
        CheckBox checkBox;
    }
}
