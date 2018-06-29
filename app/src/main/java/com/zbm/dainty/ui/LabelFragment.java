package com.zbm.dainty.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.zbm.dainty.adapter.CollectionListAdapter;
import com.zbm.dainty.util.DaintyDBHelper;
import com.zbm.dainty.R;
import com.zbm.dainty.util.MyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by zbm阿铭 on 2018/3/9.
 */

public class LabelFragment extends Fragment {
    private CollectionListAdapter adapter;
    private PopupWindow deleteWindow;
    private RelativeLayout selectMoreBar;
    private ArrayList<Map<String, Object>> mData = new ArrayList<>();
    private List<Integer> selectedItemList = new ArrayList<>();
    private int selectedPosition;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_label,container,false);
        ListView mList = view.findViewById(R.id.collection_list);
        selectMoreBar=view.findViewById(R.id.collection_select_more_bar);
        Button confirmDelete = view.findViewById(R.id.collection_confirm_delete);
        Button cancelDelete = view.findViewById(R.id.collection_cancel_delete);

        adapter=new CollectionListAdapter(getActivity(),mData);
        adapter.setOnCheckChangedListener(new CollectionListAdapter.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(int position, boolean checked) {
                if (checked){
                    selectedItemList.add(position);
                }else {
                    selectedItemList.remove((Integer) position);
                }
                Log.d("rer","select:"+selectedItemList);
            }
        });
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.isCanSelectMore()){
                    CheckBox itemCheckBox = view.findViewById(R.id.collection_delete_checkbox);
                    if (itemCheckBox.isChecked()) {
                        itemCheckBox.setChecked(false);

                    } else {
                        itemCheckBox.setChecked(true);
                    }
                }else {
                    Map map = mData.get(position);
                    Intent intent = new Intent();
                    intent.putExtra("currentUri", (String) map.get("url"));
                    getActivity().setResult(RESULT_OK, intent);
                    getActivity().finish();
                }
            }
        });
        //长按删除item
        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition= position;
                if (!selectMoreBar.isShown()) {
                    int[] positions = new int[2];
                    view.getLocationOnScreen(positions);
                    deleteWindow.showAtLocation(view, Gravity.TOP | Gravity.END, 50, positions[1] + MyUtil.dip2px(getActivity(), 60));
                }
                return true;
            }
        });
        @SuppressLint("InflateParams")
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
        deleteButton.setText("删除该条书签");
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DaintyDBHelper.getDaintyDBHelper(getActivity()).deleteTableItem(DaintyDBHelper.CTB_NAME,"where collectionID="+mData.get(selectedPosition).get("id"));
                getCollection();
                deleteWindow.dismiss();
            }
        });
        deleteWindow = new PopupWindow(contentView, MyUtil.dip2px(getActivity(),120),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteWindow.setFocusable(true);
        deleteWindow.setOutsideTouchable(true);
        deleteWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        confirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder sb=new StringBuilder();
                sb.append("where collectionID in (");
                for (int i=0;i<selectedItemList.size();i++){
                    sb.append(mData.get(selectedItemList.get(i)).get("id"));
                    if(i!=selectedItemList.size()-1)
                        sb.append(",");
                }
                sb.append(")");
                Log.d("rer","sb:"+sb.toString());
                DaintyDBHelper.getDaintyDBHelper(getActivity()).deleteTableItem(DaintyDBHelper.CTB_NAME,sb.toString());
                adapter.setRestoreCheckBox(true);
                adapter.setCanSelectMore(false);
                selectedItemList.clear();
                getCollection();   //getCollection()必须在setRestoreCheckBox和setCanSelectMore之后调用
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
        getCollection();
        return view;
    }
    private void getCollection(){
        DaintyDBHelper.getDaintyDBHelper(getActivity()).searchCollectionTable(new DaintyDBHelper.OnSearchCollectionTableListener() {
            @Override
            public void onResult(ArrayList<Map<String, Object>> mCollectionData) {
                mData.clear();
                mData.addAll(mCollectionData);
                adapter.notifyDataSetChanged();
            }
        });

        Log.d("trr","data:"+mData.size());
    }

}
