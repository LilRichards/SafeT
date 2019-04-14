package com.example.sharn.safetui;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.lang.Math.*;
import java.util.*;

import static java.lang.Math.asin;

public class Trilateration extends AppCompatActivity {

    DynamoDBMapper dynamoDBMapper;
    private TextView txtWifiInfo;
    private Button button;
    String type = "Testing";
    double lat = 80085.0;
    double lon = 80085.0;
    double ratio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trilateration);

        txtWifiInfo = findViewById(R.id.idTxt);

        //Get username
        CognitoUserPool userPool = new CognitoUserPool(this,
                "us-east-1_qvE8gB6Yl", "5mbji71cnmk4j961tvku6b77h4",
                "14djt0hg74nfgeeh01u2ip4tv0c95fq7knof5p56rjon4ma50vtf");
        final String user_name = userPool.getCurrentUser().getUserId();

        button = findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main(new String []{"arg1", "arg2", "arg3"});
                SaveLocation(lat, lon, type, user_name);
            }
        });

        // AWSMobileClient enables AWS user credentials to access your table
        AWSMobileClient.getInstance().initialize(this).execute();
        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
        final AmazonDynamoDBAsyncClient dynamoDBClient = new AmazonDynamoDBAsyncClient(credentialsProvider);
        this.dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(dynamoDBClient).awsConfiguration(configuration).build();
    }

    public void main(String[] args) {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        List<ScanResult> scan = wifi.getScanResults();
        wifi.startScan();

        /*Temporary router variables to store the closest three routers*/
        RouterInfo first, second, third;
        first = new RouterInfo("first", 0, Double.MAX_VALUE, 0,0);
        second = new RouterInfo("first", 0, Double.MAX_VALUE, 0,0);
        third = new RouterInfo("first", 0, Double.MAX_VALUE, 0,0);


        /*Create router instances for both the 2.4G and 5G bands.
        Currently only have 5 routers so 5 router instances*/
        RouterInfo r1_2 = new RouterInfo("SafeT_WIFI1", 0, 0, 0,0);
        RouterInfo r1_5 = new RouterInfo("SafeT_WIFI1-5G", 0, 0, 0,0);
        r1_2.setLatitude(38.8275233);
        r1_2.setLatitude(38.8275233);
        r1_5.setLongitude(-77.305220);
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





/*

        RouterInfo r4_2 = new RouterInfo("SafeT_WIFI4", 0, 0, 0,0);
        RouterInfo r4_5 = new RouterInfo("SafeT_WIFI4-5g", 0, 0, 0,0);


        RouterInfo r5_2 = new RouterInfo("SafeT_WIFI5", 0, 0, 0,0);
        RouterInfo r5_5 = new RouterInfo("SafeT_WIFI5-5G", 0, 0, 0,0);


        /*RouterInfo Home_1 = new RouterInfo("KenYo", 0, 0, 0);
        RouterInfo Home_2 = new RouterInfo("RDT", 0, 0, 0);
        RouterInfo Home_3 = new RouterInfo("Te'Quaya", 0, 0, 0);
        Home_1.latitude = 38.782220;
        Home_1.longitude = -77.523569;

        Home_2.latitude = 38.782154;
        Home_2.longitude = -77.523373;

        Home_3.latitude = 38.782048;
        Home_3.longitude = -77.523577;*/

        /*Use WiFi Manager to scan the area for all access points.
        Filter by "our" routers and add the information to the router instances
        NOTE: Need to add the latitude and longitude of the routers after they're placed*/
        ArrayList<RouterInfo> list = new ArrayList<>();
        RouterInfo [] close = new RouterInfo[3];
        double distance = 0;
        for (ScanResult r : scan) {
            if (r.SSID.equals(r1_2.getName())) {
                ratio = (double) r.level/r1_2.unitrssi;
                distance = (0.647345414 * Math.pow(ratio, 4.6170922718));// - 1.229882689;
                r1_2.freq = r.frequency;
                r1_2.rssi = r.level;
                r1_2.distance = distance;
                list.add(r1_2);
            }/*
            if (r.SSID.equals(r1_5.getName())) {
                ratio = (double) r.level/r1_5.unitrssi;
                distance = (0.070862507 * Math.pow(ratio, 6.235952987)) - 0.207677978;
                r1_5.freq = r.frequency;
                r1_5.rssi = r.level;
                r1_5.distance = distance;
                list.add(r1_5);
            }
           */if (r.SSID.equals(r2_2.getName())) {
                ratio = (double) r.level/r2_2.unitrssi;
                distance = (0.647345414 * Math.pow(ratio, 4.6170922718));// - 1.229882689;
                r2_2.freq = r.frequency;
                r2_2.rssi = r.level;
                r2_2.distance = distance;
                list.add(r2_2);
            }/*
            if (r.SSID.equals(r2_5.getName())) {
                ratio = (double) r.level/r2_5.unitrssi;
                distance = (0.070862507 * Math.pow(ratio, 6.235952987)) - 0.207677978;
                r2_5.freq = r.frequency;
                r2_5.rssi = r.level;
                r2_5.distance = distance;
                list.add(r2_5);
            }
          */ if (r.SSID.equals(r3_2.getName())) {
                ratio = (double) r.level/r3_2.unitrssi;
                distance = (0.647345414 * Math.pow(ratio, 4.6170922718));// - 1.229882689;
                r3_2.freq = r.frequency;
                r3_2.rssi = r.level;
                r3_2.distance = distance;
                list.add(r3_2);
            }/*
            if (r.SSID.equals(r3_5.getName())) {
                ratio = (double) r.level/r3_5.unitrssi;
                distance = (0.070862507 * Math.pow(ratio, 6.235952987)) - 0.207677978;
                r3_5.freq = r.frequency;
                r3_5.rssi = r.level;
                r3_5.distance = distance;
                list.add(r3_5);
            }*/




/*
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

            /*if (r.SSID.equals(Home_1.getName())) {
                ratio = (double) r.level/ rssi_1m_2g;
                distance = (0.070862507 * Math.pow(ratio, 6.235952987)) - 0.207677978;
                Home_1.freq = r.frequency;
                Home_1.rssi = r.level;
                Home_1.distance = distance;
                list.add(Home_1);
            }
            if (r.SSID.equals(Home_2.getName())) {
                ratio =(double)  r.level/rssi_1m_2g;
                distance = (0.070862507 * Math.pow(ratio, 6.235952987)) - 0.207677978;
                Home_2.freq = r.frequency;
                Home_2.rssi = r.level;
                Home_2.distance = distance;
                list.add(Home_2);

            }
            if (r.SSID.equals(Home_3.getName())) {
                ratio =(double)  r.level/rssi_1m_2g;
                distance = (0.070862507 * Math.pow(ratio, 6.235952987)) - 0.207677978;
                Home_3.freq = r.frequency;
                Home_3.rssi = r.level;
                Home_3.distance = distance;
                list.add(Home_3);
            }*/
        }

        /* Determine three routers with the shortest distance*/
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

        //assuming elevation = 0
        double earthR = 6371;
        double LatA = first.getLatitude();
        double LonA = first.getLongitude();
        double DistA = first.getDistance()/1000;
        double LatB = second.getLatitude();
        double LonB = second.getLongitude();
        double DistB = second.getDistance()/1000;
        double LatC = third.getLatitude();
        double LonC = third.getLongitude();
        double DistC = third.getDistance()/1000;
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

        double xA = earthR *(Math.cos(Math.toRadians(LatA)) * Math.cos(Math.toRadians(LonA)));
        double yA = earthR *(Math.cos(Math.toRadians(LatA)) * Math.sin(Math.toRadians(LonA)));
        double zA = earthR *(Math.sin(Math.toRadians(LatA)));

        double xB = earthR *(Math.cos(Math.toRadians(LatB)) * Math.cos(Math.toRadians(LonB)));
        double yB = earthR *(Math.cos(Math.toRadians(LatB)) * Math.sin(Math.toRadians(LonB)));
        double zB = earthR *(Math.sin(Math.toRadians(LatB)));

        double xC = earthR *(Math.cos(Math.toRadians(LatC)) * Math.cos(Math.toRadians(LonC)));
        double yC = earthR *(Math.cos(Math.toRadians(LatC)) * Math.sin(Math.toRadians(LonC)));
        double zC = earthR *(Math.sin(Math.toRadians(LatC)));
        double P1[] = {xA,yA,zA};
        double P2[] = {xB,yB,zB};
        double P3[] = {xC,yC,zC};

        /* #from wikipedia
        #transform to get circle 1 at origin
        #transform to get circle 2 on x axis*/

        double diffP2P1[] = {xB-xA,yB-yA,zB-zA};
        double diffP3P1[] = {xC-xA,yC-yA,zC-zA};
        double normdiffP2P1 = norm(diffP2P1);
        double ex[] = {diffP2P1[0]/normdiffP2P1,diffP2P1[1]/normdiffP2P1,diffP2P1[2]/normdiffP2P1};
        double i = dotProduct(ex,diffP3P1);
        double diffP3P1ex[] = {xC-xA-(i*ex[0]),yC-yA-(i*ex[1]),zC-zA-(i*ex[2])};
        double normdiffP3P1ex = norm(diffP3P1ex);
        double ey[] = {diffP3P1ex[0]/normdiffP3P1ex,diffP3P1ex[1]/normdiffP3P1ex,diffP3P1ex[2]/normdiffP3P1ex};
        double ez[] = {0,0,0};
        crossProduct(ex,ey,ez);
        double d = norm(diffP2P1);
        double j = dotProduct(ey,diffP3P1);
        //from wikipedia
        //plug and chug using above values
        double x = ((DistA*DistA) - (DistB*DistB) + ((d*d)/(2*d)));
        double y = (((DistA*DistA) - (DistC*DistC) + (i*i) + (j*j))/(2*j)) - ((i/j)*x);
        //only one case shown here
        double z = (DistA*DistA) - (x*x) - (y*y);
        if (z < 0 ) {
            z = Math.sqrt(Math.abs(z));
        }
        else {
            z = Math.sqrt(z);
        }
        //triPt is an array with ECEF x,y,z of trilateration point
        double triPt[] = {P1[0] + x*ex[0] + y*ey[0] + z*ez[0],
                P1[1] + x*ex[1] + y*ey[1] + z*ez[1],
                P1[2] + x*ex[2] + y*ey[2] + z*ez[2]};
        //convert back to lat/long from ECEF
        //convert to degrees
        double lat = Math.toDegrees(asin(triPt[2] / earthR));
        double lon = Math.toDegrees(Math.atan2(triPt[1],triPt[0]));
        String s = "Lat: " + lat + "\n" + "Lon: " + lon;
        txtWifiInfo.setText(s);
    }

    public static double dotProduct (double[] a, double[] b) {
        int n = a.length;
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    static void crossProduct(double vect_A[], double vect_B[],  double cross_P[]) {
        cross_P[0] = (vect_A[1] * vect_B[2])  - (vect_A[2] * vect_B[1]);
        cross_P[1] = (vect_A[2] * vect_B[0])  - (vect_A[0] * vect_B[2]);
        cross_P[2] = (vect_A[0] * vect_B[1])  - (vect_A[1] * vect_B[0]);
    }

    public static double norm (double[] a) {
        return Math.sqrt((a[0]*a[0])+(a[1]*a[1])+(a[2]*a[2]));
    }

    public void SaveLocation(Double lat, Double lon, String typeIn, String userName) {
        final Test newItem = new Test();
        newItem.setUserId(userName);
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
