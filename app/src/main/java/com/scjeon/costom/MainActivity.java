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

// git test
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    GPSInfo gi = new GPSInfo();
    String lt, lg;
    NetworkTask networkTask;
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
        }

        String url = "https://dapi.kakao.com/v2/local/geo//coord2regioncode.xml";

        networkTask = new NetworkTask(url,gi.getLg(),gi.getLt());
        networkTask.execute();


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

    /*좌표를 TM값으로 변환하기 위한 AsyncTask 클래스*/
    public class NetworkTask extends AsyncTask<Void,Void,String> {
        private String TMURL;
        private String lat;
        private String log;

        public NetworkTask(String TMURL,String lt, String lg) {
            this.TMURL = TMURL;
            this.lat=lt;
            this.log=lg;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            //Toast.makeText(getApplicationContext(),"lat : "+lat+"log : "+log,Toast.LENGTH_SHORT).show();
            result = requestHttpURLConnection.request(TMURL,lat,log);

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
        }
    }

}
