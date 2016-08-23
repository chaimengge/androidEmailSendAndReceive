package com.mengge.administrator.myemail;


import android.content.Context;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Created by Administrator on 2016/8/9.
 */
public class EmailAuthenticator extends Authenticator {


    String userName = "chaimengge@ultrapower.com.cn";
    String password = "xxxxx";
    public EmailAuthenticator(){

    }
    public EmailAuthenticator(Context c){
        userName = MailConfig.getEmailAccount(c) == null ? "chaimengge":MailConfig.getEmailAccount(c);
        password = MailConfig.getEmailAccount(c) == null ? "xxxxxx":MailConfig.getEmailPassword(c);
    }
    public EmailAuthenticator(String username, String password) {
        this.userName = username;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication(){
        return new PasswordAuthentication(userName, password);
    }
}
