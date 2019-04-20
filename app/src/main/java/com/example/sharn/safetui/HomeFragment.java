package com.example.sharn.safetui;

import android.Manifest;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.Map;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.WIFI_SERVICE;
import static java.lang.Math.asin;
import android.net.wifi.WifiManager;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    GoogleMap map;
    private ToggleButton safeTButton;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private boolean permissionGiven = false;
    DynamoDBMapper dynamoDBMapper;
    String type;
    double lat;
    double lon;
    int count = 0;
    String user_name;
    int mInterval = 3000;
    private Handler mHandler;
    long timestamp;
    double ratio;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private RadioButton radioCar;
    private RadioButton radioBike;
    private RadioButton radioPed;

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

        // AWSMobileClient enables AWS user credentials to access your table
        AWSMobileClient.getInstance().initialize(this.getContext()).execute();
        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
        // Add code to instantiate a AmazonDynamoDBClient
        final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
        this.dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(dynamoDBClient).awsConfiguration(configuration).build();
        //Get username
        CognitoUserPool userPool = new CognitoUserPool(this.getContext(), "us-east-1_qvE8gB6Yl", "5mbji71cnmk4j961tvku6b77h4", "14djt0hg74nfgeeh01u2ip4tv0c95fq7knof5p56rjon4ma50vtf");
        user_name = userPool.getCurrentUser().getUserId();
        SaveLocation(0.0, 0.0, "Inactive", user_name, timestamp);

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
                mapUpdate(false);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }
            @Override
            public void onProviderEnabled(String provider) { }
            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };


        radioGroup = getView().findViewById(R.id.radioGroup1);
        safeTButton = getView().findViewById(R.id.toggleButton2);
        radioCar = getView().findViewById(R.id.radio_car);
        radioBike = getView().findViewById(R.id.radio_bike);
        radioPed = getView().findViewById(R.id.radio_ped);

        //Toggle Button
        safeTButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Active
                if (safeTButton.isChecked()) {
                    //Radio Buttons: set Type
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    radioButton = getView().findViewById(selectedId);
                    //RB must be selected to activate
                    if(selectedId == -1) {
                        Toast.makeText(getContext(), "Select travel method.", Toast.LENGTH_SHORT).show();
                        safeTButton.setChecked(false);
                        return;
                    }else if (radioButton.getId() == R.id.radio_car){
                        type = "Car";
                        radioCar.setEnabled(false);
                        radioBike.setEnabled(false);
                        radioPed.setEnabled(false);
                    } else if (radioButton.getId() == R.id.radio_ped){
                        type = "Ped";
                        radioCar.setEnabled(false);
                        radioBike.setEnabled(false);
                        radioPed.setEnabled(false);
                    } else if (radioButton.getId() == R.id.radio_bike){
                        type = "Bike";
                        radioCar.setEnabled(false);
                        radioBike.setEnabled(false);
                        radioPed.setEnabled(false);
                    }
                    //Start Tracking/collisionCheck
                    mHandler = new Handler();
                    startRepeatingTask();
                    checkPermission();
                //Inactive
                } else {
                    radioGroup.clearCheck();
                    radioCar.setEnabled(true);
                    radioBike.setEnabled(true);
                    radioPed.setEnabled(true);

                    timestamp = System.currentTimeMillis() / 1000;
                    SaveLocation(0.0, 0.0, "Inactive", user_name, timestamp);

                    stopRepeatingTask();

                    locationManager.removeUpdates(locationListener);
                    map.clear();
                }
            }
        });
    }

    public void collisionCheck() {
        if (type == "Car") {
            AWSMobileClient.getInstance().initialize(this.getContext()).execute();
            AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
            AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
            final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
            this.dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(dynamoDBClient).awsConfiguration(configuration).build();

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
                                if (dLat >= lat - .0005 && dLat <= lat + .0005) {
                                    if (dLon >= lon - .0005 && dLon <= lon + .0005) {
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

    //Start Running Trilat
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                runTrilat();
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
    public void openDialog() {
        CollideDialog collideDialog = new CollideDialog();
        collideDialog.show(getFragmentManager(), "wut");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng me = new LatLng(38.781395, -77.523965);
        map.moveCamera(CameraUpdateFactory.newLatLng(me));
        map.animateCamera(CameraUpdateFactory.zoomTo(20.0f));//13
    }

    //run in debug to get permission????????Sometimes, IDK...
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, 10);
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

    public void trilat(String[] args) {
        WifiManager wifi = (WifiManager) getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
        List<android.net.wifi.ScanResult> scan = wifi.getScanResults();
        wifi.startScan();

        /*Temporary router variables to store the closest three routers*/
        RouterInfo first, second, third;
        first = new RouterInfo("first", 0, Double.MAX_VALUE, 0, 0);
        second = new RouterInfo("first", 0, Double.MAX_VALUE, 0, 0);
        third = new RouterInfo("first", 0, Double.MAX_VALUE, 0, 0);

        /*Create router instances for both the 2.4G and 5G bands.
        Currently only have 5 routers so 5 router instances*/
        /*
        RouterInfo r1_2 = new RouterInfo("SafeT_WIFI1", 0, 0, 0,0);
        RouterInfo r1_5 = new RouterInfo("SafeT_WIFI1-5G", 0, 0, 0,0);
        r1_2.setLatitude(38.8275233);
        r1_2.setLongitude(-77.305220);
        r1_5.setLatitude(38.8275233);
        r1_5.setLongitude(-77.305220);
        r1_2.unitrssi = -40.8;
        r1_5.unitrssi = -33;

        RouterInfo r2_2 = new RouterInfo("SafeT_WIFI2", 0, 0, 0,0);
        RouterInfo r2_5 = new RouterInfo("SafeT_WIFI2-5G", 0, 0, 0,0);
        r2_2.setLatitude(38.8275233);
        r2_2.setLongitude(-77.305220);
        r2_5.setLatitude(38.8275233);
        r2_5.setLongitude(-77.305220);
        r2_2.unitrssi = -38;
        r2_5.unitrssi = -30.1;

        RouterInfo r3_2 = new RouterInfo("SafeT_WIFI3", 0, 0, 0,0);
        RouterInfo r3_5 = new RouterInfo("SafeTWIFI3-5G", 0, 0, 0,0);
        r3_2.setLatitude(38.827533);
        r3_2.setLatitude(-77.305705);
        r3_5.setLatitude(38.827533);
        r3_5.setLatitude(-77.305705);
        r3_2.unitrssi = -37.9;
        r3_5.unitrssi = -30.8;
        RouterInfo r4_2 = new RouterInfo("SafeT_WIFI4", 0, 0, 0,0);
        RouterInfo r4_5 = new RouterInfo("SafeT_WIFI4-5g", 0, 0, 0,0);
        RouterInfo r5_2 = new RouterInfo("SafeT_WIFI5", 0, 0, 0,0);
        RouterInfo r5_5 = new RouterInfo("SafeT_WIFI5-5G", 0, 0, 0,0);
        */

        //lat/lon from gps app on huawei
        RouterInfo Home_1 = new RouterInfo("KenYo", 0, 0, 0, 0);
        Home_1.setLatitude(38.782092);//(38.782188);
        Home_1.setLongitude(-77.523436);//(-77.523479);
        Home_1.unitrssi = -31;///// 5G?????
        RouterInfo Home_2 = new RouterInfo("SafeT_WIFI2", 0, 0, 0, 0);
        Home_2.setLatitude(38.782162);//(38.782222);
        Home_2.setLongitude(-77.523511);//(-77.523524);
        Home_2.unitrssi = -38;
        RouterInfo Home_3 = new RouterInfo("SafeT_WIFI3", 0, 0, 0, 0);
        Home_3.setLatitude(38.782262);//(38.782237);
        Home_3.setLongitude(-77.523469);//(-77.523603);
        Home_3.unitrssi = -37.9;

        /*Use WiFi Manager to scan the area for all access points.
        Filter by "our" routers and add the information to the router instances
        NOTE: Need to add the latitude and longitude of the routers after they're placed

        Should use only 2g ORRRRR 5g
        else a check is needed to determine if the same router is being used twice

        */
        ArrayList<RouterInfo> list = new ArrayList<>();
        RouterInfo[] close = new RouterInfo[3];
        double distance = 0;
        for (android.net.wifi.ScanResult r : scan) {
            /*
            if (r.SSID.equals(r1_2.getName())) {
                ratio = (double) r.level/r1_2.unitrssi;
                distance = (0.647345414 * Math.pow(ratio, 4.6170922718)) - 1.229882689;
                r1_2.freq = r.frequency;
                r1_2.rssi = r.level;
                r1_2.distance = distance;
                list.add(r1_2);
            }
            if (r.SSID.equals(r1_5.getName())) {
                ratio = (double) r.level/r1_5.unitrssi;
                distance = (0.070862507 * Math.pow(ratio, 6.235952987)) - 0.207677978;
                r1_5.freq = r.frequency;
                r1_5.rssi = r.level;
                r1_5.distance = distance;
                list.add(r1_5);
            }
           if (r.SSID.equals(r2_2.getName())) {
                ratio = (double) r.level/r2_2.unitrssi;
                distance = (0.647345414 * Math.pow(ratio, 4.6170922718)) - 1.229882689;
                r2_2.freq = r.frequency;
                r2_2.rssi = r.level;
                r2_2.distance = distance;
                list.add(r2_2);
            }
            if (r.SSID.equals(r2_5.getName())) {
                ratio = (double) r.level/r2_5.unitrssi;
                distance = (0.070862507 * Math.pow(ratio, 6.235952987)) - 0.207677978;
                r2_5.freq = r.frequency;
                r2_5.rssi = r.level;
                r2_5.distance = distance;
                list.add(r2_5);
            }
             if (r.SSID.equals(r3_2.getName())) {
                ratio = (double) r.level/r3_2.unitrssi;
                distance = (0.647345414 * Math.pow(ratio, 4.6170922718)) - 1.229882689;
                r3_2.freq = r.frequency;
                r3_2.rssi = r.level;
                r3_2.distance = distance;
                list.add(r3_2);
            }
            if (r.SSID.equals(r3_5.getName())) {
                ratio = (double) r.level/r3_5.unitrssi;
                distance = (0.070862507 * Math.pow(ratio, 6.235952987)) - 0.207677978;
                r3_5.freq = r.frequency;
                r3_5.rssi = r.level;
                r3_5.distance = distance;
                list.add(r3_5);
            }
            if (r.SSID.equals(r4_2.getName())) {
                distance = (0.647345414 * Math.pow(r.level, 4.6170922718)) - 1.229882689;
                r4_2.freq = r.frequency;
                r4_2.rssi = r.level;
                r4_2.distance = distance;
                list.add(r4_2);
            }
            if (r.SSID.equals(r4_5.getName())) {
                distance = (0.070862507 * Math.pow(r.level, 6.235952987)) - 0.207677978;
                r4_5.freq = r.frequency;
                r4_5.rssi = r.level;
                r4_5.distance = distance;
                list.add(r4_5);
            }
            if (r.SSID.equals(r5_2.getName())) {
                distance = (0.647345414 * Math.pow(r.level, 4.6170922718)) - 1.229882689;
                r5_2.freq = r.frequency;
                r5_2.rssi = r.level;
                r5_2.distance = distance;
                list.add(r5_2);
            }
            if (r.SSID.equals(r5_5.getName())) {
                distance = (0.070862507 * Math.pow(r.level, 6.235952987)) - 0.207677978;
                r5_5.freq = r.frequency;
                r5_5.rssi = r.level;
                r5_5.distance = distance;
                list.add(r5_5);
            }
            */

            //2G
            //distance = (0.647345414 * Math.pow(ratio, 4.6170922718));// - 1.229882689;
            //5G
            //distance = (0.070862507 * Math.pow(ratio, 6.235952987)) - 0.207677978;

            if (r.level > -70) { //cut out really weak signals
                if (r.frequency < 4000) { //use only 2G

                    if (r.SSID.equals(Home_1.getName())) {
                        ratio = (double) r.level / Home_1.unitrssi;
                        distance = (0.647345414 * Math.pow(ratio, 4.6170922718)) - 1.229882689; //2G
                        Home_1.freq = r.frequency;
                        Home_1.rssi = r.level;
                        Home_1.distance = distance;
                        list.add(Home_1);
                    }
                    if (r.SSID.equals(Home_2.getName())) {
                        ratio = (double) r.level / Home_2.unitrssi;
                        distance = (0.647345414 * Math.pow(ratio, 4.6170922718)) - 1.229882689;
                        Home_2.freq = r.frequency;
                        Home_2.rssi = r.level;
                        Home_2.distance = distance;
                        list.add(Home_2);
                    }
                    if (r.SSID.equals(Home_3.getName())) {
                        ratio = (double) r.level / Home_3.unitrssi;
                        distance = (0.647345414 * Math.pow(ratio, 4.6170922718)) - 1.229882689;
                        Home_3.freq = r.frequency;
                        Home_3.rssi = r.level;
                        Home_3.distance = distance;
                        list.add(Home_3);
                    }
                }
            }
        }

        /*Trilat VS. GPS check*/
        //3 routers needed for Trilat, if < 3: GPS
        if (list.size() <= 2) {
            stopRepeatingTask();
            checkPermission();
            if (permissionGiven) {
                locationManager.requestLocationUpdates("gps", 2000, 3, locationListener);
            }
            return;
        } else {
            locationManager.removeUpdates(locationListener);
            //Determine three routers with the shortest distance
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getDistance() < first.getDistance()) {
                    third = second;
                    second = first;
                    first = list.get(i);
                } else if (list.get(i).getDistance() < second.getDistance()) {
                    third = second;
                    second = list.get(i);
                } else if (list.get(i).getDistance() < third.getDistance()) {
                    third = list.get(i);
                }
            }
            scan.clear();
        }

        //assuming elevation = 0
        double earthR = 6371;
        double LatA = first.getLatitude();
        double LonA = first.getLongitude();
        double DistA = first.getDistance() / 1000;
        double LatB = second.getLatitude();
        double LonB = second.getLongitude();
        double DistB = second.getDistance() / 1000;
        double LatC = third.getLatitude();
        double LonC = third.getLongitude();
        double DistC = third.getDistance() / 1000;
 /*
        //assuming elevation = 0
        double earthR = 6371;
        double LatA = 38.8275333;
        double LonA = -77.305220;
        double DistA = .0030;
        double LatB = 38.827533;
        double LonB = -77.305105;
        double DistB = .0040;
        double LatC = 38.827566;
        double LonC = -77.305177;
        double DistC = 0.0050;
*/

        /*using authalic sphere
        if using an ellipsoid this step is slightly different
        Convert geodetic Lat/Long to ECEF xyz
        1. Convert Lat/Long to radians
        2. Convert Lat/Long(radians) to ECEF*/

        double xA = earthR * (Math.cos(Math.toRadians(LatA)) * Math.cos(Math.toRadians(LonA)));
        double yA = earthR * (Math.cos(Math.toRadians(LatA)) * Math.sin(Math.toRadians(LonA)));
        double zA = earthR * (Math.sin(Math.toRadians(LatA)));

        double xB = earthR * (Math.cos(Math.toRadians(LatB)) * Math.cos(Math.toRadians(LonB)));
        double yB = earthR * (Math.cos(Math.toRadians(LatB)) * Math.sin(Math.toRadians(LonB)));
        double zB = earthR * (Math.sin(Math.toRadians(LatB)));

        double xC = earthR * (Math.cos(Math.toRadians(LatC)) * Math.cos(Math.toRadians(LonC)));
        double yC = earthR * (Math.cos(Math.toRadians(LatC)) * Math.sin(Math.toRadians(LonC)));
        double zC = earthR * (Math.sin(Math.toRadians(LatC)));
        double P1[] = {xA, yA, zA};
        double P2[] = {xB, yB, zB};
        double P3[] = {xC, yC, zC};

        /* #from wikipedia
        #transform to get circle 1 at origin
        #transform to get circle 2 on x axis*/

        double diffP2P1[] = {xB - xA, yB - yA, zB - zA};
        double diffP3P1[] = {xC - xA, yC - yA, zC - zA};
        double normdiffP2P1 = norm(diffP2P1);
        double ex[] = {diffP2P1[0] / normdiffP2P1, diffP2P1[1] / normdiffP2P1, diffP2P1[2] / normdiffP2P1};
        double i = dotProduct(ex, diffP3P1);
        double diffP3P1ex[] = {xC - xA - (i * ex[0]), yC - yA - (i * ex[1]), zC - zA - (i * ex[2])};
        double normdiffP3P1ex = norm(diffP3P1ex);
        double ey[] = {diffP3P1ex[0] / normdiffP3P1ex, diffP3P1ex[1] / normdiffP3P1ex, diffP3P1ex[2] / normdiffP3P1ex};
        double ez[] = {0, 0, 0};
        crossProduct(ex, ey, ez);
        double d = norm(diffP2P1);
        double j = dotProduct(ey, diffP3P1);
        //from wikipedia
        //plug and chug using above values
        double x = ((DistA * DistA) - (DistB * DistB) + ((d * d) / (2 * d)));
        double y = (((DistA * DistA) - (DistC * DistC) + (i * i) + (j * j)) / (2 * j)) - ((i / j) * x);
        //only one case shown here
        double z = (DistA * DistA) - (x * x) - (y * y);
        if (z < 0) {
            z = Math.sqrt(Math.abs(z));
        } else {
            z = Math.sqrt(z);
        }
        //triPt is an array with ECEF x,y,z of trilateration point
        double triPt[] = {P1[0] + x * ex[0] + y * ey[0] + z * ez[0],
                P1[1] + x * ex[1] + y * ey[1] + z * ez[1],
                P1[2] + x * ex[2] + y * ey[2] + z * ez[2]};
        //convert back to lat/long from ECEF
        //convert to degrees
        lat = Math.toDegrees(asin(triPt[2] / earthR));
        lon = Math.toDegrees(Math.atan2(triPt[1], triPt[0]));
        mapUpdate(true);
        timestamp = System.currentTimeMillis()/1000;
        SaveLocation(lat, lon,type, user_name, timestamp);
    }

    public void mapUpdate(boolean GPS_Trilat){
        map.clear();
        LatLng current = new LatLng(lat, lon);
        MarkerOptions option = new MarkerOptions();
        option.position(current);
        //if trilat: orange, if gps: blue
        if(GPS_Trilat) {
            option.icon(BitmapDescriptorFactory.fromResource(R.drawable.circle_orange_16));
        }else{
            option.icon(BitmapDescriptorFactory.fromResource(R.drawable.circle_blue_16));
        }
        map.addMarker(option);
        map.moveCamera(CameraUpdateFactory.newLatLng(current));
    }

    public void runTrilat() {
        trilat(new String[]{"arg1", "arg2", "arg3"});
    }

    public static double dotProduct(double[] a, double[] b) {
        int n = a.length;
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    static void crossProduct(double vect_A[], double vect_B[], double cross_P[]) {
        cross_P[0] = (vect_A[1] * vect_B[2]) - (vect_A[2] * vect_B[1]);
        cross_P[1] = (vect_A[2] * vect_B[0]) - (vect_A[0] * vect_B[2]);
        cross_P[2] = (vect_A[0] * vect_B[1]) - (vect_A[1] * vect_B[0]);
    }

    public static double norm(double[] a) {
        return Math.sqrt((a[0] * a[0]) + (a[1] * a[1]) + (a[2] * a[2]));
    }
}

