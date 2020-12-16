package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A PlayerListConverter converts lists of players for entry in an AWS DynamoDB table
 */
public class PlayerListConverter implements DynamoDBTypeConverter<List<String>, List<Player>> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<String> convert(List<Player> playerList) {
        List<String> strings = new ArrayList<>();
        for (Player p: playerList) {
            try {
                String s = convertOne(p);
                strings.add(s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return strings;
    }

    @Override
    public List<Player> unconvert(List<String> stringList) {
        List<Player> players = new ArrayList<>();
        for (String s: stringList) {
            Player p;
            try {
                p = unconvertOne(s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            players.add(p);
        }
        return players;
    }

    public String convertOne(Player p) throws JsonProcessingException {
        ObjectWriter ow = mapper.writer();
        return ow.writeValueAsString(p);
    }

    public Player unconvertOne(String s) throws IOException {
        return mapper.readValue(s, Player.class);
    }
}
