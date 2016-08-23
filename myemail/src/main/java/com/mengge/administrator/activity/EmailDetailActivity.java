package com.mengge.administrator.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mengge.administrator.myemail.R;
import com.mengge.administrator.utils.Util;

/**
 * 邮件详情
 */
public class EmailDetailActivity extends AppCompatActivity {


    public static void toActivity(Activity activity, String emailContent){
        Intent intent = new Intent(activity, EmailDetailActivity.class);
        intent.putExtra("emailContent", emailContent);
        activity.startActivity(intent);
    }

    WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_detail);
        mWebView = Util.f(this, R.id.loadEmailContent);
        setWebView();
        loadContent();
    }


    void loadContent(){
        Intent intent = getIntent();

        if (intent != null){
            String emailContent = intent.getStringExtra("emailContent");
            mWebView.loadDataWithBaseURL(null, emailContent, "text/html", "UTF-8", null);
        }
    }
    void setWebView(){
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        //适应屏幕
        settings.setSupportZoom(true);

        settings.setBlockNetworkImage(false);
        mWebView.setWebChromeClient(new ChromeClient());
        mWebView.setWebViewClient(new WebClient());
    }


    class ChromeClient extends WebChromeClient {

    }

    class WebClient extends WebViewClient {

    }

}
