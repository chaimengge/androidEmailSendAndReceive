package com.mengge.administrator.myemail;

import android.content.Context;

/**
 * Created by Administrator on 2016/8/12.
 * 邮箱配置
 */
public class MailConfig {
    //发送邮件服务器 you can define yours
    public static final String SEND_SERVER =  "smtp.ultrapower.com.cn";
    //接收邮件服务器， you can define yours
    public static final String RECEIVE_SERVER = "mail.ultrapower.com.cn";


    public static final String POP3 = "pop3";
    public static final String SMTP = "smtp";



    private static final String SP_NAME = MailConfig.class.getSimpleName();

    /**
     * 邮件类型， 标准和带附件的
     */
    public enum emailType{
        NORMAL, AttACHMENT
    }

    /**
     * save your email sign up account
     * @param c
     * @param emailAccount
     */
    public static void setEmailAccount(Context c, String emailAccount)
    {
        c.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit().putString("EmailAccount", emailAccount).commit();
    }

    public static String getEmailAccount(Context c){
        return  c.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).getString("EmailAccount", null);
    }

    public static void  setEmailPassword(Context c,String emailPassword){
        c.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit().putString("EmailAccount", emailPassword).commit();
    }

    public static String getEmailPassword(Context c){
        return  c.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).getString("EmailPassword", null);
    }


    public static void clearEmailConfig(Context c){
        c.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit().clear().commit();
    }
}
