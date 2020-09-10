package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CardListConverter implements DynamoDBTypeConverter<String, List<Card>> {
    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public List<Card> unconvert(String string) {
        CardConverter cc = new CardConverter();
        List<String> convertedCards;
        try {
            convertedCards = mapper.readValue(string, List.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Card> cards = new ArrayList<>();
        for (String s: convertedCards) {
            cards.add(cc.unconvert(s));
        }
        return cards;
    }

    @Override
    public String convert(List<Card> someCards) {
        CardConverter cc = new CardConverter();
        List<String> convertedCards = new ArrayList<>();
        for (Card c : someCards) {
            convertedCards.add(cc.convert(c));
        }
        ObjectWriter ow = mapper.writer();
        try {
            return ow.writeValueAsString(convertedCards);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
