package com.scjeon.costom;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// git test
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    GPSInfo gi = new GPSInfo();
    TMData tmData;
   // NetworkTask networkTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 프래그먼트 초기화
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.fragment_place, new FragmentMain());
        fragmentTransaction.commit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*여기서 부터 GPS 이용한 현재위치(위도,경도) 가져오기*/
        //권한 물어서 권한 안되어있으면 권한 세팅하기
        int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if(permissionCheck1 == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET},1);

        int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(permissionCheck2 == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},1);

        int permissionCheck3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck3 == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //리스너 등록
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, listener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, listener);

        //현재 위치 가져오기
        String locationProvider = LocationManager.GPS_PROVIDER;
        Location location = locationManager.getLastKnownLocation(locationProvider);

        if(location != null){
            gi.setLt(String.valueOf(location.getLatitude()));
            gi.setLg(String.valueOf(location.getLongitude()));

            requestResponse(gi.getLt(),gi.getLg());
        }

    }

    /* GPS로 부터 받아온 현재위치(위도, 경도)를 TM으로 바꾸기 위해 http 통신 후 주소, TM_X, TM_Y 값을 가져옴.*/
    private void requestResponse(String lat, String log){
        String url = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x="+log+"&y="+lat+"&input_coord=WGS84&output_coord=TM";

        final RequestQueue queue = HttpVolley.getInstance(getApplicationContext()).getRequestQueue();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(),networkSuccessListener(),networkErrorListener()){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                header.put("Authorization","KakaoAK 420d614ecd66bf3b982ce99cc82bdcfa");
                return header;
            }
        };
        queue.add(jsonObjectRequest);
    }

    /* TM변환을 위한 http 통신이 성공했을 때 TMData 클래스에 결과 저장 */
    private Response.Listener<JSONObject> networkSuccessListener(){
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONArray ja = response.getJSONArray("documents");
                    tmData = new TMData(ja.getJSONObject(1).getString("address_name"),ja.getJSONObject(1).getString("x"),ja.getJSONObject(1).getString("y"));

                    getNearStation(tmData.getTm_x(),tmData.getTm_y());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /* TM변환을 위한 http 통신이 실패했을 때*/
    private Response.ErrorListener networkErrorListener(){
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"network error",Toast.LENGTH_SHORT).show();
            }
        };
    }

    /*인접한 미세먼지측정소 이름를 TM값을 통해서 알아냄 */
    private void getNearStation(String x, String y){

        String getStationURL
                = "http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList?tmX="+x+"&tmY="+y+"&pageNo=1&numOfRows=10&ServiceKey=SKXbIW5Uk%2Bn4rUGkg1KdvU8spwr2FBTQvs3J6LF6gdaTkjmuFxJHnc36A3MOXqGo1JBUWelpR0DfEr5zLJding%3D%3D";
        final RequestQueue queue = HttpVolley.getInstance(getApplicationContext()).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, getStationURL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("RESULT :", response.toString());

                        try{
                            String stName=null;

                            InputStream inputStream = new ByteArrayInputStream(response.getBytes());
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"UTF-8");
                            XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
                            XmlPullParser parser = pullParserFactory.newPullParser();
                            parser.setInput(inputStreamReader);

                            int parserEvent = parser.getEventType();
                            String tag_value; // 태그에 따른 값

                           ArrayList<String> stationNameArray = new ArrayList<>();

                           while (parserEvent != XmlPullParser.END_DOCUMENT) {
                                switch (parserEvent) {
                                    case XmlPullParser.START_TAG:
                                        tag_value = parser.getName();

                                        //측정소 위치 가져오기
                                        if (tag_value.equals("stationName")) {
                                            stName = parser.nextText();
                                            stationNameArray.add(stName);
                                        }
                                        break;
                                }
                                parserEvent = parser.next();
                            }
                            getMesureInfo(stationNameArray.get(0));
                           stationNameArray.clear();

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // handle error response
                    }
                }
        );
        queue.add(request);
    }

    /*인접한 미세먼지측정소 이름을 통해 측정값들을 가져옴 */
    private void getMesureInfo(final String stationName){

        if(stationName != null) {
            String getMesureURL
                    = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?stationName=" + stationName + "&dataTerm=DAILY&pageNo=1&numOfRows=1&ServiceKey=SKXbIW5Uk%2Bn4rUGkg1KdvU8spwr2FBTQvs3J6LF6gdaTkjmuFxJHnc36A3MOXqGo1JBUWelpR0DfEr5zLJding%3D%3D&ver=1.3";
            final RequestQueue queue = HttpVolley.getInstance(getApplicationContext()).getRequestQueue();
            StringRequest request = new StringRequest(Request.Method.POST, getMesureURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("RESULT :", response.toString());

                            try {
                                String so2Value, coValue, o3Value, no2Value, pm10Value, pm25Value = null;

                                InputStream inputStream = new ByteArrayInputStream(response.getBytes());
                                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                                XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
                                XmlPullParser parser = pullParserFactory.newPullParser();
                                parser.setInput(inputStreamReader);

                                int parserEvent = parser.getEventType();
                                String tag_value; // 태그에 따른 값

                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("측정소명 : "+stationName+"\n");

                                while (parserEvent != XmlPullParser.END_DOCUMENT) {
                                    switch (parserEvent) {
                                        case XmlPullParser.START_TAG:
                                            tag_value = parser.getName();

                                            //아황산가스
                                            if (tag_value.equals("so2Value")) {
                                                so2Value = parser.nextText();
                                                stringBuilder.append("아황산가스 농도 = "+so2Value+"\n");
                                            }

                                            //일산화탄소
                                            if (tag_value.equals("coValue")) {
                                                coValue = parser.nextText();
                                                stringBuilder.append("일산화탄소 농도 = "+coValue+"\n");
                                            }

                                            //오존
                                            if (tag_value.equals("o3Value")) {
                                                o3Value = parser.nextText();
                                                stringBuilder.append("오존 농도 = "+o3Value+"\n");
                                            }

                                            //이산화질소
                                            if (tag_value.equals("no2Value")) {
                                                no2Value = parser.nextText();
                                                stringBuilder.append("이산화질소 농도 = "+no2Value+"\n");
                                            }

                                            //미세먼지
                                            if (tag_value.equals("pm10Value")) {
                                                pm10Value = parser.nextText();
                                                stringBuilder.append("미세먼지 농도 = "+pm10Value+"\n");
                                            }

                                            //초미세먼지
                                            if (tag_value.equals("pm25Value")) {
                                                pm25Value = parser.nextText();
                                                stringBuilder.append("초미세먼지 농도 = "+pm25Value+"\n");
                                            }
                                            break;
                                    }
                                    parserEvent = parser.next();
                                }
                                Toast.makeText(getApplicationContext(), stringBuilder.toString(), Toast.LENGTH_LONG).show();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // handle error response
                        }
                    }
            );
            queue.add(request);
        }
        else
            Toast.makeText(getApplicationContext(),"측정소 위치 알수없음",Toast.LENGTH_SHORT).show();
    }
     // 위치가 변경되는것을 감지하기 위한 리스너
    private LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String lt=String.valueOf(location.getLatitude());//위도
            String lg=String.valueOf(location.getLongitude());//경도
            gi.setLt(lt);
            gi.setLg(lg);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        };

        @Override
        public void onProviderEnabled(String s) {
            //Toast.makeText(getApplicationContext(),"현재 위치정보 이용가능",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderDisabled(String s) {
            // Toast.makeText(getApplicationContext(),"현재 위치정보 이용불가",Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;

        if (id == R.id.nav_camera) {
            fragment = new FragmentMain();
        } else if (id == R.id.nav_gallery) {
            fragment = new FragmentSetting();
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        if(fragment != null){
            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_place, fragment);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
