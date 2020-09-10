package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class WayToPlayConverter implements DynamoDBTypeConverter<String, WayToPlay> {
    @Override
    public WayToPlay unconvert(String s) {
        return WayToPlay.valueOf(s);
    }

    @Override
    public String convert(WayToPlay wayToPlay) {
        return WayToPlay.asString(wayToPlay);
    }
}
