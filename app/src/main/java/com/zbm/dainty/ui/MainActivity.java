package com.zbm.dainty.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.zbm.dainty.task.DownloaderTask;
import com.zbm.dainty.task.ImageTask;
import com.zbm.dainty.util.DownloadHelper;
import com.zbm.dainty.util.MyUtil;
import com.zbm.dainty.widget.CircleImageView;
import com.zbm.dainty.util.DaintyDBHelper;
import com.zbm.dainty.adapter.MenuListAdapter;
import com.zbm.dainty.bean.MessageEvent;
import com.zbm.dainty.R;
import com.zbm.dainty.util.WeatherService;
import com.zbm.dainty.util.WebPageHelper;
import com.zbm.dainty.adapter.WebPageAdapter;
import com.zbm.dainty.widget.MingWebView;
import com.zbm.dainty.widget.MyViewPager;
import com.zbm.dainty.widget.ScrollLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class MainActivity extends AppCompatActivity{

    @BindView(R.id.home_button)
    ImageView homeButton;
    @BindView(R.id.menu_button)
    ImageView menuButton;
    @BindView(R.id.query_button)
    TextView queryButton;
    @BindView(R.id.web_back)
    ImageView backButton;
    @BindView(R.id.web_freshen)
    ImageView freshenButton;
    @BindView(R.id.web_multi)
    ImageView multiButton;
    @BindView(R.id.web_next)
    ImageView nextButton;
    @BindView(R.id.web_stop_loading)
    ImageView stopLoading;

    @BindView(R.id.menu_list)
    ListView listView;
    @BindView(R.id.exit_button)
    TextView exitButton;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.head_portrait)
    CircleImageView headPortraitView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.weather)
    TextView now_temperature;
    @BindView(R.id.current_city)
    TextView city;

    @BindView(R.id.anchor)
    View anchor;
    @BindView(R.id.toolbar)
    View toolbar;
    @BindView(R.id.status_bar)
    View statusBar;
    @BindView(R.id.bottom_bar)
    View bottomBar;
    @BindView(R.id.web_page_control_bar)
    View webPageControlBackground;
    @BindView(R.id.add_web_page)
    Button addWebPage;
    @BindView(R.id.web_container)
    MyViewPager mViewPager;
    @BindView(R.id.web_layout)
    ScrollLayout webLayout;
    @BindView(R.id.dot_indicator)
    LinearLayout indicator;

    private MingWebView webView;
    private WebPageAdapter webpageAdapter;
    private MenuListAdapter menuAdapter;
    private long mExitTime;    //按下返回键退出时的时间
    private boolean first = true;  //有两种含义：第一次运行app时或标签页最后一页被删后需要重新定位当前WebView对象
    private boolean isZoom = false;  //是否缩放
    private SharedPreferences preferences;
    private int firstPosition = 0;
    private int selectMenuPosition = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_TransparentActivity);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setBackgroundDrawable(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        //网络状态变化广播监听
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChange, mFilter);

        //天气结果广播监听
        IntentFilter mFilter2 = new IntentFilter();
        mFilter2.addAction("weather_refresh");
        registerReceiver(refresh, mFilter2);

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, true);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        ButterKnife.bind(this);
        initView(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            initPermission();

        EventBus.getDefault().register(this);
    }

    @SuppressLint("SetTextI18n")
    protected void onResume() {
        super.onResume();

        now_temperature.setText(preferences.getString("wendu", "N/A"));
        city.setText(preferences.getString("cityName", "未知城市"));
        if (webView != null) {
            webView.getSettings().setTextZoom(Integer.valueOf(preferences.getString("text_size", "100")));
        }
        toolbar.setBackgroundColor(Color.parseColor(preferences.getString("theme_color", "#474747")));
        statusBar.setBackgroundColor(Color.parseColor(preferences.getString("theme_color", "#474747")));
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        String url;
        if (action != null)
            switch (action) {
                case Intent.ACTION_VIEW:
                case "com.zbm.dainty.action.VIEW":
                    url = intent.getDataString();
                    if (url != null) {
                        Log.d("Main", "onNewIntent地址Path：" + url);
                        webView.loadUrl(url);
                        return;
                    }
                    break;
                default:
                    url = intent.getStringExtra("shortcut_url");
                    webView.loadUrl(url);
            }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        List<Bundle> bundles = new ArrayList<>();
        for (WebViewFragment fragment : WebPageHelper.webpagelist) {
            Bundle save = new Bundle();
            WebView webView = fragment.getInnerWebView();
            if (webView != null)
                webView.saveState(save);
            bundles.add(save);

        }
        outState.putInt("web_page_count", WebPageHelper.webpagelist.size());
        outState.putParcelableArrayList("web_page_bundle", (ArrayList<? extends Parcelable>) bundles);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebPageHelper.webpagelist.clear();
        unregisterReceiver(networkChange);
        unregisterReceiver(refresh);
    }

    private void initPermission() {
        String[] permissions = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        Log.d("rer",toApplyList.size()+"个");
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 121);
        }else {
            startService(new Intent(this, WeatherService.class));
        }

    }

    private void initView(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            List<Bundle> bundles = savedInstanceState.getParcelableArrayList("web_page_bundle");
            int count = savedInstanceState.getInt("web_page_count");
            for (int i = 0; i < count; i++) {
                WebViewFragment fragment = new WebViewFragment(bundles != null ? bundles.get(i) : null, initWebView());
                WebPageHelper.webpagelist.add(fragment);
            }
            initDot(count);
        } else {
            String url = getIntent().getDataString();
            Log.d("Main", "onCreate地址Path：" + url);
            WebViewFragment fragment = new WebViewFragment(null, initWebView(), url);
            WebPageHelper.webpagelist.add(fragment);
            initDot(1);
        }

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        menuAdapter = new MenuListAdapter(this);
        listView.setAdapter(menuAdapter);

        mViewPager.setOnLayoutClickListener(new MyViewPager.OnLayoutClickListener() {
            @Override
            public void onLayoutClick() {
                ZoomChange(1);
            }
        });
        ((ViewGroup) mViewPager.getParent()).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mViewPager.dispatchTouchEvent(event);
            }
        });
        mViewPager.setPageMargin(MyUtil.dip2px(this, 45));
        webpageAdapter = new WebPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(webpageAdapter);

        mViewPager.setOffscreenPageLimit(5);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                indicator.getChildAt(firstPosition).setEnabled(false);
                indicator.getChildAt(position).setEnabled(true);
                firstPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                switch (selectMenuPosition) {
                    case -1:
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        break;
                    case 0:
                        webView.loadUrl("http://dushu.m.baidu.com");
                        break;
                    case 1:
                        webView.loadUrl("https://m.bilibili.com");
                        break;
                    case 2:
                        webView.loadUrl("http://m.xinhuanet.com");
                        break;
                    case 3:
                        //如果不是新的请求，getFavicon只能返回旧图，待修复
                        Intent intent = new Intent(MainActivity.this, CollectionEditActivity.class);
                        Bitmap icon = webView.getFavicon();
                        if (icon == null)
                            icon = BitmapFactory.decodeResource(getResources(), R.drawable.collection_icon_default);
                        intent.putExtra("icon", icon);
                        intent.putExtra("title", webView.getTitle());
                        intent.putExtra("url", webView.getUrl());
                        startActivity(intent);
                        break;
                    case 4:
                        startActivityForResult(new Intent(MainActivity.this, DownloadRecordActivity.class), 1);
                        break;
                    case 5:
                        startActivityForResult(new Intent(MainActivity.this, HistoryAndLabelActivity.class), 1);
                        break;
                    case 6:
                        startActivity(new Intent(MainActivity.this, ConfigActivity.class));
                        break;
                }
                selectMenuPosition = -2;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void initDot(int count) {
        indicator.removeAllViews();
        View view;
        for (int i = 0; i < count; i++) {
            //创建底部指示器(小圆点)
            view = new View(this);
            view.setBackgroundResource(R.drawable.dot_background);
            view.setEnabled(false);
            //设置宽高
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MyUtil.dip2px(this, 7), MyUtil.dip2px(this, 7));
            //设置间隔
            if (i != 0) {
                layoutParams.leftMargin = MyUtil.dip2px(this, 6);
            }
            //添加到LinearLayout
            indicator.addView(view, layoutParams);
        }
        Log.d("WP", "当前页：" + mViewPager.getCurrentItem());
        indicator.getChildAt(mViewPager.getCurrentItem()).setEnabled(true);
        firstPosition = mViewPager.getCurrentItem();
    }

    @OnItemClick(R.id.menu_list)
    public void onItemClick(int position) {
        mDrawerLayout.closeDrawer(Gravity.START);
        selectMenuPosition = position;
    }

    @OnClick({R.id.menu_button, R.id.query_button, R.id.web_back, R.id.web_next,
            R.id.web_freshen, R.id.web_stop_loading, R.id.web_multi, R.id.add_web_page,
            R.id.home_button, R.id.exit_button, R.id.head_portrait})
    public void onClick(View view) {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) mDrawerLayout.closeDrawers();
        int object = view.getId();
        switch (object) {
            case R.id.menu_button:
                mDrawerLayout.openDrawer(Gravity.START, true);
                break;
            case R.id.query_button:
                startActivityForResult(new Intent(this, QueryActivity.class), 1);
                break;
            case R.id.web_back:
                webView.goBack();
                break;
            case R.id.web_next:
                webView.goForward();
                break;
            case R.id.web_freshen:
                webView.reload();
                break;
            case R.id.web_stop_loading:
                webView.stopLoading();
                break;
            case R.id.web_multi:
                ZoomChange(0);
                break;
            case R.id.add_web_page:
                if (WebPageHelper.webpagelist.size() >= WebPageHelper.WEB_PAGE_LIMIT_NUM) {
                    Toast.makeText(this, "窗口数量超过最大值", Toast.LENGTH_SHORT).show();
                } else {
                    WebViewFragment fragment = new WebViewFragment(null, initWebView());
                    WebPageHelper.webpagelist.add(mViewPager.getCurrentItem() + 1, fragment);
                    webpageAdapter.notifyDataSetChanged();
                    initDot(WebPageHelper.webpagelist.size());
                    fixWebPage(mViewPager.getCurrentItem() + 1);
                    ZoomChange(1);

                }
                break;
            case R.id.home_button:
                webView.loadUrl("file:///android_asset/index.html");
                break;
            case R.id.exit_button:
                checkDownloadTask();
                break;
            case R.id.head_portrait:
                selectMenuPosition = -1;
                break;
            default:
                Toast.makeText(this, "开发中...", Toast.LENGTH_SHORT).show();
        }
    }

    private void ZoomChange(int flag) {
        //0为缩小，1为放大
        if (flag == 0) {
            isZoom = true;
            for (WebViewFragment webViewFragment : WebPageHelper.webpagelist) {
                webViewFragment.getInnerWebView().onPause();
                webViewFragment.getInnerWebView().pauseTimers();
            }
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            mViewPager.setFullScreen(false);

            webLayout.scrollTo(0, 0);


            mViewPager.clearAnimation();
            mViewPager.animate().scaleX(0.65f).scaleY(0.65f).setDuration(400).start();

            webPageControlBackground.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.INVISIBLE);
            bottomBar.setVisibility(View.INVISIBLE);
            indicator.setVisibility(View.VISIBLE);

        } else {
            //防止viewpager滑动错位
            fixWebPage(mViewPager.getCurrentItem());

            webView = WebPageHelper.webpagelist.get(mViewPager.getCurrentItem()).getInnerWebView(); //定位当前的WebView对象
            webView.setLayerType(View.LAYER_TYPE_NONE, null);
            isZoom = false;

            for (WebViewFragment webViewFragment : WebPageHelper.webpagelist) {
                if (WebPageHelper.webpagelist.get(mViewPager.getCurrentItem()).equals(webViewFragment)) {
                    webView.onResume();     //由于调用onResume会导致所有WebView都处于活动状态，而onPause只是针对单个
                    webView.resumeTimers();
                } else {
                    webViewFragment.getInnerWebView().onPause();
                    webViewFragment.getInnerWebView().pauseTimers();
                }
            }
            mViewPager.setFullScreen(true);

            mViewPager.clearAnimation();
            mViewPager.animate().scaleX(1f).scaleY(1f).setDuration(0).start();

            webPageControlBackground.setVisibility(View.INVISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
            indicator.setVisibility(View.INVISIBLE);


            //检测当前的webview对象是否可以向前或前后浏览
            if (!webView.canGoBack()) {
                backButton.setEnabled(false);
            } else {
                backButton.setEnabled(true);
            }
            if (!webView.canGoForward()) {
                nextButton.setEnabled(false);
            } else {
                nextButton.setEnabled(true);
            }

        }
    }

    private WebViewFragment.OnWebViewListener initWebView() {


        return new WebViewFragment.OnWebViewListener() {
            @Override
            public void onGetWebView(final MingWebView webView) {
                //调用代表为新添加的webview

                if (first) {
                    MainActivity.this.webView = webView;
                    first = false;
                }

                webView.setOnScrollChangedCallback(new MingWebView.OnScrollChangedCallback() {
                    @Override
                    public void onScroll(int dx, int dy) {
                        Log.d("ttt", "dy:" + dy);
                        webLayout.scrollBy(0, dy);
                    }
                });
                Log.d("Dainty", "调用getView:" + MainActivity.this.webView);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                //会加载几次
                if (!title.equals("") && !title.contains("https") && !title.contains("http")) {
                    insertTable(view.getUrl(), title);
                    Log.d("web_view", title + " " + view.getUrl());
                }
            }


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                webLayout.scrollTo(0, 0);
                progressBar.setVisibility(View.VISIBLE);
                backButton.setEnabled(false);
                nextButton.setEnabled(false);
                freshenButton.setVisibility(View.INVISIBLE);
                stopLoading.setVisibility(View.VISIBLE);
                menuAdapter.setAllowCollect(false);
                menuAdapter.isEnabled(3);
                menuAdapter.notifyDataSetInvalidated();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }

            @Override
            public void onProgressChanged(WebView webView, int i) {
                if (i > 80) {
                    //进度大于80，一般网页就加载完成了,但是为了能够在点击收藏标签前收到icon，必须等到加载完成到100
                    progressBar.setVisibility(View.GONE);
                    freshenButton.setVisibility(View.VISIBLE);
                    stopLoading.setVisibility(View.INVISIBLE);
                    if (!webView.canGoBack()) {
                        backButton.setEnabled(false);
                    } else {
                        backButton.setEnabled(true);
                    }
                    if (!webView.canGoForward()) {
                        nextButton.setEnabled(false);
                    } else {
                        nextButton.setEnabled(true);
                    }
                    menuAdapter.setAllowCollect(true);
                    menuAdapter.isEnabled(3);
                    menuAdapter.notifyDataSetInvalidated();

                } else {
                    progressBar.setProgress(i);
                }
            }

            @Override
            public void onQuickActionClick(WebView webView, int itemId, String extra) {
                switch (itemId) {
                    case WebViewFragment.LOAD_IN_NEW_WINDOW:
                        if (WebPageHelper.webpagelist.size() >= WebPageHelper.WEB_PAGE_LIMIT_NUM) {
                            Toast.makeText(MainActivity.this, "窗口数量超过最大值", Toast.LENGTH_SHORT).show();
                        } else {
                            WebViewFragment fragment = new WebViewFragment(null, initWebView(), extra);
                            WebPageHelper.webpagelist.add(mViewPager.getCurrentItem() + 1, fragment);
                            webpageAdapter.notifyDataSetChanged();
                            initDot(WebPageHelper.webpagelist.size());
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
                        }
                        break;
                    case WebViewFragment.LOAD_IN_BACKGROUND:
                        if (WebPageHelper.webpagelist.size() >= WebPageHelper.WEB_PAGE_LIMIT_NUM) {
                            Toast.makeText(MainActivity.this, "窗口数量超过最大值", Toast.LENGTH_SHORT).show();
                        } else {
                            WebViewFragment fragment = new WebViewFragment(null, initWebView(), extra);
                            WebPageHelper.webpagelist.add(mViewPager.getCurrentItem() + 1, fragment);
                            webpageAdapter.notifyDataSetChanged();
                            initDot(WebPageHelper.webpagelist.size());
                        }
                        break;
                    case WebViewFragment.FREE_REPLICATION:
                        break;
                    case WebViewFragment.COPY_LINK:
                        break;
                    case WebViewFragment.DOWNLOAD_IMAGE:
                        new ImageTask(MainActivity.this).execute(extra);
                        break;
                }
            }
        };
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        //删除动画
        int viewTop = event.getViewTop();
        int value;
        if (viewTop > 0) {
            value = 2500;
        } else {
            value = -2500;
        }
        View selectedView = WebPageHelper.webpagelist.get(mViewPager.getCurrentItem()).getInnerContainer();
        Animation animation = new TranslateAnimation(0, 0, viewTop, value);
        animation.setDuration(400);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                WebPageHelper.webpagelist.get(mViewPager.getCurrentItem()).getInnerContainer().removeAllViews();
                WebPageHelper.webpagelist.get(mViewPager.getCurrentItem()).getInnerWebView().destroy();
                WebPageHelper.webpagelist.remove(mViewPager.getCurrentItem());
                webpageAdapter.notifyDataSetChanged();
                if (WebPageHelper.webpagelist.size() == 0) {
                    first = true;
                    WebViewFragment fragment = new WebViewFragment(null, initWebView());
                    WebPageHelper.webpagelist.add(fragment);
                    webpageAdapter.notifyDataSetChanged();
                    ZoomChange(1);
                }
                initDot(WebPageHelper.webpagelist.size());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        selectedView.startAnimation(animation);

    }

    private void fixWebPage(int position) {
        webpageAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(position, true);
    }

    public void onBackPressed() {
        if (isZoom) {
            ZoomChange(1);
        } else {
            if (!webView.canGoBack()) {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(this, "再按一次退出浏览器", Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    checkDownloadTask();
                }
            } else {
                webView.goBack();
            }
        }
    }

    private void insertTable(final String url, final String title) {
        DaintyDBHelper.getDaintyDBHelper(this).updateHistoryTable(this, url, title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    if (!webView.getUrl().equals(data.getStringExtra("currentUri"))) {
                        webView.loadUrl(data.getStringExtra("currentUri"));
                    }

                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    headPortraitView.setImageBitmap((Bitmap) data.getParcelableExtra("touxiang"));
                }
                break;
        }
    }

    private BroadcastReceiver networkChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connectivityManager != null;
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                String name = info.getTypeName();
                Log.d("mark", "当前网络名称：" + name);
                startService(new Intent(context, WeatherService.class));
                if (webView != null)
                    webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            } else {
                if (webView != null)
                    webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            }

        }
    };
    private BroadcastReceiver refresh = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            now_temperature.setText(preferences.getString("wendu", ""));
            city.setText(preferences.getString("cityName", " "));
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 121) {
            boolean isAllGranted = true;
            Log.d("rer", "调用onRequestPermissionsResult");
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
                Log.d("rer", "grant:" + grant);

            }
            Log.d("rer", "isGrant:" + isAllGranted);
            if (!isAllGranted) {
                // 如果用户拒绝授权，则弹出对话框让用户自行设置
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("警告");
                builder.setMessage("当前应用缺少必要权限，请点击“设置”开启权限或点击“取消”关闭应用。");
                builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.fromParts("package", MainActivity.this.getPackageName(), null));
                        MainActivity.this.startActivity(intent);
                    }
                }).
                        setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                MainActivity.this.finish();
                            }
                        });

                AlertDialog dialog = builder.show();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
            }else
                startService(new Intent(this, WeatherService.class));
        }
    }

    private void checkDownloadTask() {
        if (DownloadHelper.downloadList.size() > 0) {
            AlertDialog.Builder normalDialog =
                    new AlertDialog.Builder(this);
            normalDialog.setIcon(android.R.drawable.ic_menu_info_details)
                    .setTitle("退出提示")
                    .setMessage("有下载任务正在进行，退出浏览器将删除临时下载文件，仍要退出？")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (DownloaderTask task : DownloadHelper.downloadList) {
                                        task.cancel(true);
                                        new File(task.getFilePath()).delete();
                                    }
                                    DownloadHelper.downloadList.clear();
                                    MainActivity.super.onBackPressed();
                                }
                            })
                    .setNegativeButton("取消", null).show();
        } else {
            super.onBackPressed();
        }
    }

}
