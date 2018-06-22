package com.zbm.dainty.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.zbm.dainty.R;
import com.zbm.dainty.task.ResolveDownloadUrlTask;
import com.zbm.dainty.widget.MingWebView;


public class WebViewFragment extends android.support.v4.app.Fragment {
    private Bundle bundle;
    private OnWebViewListener wl;
    private MingWebView webView;
    private View cache;
    private FrameLayout webViewContainer;
    public WebViewFragment() {
    }

    @SuppressLint("ValidFragment")
    public WebViewFragment(Bundle savedInstanceState,OnWebViewListener onWebViewListener) {
        bundle=savedInstanceState;  //为空表示是用户手动添加标签页
        this.wl = onWebViewListener;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
        Log.d("WP","Fragment onSavedInstance");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("WP","调用onCreateView cache :"+cache);
        if (cache == null) {
            cache = inflater.inflate(R.layout.webview_fragment, container, false);
            webView = cache.findViewById(R.id.web_view);   //TBS WebView必须在布局中创建，否则网页视频无法全屏
            WebSettings setting = webView.getSettings();
            setSettings(setting);
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    if (wl!=null)wl.onReceivedTitle(view, title);
                    super.onReceivedTitle(view, title);
                }


                @Override
                public void onProgressChanged(WebView webView, int i) {
                    if (wl!=null)wl.onProgressChanged(webView, i);
                    super.onProgressChanged(webView, i);
                }
            });
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // TODO Auto-generated method stub
                    if (!URLUtil.isValidUrl(url))
                        return true;    //某些网站会重定向至非有效网址，返回真取消加载该网址
                    //只有加载重定向网页才会调用
                    Log.d("appo", "shouldOverride:"+url);
                    return url.equals(webView.getUrl());
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    blockAds(view);//过滤
                    Log.d("appo", "onPageFinished" + url);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Log.d("appo", "onPageStarted" + url);
                    if (wl!=null)wl.onPageStarted(view, url, favicon);
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    if (wl!=null)wl.onReceivedError(view, errorCode, description, failingUrl);
                    super.onReceivedError(view, errorCode, description, failingUrl);
                }
            });
            webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getActivity(), "请检查手机SD卡", Toast.LENGTH_SHORT).show();
                    } else {
                        new ResolveDownloadUrlTask(getActivity(), cache).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,s);
                    }
                }
            });
            if (bundle!=null)
                webView.restoreState(bundle);
            else if (savedInstanceState!=null)
                webView.restoreState(savedInstanceState);
            else
                webView.loadUrl("file:///android_asset/index.html");

            if (wl != null)
                wl.onGetWebView(webView);  //新添加的fragment
            webViewContainer= cache.findViewById(R.id.frame_layout);

        }
        return cache;
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.resumeTimers();
    }

    @Override
    public void onPause() {
        super.onPause();
        //暂停播放
        webView.pauseTimers();
    }

    @Override
    public void onDestroy() {
//        webView.stopLoading();
//        webView.removeAllViews();
//        webView.destroy();
//        webViewContainer.removeAllViews();
//        cache=null;
        Log.d("WP","Fragment onDestroy");
        super.onDestroy();
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void setSettings(WebSettings setting) {
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

    public interface OnWebViewListener {
        void onGetWebView(MingWebView webView);

        void onReceivedTitle(WebView view, String title);

        void onPageStarted(WebView view, String url, Bitmap favicon);

        void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

        void onProgressChanged(WebView webView, int i);
    }

    public MingWebView getInnerWebView() {
        return webView;
    }

    public FrameLayout getInnerContainer(){
        return webViewContainer;
    }
}

