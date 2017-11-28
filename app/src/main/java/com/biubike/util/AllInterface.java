package com.biubike.util;

/**
 * Created by ls on 2017/11/22.
 */

public class AllInterface {

    public  interface OnMenuSlideListener{
        void onMenuSlide(float offset);
    }
    public  interface IUnlock{
        void onUnlock();
    }
    public  interface IUpdateLocation{
        void updateLocation(String totalTime,String totalDistance);
        void endLocation();
    }
}
