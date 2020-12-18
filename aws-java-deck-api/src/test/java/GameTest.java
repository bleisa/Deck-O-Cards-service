import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.dal.Card;
import com.serverless.dal.Suit;
import com.serverless.dal.DeckType;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * The primary testing class for the Game class with api endpoints
 */

public class GameTest {
    private static final Random R = new Random();

    // TODO: test discard, draw (w/ rummy)
    // TODO: test euchre deck

    @Test
    public void sampleRummyGameTest() throws IOException {
        String code = createGame("rummy");
        List<String> players = List.of("Me", "Bobby", "Joe");
        joinPlayers(code, players);
        String settings = rummySettingsStringForURL();
        startGame(code, settings, "rummy", null);
        String jsonGame = callHandlerResponse("/" + code, "GET");
        JsonNode gameNode = new ObjectMapper().readTree(jsonGame);
        Map<String, List<Card>> hands = getHands(gameNode, players);
        int first = gameNode.get("nextPlayer").asInt();
        System.out.println("Beginning play....");
        // TODO: playRummyHand(players, code, hands, first);
        scoreHand(players, code);
        endGame(code);
    }

    // check that teams, points, pinochle trick collection, showing cards and picking back up,
    // trump, deleting a game work
    @Test
    public void samplePinochleGameTest() throws IOException {
        String code = createGame("pinochle");
        List<String> players = List.of("Me", "Billy", "Bobby", "Joe");
        joinPlayers(code, players);
        String settings = pinochleSettingsStringForURL();
        Map<String, String> teams = new HashMap<>();
        teams.put("Me", "We");
        teams.put("Billy", "We");
        teams.put("Bobby", "They");
        teams.put("Joe", "They");
        startGame(code, settings, "pinochle", teams);
        String jsonGame = callHandlerResponse("/" + code, "GET");
        JsonNode gameNode = new ObjectMapper().readTree(jsonGame);
        Map<String, List<Card>> hands = getHands(gameNode, players);
        System.out.println("Beginning play...");
        meldPinochleHand(players, hands, code);
        playTrumpHand(players, hands, code, Suit.valueOf(R.nextInt(4)).toString());
        scoreHand(players, code);
        endGame(code);
    }

    // check that passing cards between players works
    @Test
    public void samplePassPinochleGameTest() throws IOException {
        String code = createGame("pass pinochle");
        List<String> players = List.of("Me", "Billy", "Bobby", "Joe");
        joinPlayers(code, players);
        int[] counts = {16, 8, 16, 8};
        String settings = pinochleSettingsStringForURL(counts, true, true);
        Map<String, String> teams = new HashMap<>();
        teams.put("Me", "We");
        teams.put("Billy", "We");
        teams.put("Bobby", "They");
        teams.put("Joe", "They");
        startGame(code, settings, "pass pinochle", teams);
        String jsonGame = callHandlerResponse("/" + code, "GET");
        JsonNode g = new ObjectMapper().readTree(jsonGame);
        Map<String, List<Card>> hands = getHands(g, players);
        System.out.println("Beginning play...");
        passPinochleHand(players, hands, teams, code);
        meldPinochleHand(players, hands, code);
        playTrumpHand(players, hands, code, Suit.valueOf(R.nextInt(4)).toString());
        scoreHand(players, code);
        endGame(code);
    }

    private int joinTeam(String code, String playerName, String teamName) throws IOException {
        return callHandler("/team/" + code + "/" + playerName + "/" + teamName, "PUT");
    }

    // check that poker deck works with trick collection
    @Test
    public void sampleHeartsGameTest() throws IOException {
        String code = createGame("hearts");
        List<String> players = List.of("Me", "Billy", "Bob", "Joe");
        joinPlayers(code, players);
        String settings = heartsSettingsStringForURL();
        startGame(code, settings, "hearts", null);
        String jsonGame = callHandlerResponse("/" + code, "GET");
        JsonNode g = new ObjectMapper().readTree(jsonGame);
        Map<String, List<Card>> hands = getHands(g, players);
        System.out.println("Beginning play...");
        playTrumpHand(players, hands, code, "HEARTS");
        scoreHand(players, code);
        endGame(code);
    }

    private void passPinochleHand(List<String> players, Map<String, List<Card>> hands, Map<String, String> teams, String code) throws IOException {
        int[] handCounts = {16, 8, 16, 8};
        ObjectMapper mapper = new ObjectMapper();
        while (handCounts[0] > 12 || handCounts[1] < 12 || handCounts[2] > 12 || handCounts[3] < 12) {
            for (String name: players) {
                String jsonGame = callHandlerResponse("/" + code, "GET");
                JsonNode g = mapper.readTree(jsonGame);
                if (g.get("nextPlayer").asText().equals(name) && hands.get(name).size() > 12) {
                    String teammate = null;
                    System.out.println(name + " is passing....");
                    Card c = hands.get(name).get(R.nextInt(hands.get(name).size()));
                    String card = mapper.writer().writeValueAsString(c);
                    for (String p2: players) {
                        if (!p2.equals(name) && teams.get(p2).equals(teams.get(name))) {
                            teammate = p2;
                            break;
                        }
                    }
                    assertNotEquals(teammate, null);
                    int status = callHandler("/turn/" + code + "/" + URLEncoder.encode(card, StandardCharsets.UTF_8) + "/PASS/" +
                            teammate + "/" + name, "PUT");
                    assertEquals(status, 200);
                    for (int i = 0; i < players.size(); i++) {
                        String pl = players.get(i);
                        if (pl.equals(name)) {
                            handCounts[i]--;
                        } else if (pl.equals(teammate)) {
                            handCounts[i]++;
                        }
                    }
                    jsonGame = callHandlerResponse("/" + code, "GET");
                    g = mapper.readTree(jsonGame);
                    JsonNode playerData = null;
                    JsonNode teammateData = null;
                    for (JsonNode child: g.get("players")) {
                        if (child.get("name").asText().equals(name)) {
                            playerData = child;
                        } else if (child.get("name").asText().equals(teammate)) {
                            teammateData = child;
                        }
                    }
                    assertNotEquals(playerData, null);
                    assertNotEquals(teammateData, null);
                    hands.put(name, jsonToCardList(playerData.get("hand")));
                    hands.put(teammate, jsonToCardList(teammateData.get("hand")));
                    System.out.println(name + "'s hand is now " + hands.get(name));
                    System.out.println(teammate + "'s hand is now " + hands.get(teammate));
                } else if (g.get("nextPlayer").asText().equals(name)) {
                    int status = callHandler("/turn/" + code + "/" + null + "/SKIP/" +
                            null + "/" + name, "PUT");
                    assertEquals(status, 200);
                    System.out.println(name + " skipped their turn...");
                }
            }
        }
    }

    private void meldPinochleHand(List<String> players, Map<String, List<Card>> hands, String code) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        for (String name: players) {
            String jsonGame = callHandlerResponse("/" + code, "GET");
            JsonNode g = mapper.readTree(jsonGame);
            if (g.get("nextPlayer").asText().equals(name)) {
                System.out.println(name + " is placing meld....");
                Card c = hands.get(name).get(R.nextInt(hands.get(name).size()));
                String card = mapper.writer().writeValueAsString(c);
                int status = callHandler("/turn/" + code + "/" + URLEncoder.encode(card, StandardCharsets.UTF_8) + "/SHOW/null/" + name, "PUT");
                assertEquals(status, 200);
            }
            jsonGame = callHandlerResponse("/" + code, "GET");
            g = mapper.readTree(jsonGame);
            for (JsonNode child: g.get("players")) {
                if (child.get("name").asText().equals(name)) {
                    System.out.println(name + " is melding " + child.get("shown").asText());
                }
            }
        }
        for (String name: players) {
            String jsonGame = callHandlerResponse("/" + code, "GET");
            JsonNode g = mapper.readTree(jsonGame);
            if (g.get("nextPlayer").asText().equals(name)) {
                JsonNode cardNode = null;
                for (JsonNode child : g.get("players")) {
                    if (child.get("name").asText().equals(name)) {
                        cardNode = child.get("shown").get(0);
                        break;
                    }
                }
                assertNotEquals(cardNode, null);
                Card c = new Card(Suit.valueOf(cardNode.get("suit").asText()), cardNode.get("value").asInt());
                String card = mapper.writer().writeValueAsString(c);
                int status = callHandler("/turn/" + code + "/" + URLEncoder.encode(card, StandardCharsets.UTF_8) + "/PICKUP/null/" + name, "PUT");
                assertEquals(status, 200);
            }
        }
    }

    private void playTrumpHand(List<String> players, Map<String, List<Card>> hands, String code, String trump) throws IOException {
        System.out.println(trump + " is trump");
        int status = callHandler("/trump/" + code + "/" + trump, "PUT");
        assertEquals(status, 200);
        int trickCount = 0;
        String jsonGame = callHandlerResponse("/" + code, "GET");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode g = mapper.readTree(jsonGame);
        Map<String, List<Card>> collected = new HashMap<>();
        for (String player: players) {
            collected.put(player, new ArrayList<>());
        }
        String currentPlayer = g.get("nextPlayer").asText();
        while (!hands.get(currentPlayer).isEmpty()) {
            if (g.get("trick").size() == 0) {
                trickCount++;
                System.out.println("\nTRICK " + trickCount + ":");
            }
            Card c = hands.get(currentPlayer).get(R.nextInt(hands.get(currentPlayer).size()));
            String card = mapper.writer().writeValueAsString(c);
            status = callHandler("/turn/" + code + "/" + URLEncoder.encode(card, StandardCharsets.UTF_8) + "/TRICK/null/" + currentPlayer, "PUT");
            assertEquals(status, 200);
            System.out.println(currentPlayer + " played " + c.toString() + " in a trick....");
            jsonGame = callHandlerResponse("/" + code, "GET");
            g = mapper.readTree(jsonGame);
            JsonNode playerData = null;
            for (JsonNode child: g.get("players")) {
                if (child.get("name").asText().equals(currentPlayer)) {
                    playerData = child;
                }
            }
            assertNotEquals(playerData, null);
            hands.put(currentPlayer, jsonToCardList(playerData.get("hand")));
            collected.put(currentPlayer, jsonToCardList(playerData.get("collected")));
            System.out.println("\t" + currentPlayer + "'s hand is now " + hands.get(currentPlayer));
            System.out.println("\t" + currentPlayer + " has collected " + collected.get(currentPlayer));
            currentPlayer = g.get("nextPlayer").asText();
        }
    }

    private void scoreHand(List<String> players, String code) throws IOException {
        for (String name: players) {
            int points = R.nextInt(100);
            int status = callHandler("/score/" + code + "/" + name + "/" + points, "PUT");
            assertEquals(status, 200);
            String jsonGame = callHandlerResponse("/" + code, "GET");
            JsonNode g = new ObjectMapper().readTree(jsonGame);
            for (JsonNode child: g.get("players")) {
                if (child.get("name").asText().equals(name)) {
                    System.out.println(name + " has " + child.get("points"));
                }
            }
        }
    }

    private String callHandlerResponse(String tail, String requestMethod) throws IOException {
        String baseEndpoint = "https://kax95zucj1.execute-api.us-west-2.amazonaws.com/dev/deckocards";
        URL url = new URL(baseEndpoint + tail);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(requestMethod);
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(5000);
        con.setReadTimeout(7000);
        int status = con.getResponseCode();
        InputStreamReader reader;
        if (status > 299) {
            reader = new InputStreamReader(con.getErrorStream());
        } else {
            reader = new InputStreamReader(con.getInputStream());
        }
        BufferedReader in = new BufferedReader(reader);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        return content.toString();
    }

    private int callHandler(String tail, String requestMethod, boolean print) throws IOException {
        String baseEndpoint = "https://kax95zucj1.execute-api.us-west-2.amazonaws.com/dev/deckocards";
        URL url = new URL(baseEndpoint + tail);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(requestMethod);
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(0);
        con.setReadTimeout(0);
        int status = con.getResponseCode();
        InputStreamReader reader;
        if (status > 299) {
            reader = new InputStreamReader(con.getErrorStream());
        } else {
            reader = new InputStreamReader(con.getInputStream());
        }
        BufferedReader in = new BufferedReader(reader);
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        if (status > 299 || print) {
            System.out.println(content);
        }
        return status;
    }

    private int callHandler(String tail, String requestMethod) throws IOException {
        return callHandler(tail, requestMethod, false);
    }

    private String createGame(String gameType) throws IOException {
        System.out.println("Starting " + gameType + " game....");
        String code = callHandlerResponse("/", "POST");
        assertEquals(code.length(), 5);
        System.out.println("Game code is " + code);
        return code;
    }

    private void endGame(String code) throws IOException {
        System.out.println("Ending game....");
        int status = callHandler("/" + code, "DELETE");
        assertEquals(status, 200);
        System.out.println("Test successful");
    }

    private void startGame(String code, String settings, String gameType, Map<String, String> teams) throws IOException {
        int status = callHandler("/settings/" + code + "/" + settings, "PUT");
        assertEquals(status, 200);
        System.out.println("Settings have been set for " + gameType + "....");
        if (teams != null) {
            for (String player: teams.keySet()) {
                status = joinTeam(code, player, teams.get(player));
                assertEquals(status, 200);
            }
        }
        status = callHandler("/start/" + code, "PUT");
        assertEquals(status, 200);
        System.out.println("Game has been started....");
        status = callHandler("/deal/" + code, "PUT");
        assertEquals(status, 200);
        System.out.println("Hands have been dealt....");
    }

    private void joinPlayers(String code, List<String> players) throws IOException {
        for (String name: players) {
            int status = callHandler("/join/" + code + "/" + name, "PUT");
            assertEquals(status, 200);
        }
        System.out.println("Players have joined....");
    }

    private Map<String, List<Card>> getHands(JsonNode gameNode, List<String> players) {
        Map<String, List<Card>> hands = new HashMap<>();
        JsonNode playersNode = gameNode.get("players");
        for (String name: players) {
            for (JsonNode node: playersNode) {
                if (node.get("name").asText().equals(name)) {
                    List<Card> hand = jsonToCardList(node.get("hand"));
                    hands.put(name, hand);
                }
            }
        }
        System.out.println("All players have their hands....");
        return hands;
    }

    private List<Card> jsonToCardList(JsonNode cardList) {
        List<Card> cards = new ArrayList<>();
        for (JsonNode card: cardList) {
            cards.add(new Card(Suit.valueOf(card.get("suit").asText()), card.get("value").asInt()));
        }
        return cards;
    }

    private String pinochleSettingsStringForURL() throws JsonProcessingException {
        int[] counts = {12, 12, 12, 12};
        return pinochleSettingsStringForURL(counts, false, false);
    }

    private String pinochleSettingsStringForURL(int[] counts, boolean pass, boolean skip) throws JsonProcessingException {
        Map<String, Object> settings = new HashMap<>();
        settings.put("cardsPer", counts);
        settings.put("deckType", DeckType.PINOCHLE);
        settings.put("skip", skip);
        settings.put("discard", false);
        settings.put("trick", true);
        settings.put("pass", pass);
        settings.put("show", true);
        settings.put("aceHigh", false);
        settings.put("teams", true);
        settings.put("points", false);
        settings.put("draw", true);
        settings.put("cardsPerTrick", 4);
        String json = new ObjectMapper().writeValueAsString(settings);
        return URLEncoder.encode(json, StandardCharsets.UTF_8);
    }

    private String heartsSettingsStringForURL() throws JsonProcessingException {
        int[] counts = {13, 13, 13, 13};
        Map<String, Object> settings = new HashMap<>();
        settings.put("cardsPer", counts);
        settings.put("deckType", DeckType.POKER);
        settings.put("skip", false);
        settings.put("discard", false);
        settings.put("trick", true);
        settings.put("pass", false);
        settings.put("show", false);
        settings.put("aceHigh", true);
        settings.put("teams", false);
        settings.put("points", false);
        settings.put("draw", true);
        settings.put("cardsPerTrick", 4);
        String json = new ObjectMapper().writeValueAsString(settings);
        return URLEncoder.encode(json, StandardCharsets.UTF_8);
    }

    private String rummySettingsStringForURL() throws JsonProcessingException {
        Map<String, Object> settings = new HashMap<>();
        int[] counts = {7, 7, 7};
        settings.put("cardsPer", counts);
        settings.put("deckType", DeckType.POKER);
        settings.put("skip", false);
        settings.put("discard", true);
        settings.put("trick", false);
        settings.put("pass", false);
        settings.put("show", true);
        settings.put("aceHigh", false);
        settings.put("teams", false);
        settings.put("points", true);
        settings.put("draw", true);
        settings.put("cardsPerTrick", 0);
        String json = new ObjectMapper().writeValueAsString(settings);
        return URLEncoder.encode(json, StandardCharsets.UTF_8);
    }
}
