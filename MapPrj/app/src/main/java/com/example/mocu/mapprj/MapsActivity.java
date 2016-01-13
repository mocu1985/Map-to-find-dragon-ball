package com.example.mocu.mapprj;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.os.CountDownTimer;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private TextView mtxtView;
    private final int DEFAULT_DIV_SCALE = 10;
    LocationManager logMgr;
    String bestProv;
    long time = 10 * 1000;
    int time2 = 5 * 1000;     //定位喪失時的緩衝時間
    boolean flag = false;   //判斷是否開始倒數，如果開始倒數不再重新判斷是否倒數
    boolean flagEnd = false;        //判斷倒數是否完成，若完成則不再進行倒數
    boolean flagBuffer = false;
    CountDownTimer timer;
    CountDownTimer bufferTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        logMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        mtxtView = (TextView) findViewById(R.id.txtView);

        Criteria criteria = new Criteria();
        bestProv = logMgr.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = logMgr.getLastKnownLocation(bestProv);

        checkLocation(location);

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

        // Add a marker in Sydney and move the camera
        LatLng home = new LatLng(24.72400977, 121.78756714);
        mMap.addMarker(new MarkerOptions().position(home).title("My Home"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 17));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        //定點範圍半徑設定
        mMap.addCircle(new CircleOptions()
                .center(home)
                .radius(100)
                .strokeColor(Color.WHITE)
                .strokeWidth(2)
                .fillColor(Color.parseColor("#500084d3")));
    }

    public void checkLocation(Location location) {
        try {
            Company myPoint = new Company(1, location.getLatitude(), location.getLongitude());
            final boolean isok = new SearchMapService().check(myPoint, 24.72400977, 121.78756714, 100);

            if (isok) {  //如果在範圍內
                Toast.makeText(MapsActivity.this, "在指定範圍內", Toast.LENGTH_SHORT).show();
                while (flag == false && flagEnd == false) {  //判斷沒有倒數及尚未完成搜索時執行
                    flag = true;        //設置正在倒數
                    flagBuffer = false; //設置沒有緩衝倒數
                    timer = new CountDownTimer(time, 1000) {    //倒數設置
                        @Override
                        public void onTick(long lastTime) {
                            long x = lastTime / 60000;      //分
                            long y = ((lastTime / 1000) - (x * 60));       //秒
                            mtxtView.setText("搜索中:" + x + ":" + y);
                        }

                        @Override
                        public void onFinish() {
                            mtxtView.setText("搜索完成");
                            flagEnd = true;     //搜索完成
                            flagBuffer = true;  //搜索完成不再進行緩衝
                        }
                    }.start();
                }

            } else if (!isok) {   //不在範圍內
                Toast.makeText(MapsActivity.this, "沒有在指定範圍內", Toast.LENGTH_SHORT).show();
                if (flag == true) {     //正在倒數
                    flag = false;       //設置沒有倒數
                    timer.cancel();
                    while (flagBuffer == false) {       //判斷沒有緩衝
                        flagBuffer = true;      //設置開始緩衝
                        bufferTime = new CountDownTimer(time2, 1000) {  //緩衝設置
                            @Override
                            public void onTick(long lastTime) {
                                long x = lastTime / 60000;      //分
                                long y = ((lastTime / 1000) - (x * 60));       //秒
                                mtxtView.setText("偏離範圍:" + x + ":" + y);
                            }

                            @Override
                            public void onFinish() {
                                mtxtView.setText("搜索失敗");
                                flagBuffer = false;
                            }
                        }.start();

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    //---------------------------------------
    public class SearchMapService {
        //lat 緯度
        //lng 經度
        //r 半徑
        public Boolean check(Company company, Double lat, Double lng, Integer r) {
            double earthR = 6371;
            double distance;
            double dLat = Double.valueOf(new BigDecimal(String.valueOf((company.getLat() - lat)))
                    .multiply(new BigDecimal(String.valueOf(Math.PI)))
                    .divide(new BigDecimal(String.valueOf(180)), DEFAULT_DIV_SCALE,
                            BigDecimal.ROUND_HALF_EVEN).toString());
            double dLng = Double.valueOf(new BigDecimal(String.valueOf((company.getLng() - lng)))
                    .multiply(new BigDecimal(String.valueOf(Math.PI)))
                    .divide(new BigDecimal(String.valueOf(180)), DEFAULT_DIV_SCALE,
                            BigDecimal.ROUND_HALF_EVEN).toString());
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(company.getLat() * Math.PI / 180) *
                    Math.cos(lat * Math.PI / 180) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
            distance = (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))) * earthR * 1000;

            if (distance > Double.valueOf(String.valueOf(r))) {
                return false;
            }
            return true;
        }
    }


    public class Company {
        private int id;
        private double lat, lng;

        public Company(int id, double lat, double lng) {
            this.id = id;
            this.lat = lat;
            this.lng = lng;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }
    }

}
