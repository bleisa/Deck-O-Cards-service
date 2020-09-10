package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class SuitConverter implements DynamoDBTypeConverter<Integer, Suit> {
    @Override
    public Suit unconvert(Integer suit) {
        return Suit.valueOf(suit);
    }

    @Override
    public Integer convert(Suit suit) {
        return Suit.numericalEquivalent(suit);
    }
}
