package com.zbm.dainty.main;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.zbm.dainty.DaintyApplication;
import com.zbm.dainty.R;
import com.zbm.dainty.adapter.PopupMenuListAdapter;
import com.zbm.dainty.bean.WebFragmentLoadBean;
import com.zbm.dainty.task.ImageTask;
import com.zbm.dainty.task.ResolveDownloadUrlTask;
import com.zbm.dainty.util.DaintyDBHelper;
import com.zbm.dainty.util.MyUtil;
import com.zbm.dainty.widget.MingWebView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


public class WebViewFragment extends android.support.v4.app.Fragment{
    public final static int LOAD_IN_NEW_WINDOW = 0;
    public final static int LOAD_IN_BACKGROUND = 1;

    private Bundle bundle;
    private MingWebView webView;
    private View cache;
    private FrameLayout webViewContainer;
    private int touchPointX, touchPointY, mHeight, mWidth;
    private PopupWindow quickAction;
    private RecyclerView popupMenuList;
    private String extra, url="file:///android_asset/index.html";

    public WebViewFragment() { }

    @SuppressLint("ValidFragment")
    public WebViewFragment(Bundle savedInstanceState) {
        this(savedInstanceState,null);
    }

    @SuppressLint("ValidFragment")
    public WebViewFragment(Bundle savedInstanceState, String url) {
        bundle = savedInstanceState;  //为空表示是用户手动添加标签页
        if (url!=null)
            this.url = url;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (cache == null) {
            cache = inflater.inflate(R.layout.webview_fragment, container, false);
            webView = cache.findViewById(R.id.web_view);   //TBS WebView必须在布局中创建，否则网页视频无法全屏
            WebSettings setting = webView.getSettings();
            setSettings(setting);
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    if (!title.equals("") && !title.contains("https") && !title.contains("http")) {
                        DaintyDBHelper.getDaintyDBHelper(getActivity()).updateHistoryTable(view.getUrl(), title);
                    }
                    super.onReceivedTitle(view, title);
                }


                @Override
                public void onProgressChanged(WebView webView, int i) {
                    super.onProgressChanged(webView, i);
                    EventBus.getDefault().post(i);
                }
            });

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // TODO Auto-generated method stub
                    if (!URLUtil.isValidUrl(url))
                        return true;    //某些网站会重定向至非有效网址，返回真取消加载该网址
                    //只有加载重定向网页才会调用
                    Log.d("appo", "shouldOverride:" + url);
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    blockAds(view);//过滤
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    EventBus.getDefault().post(url);
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                }
            });
            View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    WebView.HitTestResult result = webView.getHitTestResult();
                    int resultType = result.getType();
                    extra = result.getExtra();
                    Log.d("Ming", resultType + "");
                    boolean showCustomPopup=false;

                    switch (resultType) {
                        case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                            initLoadingWebQuickAction();
                            showCustomPopup=true;
                            break;
                        case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                        case WebView.HitTestResult.IMAGE_TYPE:
                            initImageQuickAction();
                            showCustomPopup=true;
                            break;
                    }
                    if (showCustomPopup){
                        Point point=new Point();
                        getActivity().getWindowManager().getDefaultDisplay().getSize(point);
                        int xOff = (touchPointX + mWidth)>point.x?touchPointX-mWidth:touchPointX;
                        int yOff = (touchPointY + mHeight) > point.y ? touchPointY - mHeight : touchPointY;
                        quickAction.showAtLocation(v, Gravity.TOP|Gravity.START, xOff,yOff);
                        return true;
                    }
                    return false;
                }
            };

            webView.setOnLongClickListener(onLongClickListener);
            webView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    touchPointX = (int) event.getRawX();
                    touchPointY = (int) event.getRawY();
                    return false;
                }
            });

            webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getActivity(), "请检查手机SD卡", Toast.LENGTH_SHORT).show();
                    } else {
                        new ResolveDownloadUrlTask(getActivity(), cache).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s);
                    }
                }
            });
            if (bundle != null)
                webView.restoreState(bundle);
            else if (savedInstanceState != null)
                webView.restoreState(savedInstanceState);
            else
                webView.loadUrl(url);

            EventBus.getDefault().post(webView); //新添加的fragment
            webViewContainer = cache.findViewById(R.id.frame_layout);

        }
        return cache;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DaintyApplication.getRefWatcher(getActivity()).watch(this);
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void setSettings(WebSettings setting) {
        //noinspection deprecation
        setting.setJavaScriptEnabled(true);
        setting.setJavaScriptCanOpenWindowsAutomatically(true);
        setting.setAllowFileAccess(true);
        setting.setSupportZoom(true);
        setting.setBuiltInZoomControls(true);   //允许放大缩小
        setting.setDisplayZoomControls(false);    //去掉放大缩小框
        setting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        setting.setSupportMultipleWindows(false);

        setting.setGeolocationEnabled(true);   //允许启用地理定位
        setting.setGeolocationDatabasePath(getActivity().getDir("geolocation", 0).getPath());
        setting.setSaveFormData(true);  //支持保存自动填充的表单数据
        setting.setDomStorageEnabled(true);  //支持DOM缓存
        setting.setDatabaseEnabled(true);
        setting.setAppCacheEnabled(true);
        setting.setAppCacheMaxSize(Long.MAX_VALUE);
        setting.setAppCachePath(getActivity().getDir("dainty_cache", 0).getPath());

        // 全屏显示
        setting.setUseWideViewPort(true);
        setting.setTextZoom(Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("text_size", "100")));

    }

    private void blockAds(WebView view) {
        String tags = view.getUrl();
        //noinspection MismatchedQueryAndUpdateOfStringBuilder
        StringBuilder sb = new StringBuilder();
        sb.append("javascript: ");
        String[] allTag = tags.split(",");
        for (String tag : allTag) {
            String adTag = tag;
            if (adTag.trim().length() > 0) {
                adTag = adTag.trim();
                if (adTag.contains("#")) {
                    adTag = adTag.substring(adTag.indexOf("#") + 1);
                    sb.append("document.getElementById(\'").append(adTag).append("\').remove();");

                } else if (adTag.contains(".")) {
                    adTag = adTag.substring(adTag.indexOf(".") + 1);
                    sb.append("var esc=document.getElementsByClassName(\'").append(adTag).append("\');for (var i = esc.length - 1; i >= 0; i--){esc[i].remove();};");

                } else {
                    sb.append("var esc=document.getElementsByTagName(\'").append(adTag).append("\');for (var i = esc.length - 1; i >= 0; i--){esc[i].remove();};");
                }
            }
        }
    }

    public MingWebView getInnerWebView() {
        return webView;
    }

    public FrameLayout getInnerContainer() {
        return webViewContainer;
    }


    private void initLoadingWebQuickAction() {
        @SuppressLint("InflateParams")
        View pop_layout = LayoutInflater.from(getActivity()).inflate(R.layout.popup_menu, null);
        List<String> data=new ArrayList<>();
        data.add("新窗口打开");
        data.add("后台打开");
        data.add("复制链接");
        popupMenuList=pop_layout.findViewById(R.id.popup_menu_list);
        PopupMenuListAdapter adapter=new PopupMenuListAdapter(getActivity(),data);
        adapter.setOnItemClickListener(new PopupMenuListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                quickAction.dismiss();
                switch (position){
                    case 0:
                        EventBus.getDefault().post(new WebFragmentLoadBean(LOAD_IN_NEW_WINDOW,extra));
                        break;
                    case 1:
                        EventBus.getDefault().post(new WebFragmentLoadBean(LOAD_IN_BACKGROUND,extra));
                        break;
                    case 2:
                        copyToClipboard();
                        break;
                }
            }
        });
        popupMenuList.setAdapter(adapter);
        quickAction = new PopupWindow(MyUtil.dip2px(getActivity(), 150), ViewGroup.LayoutParams.WRAP_CONTENT);
        quickAction.setContentView(pop_layout);
        quickAction.setFocusable(true);
        quickAction.setOutsideTouchable(true);
        quickAction.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        pop_layout.measure(w, h);
        //获取PopWindow宽和高
        mHeight = pop_layout.getMeasuredHeight();
        mWidth = pop_layout.getMeasuredWidth();
    }

    private void initImageQuickAction() {
        @SuppressLint("InflateParams")
        View pop_layout = LayoutInflater.from(getActivity()).inflate(R.layout.popup_menu, null);
        List<String> data=new ArrayList<>();
        data.add("保存图片");
        data.add("复制链接");
        popupMenuList=pop_layout.findViewById(R.id.popup_menu_list);
        PopupMenuListAdapter adapter=new PopupMenuListAdapter(getActivity(),data);
        adapter.setOnItemClickListener(new PopupMenuListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                quickAction.dismiss();
                switch (position){
                    case 0:
                        new ImageTask(getContext()).execute(extra);
                        break;
                    case 1:
                        copyToClipboard();
                        break;
                }
            }
        });
        popupMenuList.setAdapter(adapter);
        quickAction = new PopupWindow(MyUtil.dip2px(getActivity(), 150), ViewGroup.LayoutParams.WRAP_CONTENT);
        quickAction.setContentView(pop_layout);
        quickAction.setFocusable(true);
        quickAction.setOutsideTouchable(true);
        quickAction.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        pop_layout.measure(w, h);
        //获取PopWindow宽和高
        mHeight = pop_layout.getMeasuredHeight();
        mWidth = pop_layout.getMeasuredWidth();
    }

    private void copyToClipboard(){
        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("copy_link", extra);
        if (cm!=null) {
            cm.setPrimaryClip(mClipData);
        }else {
            Toast.makeText(getContext(),"不支持复制功能",Toast.LENGTH_SHORT).show();
        }
    }
}

