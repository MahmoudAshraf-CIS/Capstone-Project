package com.example.mannas.capstone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Mannas on 8/29/2017.
 */

public class WebViewActivity extends AppCompatActivity {
    String url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view_acticity);
        setTitle(getResources().getString(R.string.openLibrary));
        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebViewClient(new WebViewClient());

        myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        url = getIntent().getExtras().getString("url");
        myWebView.loadUrl(url);

    }
}
