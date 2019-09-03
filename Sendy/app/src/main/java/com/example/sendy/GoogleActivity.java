package com.example.sendy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
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
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.example.sendy.CameraActivity.getFromPref;

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

    ArrayList<LatLng> list = new ArrayList<>();

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAMERA_PREF = "camera_pref";
    private Object origin, dest;
    ArrayList<LatLng> markerPoints;


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
//        getGeoContext();
        getDirectionsUrl( origin, dest);
        markerPoints = new ArrayList<LatLng>();



        locations = new ArrayList();
        locations.add(new LatLng(-1.3033805, 36.7729652));
//        locations.add(new LatLng(-1.3032051,36.7073074));
//        locations.add(new LatLng(-1.2073188,36.8970392));
//        locations.add(new LatLng(-1.2477467,36.8646907));
//        locations.add(new LatLng(-1.2613673,36.808896));
//        locations.add(new LatLng(-1.2810919,36.8092147));
//        locations.add(new LatLng(-1.2542035, 36.674212));



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (getFromPref(this, ALLOW_KEY)) {
                showSettingsAlert();
            } else if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)

                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    showAlert();
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }
            }
        } else {
            openCamera();
        }

    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    @SuppressLint("LongLogTag")
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            GoogleActivity.ParserTask parserTask = new GoogleActivity.ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>>{
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = new ArrayList<>();

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.width(2);
            lineOptions.color(Color.RED);
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            if (points.size() != 0) mMap.addPolyline(lineOptions);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    public static void saveToPreferences(Context context, String key, Boolean allowed) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.commit();
    }

    public static Boolean getFromPref(Context context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));
    }

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(GoogleActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(GoogleActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                });
        alertDialog.show();
    }

    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(GoogleActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setMessage("Need to access the location");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(GoogleActivity.this);
                    }
                });

        alertDialog.show();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Location", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
                startInstalledAppDetailsActivity(GoogleActivity.this);
            }
        });
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_CAMERA: {
//                for (int i = 0, len = permissions.length; i < len; i++) {
//                    String permission = permissions[i];
//
//                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
//                        boolean
//                                showRationale =
//                                ActivityCompat.shouldShowRequestPermissionRationale(
//                                        this, permission);
//
//                        if (showRationale) {
//                            showAlert();
//                        } else if (!showRationale) {
//                            // user denied flagging NEVER ASK AGAIN
//                            // you can either enable some fall back,
//                            // disable features of your app
//                            // or open another dialog explaining
//                            // again the permission and directing to
//                            // the app setting
//                            saveToPreferences(GoogleActivity.this, ALLOW_KEY, true);
//                        }
//                    }
//                }
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static void startInstalledAppDetailsActivity(final GoogleActivity context) {
        if (context == null) {
            return;
        }

        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    private void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);
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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if(mMap != null) {
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("You placed a marker here")
                            .icon(BitmapDescriptorFactory.defaultMarker()));
                }
                if(markerPoints.size()>1){
                    markerPoints.clear();
                    mMap.clear();
                }
                markerPoints.add(latLng);
                MarkerOptions options = new MarkerOptions();

                options.position(latLng);
                if(markerPoints.size()==1){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }else if(markerPoints.size()==2){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }

                mMap.addMarker(options);

                if(markerPoints.size() >= 2){
                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);

                    String url = getDirectionsUrl(origin, dest);

                    GoogleActivity.DownloadTask downloadTask = new GoogleActivity.DownloadTask();

                    downloadTask.execute(url);
                }

            }
        });



        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 10000);
        showDirections(locations);
        new DownloadTask();
        getDirectionsUrl(dest,origin);



        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int z = 0; z < list.size(); z++) {
            LatLng point = list.get(z);
            options.add(point);
            mMap.clear();
        }


        PolylineOptions polylineOptions = new PolylineOptions().add(destination).add(place1).add(place2).add(place3).add(place4).add(place5).add(pickpoint).width(5).color(Color.BLUE).geodesic(true);
        mMap.addPolyline(polylineOptions);
        MarkerOptions markerOptions = new MarkerOptions().visible(true).position(destination).title("Destination");
        MarkerOptions marker= new MarkerOptions().visible(true).position(place5).title("Pickpoint");



        mMap.addMarker(markerOptions);
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickpoint,15));
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);




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

    private void getDirectionsUrl(Object origin, Object dest) {
    }

    private void showDirections(ArrayList<LatLng> locations) {


        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr="+locations+","+locations+"&daddr="+locations+","+locations));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_LAUNCHER );
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);

    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            for(LatLng location : locations){

                final Intent intent = new
                        Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" +
                        "saddr=" + locations + "," + locations + "&daddr=" + locations + "," +
                        locations));

            }

        }
    };




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

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean
                                showRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                        this, permission);

                        if (showRationale) {
                            showAlert();
                        } else if (!showRationale) {
                            // user denied flagging NEVER ASK AGAIN
                            // you can either enable some fall back,
                            // disable features of your app
                            // or open another dialog explaining
                            // again the permission and directing to
                            // the app setting
                            saveToPreferences(GoogleActivity.this, ALLOW_KEY, true);
                        }
                    }
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
        }
        private void hideSoftKeyboard(){
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

