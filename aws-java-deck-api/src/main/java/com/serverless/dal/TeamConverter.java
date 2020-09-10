package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamConverter implements DynamoDBTypeConverter<Map<String, List<String>>, Map<String, List<Player>>> {

    @Override
    public Map<String, List<String>> convert(Map<String, List<Player>> stringListMap) {
        PlayerConverter pc = new PlayerConverter();
        Map<String, List<String>> result = new HashMap<>();
        for (String key: stringListMap.keySet()) {
            result.put(key, pc.convert(stringListMap.get(key)));
        }
        return result;
    }

    @Override
    public Map<String, List<Player>> unconvert(Map<String, List<String>> stringListMap) {
        PlayerConverter pc = new PlayerConverter();
        Map<String, List<Player>> result = new HashMap<>();
        for (String key: stringListMap.keySet()) {
            result.put(key, pc.unconvert(stringListMap.get(key)));
        }
        return result;
    }
}
