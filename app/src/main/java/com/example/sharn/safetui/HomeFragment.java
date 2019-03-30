package com.example.sharn.safetui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
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
import java.lang.reflect.Type;
import java.util.Map;
import static android.content.Context.LOCATION_SERVICE;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    GoogleMap map;
    private ToggleButton safeTButton;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private boolean permissionGiven = false;
    DynamoDBMapper dynamoDBMapper;
    String type = "Car";
    double lat = 0;
    double lon = 0;

    int count = 0;//for testing loops

    //private RadioGroup radioGroup;
    //private RadioButton radioButton;

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

        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            ////////Update Location
            public void onLocationChanged(Location location) {
                map.clear();

                lat = location.getLatitude();
                lon = location.getLongitude();

                SaveLocation(lat, lon, type);

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

        this.dynamoDBMapper = DynamoDBMapper.builder()
            .dynamoDBClient(dynamoDBClient)
            .awsConfiguration(configuration)
            .build();

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
                    //double dId = Double.parseDouble(sLon);

                    //Get Type
                    Object oType = item.get("Type").getS();
                    String sType = (String) oType;

                    //Collision Checks
                    if (dLat == lat){//lat
                        count++;
                    }
                }
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();
        }

        ////////////////Toggle Button
        safeTButton = getView().findViewById(R.id.toggleButton2);
        safeTButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            if (safeTButton.isChecked()) {
                checkPermission();
                if (permissionGiven == true) {
                    //locationManager.requestLocationUpdates("gps", 2000, 3, locationListener);
                    SaveLocation(38.0, -77.0, "Car");
                }
            } else {
                locationManager.removeUpdates(locationListener);
                SaveLocation(0.0, 0.0, "Inactive");
                map.clear();

            }
            }
        });
        ////////////////Radio Button
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng me = new LatLng(38.781395, -77.523965);
        //MarkerOptions option = new MarkerOptions();
        //option.position(me).title("Manass");
        //map.addMarker(option);
        map.moveCamera(CameraUpdateFactory.newLatLng(me));
        map.animateCamera(CameraUpdateFactory.zoomTo(13.0f));
    }

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

////////////////////save to db
    public void SaveLocation(Double lat, Double lon, String typeIn) {
        final Test newItem = new Test();     ///////getClass().HomeFragment
        CognitoUserPool userPool = new CognitoUserPool(this.getContext(),
                "us-east-1_qvE8gB6Yl", "5mbji71cnmk4j961tvku6b77h4",
                "14djt0hg74nfgeeh01u2ip4tv0c95fq7knof5p56rjon4ma50vtf");
        String user_name = userPool.getCurrentUser().getUserId();

        newItem.setUserId(user_name);
        newItem.setType(typeIn);
        newItem.setLatitude(lat);
        newItem.setLongitude(lon);

        new Thread(new Runnable() {
            @Override
            public void run() {
            dynamoDBMapper.save(newItem);
            }
        }).start();
    }
}
