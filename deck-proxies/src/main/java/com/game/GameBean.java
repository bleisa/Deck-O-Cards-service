package com.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A GameBean is a proxy for the Game class, intended for use by the client of the service.
 *
 * A GameBean has settings, a list of players, and information about the current state of the game,
 * including whose turn it is, what suit is trump (if applicable), what cards have been played and how.
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

    // data about the last turn taken
    @JsonProperty("lastPlayer")
    private String lastPlayer;
    @JsonProperty("pass")
    private String passedTo;
    @JsonProperty("cardPlayed")
    private Card cardPlayed;
    @JsonProperty("way")
    private WayToPlay how;

    /**
     * Constructs a new GameBean object with the given access code
     *
     * @param code the code to join this game
     */
    public GameBean(@JsonProperty("code") String code) {
        this.code = code;
    }

    /**
     * Gets the players in this game
     *
     * @return an unmodifiable list of the players
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Gets the access code for this game
     *
     * @return the access code for the game
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the settings for this game
     *
     * @return the settings for this game
     */
    public Settings getSettings() {
        return this.settings;
    }

    /**
     * Sets the settings for this game
     *
     * @param settings the settings to be used
     */
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * Gets the suit that is trump
     *
     * @return the suit that is trump
     */
    public Suit getTrump() {
        return this.trump;
    }

    /**
     * Sets the suit that is trump
     *
     * @param trump the suit that is to be trump
     */
    public void setTrump(Suit trump) {
        this.trump = trump;
    }

    /**
     * Gets the cards that have been played on the current trick
     *
     * @return an unmodifiable list of the cards that have been played on the current trick
     */
    public List<Card> getTrick() {
        return Collections.unmodifiableList(this.trick);
    }

    /**
     * Gets who played the cards on the current trick.
     * The ith card in this.getTrick() was played by the ith player in this.getTrickPlayers()
     *
     * @return an unmodifiable list of the players who played cards on the current trick
     */
    public List<Player> getTrickPlayers() {
        return Collections.unmodifiableList(this.trickPlayers);
    }

    /**
     * Gets the draw pile
     *
     * @return an unmodifiable list of the cards on the draw pile
     */
    public List<Card> getDraw() {
        return Collections.unmodifiableList(this.draw);
    }

    /**
     * Gets the discard pile
     *
     * @return an unmodifiable list of the cards on the discard pile
     */
    public List<Card> getDiscard() {
        return Collections.unmodifiableList(this.discard);
    }

    /**
     * Gets whether the game has been started
     *
     * @return whether the game has been started
     */
    public boolean getStarted() {
        return this.started;
    }

    /**
     * Gets the last player to take a turn
     *
     * @return the player who last took a turn
     */
    @JsonIgnore
    public Player getLastPlayer() {
        return getPlayer(lastPlayer);
    }

    /**
     * If the last player passed a card to another player on their turn, gets the player
     * to whom they passed a card
     *
     * @return the player to whom the last player passed a card, or null if the last player did
     * not pass any cards
     */
    @JsonIgnore
    public Player getPassedTo() {
        if (passedTo != null) {
            return getPlayer(passedTo);
        } else {
            return null;
        }
    }

    /**
     * Gets the card played on the last turn
     *
     * @return the card that the last player played
     */
    @JsonIgnore
    public Card getCardPlayed() {
        return cardPlayed;
    }

    /**
     * Gets how the last player played their card
     *
     * @return the way that the last player played their card
     */
    @JsonIgnore
    public WayToPlay getWayPlayed() {
        return how;
    }

    /**
     * Gets the JSON representation of this object
     *
     * @return the JSON string representation of this object
     * @throws JsonProcessingException if conversion to JSON fails
     */
    public String toJson() throws JsonProcessingException {
        return MAPPER.writer().writeValueAsString(this);
    }

    /**
     * Gets the player whose turn is next
     *
     * @return the player who turn is next
     */
    @JsonIgnore
    public Player getNextPlayer() {
        return getPlayer(nextPlayer);
    }

    /**
     * Gets the player with the given name
     *
     * @param name the name of the player to get
     * @return the player with the given name
     */
    @JsonIgnore
    public Player getPlayer(String name) {
        for (Player p: players) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Constructs a GameBean from a JSON string
     *
     * @param s the string JSON representation
     * @return the GameBean read from the string
     * @throws IOException if JSON processing fails
     */
    public static GameBean fromJson(String s) throws IOException {
        return MAPPER.readValue(s, GameBean.class);
    }
}
