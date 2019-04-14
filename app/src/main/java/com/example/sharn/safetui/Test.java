package com.example.sharn.safetui;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
//1: has range key
//2: no range key
@DynamoDBTable(tableName = "SafeT_Table3")
public class Test {
    private String _userId;
    private String _Type ;
    private double _Latitude;
    private double _Longitude;
    private long   _TimeStamp;
    private String _Emergency;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }
    public void setUserId(final String _userId) {
        this._userId = _userId;
    }

   // @DynamoDBRangeKey(attributeName = "Type")
    @DynamoDBAttribute(attributeName = "Type")
    public String getType() {
        return _Type;
    }
    public void setType(final String _Type) {
        this._Type = _Type;
    }

    //Latitude
    @DynamoDBAttribute(attributeName = "Latitude")
    public double getLatitude() {
        return _Latitude;
    }
    public void setLatitude(final double _Latitude) {
        this._Latitude = _Latitude;
    }

    //Longitude
    @DynamoDBAttribute(attributeName = "Longitude")
    public double getLongitude() {
        return _Longitude;
    }
    public void setLongitude(final double _Longitude) {
        this._Longitude = _Longitude;
    }

    //Timestamp
    @DynamoDBAttribute(attributeName = "TimeStamp")
    public long getTimeStamp() {
        return _TimeStamp;
    }
    public void setTimeStamp(final long _TimeStamp) {
        this._TimeStamp = _TimeStamp;
    }

    //Emergency
    @DynamoDBAttribute(attributeName = "Emergency")
    public String getEmergency() {
        return _Emergency;
    }
    public void setEmergency(final String _Emergency) {
        this._Emergency = _Emergency;
    }
}
