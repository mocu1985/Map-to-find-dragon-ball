package com.example.mocu.mapprj;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private TextView mtxtView;
    LocationManager logMgr;
    String bestProv;
    int radius = 50;    //顯示半徑
    int checkRadius = 50;    //偵測半徑
    int j;  //標籤移除索引
    long x, y, m, n;        //倒數分秒
    long passTime;  //使用時間
    int xTime = 10; //設定秒數
    long time = xTime * 1000;   //設定秒數轉成毫秒
    long time2 = 5 * 1000;     //定位喪失時的緩衝時間
    CountDownTimer timer;
    CountDownTimer bufferTime;
    MapLocationThread mapLocationThread;
    ArrayList<LatLng> aryLoc;
    //    ParseUrlClass parseUrlClass;
    ArrayList<Marker> aryMarker;    //地標標籤
    ArrayList<Circle> aryCircle;    //範圍標籤
    ArrayList<Boolean> aryBufFlag;  //緩衝標籤
    ArrayList<Boolean> flag;   //判斷是否開始倒數，如果開始倒數不再重新判斷是否倒數
    //    boolean flagEnd = false;        //判斷倒數是否完成，若完成則不再進行倒數
    ArrayList<Boolean> checkFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Criteria criteria = new Criteria();
//        parseUrlClass = new ParseUrlClass();
        aryLoc = new ArrayList<>();
        aryMarker = new ArrayList<>();
        aryCircle = new ArrayList<>();
        checkFlag = new ArrayList<>();
        aryBufFlag = new ArrayList<>();
        flag = new ArrayList<>();

        //執行續
        mapLocationThread = new MapLocationThread();
        mapLocationThread.start();

        logMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        mtxtView = (TextView) findViewById(R.id.txtView);


        bestProv = logMgr.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = logMgr.getLastKnownLocation(bestProv);


        try {
            checkLocation(location);
        } catch (Exception e) {
            Toast.makeText(MapsActivity.this, "ERROR:" + e, Toast.LENGTH_LONG).show();
        }

    }

    @Override       //若沒有onResume,無法執行定位判斷, requestLocationUpdates(更新位置)
    protected void onResume() {
        super.onResume();
        if (logMgr.isProviderEnabled(LocationManager.GPS_PROVIDER) || logMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            logMgr.requestLocationUpdates(bestProv, 1000, 1, this);
        } else {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        logMgr.removeUpdates(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        double markerLat, markerLng;
        //導入MARKER標籤清單
        for (int i = 0; i < mapLocationThread.arrayLat.size(); i++) {
            markerLat = Double.valueOf(mapLocationThread.arrayLat.get(i));
            markerLng = Double.valueOf(mapLocationThread.arrayLng.get(i));
            aryLoc.add(new LatLng(markerLat, markerLng));
            aryMarker.add(mMap.addMarker(new MarkerOptions().position(aryLoc.get(i)).title(mapLocationThread.arrayPosition.get(i))));

            //定點範圍半徑設定

            aryCircle.add(mMap.addCircle(new CircleOptions()
                    .center(aryLoc.get(i))
                    .radius(radius)
                    .strokeColor(Color.WHITE)
                    .strokeWidth(2)
                    .fillColor(Color.parseColor("#500084d3"))));

            checkFlag.add(false);
            aryBufFlag.add(false);
            flag.add(false);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 17));
        checkLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Criteria criteria = new Criteria();
        bestProv = logMgr.getBestProvider(criteria, true);
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    public void checkLocation(Location location) {
        double checkLat, checkLng;
        JudgmentMap judgmentMapPoint = new JudgmentMap();
        JudgmentMap mapIsok = new JudgmentMap();
        boolean isok = false;

        JudgmentMap.Company myPoint = judgmentMapPoint.new Company(1, location.getLatitude(), location.getLongitude());

        try {
            for (j = 0; j < aryMarker.size(); j++) { //當isok不等於真
                checkLat = Double.valueOf(mapLocationThread.arrayLat.get(j));
                checkLng = Double.valueOf(mapLocationThread.arrayLng.get(j));
                isok = mapIsok.check(myPoint, checkLat, checkLng, checkRadius);  //檢查定位範圍
            }
            if (isok) {  //如果在範圍內
                Toast.makeText(this, "在指定範圍內", Toast.LENGTH_SHORT).show();
                if (!checkFlag.get(j)) {    //如果沒有搜索過
                    if (!flag.get(j)) {  //沒有倒數
                        flag.set(j, true);        //開始倒數
                        if (aryBufFlag.get(j)) {       //如果有開啟緩衝執行
                            aryBufFlag.set(j, false); //關閉緩衝倒數
                            bufferTime.cancel();    //緩衝取消
                        }
                        timer = new CountDownTimer(time, 1000) {    //倒數設置
                            @Override
                            public void onTick(long lastTime) {
                                x = lastTime / 60000;      //分
                                y = ((lastTime / 1000) - (x * 60));       //秒
                                mtxtView.setText("偏離範圍:" + m + ":" + n + "搜索中:" + x + ":" + y + " 搜索目標數:" + aryMarker.size());
                                passTime = time - lastTime;     //經過時間
                            }

                            @Override
                            public void onFinish() {
                                checkFlag.set(j, true);     //搜索完成，變成搜索過
                                aryBufFlag.set(j, false);  //搜索完成不再進行緩衝
                                flag.set(j, false);
                                aryMarker.get(j).setVisible(false); //隱藏標籤
                                aryCircle.get(j).setVisible(false);
                                time = xTime * 1000;
                                mtxtView.setText("搜索完成  搜索目標數:" + aryMarker.size());
                            }
                        }.start();
                    }
                }
            }
            if (!isok) {   //不在範圍內
                Toast.makeText(this, "沒有在指定範圍內", Toast.LENGTH_SHORT).show();
                if (flag.get(j)) {     //正在倒數
                    flag.set(j, false);       //設置沒有倒數
                    timer.cancel();
                    time -= passTime;    //剩餘時間
                    if (!aryBufFlag.get(j)) {       //判斷沒有緩衝
                        aryBufFlag.set(j, true);      //設置開始緩衝
                        bufferTime = new CountDownTimer(time2, 1000) {  //緩衝設置
                            @Override
                            public void onTick(long lastTime) {
                                m = lastTime / 60000;      //分
                                n = ((lastTime / 1000) - (m * 60));       //秒
                                mtxtView.setText("偏離範圍:" + m + ":" + n + "  搜索中:" + x + ":" + y);
                            }

                            @Override
                            public void onFinish() {
                                mtxtView.setText("搜索失敗");
                                aryBufFlag.set(j, false);
                                time = xTime * 1000;
                            }
                        }.start();

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}






