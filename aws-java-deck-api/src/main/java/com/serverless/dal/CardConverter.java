package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * A CardConverter converts cards for entry in an AWS DynamoDB table
 */
public class CardConverter implements DynamoDBTypeConverter<String, Card> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convert(Card card) {
        try {
            return mapper.writer().writeValueAsString(card);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Card unconvert(String s) {
        try {
            return mapper.readValue(s, Card.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
