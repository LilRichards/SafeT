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
    private Double _Latitude;
    private Double _Longitude;

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

    @DynamoDBAttribute(attributeName = "Latitude")
    public Double getLatitude() {
        return _Latitude;
    }

    public void setLatitude(final Double _Latitude) {
        this._Latitude = _Latitude;
    }

    @DynamoDBAttribute(attributeName = "Longitude")
    public Double getLongitude() {
        return _Longitude;
    }

    public void setLongitude(final Double _Longitude) {
        this._Longitude = _Longitude;
    }

}
