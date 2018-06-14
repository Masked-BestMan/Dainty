package com.zbm.dainty.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zbm.dainty.util.DaintyDBHelper;
import com.zbm.dainty.bean.HistoryItemBean;
import com.zbm.dainty.adapter.HistoryListAdapter;
import com.zbm.dainty.widget.HistoryListView;
import com.zbm.dainty.util.IDockingHeaderUpdateListener;
import com.zbm.dainty.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by zbm阿铭 on 2018/3/9.
 */

public class HistoryFragment extends Fragment {
    private HistoryListView mList;
    private RelativeLayout selectMoreBar;
    private Button confirmDelete;
    private Button cancelDelete;
    private ImageView emptyHistory;
    private Map<String, List<HistoryItemBean>> mHistoryData;
    private List<String> parentList;  //header view的日期标题
    private HistoryListAdapter adapter;
    private PopupWindow deleteWindow;
    private int groupPos, childPos; //参数值是在setTag时使用的对应资源id号,标识具体删除哪个item
    private List<Integer> selectedItemList = new ArrayList<>();  //记录要删除的item所在的childPosition groupPosition

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHistoryData = new HashMap<>();
        parentList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_history,container,false);
        selectMoreBar=view.findViewById(R.id.history_select_more_bar);
        confirmDelete=view.findViewById(R.id.history_confirm_delete);
        cancelDelete=view.findViewById(R.id.history_cancel_delete);
        emptyHistory=view.findViewById(R.id.empty_history);
        mList=view.findViewById(R.id.history_list);

        adapter = new HistoryListAdapter(getActivity(), mList, parentList, mHistoryData);
        adapter.setOnCheckChangedListener(new HistoryListAdapter.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(int groupPosition, int childPosition, boolean checked) {
                if (checked) {
                    selectedItemList.add(groupPosition);
                    selectedItemList.add(childPosition);
                } else {
                    selectedItemList.remove(Integer.valueOf(groupPosition));
                    selectedItemList.remove(Integer.valueOf(childPosition));
                }
                Log.d("aaa", "selectedItemList:" + selectedItemList);
            }
        });

        mList.setAdapter(adapter);
        mList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (adapter.isCanSelectMore()) {
                    CheckBox itemCheckBox = v.findViewById(R.id.delete_checkbox);
                    if (itemCheckBox.isChecked()) {
                        itemCheckBox.setChecked(false);

                    } else {
                        itemCheckBox.setChecked(true);
                    }
                } else {
                    HistoryItemBean hb = mHistoryData.get(parentList.get(groupPosition)).get(childPosition);
                    Intent intent = new Intent();
                    intent.putExtra("currentUri", hb.getHistoryURI());
                    DaintyDBHelper.getDaintyDBHelper(getActivity()).deleteTableItem(DaintyDBHelper.TB_NAME,hb.getHistoryID()+"");
                    getActivity().setResult(RESULT_OK, intent);
                    getActivity().finish();
                }
                return true;
            }
        });
        View headerView = getLayoutInflater().inflate(R.layout.history_of_date, mList, false);

        //更新标题
        mList.setDockingHeader(headerView, new IDockingHeaderUpdateListener() {
            @Override
            public void onUpdate(View headerView, int groupPosition, boolean expanded) {
                String groupTitle = parentList.get(groupPosition);
                TextView titleView = headerView.findViewById(R.id.history_date);
                titleView.setText(groupTitle);
            }
        });

        //长按删除item
        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                groupPos = (Integer) view.getTag(R.id.web_title); //参数值是在setTag时使用的对应资源id号
                childPos = (Integer) view.getTag(R.id.web_url);
                if (childPos != -1) {

                    deleteWindow.showAsDropDown(view, 50, 50, Gravity.BOTTOM);
                }

                return true;
            }
        });

        emptyHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNormalDialog();
            }
        });
        View contentView = ((LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.history_item_delete_window, null);
        Button editButton = contentView.findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.setRestoreCheckBox(false);
                adapter.setCanSelectMore(true);
                adapter.notifyDataSetInvalidated();
                selectMoreBar.setVisibility(View.VISIBLE);
                deleteWindow.dismiss();
            }
        });
        Button deleteButton = contentView.findViewById(R.id.deleteButton1);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("aaa",groupPos+""+childPos);
                DaintyDBHelper.getDaintyDBHelper(getActivity()).deleteTableItem(DaintyDBHelper.TB_NAME,"where historyID="+mHistoryData.get(parentList.get(groupPos)).get(childPos).getHistoryID());
                getHistory();
                deleteWindow.dismiss();
            }
        });
        deleteWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteWindow.setFocusable(true);
        deleteWindow.setOutsideTouchable(true);
        deleteWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        confirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder sb=new StringBuilder();
                sb.append("where historyID in (");
                for (int i = 0; i < selectedItemList.size() - 1; i = i + 2) {
                    sb.append(mHistoryData.get(parentList.get(selectedItemList.get(i))).get(selectedItemList.get(i + 1)).getHistoryID());
                    if (i!=selectedItemList.size()-2){
                        sb.append(",");
                    }
                }
                sb.append(")");
                DaintyDBHelper.getDaintyDBHelper(getActivity()).deleteTableItem(DaintyDBHelper.TB_NAME,sb.toString());
                selectedItemList.clear();
                adapter.setRestoreCheckBox(true);
                adapter.setCanSelectMore(false);
                getHistory();     //getHistory()必须在setRestoreCheckBox和setCanSelectMore之后调用
                selectMoreBar.setVisibility(View.GONE);
            }
        });
        cancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.setCanSelectMore(false);
                adapter.setRestoreCheckBox(true);
                adapter.notifyDataSetInvalidated();
                selectMoreBar.setVisibility(View.GONE);
            }
        });
        getHistory();
        return view;
    }

    private void getHistory() {
        DaintyDBHelper.getDaintyDBHelper(getActivity()).searchHistoryTable(new DaintyDBHelper.OnSearchHistoryTableListener() {
            @Override
            public void onResult(Map<String, List<HistoryItemBean>> mHistoryData) {
                parentList.clear();
                HistoryFragment.this.mHistoryData.clear();
                HistoryFragment.this.mHistoryData.putAll(mHistoryData);
                parentList.addAll(mHistoryData.keySet());
                adapter.notifyDataSetChanged();
                for(int i=0;i<adapter.getGroupCount();i++){
                    mList.expandGroup(i);
                }
            }
        });
    }

    private void showNormalDialog() {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getActivity());
        normalDialog.setIcon(android.R.drawable.ic_menu_info_details)
                .setTitle("删除提示")
                .setMessage("确认清空输入历史？")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DaintyDBHelper.getDaintyDBHelper(getActivity()).deleteTableItem(DaintyDBHelper.TB_NAME,null);
                                getHistory();
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }
}
