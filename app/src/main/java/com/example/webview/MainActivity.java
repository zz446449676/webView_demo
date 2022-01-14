package com.example.webview;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private WebView mWebView;
    private ProgressBar progressBar;
    private TextView titleText;
    private ImageView backImg,searchImg,refreshImg;
    private EditText editText;
    private String defaultURL = "https://m.wingontravel.com/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = this.findViewById(R.id.progressBar);
        titleText = this.findViewById(R.id.titleText);
        backImg = this.findViewById(R.id.backImg);
        mWebView = this.findViewById(R.id.mWebView);
        searchImg = this.findViewById(R.id.searchImg);
        refreshImg = this.findViewById(R.id.refreshImg);
        editText = this.findViewById(R.id.editText);
        initWebView();
        initListener();
        mWebView.loadUrl(defaultURL);
    }

    private void initListener() {
        backImg.setOnClickListener(this);
        searchImg.setOnClickListener(this);
        refreshImg.setOnClickListener(this);
        mWebView.setOnTouchListener(this);
    }

    private void initWebView() {
        WebSettings mWebSettings = mWebView.getSettings();
        mWebSettings.setSupportZoom(false);
        mWebSettings.setBuiltInZoomControls(false);
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setDefaultTextEncodingName("utf-8");
        mWebSettings.setLoadsImagesAutomatically(true);
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebSettings.setDomStorageEnabled(true);
        saveData(mWebSettings);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d("zhang", "onPageStarted enter url : " + url);
                titleText.setText("加载中......");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("zhang", "shouldOverrideUrlLoading enter url: " + url);
                try {
                    if (url.startsWith("http:") || url.startsWith("https:")) {
                        Log.d("zhang", "load http ....");
                        view.loadUrl(url);
                        return super.shouldOverrideUrlLoading(view, url);
                    } else {
                        Log.d("zhang", "load unknown scheme ....");
                        if (url.startsWith("intent:")) {
                            url = url.replaceFirst("intent:","wingonwireless:");
                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return super.shouldOverrideUrlLoading(view, url);
                    }
                } catch (Exception e) {
                    Log.d("zhang", "Exception : " + e);
                    return false;
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("zhang", "onPageFinished enter url : " + url);
                //清空输入框并失去焦点
                titleText.setText("永安Chrome");
                editText.setText("");
                editText.clearFocus();
                super.onPageFinished(view, url);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            //当网页加载进度改变时会回调onProgressChanged方法，在这里更新用于显示进度的ProgressBar。注意newProgress值为 0 到 100 ，因而将ProgressBar最大值设为 100 。
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                refreshImg.setVisibility(View.GONE);
                if (newProgress >= 100) {
                    progressBar.setProgress(0);
                    progressBar.setVisibility(View.GONE);
                    refreshImg.setVisibility(View.VISIBLE);
                } else if (progressBar.getVisibility() == View.GONE) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                progressBar.setProgress(newProgress);
            }

            @Override
            //onReceivedTitle返回当前加载网页的标题将标题显示在 actionBar 内的 EditText 上。
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            handleBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * HTML5数据存储
     */
    private void saveData(WebSettings mWebSettings) {
        //有时候网页需要自己保存一些关键数据,Android WebView 需要自己设置
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setDatabaseEnabled(true);
        mWebSettings.setAppCacheEnabled(true);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        mWebSettings.setAppCachePath(appCachePath);
    }

    private void handleBack() {
        if (mWebView != null && mWebView.canGoBack()) {
            String url = mWebView.getUrl();
            mWebView.goBack();
            if (mWebView.getUrl().equals(url)) {
                mWebView.goBack();
            }
        } else {
            finish();
        }
    }


    @SuppressLint("WrongConstant")
    private void handleSearch() {
        String url = editText.getText().toString();
        if (!emptyOrNull(url)) {
            mWebView.loadUrl(url);
        } else {
            Toast.makeText(this, "输入的内容不允许为空", 2000).show();
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backImg:
                Log.d("zhang", "click back");
                handleBack();
                break;

            case R.id.searchImg:
                Log.d("zhang", "click search");
                handleSearch();
                break;

            case R.id.refreshImg:
                Log.d("zhang", "click refresh");
                mWebView.loadUrl(defaultURL);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
                }
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
            }
        }
        return false;
    }

    public boolean emptyOrNull(String str) {
        return str == null || str.length() == 0;
    }

}