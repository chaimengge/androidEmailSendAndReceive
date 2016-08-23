package com.mengge.administrator.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mengge.administrator.myemail.R;

/**
 * 收发邮件入口
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //收邮件
    public void receiveEmail(View view){
        EmailListActivity.toActivity(this);
    }

    public void toSendEmail(View view){
        SendMailActivity.toActivity(this);
    }

    public void getFilePath(View view){

    }


}
