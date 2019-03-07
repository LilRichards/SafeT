package com.example.sharn.safetui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Trilateration extends AppCompatActivity {


    private TextView txtWifiInfo;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trilateration);

        txtWifiInfo = findViewById(R.id.idTxt);

        button = findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main(new String []{"arg1", "arg2", "arg3"});
            }
        });
    }

    public void main(String[] args) {
//assuming elevation = 0
        double earthR = 6371;
        double LatA = 38.831723;
        double LonA = -77.31404;
        double DistA = 0.0198;
        double LatB = 38.831698;
        double LonB = -77.313607;
        double DistB = 0.0198;
        double LatC = 38.831607;
        double LonC = -77.313853;
        double DistC = 0.0168;

    /*
    using authalic sphere
    if using an ellipsoid this step is slightly different
    Convert geodetic Lat/Long to ECEF xyz
       1. Convert Lat/Long to radians
       2. Convert Lat/Long(radians) to ECEF
    */
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
    /*
    #from wikipedia
    #transform to get circle 1 at origin
    #transform to get circle 2 on x axis
    */
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
        double z = Math.sqrt((DistA*DistA) - (x*x) - (y*y));
        //triPt is an array with ECEF x,y,z of trilateration point
        double triPt[] = {P1[0] + x*ex[0] + y*ey[0] + z*ez[0],
                P1[1] + x*ex[1] + y*ey[1] + z*ez[1],
                P1[2] + x*ex[2] + y*ey[2] + z*ez[2]};
        //convert back to lat/long from ECEF
        //convert to degrees
        double lat = Math.toDegrees(Math.asin(triPt[2] / earthR));
        double lon = Math.toDegrees(Math.atan2(triPt[1],triPt[0]));

        String s = "Lat: " + lat + "\n" + "Lon: " + lon;
        //txtWifiInfo.append(s);
        txtWifiInfo.setText(s);

    }
    public static double dotProduct (double[] a, double[] b)
    {
        int n = a.length;
        double sum = 0;
        for (int i = 0; i < n; i++)
        {
            sum += a[i] * b[i];
        }
        return sum;
    }

    static void crossProduct(double vect_A[], double vect_B[],  double cross_P[])
    {
        cross_P[0] = (vect_A[1] * vect_B[2])  - (vect_A[2] * vect_B[1]);
        cross_P[1] = (vect_A[2] * vect_B[0])  - (vect_A[0] * vect_B[2]);
        cross_P[2] = (vect_A[0] * vect_B[1])  - (vect_A[1] * vect_B[0]);
    }

    public static double norm (double[] a)
    {
        return Math.sqrt((a[0]*a[0])+(a[1]*a[1])+(a[2]*a[2]));
    }
}
