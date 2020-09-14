import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.dal.*;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The primary testing class for the Game class with api endpoints
 */

public class GameTest {
    private static final Random R = new Random();

    // TODO: test discard, draw (w/ rummy)

    // Better test because does not use POJOs except Card
    @Test
    public void sampleRummyGameTest() throws IOException {
        System.out.println("Starting rummy game....");
        String code = callHandlerResponse("/", "POST");
        assert code.length() == 5;
        System.out.println("Game code is " + code);
        List<String> players = List.of("Me", "Bobby", "Joe");
        for (String name: players) {
            int status = callHandler("/join/" + code + "/" + name, "PUT");
            assert status == 200;
        }
        System.out.println("Players have joined....");
        String settings = rummySettingsStringForURL();
        int status = callHandler("/settings/" + code + "/" + settings, "PUT");
        assert status == 200;
        System.out.println("Settings have been set for rummy....");
        status = callHandler("/start/" + code, "PUT");
        assert status == 200;
        System.out.println("Game has been started....");
        status = callHandler("/deal/" + code, "PUT");
        assert status == 200;
        System.out.println("Hands have been dealt....");
        String jsonGame = callHandlerResponse("/" + code, "GET");
        JsonNode gameNode = new ObjectMapper().readTree(jsonGame);
        List<List<Card>> hands = getHands(gameNode, players);
        int first = gameNode.get("nextPlayer").asInt();
        System.out.println("Hands have been retrieved....");
        System.out.println("Beginning play....");
        // TODO: playRummyHand(players, code, hands, first);
        System.out.println("Ending game....");
        status = callHandler("/" + code, "DELETE");
        assert status == 200;
        System.out.println("Test successful");
    }

    private List<List<Card>> getHands(JsonNode gameNode, List<String> players) {
        List<List<Card>> hands = new ArrayList<>();
        JsonNode playersNode = gameNode.get("players");
        for (String name: players) {
            List<Card> hand = new ArrayList<>();
            for (JsonNode node: playersNode) {
                if (node.get("name").asText().equals(name)) {
                    for (JsonNode card: node.get("hand")) {
                        hand.add(new Card(Suit.valueOf(card.get("suit").asText()), card.get("value").asInt()));
                    }
                    hands.add(hand);
                }
            }
        }
        return hands;
    }

    @Test
    public void samplePinochleGameTest() throws IOException {
        CardConverter cc = new CardConverter();
        System.out.println("Starting game....");
        String code = callHandlerResponse("/", "POST");
        System.out.println("Game code is " + code);
        assert code.length() == 5;
        List<Player> players = new ArrayList<>();
        players.add(new Player("Bobby"));
        players.add(new Player("Billy"));
        players.add(new Player("Joe"));
        players.add(new Player("Me"));
        for (Player p: players) {
            int status = callHandler("/join/" + code + "/" + p.getName(), "PUT");
            assert status == 200;
        }
        System.out.println("Players have joined....");
        String settings = pinochleSettingsStringForURL();
        int status = callHandler("/settings/" + code + "/" + settings, "PUT");
        assert status == 200;
        status = joinTeam(code, "Me", "We");
        assert status == 200;
        status = joinTeam(code, "Billy", "We");
        assert status == 200;
        status = joinTeam(code, "Bobby", "They");
        assert status == 200;
        status = joinTeam(code, "Joe", "They");
        assert status == 200;
        System.out.println("Settings have been set for pinochle....");
        status = callHandler("/start/" + code, "PUT");
        assert status == 200;
        System.out.println("Game has been started....");
        status = callHandler("/deal/" + code, "PUT");
        assert status == 200;
        System.out.println("Hands have been dealt....");
        String jsonGame = callHandlerResponse("/" + code, "GET");
        Game g = new ObjectMapper().readValue(jsonGame, Game.class);
        for (Player p: players) {
            List<Card> hand = g.getPlayer(p.getName()).getHand();
            p.setHand(hand);
        }
        System.out.println("All players have their hands....");
        System.out.println("Beginning play...");
        meldPinochleHand(players, code, cc);
        playTrumpHand(players, code, cc, Suit.valueOf(R.nextInt(4)).toString());
        scorePinochleHand(players, code);
        System.out.println("Ending game....");
        status = callHandler("/" + code, "DELETE");
        assert status == 200;
        System.out.println("Test successful");
    }

    @Test
    public void samplePassPinochleGameTest() throws IOException {
        CardConverter cc = new CardConverter();
        System.out.println("Starting game....");
        String code = callHandlerResponse("/", "POST");
        System.out.println("Game code is " + code);
        assert code.length() == 5;
        List<Player> players = new ArrayList<>();
        Player me = new Player("Me");
        Player billy = new Player("Billy");
        Player bobby = new Player("Bobby");
        Player joe = new Player("Joe");
        players.add(me);
        players.add(billy);
        players.add(bobby);
        players.add(joe);
        for (Player p: players) {
            int status = callHandler("/join/" + code + "/" + p.getName(), "PUT");
            assert status == 200;
        }
        System.out.println("Players have joined....");
        int[] counts = {16, 8, 16, 8};
        String settings = pinochleSettingsStringForURL(counts, true, true);
        int status = callHandler("/settings/" + code + "/" + settings, "PUT");
        assert status == 200;
        status = joinTeam(code, "Me", "We");
        assert status == 200;
        me.setTeamName("We");
        status = joinTeam(code, "Billy", "We");
        assert status == 200;
        billy.setTeamName("We");
        status = joinTeam(code, "Bobby", "They");
        assert status == 200;
        bobby.setTeamName("They");
        status = joinTeam(code, "Joe", "They");
        assert status == 200;
        joe.setTeamName("They");
        System.out.println("Settings have been set for pinochle....");
        status = callHandler("/start/" + code, "PUT");
        assert status == 200;
        System.out.println("Game has been started....");
        status = callHandler("/deal/" + code, "PUT");
        assert status == 200;
        System.out.println("Hands have been dealt....");
        for (Player p: players) {
            String jsonGame = callHandlerResponse("/" + code, "GET");
            Game g = new ObjectMapper().readValue(jsonGame, Game.class);
            List<Card> hand = g.getPlayer(p.getName()).getHand();
            p.setHand(hand);
        }
        System.out.println("All players have their hands....");
        System.out.println("Beginning play...");
        passPinochleHand(players, code, cc);
        meldPinochleHand(players, code, cc);
        playTrumpHand(players, code, cc, Suit.valueOf(R.nextInt(4)).toString());
        scorePinochleHand(players, code);
        System.out.println("Ending game....");
        status = callHandler("/" + code, "DELETE");
        assert status == 200;
        System.out.println("Test successful");
    }

    private int joinTeam(String code, String playerName, String teamName) throws IOException {
        int status = callHandler("/team/" + code + "/" + playerName + "/" + teamName, "PUT");
        return status;
    }

    @Test
    public void sampleHeartsGameTest() throws IOException {
        CardConverter cc = new CardConverter();
        System.out.println("Starting game....");
        String code = callHandlerResponse("/", "POST");
        System.out.println("Game code is " + code);
        assert code.length() == 5;
        List<Player> players = new ArrayList<>();
        players.add(new Player("Me"));
        players.add(new Player("Billy"));
        players.add(new Player("Bobby"));
        players.add(new Player("Joe"));
        for (Player p: players) {
            int status = callHandler("/join/" + code + "/" + p.getName(), "PUT");
            assert status == 200;
        }
        System.out.println("Players have joined....");
        String settings = heartsSettingsStringForURL();
        int status = callHandler("/settings/" + code + "/" + settings, "PUT");
        assert status == 200;
        System.out.println("Settings have been set for hearts....");
        status = callHandler("/start/" + code, "PUT");
        assert status == 200;
        System.out.println("Game has been started....");
        status = callHandler("/deal/" + code, "PUT");
        assert status == 200;
        System.out.println("Hands have been dealt....");
        for (Player p: players) {
            String jsonGame = callHandlerResponse("/" + code, "GET");
            Game g = new ObjectMapper().readValue(jsonGame, Game.class);
            List<Card> hand = g.getPlayer(p.getName()).getHand();
            p.setHand(hand);
        }
        System.out.println("All players have their hands....");
        System.out.println("Beginning play...");
        playTrumpHand(players, code, cc, "HEARTS");
        scorePinochleHand(players, code);
        System.out.println("Ending game....");
        status = callHandler("/" + code, "DELETE");
        assert status == 200;
        System.out.println("Test successful");
    }

    private void passPinochleHand(List<Player> players, String code, CardConverter cc) throws IOException {
        int[] handCounts = {16, 8, 16, 8};
        while (handCounts[0] > 12 || handCounts[1] < 12 || handCounts[2] > 12 || handCounts[3] < 12) {
            for (Player p: players) {
                String jsonGame = callHandlerResponse("/" + code, "GET");
                Game g = new ObjectMapper().readValue(jsonGame, Game.class);
                if (g.getWhoseTurn().equals(p) && g.getHand(p).size() > 12) {
                    Player teammate = null;
                    System.out.println(p.getName() + " is passing....");
                    Card c = p.getHand().get(R.nextInt(p.getHand().size()));
                    String card = cc.convert(c);
                    for (Player p2: players) {
                        if (!p2.equals(p) && p2.getTeamName().equals(p.getTeamName())) {
                            teammate = p2;
                            break;
                        }
                    }
                    assert teammate != null;
                    int status = callHandler("/turn/" + code + "/" + URLEncoder.encode(card, StandardCharsets.UTF_8) + "/PASS/" +
                            teammate.getName() + "/" + p.getName(), "PUT");
                    assert status == 200;
                    for (int i = 0; i < players.size(); i++) {
                        Player pl = players.get(i);
                        if (pl.equals(p)) {
                            handCounts[i]--;
                        } else if (pl.equals(teammate)) {
                            handCounts[i]++;
                        }
                    }
                    jsonGame = callHandlerResponse("/" + code, "GET");
                    g = new ObjectMapper().readValue(jsonGame, Game.class);
                    teammate.setHand(g.getPlayer(teammate.getName()).getHand());
                    p.setHand(g.getPlayer(p.getName()).getHand());
                    System.out.println(p.getName() + "'s hand is now " + p.getHand());
                    System.out.println(teammate.getName() + "'s hand is now " + teammate.getHand());
                } else if (g.getWhoseTurn().equals(p)) {
                    int status = callHandler("/turn/" + code + "/" + null + "/SKIP/" +
                            null + "/" + p.getName(), "PUT");
                    assert status == 200;
                    System.out.println(p.getName() + " skipped their turn...");
                }
            }
        }
    }

    private void meldPinochleHand(List<Player> players, String code, CardConverter cc) throws IOException {
        for (Player p: players) {
            String jsonGame = callHandlerResponse("/" + code, "GET");
            Game g = new ObjectMapper().readValue(jsonGame, Game.class);
            if (g.getWhoseTurn().equals(p)) {
                System.out.println(p.getName() + " is placing meld....");
                Card c = p.getHand().get(R.nextInt(p.getHand().size()));
                String card = cc.convert(c);
                int status = callHandler("/turn/" + code + "/" + URLEncoder.encode(card, StandardCharsets.UTF_8) + "/SHOW/null/" + p.getName(), "PUT");
                assert status == 200;
            }
            jsonGame = callHandlerResponse("/" + code, "GET");
            g = new ObjectMapper().readValue(jsonGame, Game.class);
            System.out.println(p.getName() + " is melding " + g.getPlayer(p.getName()).getShown());
        }
        for (Player p: players) {
            String jsonGame = callHandlerResponse("/" + code, "GET");
            Game g = new ObjectMapper().readValue(jsonGame, Game.class);
            if (g.getWhoseTurn().equals(p)) {
                Card c = g.getPlayer(p.getName()).getShown().get(0);
                String card = cc.convert(c);
                int status = callHandler("/turn/" + code + "/" + URLEncoder.encode(card, StandardCharsets.UTF_8) + "/PICKUP/null/" + p.getName(), "PUT");
                assert status == 200;
            }
        }
    }

    private void playTrumpHand(List<Player> players, String code, CardConverter cc, String trump) throws IOException {
        System.out.println(trump + " is trump");
        int status = callHandler("/trump/" + code + "/" + trump, "PUT");
        assert status == 200;
        int trickCount = 0;
        String jsonGame = callHandlerResponse("/" + code, "GET");
        Game g = new ObjectMapper().readValue(jsonGame, Game.class);
        while (!g.getWhoseTurn().getHand().isEmpty()) {
            Player p = players.get(g.getNextPlayer());
            if (g.getTrick().size() == 0) {
                trickCount++;
                System.out.println("\nTRICK " + trickCount + ":");
            }
            Card c = p.getHand().get(R.nextInt(p.getHand().size()));
            String card = cc.convert(c);
            status = callHandler("/turn/" + code + "/" + URLEncoder.encode(card, StandardCharsets.UTF_8) + "/TRICK/null/" + p.getName(), "PUT");
            assert status == 200;
            System.out.println(p.getName() + " played " + c.toString() + " in a trick....");
            jsonGame = callHandlerResponse("/" + code, "GET");
            g = new ObjectMapper().readValue(jsonGame, Game.class);
            p.setHand(g.getHand(p));
            p.setCollected(g.getPlayer(p.getName()).getCollected());
            System.out.println("\t" + p.getName() + "'s hand is now " + p.getHand());
            System.out.println("\t" + p.getName() + " has collected " + p.getCollected());
        }
    }

    private void scorePinochleHand(List<Player> players, String code) throws IOException {
        for (Player p: players) {
            int points = R.nextInt(100);
            int status = callHandler("/score/" + code + "/" + p.getName() + "/" + points, "PUT");
            assert status == 200;
            String jsonGame = callHandlerResponse("/" + code, "GET");
            Game g = new ObjectMapper().readValue(jsonGame, Game.class);
            System.out.println(p.getName() + " has " + g.getPlayer(p.getName()).getPoints());
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
        StringBuffer content = new StringBuffer();
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

    private String pinochleSettingsStringForURL() {
        int[] counts = {12, 12, 12, 12};
        return pinochleSettingsStringForURL(counts, false, false);
    }

    private String pinochleSettingsStringForURL(int[] counts, boolean pass, boolean skip) {
        SettingsConverter sc = new SettingsConverter();
        Settings settings = new Settings(counts, DeckType.PINOCHLE, skip, false, true,
                pass, true, false, true, false, true, 4);
        String json = sc.convert(settings);
        return URLEncoder.encode(json, StandardCharsets.UTF_8);
    }

    private String heartsSettingsStringForURL() {
        SettingsConverter sc = new SettingsConverter();
        int[] counts = {13, 13, 13, 13};
        Settings settings = new Settings(counts, DeckType.POKER, false, false, true,
                false, false, true, false, false, true, 4);
        String json = sc.convert(settings);
        return URLEncoder.encode(json, StandardCharsets.UTF_8);
    }

    private String rummySettingsStringForURL() throws JsonProcessingException {
        Map<String, Object> settings = new HashMap<>();
        int[] counts = {7, 7, 7};
        settings.put("cardsPer", counts);
        settings.put("deckType", DeckType.POKER);
        settings.put("skipEnabled", false);
        settings.put("discardEnabled", true);
        settings.put("trickEnabled", false);
        settings.put("passEnabled", false);
        settings.put("showEnabled", true);
        settings.put("aceHigh", false);
        settings.put("teamsEnabled", false);
        settings.put("pointsEnabled", true);
        settings.put("drawEnabled", true);
        settings.put("cardsPerTrick", 0);
        String json = new ObjectMapper().writeValueAsString(settings);
        return URLEncoder.encode(json, StandardCharsets.UTF_8);
    }
}
