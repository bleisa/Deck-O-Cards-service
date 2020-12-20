import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A GameBean is a proxy for the Game class, intended for use by the client of the service.
 *
 */
public class GameBean {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("players")
    private List<Player> players;
    @JsonProperty("nextPlayer")
    private String nextPlayer;
    private final String code;
    @JsonProperty("settings")
    private Settings settings;
    @JsonProperty("trump")
    private Suit trump;

    // fields for games with trick enabled
    @JsonProperty("trick")
    private List<Card> trick;
    @JsonProperty("trickPlayers")
    private List<Player> trickPlayers;
    @JsonProperty("count")
    private int count;

    // for if draw is enabled
    @JsonProperty("draw")
    private List<Card> draw;

    // for if discard is enabled
    @JsonProperty("discard")
    private List<Card> discard;

    @JsonProperty("started")
    private boolean started;

    public GameBean(@JsonProperty("code") String code) {
        this.code = code;
    }

    public List<Player> getPlayers() {
        return List.copyOf(players);
    }

    public String getCode() {
        return code;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public Suit getTrump() {
        return this.trump;
    }

    public void setTrump(Suit trump) {
        this.trump = trump;
    }

    public List<Card> getTrick() {
        return List.copyOf(this.trick);
    }

    public List<Player> getTrickPlayers() {
        return List.copyOf(this.trickPlayers);
    }

    public List<Card> getDraw() {
        return List.copyOf(this.draw);
    }

    public List<Card> getDiscard() {
        return List.copyOf(this.discard);
    }

    public boolean getStarted() {
        return this.started;
    }

    public String toJson() throws JsonProcessingException {
        return MAPPER.writer().writeValueAsString(this);
    }

    @JsonIgnore
    public Player getNextPlayer() {
        return getPlayer(nextPlayer);
    }

    @JsonIgnore
    public Player getPlayer(String name) {
        for (Player p: players) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public static GameBean fromJson(String s) throws IOException {
        return MAPPER.readValue(s, GameBean.class);
    }
}
