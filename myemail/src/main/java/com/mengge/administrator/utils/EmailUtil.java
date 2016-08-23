package com.mengge.administrator.utils;

import android.os.Environment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/8/18.
 */
public class EmailUtil {


    /**
     * 内部判断是否是正确的邮件地址
     * @param emailAdd
     * @return
     */
    public static  boolean isEmail(String emailAdd) {

        if (emailAdd == null || emailAdd.length() == 0) {
            return false;
        }

        String parttenString = "^[a-zA-Z0-9]+([\\_|\\-|\\.]?[a-zA-Z0-9])*\\@[a-zA-Z0-9]+([\\_|\\-|\\.]?[a-zA-Z0-9])*\\.[a-zA-Z]{2,3}$";


        Pattern pattern = Pattern.compile(parttenString);
        Matcher matcher = pattern.matcher(emailAdd);

        return matcher.matches();
    }


    public static  boolean isHaveSDCard(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    public static String getSDcardPath(){
        return  Environment.getExternalStorageDirectory().getPath();
    }
}
