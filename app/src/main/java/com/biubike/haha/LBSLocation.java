package com.biubike.haha;

import android.location.Location;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.biubike.application.DemoApplication;
public class LBSLocation {

	private static LBSLocation location = null;
	private static DemoApplication app = null;

	private MyLocationListenner myListener = new MyLocationListenner();
	public LocationClient mLocationClient = null;

	public static LBSLocation getInstance(DemoApplication application) {
		app = application;
		if (location == null) {
			location = new LBSLocation(app);
		}

		return location;
	}

	private LBSLocation(DemoApplication app) {
		mLocationClient = new LocationClient(app);
		mLocationClient.registerLocationListener(myListener);
		mLocationClient.start();
	}


	public void startLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("all");
		option.setCoorType("bd09ll");
		option.disableCache(true);
		mLocationClient.setLocOption(option);
		mLocationClient.requestLocation();
	}

	public class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			app.currlocation = location;
			mLocationClient.stop();

			}
		}

		public void onReceivePoi(BDLocation poiLocation) {

		}
	}

