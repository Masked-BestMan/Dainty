package com.zbm.dainty.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zbm.dainty.R;
import com.zbm.dainty.util.FileUtil;
import com.zbm.dainty.util.MyUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;

import de.innosystec.unrar.rarfile.FileHeader;

public class CompressionListAdapter extends BaseAdapter {
    private Context context;
    private List<Object> data;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    public CompressionListAdapter(Context context,List<Object> data){
        this.context=context;
        this.data=data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null){
            convertView=LayoutInflater.from(context).inflate(R.layout.compression_file_list_item,parent,false);
            ViewHolder holder=new ViewHolder();
            holder.icon=convertView.findViewById(R.id.compression_icon);
            holder.name=convertView.findViewById(R.id.compression_name);
            holder.info=convertView.findViewById(R.id.compression_modified_date_and_size);
            convertView.setTag(holder);
        }
        ViewHolder holder= (ViewHolder) convertView.getTag();
        Object o=data.get(position);
        String suffix;
        if (o instanceof ZipEntry){
            suffix = FileUtil.getExtensionName(((ZipEntry)o).getName());
            holder.name.setText(FileUtil.getFileNameFromPath(((ZipEntry)o).getName()));
            holder.info.setText(format.format(new Date(((ZipEntry)o).getTime())) +
                    "   " + Formatter.formatFileSize(context, ((ZipEntry)o).getSize()));
        }else if (o instanceof FileHeader){
            FileHeader fh= (FileHeader) o;
            String entryPath;
            if (fh.isUnicode()){
                entryPath = fh.getFileNameW().trim();
            }else{
                entryPath = fh.getFileNameString().trim();
            }
            entryPath = entryPath.replaceAll("\\\\", "/");
            suffix = FileUtil.getExtensionName(entryPath);
            holder.name.setText(FileUtil.getFileNameFromPath(entryPath));
            holder.info.setText(format.format(fh.getMTime()) +
                    "   " + Formatter.formatFileSize(context, fh.getUnpSize()));
        }else {
            suffix="";
        }

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

        holder.icon.setImageResource(image);


        ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,
                MyUtil.dip2px(context, 65));//设置宽度和高度
        convertView.setLayoutParams(params);
        return convertView;
    }

    private static class ViewHolder{
        ImageView icon;
        TextView name;
        TextView info;
    }
}
