package com.biubike;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.biubike.application.DemoApplication;
import com.biubike.bean.MyInformation;
import com.biubike.haha.LBSCloudSearch;
import com.biubike.util.OverlayManager;
import com.biubike.util.LocationManager;
import com.biubike.util.MyOrientationListener;
import com.biubike.util.Utils;
import com.google.gson.Gson;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LBSActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;
    public int statusBarHeight = 0, titleHeight;

    public static final int MSG_NET_TIMEOUT = 100;
    public static final int MSG_NET_STATUS_ERROR = 200;
    public static final int MSG_NET_SUCC = 1;

    private boolean initSearchFlag = false;

    //定位：
    public MyLocationListenner myListener = new MyLocationListenner();
    private float mCurrentX;
    boolean useDefaultIcon = true, hasPlanRoute = false, isServiceLive = false;
    //自定义图标
    private BitmapDescriptor mIconLocation, dragLocationIcon, bikeIcon, nearestIcon;
    /*
     * 处理网络请求
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NET_TIMEOUT:
                    break;
                case MSG_NET_STATUS_ERROR:
                    break;
                case MSG_NET_SUCC:
                    initSearchFlag = true;
                    String result = msg.obj.toString();
                    // try {
                    //  JSONObject json = new JSONObject(result);
                    parser(result);
                    //} catch (JSONException e) {
                    // TODO Auto-generated catch block
                    /*    e.printStackTrace();
                    }*/
                    break;

            }
        }
    };
    private double currentLatitude;
    private double currentLongitude;
    private LatLng currentLL;
    private PlanNode startNodeStr;
    private boolean isFirstLoc = true; // 是否首次定位
    private double changeLatitude;
    private double changeLongitude;
    private LocationClient mlocationClient;

    private MyInformation.ContentsEntity bInfo;
    private final int DISMISS_SPLASH = 0;
    private ImageView btn_locale, btn_refresh;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS_SPLASH:

                    Animator animator = AnimatorInflater.loadAnimator(LBSActivity.this, R.animator.splash);
                    animator.setTarget(R.drawable.icon_geo);
                    animator.start();
                    break;
            }
        }
    };
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private MyOrientationListener myOrientationListener;
    private RoutePlanSearch mSearch;
    private ArrayList<MyInformation.ContentsEntity> myInformations;
    private MyInformation myInformation;
    private static final int BAIDU_READ_PHONE_STATE = 100;


    /*
     医生详情：medical_header_iv medical_name_tv， medical_tager_one
     medical_tager_two medical_tager_three medical_informations
     */
    private RelativeLayout medical_information_rl;
    private ImageView medical_header_iv;
    private TextView medical_name_tv, medical_tager_one, medical_tager_two, medical_tager_three, medical_informations;
    private LinearLayout bike_layout;
    private List<Marker> makes;
    //这个点击显示RecylerView所有的说！！！
    private TextView myRecylerView_show_tv;
    private PopupWindow popwindow;
    private View view_pop_view;

    /*
     * 解析返回数据
	 */
    private void parser(String json) {
        Gson gson = new Gson();
        myInformation = gson.fromJson(json, MyInformation.class);

        myInformations.addAll(myInformation.getContents());
        Toast.makeText(this, "" + json.toString(), Toast.LENGTH_SHORT).show();
        Log.e("Myinformation", myInformation.getContents().toString());
        if (!isServiceLive && myInformations.size() > 0) {
            addOverLayout(currentLatitude, currentLongitude);
        }
    }

    public BMapManager mBMapManager = null;
    public MapView mMapView = null;
    public BaiduMap mBaiduMap;
    public static final String strKey = "63418012748CD126610D926A0546374D0BFC86D5";
    OverlayManager routeOverlay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());//在Application的onCreate()不行，必须在activity的onCreate()中
        setContentView(R.layout.activity_lbs);
        myInformations = new ArrayList<>();
        BMapManager.init();
        showContacts();

    }

    private void starGetJW() {
        mMapView = (MapView) findViewById(R.id.id_bmapViews);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        // 定位初始化
        mlocationClient = new LocationClient(this);
        mlocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);//设置onReceiveLocation()获取位置的频率
        option.setIsNeedAddress(true);//如想获得具体位置就需要设置为true
        mlocationClient.setLocOption(option);
        mlocationClient.start();

        Log.e("进度和维度1", "金纬度：" + changeLatitude + ":纬度" + changeLongitude);
        //这里的模式可以庚随地图或者null时候不会自己在中心
        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                null, true, null));
        myOrientationListener = new MyOrientationListener(this);
        //通过接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
        myOrientationListener.start();
        mSearch = RoutePlanSearch.newInstance();
        initMarkerClickEvent();
    }


    private void initData() {
        Log.e("进度和维度2", "金纬度：" + changeLatitude + ":纬度" + changeLongitude);

        // 发起搜索请求
        search();
        DemoApplication.getInstance().setHandler(mHandler);
    }

    private String latlng(String regexStr, String str) {
        Pattern pattern = Pattern.compile(regexStr);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            str = matcher.group(1);
        }
        return str;
    }

    private BaiduMap.OnMapStatusChangeListener changeListener = new BaiduMap.OnMapStatusChangeListener() {
        public void onMapStatusChangeStart(MapStatus mapStatus) {
        }

        public void onMapStatusChangeFinish(MapStatus mapStatus) {
            String _str = mapStatus.toString();
            String _regex = "target lat: (.*)\ntarget lng";
            String _regex2 = "target lng: (.*)\ntarget screen x";
            changeLatitude = Double.parseDouble(latlng(_regex, _str));
            changeLongitude = Double.parseDouble(latlng(_regex2, _str));
            LatLng changeLL = new LatLng(changeLatitude, changeLongitude);
            startNodeStr = PlanNode.withLocation(changeLL);
            Log.d("gaolei", "changeLatitude-----change--------" + changeLatitude);
            Log.d("gaolei", "changeLongitude-----change--------" + changeLongitude);
        }

        public void onMapStatusChange(MapStatus mapStatus) {
        }
    };

    private void initMapView() {
        view_pop_view = findViewById(R.id.view_pop_view);
        Log.e("进度和维度3", "金纬度：" + changeLatitude + ":纬度" + changeLongitude);
         /*
    医生详情：medical_header_iv medical_name_tv， medical_tager_one
    medical_tager_two medical_tager_three medical_informations

        private ImageView medical_header_iv;
        private TextView medical_name_tv,medical_tager_one,medical_tager_two,medical_tager_three,medical_informations;
         */
        medical_information_rl = (RelativeLayout) findViewById(R.id.medical_information_rl);
        medical_header_iv = (ImageView) findViewById(R.id.medical_header_iv);
        medical_name_tv = (TextView) findViewById(R.id.medical_name_tv);
        medical_tager_one = (TextView) findViewById(R.id.medical_tager_one);
        medical_tager_two = (TextView) findViewById(R.id.medical_tager_two);
        medical_tager_three = (TextView) findViewById(R.id.medical_tager_three);
        medical_informations = (TextView) findViewById(R.id.medical_informations);
        myRecylerView_show_tv = (TextView) findViewById(R.id.myRecylerView_show_tv);
        //这个是定位快速获取医生的按钮v
        bike_layout = (LinearLayout) findViewById(R.id.bike_layout);
        btn_locale = (ImageView) findViewById(R.id.btn_locale);
        btn_refresh = (ImageView) findViewById(R.id.btn_refresh);
        btn_locale.setOnClickListener(this);
        btn_refresh.setOnClickListener(this);
        myRecylerView_show_tv.setOnClickListener(this);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(this, 50));
        layoutParams.setMargins(0, statusBarHeight, 0, 0);//4个参数按顺序分别是左上右下
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.d("gaolei", "statusBarHeight---------------" + statusBarHeight);
        layoutParams2.setMargins(40, statusBarHeight + Utils.dp2px(LBSActivity.this, 50), 0, 0);//4个参数按顺序分别是左上右下

        UiSettings uiSettings = mBaiduMap.getUiSettings();
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setRotateGesturesEnabled(true);
        mBaiduMap.setOnMapStatusChangeListener(changeListener);
        mMapView.setOnClickListener(this);
        dragLocationIcon = BitmapDescriptorFactory.fromResource(R.mipmap.myselfss);
        bikeIcon = BitmapDescriptorFactory.fromResource(R.mipmap.medical);
        handler.sendEmptyMessageDelayed(DISMISS_SPLASH, 2000);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    protected void onRestart() {
        super.onRestart();
        mBaiduMap.setMyLocationEnabled(true);
        mlocationClient.start();
        myOrientationListener.start();
        mlocationClient.requestLocation();
    }

    @Override
    protected void onResume() {
        if (myRecylerView_show_tv != null) {
            myRecylerView_show_tv.setVisibility(View.VISIBLE);

        }
        if (medical_information_rl != null) {
            medical_information_rl.setVisibility(View.GONE);
        }
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出时销毁定位
        mlocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        Log.d("gaolei", "MainActivity------------onDestroy------------------");
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (bike_layout.getVisibility() == View.VISIBLE) {
                if (!Utils.isServiceWork(this, "com.biubike.service.RouteService")) {
                    cancelBook();
                }
                return true;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void cancelBook() {

        if (routeOverlay != null)
            routeOverlay.removeFromMap();
        MapStatus.Builder builder = new MapStatus.Builder();
        //地图缩放比设置为18
        builder.target(currentLL).zoom(10.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    /*
     * 云检索发起
	 */
    private void search() {
        search(LBSCloudSearch.SEARCH_TYPE_LOCAL);
    }

    /*
     * 根据搜索类型发起检索
	 */
    private void search(int searchType) {
        DemoApplication app = DemoApplication.getInstance();
        // 云检索发起
        LBSCloudSearch.request(searchType, getRequestParams(), mHandler,
                DemoApplication.networkType);
    }

    /*
     * 获取云检索参数
	 */
    private HashMap<String, String> getRequestParams() {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            String filter = "";
            // 附件，周边搜索
            map.put("search_name", URLEncoder.encode("天津市", "utf-8"));
            map.put("radius", "2000");
            DemoApplication app = DemoApplication.getInstance();
            if (app.currlocation != null) {
                map.put("location", app.currlocation.getLongitude() + ","
                        + app.currlocation.getLatitude());
            } else {
                // 无定位数据默认北京中心
                double cLat = currentLatitude;
                double cLon = currentLongitude;
                map.put("location", cLat + "," + cLon);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DemoApplication.getInstance().setFilterParams(map);

        return map;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_locale:
                if (!Utils.isServiceWork(this, "com.biubike.service.RouteService"))
                    cancelBook();
                break;
            case R.id.btn_refresh:
                if (routeOverlay != null)
                    routeOverlay.removeFromMap();
                Log.d("gaolei", "changeLatitude-----btn_refresh--------" + changeLatitude);
                Log.d("gaolei", "changeLongitude-----btn_refresh--------" + changeLongitude);
                addOverLayout(currentLatitude, currentLongitude);
                break;
            case R.id.myRecylerView_show_tv:
                if (myInformations != null) {
                    if (myInformations.size() > 0) {
                        initBottomSheetDialog2();
                        myRecylerView_show_tv.setVisibility(View.GONE);
                    }
                }
                break;
        }
    }

    //展示BottomSheetDialog，列表形式
    private void initBottomSheetDialog2() {
     /*   final View popview = LayoutInflater.from(this).inflate(R.layout.popwindows_mylocaton, null, false);
        LinearLayout linearLayout = popview.findViewById(R.id.all_pop);
        //创建recyclerView
        RecyclerView recyclerView = popview.findViewById(R.id.pop_recylerview);
        popwindow = new PopupWindow(popview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(myInformations, this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(View item, int position) {
                Toast.makeText(LBSActivity.this, "item " + position, Toast.LENGTH_SHORT).show();
                popwindow.dismiss();
            }
        });
        popwindow.setTouchable(true);
// 如果不设置PopupWindow的背景，有些版本就会出现一个问题：无论是点击外部区域还是Back键都无法dismiss弹框
// 这里单独写一篇文章来分析
        popwindow.setBackgroundDrawable(new ColorDrawable());
// 设置好参数之后再show
        popwindow.showAsDropDown(view_pop_view, Gravity.BOTTOM, 0, 0);
*/
     /*   List<String> mList;
        mList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            mList.add("item " + i);
        }

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);



        bottomSheetDialog.setContentView(recyclerView);
        bottomSheetDialog.show();*/
        //创建recyclerView
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        RecyclerView recyclerView = new RecyclerView(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(myInformations, this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(View item, int position) {
                Toast.makeText(LBSActivity.this, "item " + position, Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(recyclerView);
        bottomSheetDialog.show();
    }

    //获取自己当前的位置哦！！
    public void getMyLocation() {
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(msu);
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            // map view 销毁后不在处理新接收的位置
            if (bdLocation == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    .direction(mCurrentX)//设定图标方向     // 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            currentLatitude = bdLocation.getLatitude();
            currentLongitude = bdLocation.getLongitude();
            // current_addr.setText(bdLocation.getAddrStr());
            currentLL = new LatLng(bdLocation.getLatitude(),
                    bdLocation.getLongitude());

            LocationManager.getInstance().setCurrentLL(currentLL);
            LocationManager.getInstance().setAddress(bdLocation.getAddrStr());
            //  startNodeStr = PlanNode.withLocation(currentLL);
            //option.setScanSpan(5000)，每隔5000ms这个方法就会调用一次，而有些我们只想调用一次，所以要判断一下isFirstLoc
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                //地图缩放比设置为18
                builder.target(ll).zoom(10.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                changeLatitude = bdLocation.getLatitude();
                changeLongitude = bdLocation.getLongitude();

                Toast.makeText(LBSActivity.this, "金纬度：" + changeLatitude + ":纬度" + changeLongitude, Toast.LENGTH_SHORT).show();
                Log.e("进度和维度0", "金纬度：" + changeLatitude + ":纬度" + changeLongitude);
                initData();

            }
        }

    }

    private void addOverLayout(double _latitude, double _longitude) {
        //先清除图层
        mBaiduMap.clear();
        mlocationClient.requestLocation();
    /*    // 定义Maker坐标点
        LatLng point = new LatLng(_latitude, _longitude);
        // 构建MarkerOption，用于在地图上添加Marker:这个是那个大红点哦！！！！
        MarkerOptions options = new MarkerOptions().position(point)
                .icon(dragLocationIcon);
        // 在地图上添加Marker，并显示
        mBaiduMap.addOverlay(options);*/
        if (myInformations.size() > 0) {

            addInfosOverlay(myInformations);
        }
    }

    //这个设置点击事件来获取地图上面的所有标记物体。可以这里进行对应的详情
    private void initMarkerClickEvent() {
        // 对Marker的点击
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                // 获得marker中的数据
                if (marker != null && marker.getExtraInfo() != null) {
                    MyInformation.ContentsEntity bikeInfo = (MyInformation.ContentsEntity) marker.getExtraInfo().get("info");
                    if (bikeInfo != null)
                        //这里首先将所有的图标还原为之前的:这样之前所有的图标就变回去了。地图上面就你一个具体选中的位置变图片。
                        resetInfosOverlays(myInformations);
                    //这里给点击过的地方添加标记
                    marker.setIcon(dragLocationIcon);
                    updateBikeInfo(bikeInfo);
                }
                return true;
            }
        });
    }


    public void addInfosOverlay(List<MyInformation.ContentsEntity> infos) {
        makes = new ArrayList<>();
        LatLng latLng = null;
        OverlayOptions overlayOptions = null;
        Marker marker = null;
        for (MyInformation.ContentsEntity info : infos) {
            // 位置
            latLng = new LatLng(info.getLocation().get(1), info.getLocation().get(0));
            // 图标
            overlayOptions = new MarkerOptions().position(latLng)
                    .icon(bikeIcon).zIndex(5);
            //这个marker设置消息之后可以点击世纪之后显示哦！弹出的又来
            marker = (Marker) (mBaiduMap.addOverlay(overlayOptions));
            Bundle bundle = new Bundle();
            bundle.putSerializable("info", info);
            marker.setExtraInfo(bundle);
            makes.add(marker);
        }
        LatLng myLocation = new LatLng(currentLatitude, currentLongitude);
        // 将地图移到到自己中心点。
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(myLocation);
        mBaiduMap.setMapStatus(u);
    }

    //还原之前的每一个图标样子
    public void resetInfosOverlays(List<MyInformation.ContentsEntity> infos) {
        for (int i = 0; i < makes.size(); i++) {
            makes.get(i).setIcon(bikeIcon);
        }
    }

    //这里为标记的所有的点都来标记出来就可以
    private void initNearestBike(final MyInformation.ContentsEntity bikeInfo, LatLng ll) {
        ImageView nearestIcon = new ImageView(getApplicationContext());
        nearestIcon.setImageResource(R.mipmap.alarm_icon);
        InfoWindow.OnInfoWindowClickListener listener = null;
        listener = new InfoWindow.OnInfoWindowClickListener() {
            public void onInfoWindowClick() {
                updateBikeInfo(bikeInfo);
                mBaiduMap.hideInfoWindow();
            }
        };
        InfoWindow mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(nearestIcon), ll, -108, listener);
        mBaiduMap.showInfoWindow(mInfoWindow);
    }

    //这里来显示弹窗的哦！
    private void updateBikeInfo(MyInformation.ContentsEntity bikeInfo) {

        if (!hasPlanRoute) {
            myRecylerView_show_tv.setVisibility(View.GONE);
            medical_information_rl.setVisibility(View.VISIBLE);
            bike_layout.setVisibility(View.VISIBLE);
            medical_name_tv.setText(bikeInfo.getTags());
            medical_tager_one.setText(bikeInfo.getCity());
            medical_informations.setText(bikeInfo.getAddress());
            bInfo = bikeInfo;
        }
    }

    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "没有权限,请手动开启定位权限", Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(LBSActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, BAIDU_READ_PHONE_STATE);
        } else {
            starGetJW();

            initMapView();
        }
    }


    //Android6.0申请权限的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case BAIDU_READ_PHONE_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                    BMapManager.init();
                    starGetJW();

                    initMapView();
                } else {
                    // 没有获取到权限，做特殊处理
                    Toast.makeText(getApplicationContext(), "获取位置权限失败，请手动开启", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        private List<MyInformation.ContentsEntity> list;
        private Context mContext;
        private OnItemClickListener onItemClickListener;

        public RecyclerAdapter(List<MyInformation.ContentsEntity> list, Context mContext) {
            this.list = list;
            this.mContext = mContext;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview_layout, null);
            return new ViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.tv.setText(list.get(position).getTags() + "");
            holder.tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClickListener(v, position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv;

            public ViewHolder(View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.medical_name_tv);
            }
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public interface OnItemClickListener {
            void onItemClickListener(View item, int position);
        }
    }
}
