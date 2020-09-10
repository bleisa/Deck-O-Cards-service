package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class DeckTypeConverter implements DynamoDBTypeConverter<String, DeckType> {
    @Override
    public DeckType unconvert(String s) {
        return DeckType.valueOf(s);
    }

    @Override
    public String convert(DeckType deckType) {
        if (deckType == DeckType.POKER) {
            return "POKER";
        } else if (deckType == DeckType.PINOCHLE) {
            return "PINOCHLE";
        } else {
            return "EUCHRE";
        }
    }
}
