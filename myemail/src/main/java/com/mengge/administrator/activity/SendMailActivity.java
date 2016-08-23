package com.mengge.administrator.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mengge.administrator.myemail.Mail;
import com.mengge.administrator.myemail.MailConfig;
import com.mengge.administrator.myemail.R;
import com.mengge.administrator.myemail.SendOneEmail;
import com.mengge.administrator.utils.EmailUtil;
import com.mengge.administrator.utils.Util;

import java.io.File;
import java.util.ArrayList;

public class SendMailActivity extends AppCompatActivity implements Mail.SendEmailListener {

    EditText inputContent, inputTo, inputCC, inputBcc, inputAttachmentPath;
    Mail mMail;
    TextView setSDcardPath;
    public static void toActivity(Activity activity){
        Intent intent = new Intent(activity, SendMailActivity.class);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_mail);

        inputContent =  Util.f(this, R.id.inputEmailContent);

        inputTo = Util.f(this, R.id.inputTo);

        inputCC = Util.f(this, R.id.inputCC);

        inputBcc = Util.f(this, R.id.inputBcc);

        inputAttachmentPath = Util.f(this, R.id.inputAttachmentPath);
        setSDcardPath = Util.f(this, R.id.setSDcardPath);

        setSDcardPath.setText(EmailUtil.getSDcardPath() + "");
    }



    public void sendEmail(View view){
        String toEmail = inputTo.getText().toString().trim();
        String cc = inputCC.getText().toString().trim();
        String bcc = inputBcc.getText().toString().trim();
        String sendPerson = "chaimengge@ultrapower.com.cn";
        String emailContent = inputContent.getText().toString();

        if (toEmail.length() == 0){
            Toast.makeText(this, "请输入收件人", Toast.LENGTH_SHORT).show();

            return;
        }

        if (!EmailUtil.isEmail(toEmail)){
            Toast.makeText(this, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        String filePath = setSDcardPath + inputAttachmentPath.getText().toString().trim().toLowerCase();
        File file = new File(filePath);
        SendOneEmail sendOneEmail = new SendOneEmail();
        sendOneEmail.setEmailContent(emailContent);
        sendOneEmail.setSendPerson(sendPerson);
        sendOneEmail.setSubject("手机端发邮件测试");
        sendOneEmail.addReceivePerson(toEmail);

        if (file.isFile()){
            sendOneEmail.addAttachment(file);
        }

        if (cc.length() != 0){

            sendOneEmail.addCC(cc);
        }

        if (bcc.length() != 0){

            sendOneEmail.addBCCEmailAdd(bcc);
        }

        if (mMail == null){
            mMail = new Mail(this);
        }
        mMail.setOnSendEmailListener(this);
        mMail.setSendOneMail(sendOneEmail);
        if (file.isFile()){
            mMail.setMailType(MailConfig.emailType.AttACHMENT);
        }else{
            mMail.setMailType(MailConfig.emailType.NORMAL);
        }

        mMail.sendMail(mMail.getMailType());

    }

    @Override
    public void onSendEmailComplete(ArrayList<Object> dataOrException) {
        if (dataOrException.size() > 0){
            //有异常，否则为空
            String exception = (String) dataOrException.get(0);
            Toast.makeText(this, "出错啦" + exception, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "已发送成功", Toast.LENGTH_SHORT).show();
        }
    }
}
