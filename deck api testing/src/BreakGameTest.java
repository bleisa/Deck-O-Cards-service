import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.dal.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Let's break this
 */

public class BreakGameTest {
    private static final String CODE = "IGIHC"; // current game object being used for testing
    private static final Random R = new Random();

    @Ignore
    @Test
    public void newHTTPTest() throws IOException {
        int status = callHandler("", "POST", true);
        assert status == 200;
    }

    // -- same player names
    // -- bad player names - see note in Player
    @Test
    public void joinTwoOfSameHTTPTest() throws IOException {
        int status = callHandler("/join/" + CODE + "/Emma", "PUT");
        assert status == 200;
        status = callHandler("/join/" + CODE + "/Emma", "PUT");
        assert status == 500;
    }

    // -- ask for bad code
    @Test
    public void getNonexistentGameTest() throws IOException {
        int status = callHandler("/xxxxxxx", "GET");
        assert status == 404;
    }

    @Test
    public void outOfOrderTest() throws IOException {
        String code = callHandlerResponse("", "POST");
        assert code.length() == 5;
        // -- start before settings
        int status = callHandler("/start/" + code, "PUT");
        assert status == 500;
        // -- take turn before started
        CardConverter cc = new CardConverter();
        String card = cc.convert(new Card(Suit.HEARTS, 9));
        status = callHandler("/turn/" + code + "/" + URLEncoder.encode(card, StandardCharsets.UTF_8) + "/TRICK/null/Henry", "PUT", true);
        assert status == 500;
        // -- deal before started
        status = callHandler("/deal/" + code, "PUT");
        assert status == 500;
        List<String> names = List.of("Abby", "Alan", "Emma", "Henry");
        for (String name: names) {
            status = callHandler("/join/" + code + "/" + name, "PUT");
            assert status == 200;
        }
        // -- start before settings again
        status = callHandler("/start/" + code, "PUT");
        assert status == 500;
        String settings = pinochleSettingsStringForURL();
        status = callHandler("/settings/" + code + "/" + settings, "PUT");
        assert status == 200;
        status = callHandler("/start/" + code, "PUT");
        assert status == 200;
        // -- settings after started
        status = callHandler("/settings/" + code + "/" + settings, "PUT");
        assert status == 500;
        // -- join game after started
        status = callHandler("/join/" + code + "/Rando", "PUT");
        assert status == 500;
        status = callHandler("/deal/" + code, "PUT");
        assert status == 200;
        // -- ask for a non-existent player
        card = cc.convert(new Card(Suit.HEARTS, 9));
        status = callHandler("/turn/" + code + "/" + URLEncoder.encode(card, StandardCharsets.UTF_8) + "/TRICK/null/Rando", "PUT", true);
        assert status == 500;
        // -- play card player does not have
        card = cc.convert(new Card(Suit.HEARTS, 6));
        status = callHandler("/turn/" + code + "/" + URLEncoder.encode(card, StandardCharsets.UTF_8) + "/TRICK/null/Henry", "PUT", true);
        assert status == 500;
        // -- mismatch settings
        card = cc.convert(new Card(Suit.HEARTS, 9));
        status = callHandler("/turn/" + code + "/" + URLEncoder.encode(card, StandardCharsets.UTF_8) + "/DISCARD/null/Henry", "PUT", true);
        assert status == 500;
        // -- get a game that does not exist
        status = callHandler("/xxxxxxx", "GET");
        assert status == 404;
        status = callHandler("/" + code, "DELETE");
        assert status == 200;
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
        return URLEncoder.encode(sc.convert(settings));
    }
}
