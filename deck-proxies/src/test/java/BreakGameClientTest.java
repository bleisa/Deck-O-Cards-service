import com.game.*;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Let's break this
 */

public class BreakGameClientTest {
    private static String code; // current game object being used for testing

    @Before
    public void newHTTPTest() throws IOException {
        code = callHandlerResponse("", "POST");
    }

    // -- same player names
    @Test
    public void joinTwoOfSameHTTPTest() throws IOException {
        Player p = new Player("Billy");
        int status = callHandler("/" + code + "/players", "PUT", p.toJson());
        assertEquals(200, status);
        status = callHandler("/" + code + "/players", "PUT", p.toJson());
        assertEquals(500, status);
    }

    // -- ask for bad code
    @Test
    public void getNonexistentGameTest() throws IOException {
        int status = callHandler("/xxxxxxx", "GET");
        assertEquals(404, status);
    }

    @Test
    public void outOfOrderTest() throws IOException {
        String code = callHandlerResponse("", "POST");
        assertEquals(5, code.length());
        // -- start before settings
        int status = callHandler("/" + code + "/start", "PUT");
        assertEquals(500, status);
        // -- take turn before started
        TurnRequestBody body = new TurnRequestBody(new Card(Suit.HEARTS, 9), new Player("Billy"), WayToPlay.TRICK, null);
        status = callHandler("/" + code + "/turn", "PUT", body.toJson());
        assertEquals(500, status);
        // -- deal before started
        status = callHandler("/" + code + "/deal", "PUT");
        assertEquals(500, status);
        List<String> names = List.of("Billy", "Bob", "Joe", "Me");
        for (String name: names) {
            Player p = new Player(name);
            status = callHandler("/" + code + "/players", "PUT", p.toJson());
            assertEquals(200, status);
        }
        // -- start before settings again
        status = callHandler("/" + code + "/start", "PUT");
        assertEquals(500, status);
        Settings settings = pinochleSettings();
        GameBean g = GameBean.fromJson(callHandlerResponse("/" + code, "GET"));
        g.setSettings(settings);
        status = callHandler("/" + code + "/settings", "PUT", g.toJson());
        assertEquals(200, status);
        status = callHandler("/" + code + "/start", "PUT");
        assertEquals(200, status);
        // -- settings after started
        status = callHandler("/" + code + "/settings", "PUT", g.toJson());
        assertEquals(500, status);
        // -- join game after started
        Player p = new Player("Rando");
        status = callHandler("/" + code + "/players", "PUT", p.toJson());
        assertEquals(500, status);
        status = callHandler("/" + code + "/deal", "PUT");
        assertEquals(200, status);
        // -- ask for a non-existent player to take a turn
        body = new TurnRequestBody(new Card(Suit.HEARTS, 9), p, WayToPlay.TRICK, null);
        status = callHandler("/" + code + "/turn", "PUT", body.toJson());
        assertEquals(500, status);
        // -- play card player does not have
        body = new TurnRequestBody(new Card(Suit.HEARTS, 6), new Player("Billy"), WayToPlay.TRICK, null);
        status = callHandler("/" + code + "/turn", "PUT", body.toJson());
        assertEquals(500, status);
        // -- mismatch settings
        body = new TurnRequestBody(new Card(Suit.HEARTS, 9), new Player("Billy"), WayToPlay.DISCARD, null);
        status = callHandler("/" + code + "/turn", "PUT", body.toJson());
        assertEquals(500, status);
        // -- get a game that does not exist
        status = callHandler("/xxxxxxx", "GET");
        assertEquals(404, status);
        status = callHandler("/" + code, "DELETE");
        assertEquals(200, status);
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
