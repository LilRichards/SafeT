package com.example.sharn.safetui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.ToggleButton;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.Map;
import static android.content.Context.LOCATION_SERVICE;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    GoogleMap map;
    private ToggleButton safeTButton;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private boolean permissionGiven = false;
    DynamoDBMapper dynamoDBMapper;

    String type = "Inactive";
    double lat = 0;
    double lon = 0;
    int count = 0;//for loop testing
    String user_name;

    int mInterval = 3000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    long timestamp;


    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

        //Get username
        CognitoUserPool userPool = new CognitoUserPool(this.getContext(),
                "us-east-1_qvE8gB6Yl", "5mbji71cnmk4j961tvku6b77h4",
                "14djt0hg74nfgeeh01u2ip4tv0c95fq7knof5p56rjon4ma50vtf");
        user_name = userPool.getCurrentUser().getUserId();

        mHandler = new Handler();
        if (type == "Car") {
            startRepeatingTask();
        }

        //Location
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            //Update Location in DB
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                timestamp = System.currentTimeMillis()/1000;

                SaveLocation(lat, lon, type, user_name, timestamp);

                map.clear();
                LatLng current = new LatLng(lat, lon);
                MarkerOptions option = new MarkerOptions();
                option.position(current);
                map.addMarker(option);
                map.moveCamera(CameraUpdateFactory.newLatLng(current));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        // AWSMobileClient enables AWS user credentials to access your table
        AWSMobileClient.getInstance().initialize(this.getContext()).execute();
        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
        // Add code to instantiate a AmazonDynamoDBClient
        final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
        //Create DB Mapper
        this.dynamoDBMapper = DynamoDBMapper.builder()
            .dynamoDBClient(dynamoDBClient)
            .awsConfiguration(configuration)
            .build();

/*
        //Collision Checking from Cars
        if (type == "Car"){
            Runnable runnable = new Runnable() {
                public void run() {
                    ScanRequest scan1 = new ScanRequest().withTableName("SafeT_Table3");
                    ScanResult result = dynamoDBClient.scan(scan1);

                    for(Map<String, AttributeValue> item :result.getItems()){
                        //Get Lat
                        Object oLat = item.get("Latitude").getN();
                        String sLat = (String) oLat;
                        double dLat = Double.parseDouble(sLat);

                        //Get Lon
                        Object oLon = item.get("Longitude").getN();
                        String sLon = (String) oLon;
                        double dLon = Double.parseDouble(sLon);

                        //Get ID
                        Object oId = item.get("userId").getS();
                        String sId = (String) oId;

                        //Get Type
                        Object oType = item.get("Type").getS();
                        String sType = (String) oType;

                        //Collision Checks
                        if (!sId.equals(user_name)){
                            if (sType.equals("Ped") || sType.equals("Bike")) {
                                if (dLat >= lat - 1 && dLat <= lat + 1) {
                                    if (dLon >= lon - 1 && dLon <= lon + 1) {
                                        count++;
                                        //AlertDialogPop();
                                    }
                                }
                            }
                        }
                    }
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();
        }
        */


        //Toggle Button
        safeTButton = getView().findViewById(R.id.toggleButton2);
        safeTButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            if (safeTButton.isChecked()) {
                checkPermission();
                //Active
                if (permissionGiven == true) {
                    type = "Ped";
                    locationManager.requestLocationUpdates("gps", 2000, 3, locationListener);

                    //timestamp = System.currentTimeMillis()/1000;
                    //SaveLocation(lat, lon, type, user_name, timestamp);

                    /////Alert Dialog
                    //openDialog();
                }
                //Inactive
            } else {
                locationManager.removeUpdates(locationListener);
                type = "Inactive";
                timestamp = System.currentTimeMillis()/1000;
                SaveLocation(0.0, 0.0, type, user_name, timestamp);
                map.clear();
                }
            }
        });
        mHandler = new Handler();
        startRepeatingTask();


    }//End OnCreate


    public void collisionCheck(){
        if (type == "Car") {
            AWSMobileClient.getInstance().initialize(this.getContext()).execute();
            AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
            AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
            // Add code to instantiate a AmazonDynamoDBClient
            final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
            //Create DB Mapper
            this.dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(configuration)
                    .build();

            Runnable runnable = new Runnable() {
                public void run() {
                    ScanRequest scan1 = new ScanRequest().withTableName("SafeT_Table3");
                    ScanResult result = dynamoDBClient.scan(scan1);

                    for (Map<String, AttributeValue> item : result.getItems()) {
                        //Get Lat
                        Object oLat = item.get("Latitude").getN();
                        String sLat = (String) oLat;
                        double dLat = Double.parseDouble(sLat);

                        //Get Lon
                        Object oLon = item.get("Longitude").getN();
                        String sLon = (String) oLon;
                        double dLon = Double.parseDouble(sLon);

                        //Get ID
                        Object oId = item.get("userId").getS();
                        String sId = (String) oId;

                        //Get Type
                        Object oType = item.get("Type").getS();
                        String sType = (String) oType;

                        //Collision Checks
                        if (!sId.equals(user_name)) {
                            if (sType.equals("Ped") || sType.equals("Bike")) {
                                if (dLat >= lat - 1 && dLat <= lat + 1) {
                                    if (dLon >= lon - 1 && dLon <= lon + 1) {
                                        count++;
                                            openDialog();
                                    }
                                }
                            }
                        }
                    }
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();
        }
    }


    //Timed Collision Checks
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                collisionCheck();
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };
    void startRepeatingTask() {
        mStatusChecker.run();
    }
    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    ////Alert Dialog
    public void openDialog(){
        //AlertDialog alertDialog = builder.create();
        CollideDialog collideDialog = new CollideDialog();

        if (collideDialog.getShowsDialog()){
            return;
        }
        collideDialog.show(getFragmentManager(), "wut");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng me = new LatLng(38.781395, -77.523965);
        map.moveCamera(CameraUpdateFactory.newLatLng(me));
        map.animateCamera(CameraUpdateFactory.zoomTo(13.0f));
    }

//run in debug to get permission????????
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET},
                10);
                checkPermission();
            } else {
                permissionGiven = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGiven = true;
                }
        }
    }

    public void SaveLocation(double lat, double lon, String typeIn, String userName, long TS) {
        final Test newItem = new Test();
        newItem.setUserId(userName);
        newItem.setType(typeIn);
        newItem.setLatitude(lat);
        newItem.setLongitude(lon);
        newItem.setTimeStamp(TS);

        new Thread(new Runnable() {
            @Override
            public void run() {
            dynamoDBMapper.save(newItem);
            }
        }).start();
    }

    /*public void AlertDialogPop(){
        AlertDialog alertDialog = new AlertDialog.Builder(this.getContext()).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Alert message to be shown");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        }*/
}
