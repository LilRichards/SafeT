package com.example.sharn.safetui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sharn.safetui.Test;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;

import com.amazonaws.mobileconnectors.appsync.cache.normalized.sql.AppSyncSqlHelper;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.Random;


import java.util.HashMap;
import java.util.Map;


public class DynamoActivity extends AppCompatActivity {
    // Declare a DynamoDBMapper object
    DynamoDBMapper dynamoDBMapper;
/*
    private TextView txtUser;
    private TextView txtType;
    private TextView txtLat;
    private TextView txtLon;
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamo);

        final Button button1 = findViewById(R.id.add_data_btn);
        final Button button2 = findViewById(R.id.load_btn);

        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createNews();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                readNews();
            }
        });
/*
        txtUser = findViewById(R.id.disp_user);
        txtType = findViewById(R.id.disp_type);
        txtLat = findViewById(R.id.disp_lat);
        txtLon = findViewById(R.id.disp_lon);
*/
        // AWSMobileClient enables AWS user credentials to access your table
        AWSMobileClient.getInstance().initialize(this).execute();

        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();


        // Add code to instantiate a AmazonDynamoDBClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);

        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();
    }

    public void createNews() {
        final Test newItem = new Test();
        CognitoUserPool userPool = new CognitoUserPool(DynamoActivity.this,
                "us-east-1_qvE8gB6Yl", "5mbji71cnmk4j961tvku6b77h4",
                "14djt0hg74nfgeeh01u2ip4tv0c95fq7knof5p56rjon4ma50vtf");
        String user_name = userPool.getCurrentUser().getUserId();
        //CognitoUser user = userPool.getCurrentUser();
        //CognitoUser user = userPool.getUser(userId);

        newItem.setUserId("test2");
        newItem.setType("Car");
        newItem.setLatitude(40.917664);
        newItem.setLongitude(25.002569);

        new Thread(new Runnable() {
            @Override
            public void run() {
                dynamoDBMapper.save(newItem);
                // Item saved
            }
        }).start();
    }

    public void readNews() {
        new Thread(new Runnable() {

            CognitoUserPool userPool = new CognitoUserPool(DynamoActivity.this,
                    "us-east-1_qvE8gB6Yl", "5mbji71cnmk4j961tvku6b77h4",
                    "14djt0hg74nfgeeh01u2ip4tv0c95fq7knof5p56rjon4ma50vtf");
            String user_name = userPool.getCurrentUser().getUserId();

            @Override
            public void run() {
                //userId
                Test newItem1 = dynamoDBMapper.load(Test.class, user_name/*, "Walk" */);
                String type_is = newItem1.getType();
                Double lat_is = newItem1.getLatitude();
                Double lon_is = newItem1.getLongitude();
            }
        }).start();
    }
}