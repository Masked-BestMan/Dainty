package com.zbm.dainty.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.TextView;


import com.zbm.dainty.util.IDockingController;
import com.zbm.dainty.R;
import com.zbm.dainty.bean.HistoryItemBean;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by Zbm阿铭 on 2017/11/16.
 */

public class HistoryListAdapter extends BaseExpandableListAdapter implements IDockingController {
    private Map<String,List<HistoryItemBean>> mHistoryData;
    private List<String> parentList;
    private ExpandableListView mListView;
    private Context context;
    private boolean canSelectMore;
    private boolean restoreCheckBox=false;

    public boolean isRestoreCheckBox() {
        return restoreCheckBox;
    }

    public void setRestoreCheckBox(boolean restoreCheckBox) {
        this.restoreCheckBox = restoreCheckBox;
    }

    public void setOnCheckChangedListener(OnCheckChangedListener onCheckChangedListener) {
        this.onCheckChangedListener = onCheckChangedListener;
    }

    private OnCheckChangedListener onCheckChangedListener;

    public boolean isCanSelectMore() {
        return canSelectMore;
    }

    public void setCanSelectMore(boolean canSelectMore) {
        this.canSelectMore = canSelectMore;
    }

    public HistoryListAdapter(Context context, ExpandableListView listView, List<String> parentList, Map<String,List<HistoryItemBean>> mHistoryData){
        this.context=context;
        mListView = listView;
        this.parentList=parentList;
        this.mHistoryData=mHistoryData;
    }
    @Override
    public int getGroupCount() {
        return mHistoryData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mHistoryData.get(parentList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mHistoryData.get(parentList.get(groupPosition));
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mHistoryData.get(parentList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    //因为要支持删除item，必须返回false
    @Override
    public boolean hasStableIds() {
        return false;
    }
    // 标记位置
    // 必须使用资源Id当key（不是资源id会出现运行时异常），android本意应该是想用tag来保存资源id对应组件。
    // 将groupPosition，childPosition通过setTag保存,在onItemLongClick方法中就可以通过view参数直接拿到了！
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view;
        GroupHolder groupholder;
        if(convertView!=null){
            view=convertView;
            groupholder=(GroupHolder)view.getTag();
        }else{
            view= View.inflate(context, R.layout.history_of_date,null);
            groupholder=new GroupHolder();
            groupholder.date= view.findViewById(R.id.history_date);
            view.setTag(groupholder);
        }
        groupholder.date.setText(parentList.get(groupPosition));

        view.setTag(R.id.web_title, groupPosition); //设置-1表示长按时点击的是父项，到时好判断。
        view.setTag(R.id.web_url, -1);
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view;
        ChildHolder childHolder;
        if(convertView!=null){
            view=convertView;
            childHolder=(ChildHolder)view.getTag();
        }else {
            view=View.inflate(context,R.layout.history_list_item,null);
            childHolder=new ChildHolder();
            childHolder.webTitle= view.findViewById(R.id.web_title);
            childHolder.webUrl= view.findViewById(R.id.web_url);
            childHolder.deleteCheckBox=view.findViewById(R.id.delete_checkbox);
            view.setTag(childHolder);
        }

        HistoryItemBean historyItemBean=mHistoryData.get(parentList.get(groupPosition)).get(childPosition);
        childHolder.webTitle.setText(historyItemBean.getHistoryNAME());
        Log.d("ttt","Url :"+historyItemBean.getHistoryURI());
        String host;
        try {
            URL url=new URL(historyItemBean.getHistoryURI());
            host=url.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            host="";
        }

        childHolder.webUrl.setText(host);

        if(canSelectMore){
            childHolder.deleteCheckBox.setVisibility(View.VISIBLE);
        }else {
            childHolder.deleteCheckBox.setVisibility(View.INVISIBLE);
        }

        if (restoreCheckBox)childHolder.deleteCheckBox.setChecked(false);

        childHolder.deleteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String[] a=String.valueOf(compoundButton.getTag(R.id.delete_checkbox)).split(" ");
                int groupPosition= Integer.parseInt(a[0]);
                int childPosition= Integer.parseInt(a[1]);
                Log.d("aaa","groupPosition:"+groupPosition+" childPosition:"+childPosition);
                onCheckChangedListener.onCheckChanged(groupPosition,childPosition,b);
            }
        });
        childHolder.deleteCheckBox.setTag(R.id.delete_checkbox,groupPosition+" "+childPosition);
        view.setTag(R.id.web_title, groupPosition);
        view.setTag(R.id.web_url, childPosition);
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getDockingState(int firstVisibleGroup, int firstVisibleChild) {
        // 如果group没有child或者没有展开则不用画header view
        if (firstVisibleChild == -1 && !mListView.isGroupExpanded(firstVisibleGroup)) {
            return DOCKING_HEADER_HIDDEN;
        }

        // 第一个可视child为当前组最后一个child, 准备替换下一组的header
        if (firstVisibleChild == getChildrenCount(firstVisibleGroup) - 1) {
            return IDockingController.DOCKING_HEADER_DOCKING;
        }

        // 在当前组中滑动， header view固定.
        return IDockingController.DOCKING_HEADER_DOCKED;
    }

    static class GroupHolder{
        TextView date;
    }

    static class ChildHolder{
        TextView webTitle;
        TextView webUrl;
        CheckBox deleteCheckBox;
    }

    public interface OnCheckChangedListener{
        void onCheckChanged(int groupPosition,int childPosition,boolean checked);
    }
}
