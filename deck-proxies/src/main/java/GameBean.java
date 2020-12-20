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
    private Settings settings;
    private Suit trump;

    // fields for games with trick enabled
    private List<Card> trick;
    private List<Player> trickPlayers;
    private int count;

    // for if draw is enabled
    private List<Card> draw;

    // for if discard is enabled
    private List<Card> discard;

    private boolean started;

    public GameBean(@JsonProperty("code") String code) {
        this.code = code;
    }

    public List<Player> getPlayers() {
        return players;
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
        return this.trick;
    }

    public void setTrick(List<Card> trick) {
        this.trick = trick;
    }

    public List<Player> getTrickPlayers() {
        return this.trickPlayers;
    }

    public void setTrickPlayers(List<Player> trickPlayers) {
        this.trickPlayers = trickPlayers;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Card> getDraw() {
        return this.draw;
    }

    public void setDraw(List<Card> draw) {
        this.draw = draw;
    }

    public List<Card> getDiscard() {
        return this.discard;
    }

    public void setDiscard(List<Card> discard) {
        this.discard = discard;
    }

    public boolean getStarted() {
        return this.started;
    }

    public void setStarted(boolean started) {
        this.started = started;
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
