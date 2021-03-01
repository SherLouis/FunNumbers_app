package com.llightning.funnumbers;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    WebView wv_about;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        wv_about = findViewById(R.id.wv_about);
        wv_about.setWebChromeClient(new WebChromeClient());
        wv_about.loadUrl("file:///android_res/raw/about.html");
    }
}