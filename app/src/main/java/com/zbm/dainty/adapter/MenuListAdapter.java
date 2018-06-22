package com.zbm.dainty.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zbm.dainty.R;

/**
 * Created by zbm阿铭 on 2018/2/28.
 */

public class MenuListAdapter extends BaseAdapter {
    private String[] titles={"小说阅读","海量视频","头条新闻","收藏书签","文件下载","历史/书签","设置"};
    private int[] imageId={R.drawable.book,R.drawable.video,R.drawable.news,R.drawable.collect,R.drawable.file,R.drawable.history,R.drawable.setting};
    private LayoutInflater mInflater;
    private boolean allowCollect=false;
    private Context context;
    public MenuListAdapter(Context context){
        this.mInflater= LayoutInflater.from(context);
        this.context=context;
    }
    @Override
    public int getCount() {
        return 7;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return !(position == 3 && !allowCollect) && super.isEnabled(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder ;
        if(convertView==null){
            holder=new ViewHolder();
            convertView=mInflater.inflate(R.layout.menu_drawer_list_item,parent,false);
            holder.menuImage=convertView.findViewById(R.id.menu_image);
            holder.menuTitle=convertView.findViewById(R.id.menu_title);
            convertView.setTag(holder);
        }else {
            holder=(ViewHolder)convertView.getTag();
        }
        holder.menuImage.setImageResource(imageId[position]);
        holder.menuTitle.setText(titles[position]);
        if (position==3) {
            if (!allowCollect) {
                holder.menuTitle.setTextColor(context.getResources().getColor(R.color.light_gray));
            } else
                holder.menuTitle.setTextColor(context.getResources().getColor(R.color.text_color_black));
        }else
            holder.menuTitle.setTextColor(context.getResources().getColor(R.color.text_color_black));
        return convertView;
    }

    private static class ViewHolder{
        ImageView menuImage;
        TextView menuTitle;
    }

    public void setAllowCollect(boolean allowCollect) {
        this.allowCollect = allowCollect;
    }
}
