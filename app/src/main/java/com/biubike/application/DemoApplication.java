package com.biubike.application;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.biubike.haha.LBSLocation;

import java.util.HashMap;
public class DemoApplication extends Application {
    private static DemoApplication mInstance = null;
    //云检索参数
    private HashMap<String, String> filterParams;
    // 定位结果
    public BDLocation currlocation = null;
    public static String networkType;
    private Handler handler;
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        SDKInitializer.initialize(getApplicationContext());
        networkType = setNetworkType();
        // 启动定位
        LBSLocation.getInstance(this).startLocation();
    }
    public HashMap<String, String> getFilterParams() {
        return filterParams;
    }

    public void setFilterParams(HashMap<String, String> filterParams) {
        this.filterParams = filterParams;
    }

    public static DemoApplication getInstance() {
        return mInstance;
    }
    public void setHandler(Handler handler) {
        this.handler = handler;
    }
    /**
     * 设置手机网络类型，wifi，cmwap，ctwap，用于联网参数选择这个必须要的
     * @return
     */
    static String setNetworkType() {
        String networkType = "wifi";
        ConnectivityManager manager = (ConnectivityManager) mInstance
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netWrokInfo = manager.getActiveNetworkInfo();
        if (netWrokInfo == null || !netWrokInfo.isAvailable()) {
            // 当前网络不可用
            return "";
        }

        String info = netWrokInfo.getExtraInfo();
        if ((info != null)
                && ((info.trim().toLowerCase().equals("cmwap"))
                || (info.trim().toLowerCase().equals("uniwap"))
                || (info.trim().toLowerCase().equals("3gwap")) || (info
                .trim().toLowerCase().equals("ctwap")))) {
            // 上网方式为wap
            if (info.trim().toLowerCase().equals("ctwap")) {
                // 电信
                networkType = "ctwap";
            } else {
                networkType = "cmwap";
            }

        }
        return networkType;
    }
}
