package com.zbm.dainty.ui;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.zbm.dainty.R;
import com.zbm.dainty.widget.SwipeBackActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zbm阿铭 on 2018/3/9.
 */

public class HistoryAndLabelActivity extends SwipeBackActivity {

    @BindView(R.id.label_history_viewpager)
    ViewPager labelHistoryViewPager;
    @BindView(R.id.history_bar_theme)
    View historyBarTheme;
    @BindView(R.id.history_label_back)
    Button historyLabelBack;
    @BindView(R.id.tab_bar)
    TabLayout tabBar;


    private List<Fragment> mFragments;
    private String[] tabTitle={"历史","书签"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_history);
        ButterKnife.bind(this);
        initData();//初始化数据
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        historyBarTheme.setBackgroundColor(Color.parseColor(preferences.getString("theme_color", "#474747")));
    }


    private void initData(){

        mFragments = new ArrayList<>();
        mFragments.add(new HistoryFragment());
        mFragments.add(new LabelFragment());
        //初始化适配器
        FragmentPagerAdapter mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public CharSequence getPageTitle(int position) {

                return tabTitle[position];
            }

            @Override
            public Fragment getItem(int position) {//从集合中获取对应位置的Fragment
                return mFragments.get(position);
            }

            @Override
            public int getCount() {//获取集合中Fragment的总数
                return mFragments.size();
            }

        };
        tabBar.setupWithViewPager(labelHistoryViewPager);
        labelHistoryViewPager.setAdapter(mAdapter);
        labelHistoryViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            //页面滚动事件
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //页面选中事件
            @Override
            public void onPageSelected(int position) {
                //设置position对应的集合中的Fragment
                if (position==0){
                    HistoryAndLabelActivity.this.canSwipe(true);

                }
                else {
                    HistoryAndLabelActivity.this.canSwipe(false);
                }
            }

            @Override
            //页面滚动状态改变事件
            public void onPageScrollStateChanged(int state) {

            }
        });
        historyLabelBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

}
