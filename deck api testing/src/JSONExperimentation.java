import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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

public class JSONExperimentation {
    private static final Random R = new Random();
    private static final ObjectMapper mapper = new ObjectMapper();
    @Test
    public void lessLearnJSON() throws IOException {
        // Instantiate object, put some values in
        Game g = new Game();
        Player me = new Player("me1");
        g.joinGame("me1");
        g.joinGame("me2");
        g.joinGame("me3");
        g.joinGame("me4");
        int[] cards = {12, 12, 12, 12};
        Settings s = new Settings(cards, DeckType.PINOCHLE, false, false, true, false,
                true, false, false, false, false, 4);
        g.setUp(s);
        g.startGame();
        g.deal();
        me.setHand(g.getHand(me.getName()));
        g.takeTurn(me.getName(), me.getHand().get(R.nextInt(me.getHand().size())), WayToPlay.TRICK, null);
        // Convert object to json
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(g);
        System.out.println(json);
        // in here would be stuff like convert to url and then converting back out?
        // read json tree to get stuff
        JsonNode node = mapper.readTree(json);
        System.out.println(node);
        System.out.println(node.get("players"));
        System.out.println(node.get("players").get(0).get("name"));
    }

    @Test
    public void playerJSONdirectTest() throws JsonProcessingException {
        List<String> names = List.of("me1", "me2", "me3", "me4");
        Game g = new Game();
        for (String name: names) {
            g.joinGame(name);
        }
        List<Player> players = g.getPlayers();
        int[] counts = {12, 12, 12, 12};
        Settings s = new Settings(counts, DeckType.PINOCHLE, false, false, true,
                false, false, false, false, false, false,
                4);
        g.setSettings(s);
        g.startGame();
        g.deal();
        ObjectWriter ow = mapper.writer();
        for (Player p: players) {
            String json = ow.writeValueAsString(p);
            System.out.println(json);
        }
        boolean[] handOver = {false, false, false, false};
        while (!handOver[0] || !handOver[1] || !handOver[2] || !handOver[3]) {
            for (int i = 0; i < 4; i++) {
                if (!handOver[0] || !handOver[1] || !handOver[2] || !handOver[3]) {
                    Player p = players.get(i);
                    if (g.getWhoseTurn().equals(p)) {
                        Card c = p.getHand().get(R.nextInt(p.getHand().size()));
                        g.takeTurn(p.getName(), c, WayToPlay.TRICK, null);
                        String json = ow.writeValueAsString(p);
                        System.out.println(json);
                        if (p.getHand().size() == 0) {
                            handOver[i] = true;
                        }
                    }
                }
            }
        }
    }

    @Test
    public void mapToObjectJSONTest() throws IOException {
        Map<String, Object> totallyNotSettings = new HashMap<>();
        int[] counts = {12, 12, 12, 12};
        totallyNotSettings.put("cardsPer", counts);
        totallyNotSettings.put("deckType", DeckType.PINOCHLE);
        totallyNotSettings.put("skipEnabled", true);
        totallyNotSettings.put("discardEnabled", false);
        totallyNotSettings.put("trickEnabled", true);
        totallyNotSettings.put("passEnabled", false);
        totallyNotSettings.put("showEnabled", true);
        totallyNotSettings.put("aceHigh", false);
        totallyNotSettings.put("teamsEnabled", true);
        totallyNotSettings.put("pointsEnabled", true);
        totallyNotSettings.put("drawEnabled", false);
        totallyNotSettings.put("cardsPerTrick", 4);
        Settings actualSettings = new Settings(counts, DeckType.PINOCHLE, true, false, true,
                false, true, false, true, false, true, 4);
        ObjectWriter ow = mapper.writer();
        String jsonNotSettings = ow.writeValueAsString(totallyNotSettings);
        String jsonSettings = ow.writeValueAsString(actualSettings);
        System.out.println(jsonSettings);
        System.out.println(jsonNotSettings);
        Settings notSettings = mapper.readValue(jsonNotSettings, Settings.class);
        assert notSettings.equals(actualSettings);
    }

    @Test
    public void JSONtoURLandBackTest() throws IOException {
        Map<String, Object> totallyNotSettings = new HashMap<>();
        int[] counts = {12, 12, 12, 12};
        totallyNotSettings.put("cardsPer", counts);
        totallyNotSettings.put("deckType", DeckType.PINOCHLE);
        totallyNotSettings.put("skipEnabled", true);
        totallyNotSettings.put("discardEnabled", false);
        totallyNotSettings.put("trickEnabled", true);
        totallyNotSettings.put("passEnabled", false);
        totallyNotSettings.put("showEnabled", true);
        totallyNotSettings.put("aceHigh", false);
        totallyNotSettings.put("teamsEnabled", true);
        totallyNotSettings.put("pointsEnabled", true);
        totallyNotSettings.put("drawEnabled", false);
        totallyNotSettings.put("cardsPerTrick", 4);
        ObjectWriter ow = mapper.writer();
        String jsonBeforeURL = ow.writeValueAsString(totallyNotSettings);
        String encodedJSON = URLEncoder.encode(jsonBeforeURL, StandardCharsets.UTF_8);
        int status = callHandler("/settings/GDUEI/" + encodedJSON, "PUT");
        assert status == 200;
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

}
