package com.mengge.administrator.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;

/**
 * Created by Administrator on 2016/8/12.
 */
public class Util {

    public static  <T extends View> T f(Activity c, int resId){
        return (T) c.findViewById(resId);
    }

    /**
     * 判断网络是否有效
     * @param c
     * @return
     */
    public static boolean isNetAvailable(Context c){

        ConnectivityManager manager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo [] networkInfos =  manager.getAllNetworkInfo();
        for (NetworkInfo network:networkInfos) {
            if (network.getState() == NetworkInfo.State.CONNECTED){
                return  true;
            }
        }
        return  false;
    }

}
