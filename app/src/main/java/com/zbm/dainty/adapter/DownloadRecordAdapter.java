package com.zbm.dainty.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Formatter;
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
    private boolean restoreCheckBox = false;
    private OnCheckChangedListener onCheckChangedListener;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private Context context;

    public DownloadRecordAdapter(Context context, List<FileDownloadBean> data) {
        this.context = context;
        this.data = data;
        mInflater = LayoutInflater.from(context);
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
        if (!data.get(position).isFinished())
            return 0;
        else
            return 1;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            if (getItemViewType(position) == 0) {
                DownloadingHolder holder = new DownloadingHolder();
                convertView = mInflater.inflate(R.layout.downloading_list_item, parent, false);
                holder.icon = convertView.findViewById(R.id.downloading_icon);
                holder.name = convertView.findViewById(R.id.downloading_filename);
                holder.speed = convertView.findViewById(R.id.download_speed);
                holder.progressBar = convertView.findViewById(R.id.download_progress);
                holder.downloadStatus = convertView.findViewById(R.id.download_status);
                holder.checkBox = convertView.findViewById(R.id.download_record_delete_checkbox);
                convertView.setTag(holder);
            } else {
                ViewHolder holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.download_record_list_item, parent, false);
                holder.icon = convertView.findViewById(R.id.download_record_icon);
                holder.name = convertView.findViewById(R.id.download_record_name);
                holder.describe = convertView.findViewById(R.id.download_record_modified_date_and_size);
                holder.checkBox = convertView.findViewById(R.id.download_record_delete_checkbox);
                convertView.setTag(holder);
            }

        }

        String suffix = getExtensionName(data.get(position).getFileName());
        int image;
        switch (suffix) {
            case "apk":
                image = R.drawable.fileicon_apk;
                break;
            case "m4a":
            case "mp3":
            case "mid":
            case "xmf":
            case "ogg":
            case "wav":
                image = R.drawable.fileicon_audio;
                break;
            case "3gp":
            case "mp4":
                image = R.drawable.fileicon_video;
                break;
            case "jpg":
            case "gif":
            case "png":
            case "jpeg":
            case "bmp":
                image = R.drawable.fileicon_image;
                break;
            case "pdf":
                image = R.drawable.fileicon_pdf;
                break;
            case "txt":
            case "doc":
            case "docx":
            case "xls":
            case "xlsx":
            case "ppt":
            case "pptx":
                image = R.drawable.fileicon_document;
                break;
            case "rar":
            case "zip":
            case "7z":
                image = R.drawable.fileicon_compressfile;
                break;
            case "htm":
            case "html":
            case "jsp":
            case "php":
            case "xml":
                image = R.drawable.fileicon_webpage;
                break;
            default:
                image = R.drawable.fileicon_default;
        }
        if (getItemViewType(position) == 0) {
            DownloadingHolder holder = (DownloadingHolder) convertView.getTag();
            holder.icon.setImageResource(image);
            holder.name.setText(data.get(position).getFileName());
            holder.progressBar.setMax(data.get(position).getFileSize());
            holder.progressBar.setProgress(data.get(position).getDownloadProgress());
            if (data.get(position).isDownloading()) {
                holder.speed.setText(data.get(position).getSpeed());
                holder.downloadStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.stop_download));
            } else {
                holder.speed.setText("暂停");
                holder.downloadStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.start_download));
            }
            if (canSelectMore) {
                holder.checkBox.setVisibility(View.VISIBLE);
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }
            if (restoreCheckBox) holder.checkBox.setChecked(false);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    onCheckChangedListener.onCheckChanged((int) compoundButton.getTag(R.id.download_record_delete_checkbox), b);
                }
            });
            holder.checkBox.setTag(R.id.download_record_delete_checkbox, position);
        } else {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.icon.setImageResource(image);
            holder.name.setText(data.get(position).getFileName());
            holder.describe.setText(format.format(new Date(data.get(position).getLastModified())) + "   " + Formatter.formatFileSize(context, data.get(position).getFileSize()));
            if (canSelectMore) {
                holder.checkBox.setVisibility(View.VISIBLE);
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }
            if (restoreCheckBox) holder.checkBox.setChecked(false);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    onCheckChangedListener.onCheckChanged((int) compoundButton.getTag(R.id.download_record_delete_checkbox), b);
                }
            });
            holder.checkBox.setTag(R.id.download_record_delete_checkbox, position);
        }

        ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, MyUtil.dip2px(context, 70));//设置宽度和高度
        convertView.setLayoutParams(params);
        return convertView;
    }

    public interface OnCheckChangedListener {
        void onCheckChanged(int position, boolean checked);
    }

    private static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView describe;
        CheckBox checkBox;
    }

    private static class DownloadingHolder {
        ImageView icon;
        TextView name;
        TextView speed;
        ProgressBar progressBar;
        ImageView downloadStatus;
        CheckBox checkBox;
    }

    /**
     * 获取文件扩展名
     */
    private static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        if (filename != null)
            return filename.toLowerCase();
        else
            return null;
    }
}
