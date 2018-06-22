package com.zbm.dainty.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.zbm.dainty.bean.HistoryItemBean;
import com.zbm.dainty.bean.QueryItemBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DaintyDBHelper extends SQLiteOpenHelper {
    private static DaintyDBHelper helper = null;
    /*
    queryTYPE为搜索类型：url网址；word内容
     */
    public static final String TB_NAME = "historyTB";
    public static final String QTB_NAME = "queryTB";
    public static final String CTB_NAME = "collectionTB";
    private static final String CREATE_HISTORYTB = "create table " + TB_NAME + "(" +
            "historyID integer primary key autoincrement," +
            "historyURL text," +
            "historyTIME date," +
            "historyNAME text unique)";
    private static final String CREATE_QUERYTB = "create table " + QTB_NAME + "(" +
            "queryID integer primary key autoincrement," +
            "queryTYPE text," +
            "queryTIME date," +
            "queryNAME text unique)";
    private static final String CREATE_COLLECTIONTB = "create table " + CTB_NAME + "(" +
            "collectionID integer primary key autoincrement," +
            "collectionICON blob," +
            "collectionNAME text," +
            "collectionURL text unique," +
            "collectionTIME date)";

    private OnSearchHistoryTableListener hl;
    private OnSearchQueryTableListener ql;
    private OnSearchCollectionTableListener cl;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    hl.onResult((Map<String, List<HistoryItemBean>>) msg.obj);
                    break;
                case 2:
                    cl.onResult((ArrayList<Map<String, Object>>) msg.obj);
                    break;
                case 3:
                    ql.onResult((ArrayList<QueryItemBean>) msg.obj);
            }
        }
    };

    public void removeMessage() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public DaintyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HISTORYTB);
        db.execSQL(CREATE_QUERYTB);
        db.execSQL(CREATE_COLLECTIONTB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public static DaintyDBHelper getDaintyDBHelper(Context context) {
        if (helper == null)
            helper = new DaintyDBHelper(context.getApplicationContext(), "DaintyDatabase", null, 1);
        return helper;
    }

    public void searchHistoryTable(OnSearchHistoryTableListener hl) {
        this.hl = hl;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                Map<String, List<HistoryItemBean>> mHistoryData = new LinkedHashMap<>();
                List<String> parentList = new ArrayList<>();
                List[] children;  //用于暂时记录分组成员
                String lastDate = "";  //保持上个组的日期
                int key = -1;  //记录子成员索引
                Calendar calendar = Calendar.getInstance();
                String currentDate = format.format(calendar.getTime());
                calendar.add(Calendar.DATE, -1);
                String yesterday = format.format(calendar.getTime());
                SQLiteDatabase db = helper.getWritableDatabase();
                Log.d("history", "currentDate:" + currentDate);
                //查询3天内的历史记录
                Cursor mCursor = db.rawQuery("select * from " + DaintyDBHelper.TB_NAME + " where historyTIME>=datetime('now','localtime','-1 months') and historyTIME<=datetime('now','localtime') order by historyTIME desc", null);
                Log.d("history", mCursor.getCount() + "");
                //先计算有多少个组
                if (mCursor.moveToFirst()) {
                    do {
                        String date = mCursor.getString(mCursor.getColumnIndex("historyTIME")).split(" ")[0];
                        //需要添加头节点
                        if (!lastDate.equals(date)) {
                            if (currentDate.equals(date)) {

                                parentList.add("今天");

                            } else if (yesterday.equals(date)) {

                                parentList.add("昨天");
                            } else {

                                parentList.add(date);
                            }
                        }
                        lastDate = date;
                    } while (mCursor.moveToNext());
                    children = new List[parentList.size()];
                    //接着再计算成员
                    mCursor.moveToFirst();
                    for (int i = 0; i < parentList.size(); i++) {
                        children[i] = new ArrayList<HistoryItemBean>();
                    }
                    lastDate = "";
                    do {
                        String date = mCursor.getString(mCursor.getColumnIndex("historyTIME")).split(" ")[0];
                        if (!lastDate.equals(date)) {
                            key += 1;
                        }
                        String id = mCursor.getString(mCursor.getColumnIndex("historyID"));
                        String name = mCursor.getString(mCursor.getColumnIndex("historyNAME"));
                        String url = mCursor.getString(mCursor.getColumnIndex("historyURL"));
                        children[key].add(new HistoryItemBean(Integer.parseInt(id), name, url));
                        lastDate = date;
                    } while (mCursor.moveToNext());
                    Log.d("aaa", "日期:" + parentList);
                    //该步把所有组加入list
                    for (int i = 0; i < children.length; i++) {
                        mHistoryData.put(parentList.get(i), children[i]);
                    }
                }
                mCursor.close();
                mHandler.sendMessage(mHandler.obtainMessage(1, mHistoryData));
                Log.d("aaa", "日期:" + mHistoryData);
            }
        }).start();
    }

    public void updateHistoryTable(final Context context, final String url, final String title) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = helper.getWritableDatabase();
                String historyTime = format.format(new Date(System.currentTimeMillis()));
                Log.d("rrr", "" + historyTime);
                String sql = "replace into " + DaintyDBHelper.TB_NAME + "(historyURL,historyTIME,historyNAME) values('" + url + "','" + historyTime + "','" + title + "')";
                try {
                    db.execSQL(sql);
                } catch (SQLException e) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "记录出错", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "记录成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }).start();
    }

    public void searchCollectionTable(OnSearchCollectionTableListener cl) {
        this.cl = cl;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Map<String, Object>> searchResult = new ArrayList<>();
                SQLiteDatabase db = helper.getWritableDatabase();
                Cursor mCursor = db.rawQuery("select * from " + DaintyDBHelper.CTB_NAME + " order by collectionTIME desc", null);
                Log.d("rea", "count:" + mCursor.getCount());
                while (mCursor.moveToNext()) {
                    int id = mCursor.getInt(mCursor.getColumnIndex("collectionID"));
                    byte[] icon = mCursor.getBlob(mCursor.getColumnIndex("collectionICON"));
                    String name = mCursor.getString(mCursor.getColumnIndex("collectionNAME"));
                    String url = mCursor.getString(mCursor.getColumnIndex("collectionURL"));
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", id);
                    item.put("icon", icon);
                    item.put("name", name);
                    item.put("url", url);
                    searchResult.add(item);
                }
                mCursor.close();
                mHandler.sendMessage(mHandler.obtainMessage(2, searchResult));
            }
        }).start();

    }

    public void updateCollectionTable(final Context context, final byte[] icon, final String url, final String title) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String collectionTime = format.format(new Date(System.currentTimeMillis()));
                Log.d("rrr", "" + collectionTime);
                SQLiteDatabase db = helper.getWritableDatabase();
                try {
                    ContentValues values = new ContentValues();
                    values.put("collectionICON", icon);
                    values.put("collectionNAME", title);
                    values.put("collectionURL", url);
                    values.put("collectionTIME", collectionTime);
                    db.replace(DaintyDBHelper.CTB_NAME, null, values);
                } catch (SQLException e) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "收藏出错", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "收藏成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }).start();
    }

    public void searchQueryTable(final String sql, OnSearchQueryTableListener ql) {
        this.ql = ql;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<QueryItemBean> searchResult = new ArrayList<>();
                SQLiteDatabase db = helper.getWritableDatabase();
                Cursor mCursor = db.rawQuery(sql, null);
                while (mCursor.moveToNext()) {
                    int id = mCursor.getInt(mCursor.getColumnIndex("queryID"));
                    String type = mCursor.getString(mCursor.getColumnIndex("queryTYPE"));
                    String time = mCursor.getString(mCursor.getColumnIndex("queryTIME"));
                    String name = mCursor.getString(mCursor.getColumnIndex("queryNAME"));
                    searchResult.add(new QueryItemBean(id, type, time, name));
                    Log.d("aaa", "time:" + time);
                }
                mCursor.close();
                mHandler.sendMessage(mHandler.obtainMessage(3, searchResult));
            }
        }).start();
    }

    public void updateQueryTable(final String text,final boolean isURL) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String queryTime = format.format(new Date(System.currentTimeMillis()));
                SQLiteDatabase db=helper.getWritableDatabase();
                String sql;
                    if (isURL) {
                        sql = "replace into " + DaintyDBHelper.QTB_NAME + "(queryTYPE,queryTIME,queryNAME) values('url','" + queryTime + "','" + text + "')";

                    } else {
                        sql = "replace into " + DaintyDBHelper.QTB_NAME + "(queryTYPE,queryTIME,queryNAME) values('word','" + queryTime + "','" + text + "')";
                    }
                try {
                    db.execSQL(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    public void deleteTableItem(final String table, final String where) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = helper.getWritableDatabase();
                if (where != null)
                    db.execSQL("delete from " + table + " " + where);
                else
                    db.execSQL("delete from " + table);
            }
        }).start();
    }

    public interface OnSearchHistoryTableListener {
        void onResult(Map<String, List<HistoryItemBean>> mHistoryData);
    }

    public interface OnSearchQueryTableListener {
        void onResult(ArrayList<QueryItemBean> mQueryData);
    }

    public interface OnSearchCollectionTableListener {
        void onResult(ArrayList<Map<String, Object>> mCollectionData);
    }
}
