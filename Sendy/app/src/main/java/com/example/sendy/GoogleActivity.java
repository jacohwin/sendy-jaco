package com.example.sendy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.GeoApiContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GoogleActivity extends AppCompatActivity implements OnMapReadyCallback,BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "GoogleActivity";

    private static final String FINE_LOCATION= Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION= Manifest.permission.ACCESS_COARSE_LOCATION;
    private  static final  int LOCATION_PERMISSION_REQUEST_CODE= 00100;

    //widgets
    private EditText searchText;
    private ImageView gps;

    private Boolean mLocationPermissionGranted= false;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15f;
    private GeoApiContext geoApiContext=null;
    private static final LatLng pickpoint = new LatLng(-1.3033805, 36.7729652);
    private static final LatLng place1 = new LatLng(-1.3032051,36.7073074);
    private static final LatLng place2 = new LatLng(-1.2073188,36.8970392);
    private static final LatLng place3 = new LatLng(-1.2477467,36.8646907);
    private static final LatLng place4 = new LatLng(-1.2613673,36.808896);
    private static final LatLng place5 = new LatLng(-1.2810919,36.8092147);
    private static final LatLng destination = new LatLng(-1.2542035, 36.674212);

    ArrayList<LatLng> locations;





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);
        searchText = findViewById(R.id.input_search);

        gps = findViewById(R.id.ic_gps);
        getLocationPermission();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(this);
        LoadFragment(new Home());

//        ArrayList<LatLng> locations = new ArrayList();
        locations = new ArrayList();
        locations.add(new LatLng(-1.3033805, 36.7729652));
        locations.add(new LatLng(-1.3032051,36.7073074));
        locations.add(new LatLng(-1.2073188,36.8970392));
        locations.add(new LatLng(-1.2477467,36.8646907));
        locations.add(new LatLng(-1.2613673,36.808896));
        locations.add(new LatLng(-1.2810919,36.8092147));
        locations.add(new LatLng(-1.2542035, 36.674212));




    }

    private boolean LoadFragment (Fragment fragment){
        if(fragment !=null){

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,fragment)
                    .commit();
            return true;
        }
        return false;

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        Fragment fragment =null;

        switch (menuItem.getItemId()){
            case R.id.navigation_home:
                fragment = new Home();
                break;
            case R.id.navigation_dashboard:
                fragment = new Dashboard();
                break;
            case R.id.navigation_notifications:
                fragment = new Notifications();
                break;
        }
        return LoadFragment(fragment);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready, Search your place", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady:map is ready");
        mMap = googleMap;




        for(LatLng location : locations){
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("your route"));
        }


        PolylineOptions polylineOptions = new PolylineOptions().add(destination).add(place1).add(place2).add(place3).add(place4).add(place5).width(5).color(Color.BLUE).geodesic(true);
        mMap.addPolyline(polylineOptions);
        MarkerOptions markerOptions = new MarkerOptions().visible(true).position(destination).title("Destination");
        MarkerOptions marker= new MarkerOptions().visible(true).position(place5).title("Pickpoint");

        mMap.addMarker(markerOptions);
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickpoint,15));



        if (mLocationPermissionGranted) {
            getCurrentLocation();
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();

        }
    }

    private void init(){
        Log.d(TAG, "init:initializing ");

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
               if(actionId== EditorInfo.IME_ACTION_SEARCH
               || actionId==EditorInfo.IME_ACTION_DONE
               ||keyEvent.getAction()==KeyEvent.ACTION_DOWN
               ||keyEvent.getAction()==KeyEvent.KEYCODE_ENTER){

                   //executing searching method here
                   geoLocate();

               }
                return false;
            }
        });

        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClink:clicked gps icon");

                getCurrentLocation();
            }
        });

        hideSoftKeyboard();
    }

    private  void geoLocate(){
        Log.d(TAG,"geoLocate: geolocating");

        String searchString= searchText.getText().toString();
        Geocoder geocoder= new Geocoder(GoogleActivity.this);
        List<Address> list=new ArrayList<>();
        try{
            list= geocoder.getFromLocationName(searchString,1);
        }catch (IOException e){
            Log.e(TAG,"geoLocate: IOException:"+e.getMessage());

        }
        if(list.size()>0){
            Address address=list.get(0);
            Log.d(TAG,"geoLocate: found your location:"+address.toString());

            Toast.makeText(this, address.toString(),Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));

        }
    }

    private void getCurrentLocation(){
        Log.d(TAG,"getCurrentLocation: getting my current location");
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionGranted){
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG,"onComplete: found location");
                            Location currentLocation=(Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My location");

                        }else {
                            Log.d(TAG,"onComplete: current location is null!");
                            Toast.makeText(GoogleActivity.this,"unable to get your current location",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (SecurityException e){
            Log.e(TAG,"getCurrentLocation: SecurityException:"+ e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng,float zoom,String title){
        Log.d(TAG,"moveCamera: moving the camera to: lat:"+latLng.latitude +"lng"+latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        if(!title.equals("My location")){
            MarkerOptions markerOptions=new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(markerOptions);
        }

        hideSoftKeyboard();
    }

    private void initMap(){
        Log.d(TAG,"initMap:initializing map");
        SupportMapFragment supportMapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(GoogleActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location Permission");
        String[] permissions= {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){

                mLocationPermissionGranted= true;
                initMap();

            }else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE  );
            }

        }else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE  );
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG,"onRequestPermissionsResult:called.");
        mLocationPermissionGranted=false;

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length >0){
                    for(int i=0; i< grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted=false;
                            Log.d(TAG,"onRequestPermissionsResult:permission failed.");

                            return;
                        }
                    }
                    Log.d(TAG,"onRequestPermissionsResult:permission granted.");
                    mLocationPermissionGranted=true;
                    initMap();
                }
            }
            }
        }
        private void hideSoftKeyboard(){
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

