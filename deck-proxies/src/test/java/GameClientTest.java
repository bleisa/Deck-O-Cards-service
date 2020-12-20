import com.game.*;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * The primary testing class for the Game class with api endpoints
 */

public class GameClientTest {
    private static final Random R = new Random();

    // TODO: test discard, draw (w/ rummy)
    // TODO: test euchre deck

    @Test
    public void sampleRummyGameTest() throws IOException {
        String code = createGame("rummy");
        joinPlayers(code, List.of("Me", "Bobby", "Joe"));
        Settings settings = rummySettings();
        startGame(code, settings, "rummy", null);
        System.out.println("Beginning play....");
        // TODO: playRummyHand(players, code, hands, first);
        scoreHand(code);
        endGame(code);
    }

    // check that teams, points, pinochle trick collection, showing cards and picking back up,
    // trump, deleting a game work
    @Test
    public void samplePinochleGameTest() throws IOException {
        String code = createGame("pinochle");
        joinPlayers(code, List.of("Me", "Billy", "Bobby", "Joe"));
        Settings settings = pinochleSettings();
        Map<String, String> teams = new HashMap<>();
        teams.put("Me", "We");
        teams.put("Billy", "We");
        teams.put("Bobby", "They");
        teams.put("Joe", "They");
        startGame(code, settings, "pinochle", teams);
        System.out.println("Beginning play...");
        meldPinochleHand(code);
        playTrumpHand(code, Suit.valueOf(R.nextInt(4)).toString());
        scoreHand(code);
        endGame(code);
    }

    // check that passing cards between players works
    @Test
    public void samplePassPinochleGameTest() throws IOException {
        String code = createGame("pass pinochle");
        joinPlayers(code, List.of("Me", "Billy", "Bobby", "Joe"));
        int[] counts = {16, 8, 16, 8};
        Settings settings = pinochleSettings(counts, true, true);
        Map<String, String> teams = new HashMap<>();
        teams.put("Me", "We");
        teams.put("Billy", "We");
        teams.put("Bobby", "They");
        teams.put("Joe", "They");
        startGame(code, settings, "pass pinochle", teams);
        System.out.println("Beginning play...");
        passPinochleHand(code);
        meldPinochleHand(code);
        playTrumpHand(code, Suit.valueOf(R.nextInt(4)).toString());
        scoreHand(code);
        endGame(code);
    }

    // check that poker deck works with trick collection
    @Test
    public void sampleHeartsGameTest() throws IOException {
        String code = createGame("hearts");
        joinPlayers(code, List.of("Me", "Billy", "Bob", "Joe"));
        Settings settings = heartsSettings();
        startGame(code, settings, "hearts", null);
        System.out.println("Beginning play...");
        playTrumpHand(code, "HEARTS");
        scoreHand(code);
        endGame(code);
    }

    private String createGame(String gameType) throws IOException {
        System.out.println("Starting " + gameType + " game....");
        String code = callHandlerResponse("/", "POST");
        assertEquals(code.length(), 5);
        System.out.println("Game code is " + code);
        return code;
    }

    private void startGame(String code, Settings s, String gameType, Map<String, String> teams) throws IOException {
        GameBean g = getGame(code);
        g.setSettings(s);
        int status = callHandler("/" + code + "/settings", "PUT", g.toJson());
        assertEquals(200, status);
        System.out.println("Settings have been set for " + gameType + "....");
        if (teams != null) {
            for (Player p: g.getPlayers()) {
                status = joinTeam(code, p, teams.get(p.getName()));
                assertEquals(200, status);
            }
        }
        status = callHandler("/" + code + "/start", "PUT");
        assertEquals(200, status);
        System.out.println("Game has been started....");
        status = callHandler("/" + code + "/deal", "PUT");
        assertEquals(200, status);
        System.out.println("Hands have been dealt....");
    }

    private void joinPlayers(String code, List<String> players) throws IOException {
        for (String name: players) {
            Player p = new Player(name);
            int status = callHandler("/" + code + "/players", "PUT", p.toJson());
            assertEquals( 200, status);
        }
        System.out.println("Players have joined....");
    }

    private int joinTeam(String code, Player p, String teamName) throws IOException {
        p.setTeamName(teamName);
        return callHandler("/" + code + "/" + p.getName() + "/team", "PUT", p.toJson());
    }

    private Settings pinochleSettings() {
        int[] counts = {12, 12, 12, 12};
        return pinochleSettings(counts, false, false);
    }

    private Settings pinochleSettings(int[] counts, boolean pass, boolean skip) {
        return new Settings.SettingsBuilder(counts, DeckType.PINOCHLE)
                .skip(skip)
                .pass(pass)
                .trick(true)
                .show(true)
                .teams(true)
                .draw(true)
                .build();
    }

    private Settings heartsSettings(){
        int[] counts = {13, 13, 13, 13};
        return new Settings.SettingsBuilder(counts, DeckType.POKER)
                .trick(true)
                .aceHigh(true)
                .draw(true)
                .build();
    }

    private Settings rummySettings() {
        int[] counts = {7, 7, 7};
        return new Settings.SettingsBuilder(counts, DeckType.POKER)
                .discard(true)
                .show(true)
                .points(true)
                .draw(true)
                .build();
    }

    private GameBean getGame(String code) throws IOException {
        String json = callHandlerResponse("/" + code, "GET");
        return GameBean.fromJson(json);
    }

    private void passPinochleHand(String code) throws IOException {
        int[] handCounts = {16, 8, 16, 8};
        GameBean g = getGame(code);
        List<Player> players = g.getPlayers();
        while (handCounts[0] > 12 || handCounts[1] < 12 || handCounts[2] > 12 || handCounts[3] < 12) {
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                if (g.getNextPlayer().equals(p) && p.numberOfCardsLeft() > 12) {
                    Player teammate = null;
                    System.out.println(p.getName() + " is passing....");
                    Card c = p.getFromHand(R.nextInt(p.numberOfCardsLeft()));
                    for (Player p2 : players) {
                        if (!p2.equals(p) && p2.isOnSameTeam(p)) {
                            teammate = p2;
                            break;
                        }
                    }
                    assertNotEquals(null, teammate);
                    assert teammate != null;
                    TurnRequestBody body = new TurnRequestBody(c, p, WayToPlay.PASS, teammate);
                    int status = callHandler("/" + code + "/turn", "PUT", body.toJson());
                    assertEquals(200, status);
                    for (int j = 0; j < g.getPlayers().size(); j++) {
                        Player pl = g.getPlayers().get(j);
                        if (pl.equals(p)) {
                            handCounts[j]--;
                        } else if (pl.equals(teammate)) {
                            handCounts[j]++;
                        }
                    }
                    g = getGame(code);
                    players = g.getPlayers();
                    p = g.getPlayer(p.getName());
                    teammate = g.getPlayer(teammate.getName());
                    System.out.println(p.getName() + "'s hand is now " + p.getHand());
                    System.out.println(teammate.getName() + "'s hand is now " + teammate.getHand());
                } else if (g.getNextPlayer().equals(p)) {
                    TurnRequestBody body = new TurnRequestBody(null, p, WayToPlay.SKIP, null);
                    int status = callHandler("/" + code + "/turn", "PUT", body.toJson());
                    assertEquals(200, status);
                    System.out.println(p.getName() + " skipped their turn...");
                    g = getGame(code);
                }
            }
        }
    }

    private void meldPinochleHand(String code) throws IOException {
        GameBean g = getGame(code);
        for (Player p: g.getPlayers()) {
            if (g.getNextPlayer().equals(p)) {
                System.out.println(p.getName() + " is placing meld....");
                Card c = p.getFromHand(R.nextInt(p.getHand().size()));
                TurnRequestBody body = new TurnRequestBody(c, p, WayToPlay.SHOW, null);
                int status = callHandler("/" + code + "/turn", "PUT", body.toJson());
                assertEquals(200, status);
            }
            g = getGame(code);
            System.out.println(p.getName() + " is melding " + g.getPlayer(p.getName()).getShown());
        }
        for (Player p: g.getPlayers()) {
            g = getGame(code);
            if (g.getNextPlayer().equals(p) && g.getPlayer(p.getName()).getShown().size() > 0) {
                Card c = g.getPlayer(p.getName()).getShown().get(0);
                TurnRequestBody body = new TurnRequestBody(c, p, WayToPlay.PICKUP, null);
                int status = callHandler("/" + code + "/turn", "PUT", body.toJson());
                assertEquals(200, status);
            }
        }
    }

    private void playTrumpHand(String code, String trump) throws IOException {
        GameBean g = getGame(code);
        g.setTrump(Suit.valueOf(trump));
        System.out.println(trump + " is trump");
        int status = callHandler("/" + code + "/trump", "PUT", g.toJson());
        assertEquals(200, status);
        int trickCount = 0;
        Player currentPlayer = g.getNextPlayer();
        while (currentPlayer.hasCardsLeft()) {
            if (g.getTrick().size() == 0) {
                trickCount++;
                System.out.println("\nTRICK " + trickCount + ":");
            }
            Card c = currentPlayer.getFromHand(R.nextInt(currentPlayer.getHand().size()));
            TurnRequestBody body = new TurnRequestBody(c, currentPlayer, WayToPlay.TRICK, null);
            status = callHandler("/" + code + "/turn", "PUT", body.toJson());
            assertEquals(200, status);
            System.out.println(currentPlayer.getName() + " played " + c.toString() + " in a trick....");
            g = getGame(code);
            currentPlayer = g.getPlayer(currentPlayer.getName());
            System.out.println("\t" + currentPlayer.getName() + "'s hand is now " + currentPlayer.getHand());
            System.out.println("\t" + currentPlayer.getName() + " has collected " + currentPlayer.getCollected());
            currentPlayer = g.getNextPlayer();
        }
    }

    private void scoreHand(String code) throws IOException {
        GameBean g = getGame(code);
        for (Player p: g.getPlayers()) {
            int prevScore = p.getPoints();
            int points = R.nextInt(100);
            p.addPoints(points);
            int status = callHandler("/" + code + "/" + p.getName() + "/score", "PUT", p.toJson());
            assertEquals(200, status);
            g = getGame(code);
            System.out.println(p.getName() + " has " + g.getPlayer(p.getName()).getPoints());
            assertEquals(points + prevScore, g.getPlayer(p.getName()).getPoints());
        }
    }

    private void endGame(String code) throws IOException {
        System.out.println("Ending game....");
        int status = callHandler("/" + code, "DELETE");
        assertEquals(status, 200);
        System.out.println("Test successful");
    }

    /* Various methods for calling handlers */

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

    private int callHandler(String tail, String requestMethod, String body, boolean print) throws IOException {
        String baseEndpoint = "https://kax95zucj1.execute-api.us-west-2.amazonaws.com/dev/deckocards";
        URL url = new URL(baseEndpoint + tail);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(requestMethod);
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(0);
        con.setReadTimeout(0);
        con.setDoOutput(true);
        byte[] input = body.getBytes(StandardCharsets.UTF_8);
        con.getOutputStream().write(input, 0, input.length);
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

    private int callHandler(String tail, String requestMethod, String body) throws IOException {
        return callHandler(tail, requestMethod, body, false);
    }

    private int callHandler(String tail, String requestMethod) throws IOException {
        return callHandler(tail, requestMethod, false);
    }
}
