package com.zbm.dainty.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zbm.dainty.R;

import java.util.List;

public class PopupMenuListAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<String> data;
    private OnItemClickListener onItemClickListener;

    public PopupMenuListAdapter(Context context,List<String> data) {
        this.context=context;
        this.data=data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(context).inflate(R.layout.popup_menu_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((MyHolder)holder).item.setText(data.get(position));
        if (onItemClickListener != null)
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(holder.getLayoutPosition());
                }
            });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }


    private static class MyHolder extends RecyclerView.ViewHolder{
        TextView item;
        MyHolder(View view){
            super(view);
            item= view.findViewById(R.id.popup_menu_list_item);
        }
    }
}
