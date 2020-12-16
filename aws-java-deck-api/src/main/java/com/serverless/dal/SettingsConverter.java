package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A SettingsConverter converts Settings for entry in an AWS DynamoDB table
 */
public class SettingsConverter implements DynamoDBTypeConverter<String, Settings> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convert(Settings settings) {
        ObjectWriter ow = mapper.writer();
        try {
            return ow.writeValueAsString(settings);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Settings unconvert(String s) {
        try {
            return mapper.readValue(s, Settings.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
