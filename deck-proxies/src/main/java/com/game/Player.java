package com.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A Player is a mutable representation of a player in a card game. A Player has a name, a hand
 * of cards, a score, and an optional team name.
 *
 * This Player is intended for use by the client.
 */

public class Player {
    private static final boolean CHECK_REP = false;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("hand")
    private List<Card> hand;
    private final String name;
    @JsonProperty("collected")
    private List<Card> collected;
    @JsonProperty("shown")
    private List<Card> shown;
    private int points;
    private String teamName;

    // Abstraction Function: this.hand is the player's hand
    //                       this.name is the player's name
    //                       this.collected is the list of the cards the player has collected
    //                       this.shown is the list of cards they are currently showing
    //                       this.points is the number of points the player has scored
    //                       this.teamName is the name of the team the player is on

    // Representation Invariant: this.hand != null
    //                           this.name != null
    //                           this.collected != null
    //                           this.shown != null

    /**
     * Constructs a new Player object with no cards, points, or team
     *
     * @param name the name of the player - cannot be null
     */
    public Player(@JsonProperty("name")String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
        hand = new ArrayList<>();
        collected = new ArrayList<>();
        shown = new ArrayList<>();
        points = 0;
        teamName = null;
        checkRep();
    }


    /**
     * Returns the name of the player
     *
     * @return the name of the player
     */
    public String getName() {
        checkRep();
        return name;
    }

    /**
     * Returns the hand of this player
     *
     * @return the hand of this player
     */
    public List<Card> getHand() {
        checkRep();
        return Collections.unmodifiableList(this.hand);
    }

    /**
     * Returns a list of the cards that this player has collected (i.e. through winning tricks)
     *
     * @return the cards that have been collected by this player
     */
    public List<Card> getCollected() {
        checkRep();
        return Collections.unmodifiableList(collected);
    }

    /**
     * Returns the cards this player is allowing all other players to view
     *
     * @return the list of cards on view
     */
    public List<Card> getShown() {
        checkRep();
        return Collections.unmodifiableList(shown);
    }

    /**
     * Returns this player's score
     *
     * @return the number of points this player has
     */
    public int getPoints() {
        checkRep();
        return points;
    }

    /**
     * Adds points to this player's current points
     *
     * @param points the number of points to be given to this player (can be positive or negative)
     */
    public void addPoints(int points) {
        checkRep();
        this.points += points;
        checkRep();
    }

    /**
     * Returns the team this player is one
     *
     * @return the name of the team this player is on; null if none
     */
    public String getTeamName() {
        checkRep();
        return teamName;
    }

    /**
     * Sets this player's team name
     *
     * @param teamName the name of the team this player is joining
     */
    public void setTeamName(String teamName) {
        this.teamName = teamName;
        checkRep();
    }

    /**
     * gets the JSON representation of this object
     *
     * @return the JSON string representation of this object
     * @throws JsonProcessingException if conversion fails
     */
    public String toJson() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this);
    }

    /**
     * checks if another player is on the same team as this player
     *
     * @param p the other player to check
     * @return whether p is on the same team as this player
     */
    public boolean isOnSameTeam(Player p) {
        return this.teamName.equals(p.teamName);
    }

    /**
     * checks if this player still has cards left in their hand
     *
     * @return whether this player still has cards in their hand
     */
    public boolean hasCardsLeft() {
        return !this.hand.isEmpty();
    }

    /**
     * gets the number of cards left in this player's hand
     *
     * @return the number of cards left in this player's hand
     */
    public int numberOfCardsLeft() {
        return this.hand.size();
    }

    /**
     * gets the card at index in this player's hand
     *
     * @param index the index to get
     * @return the card at index from the player's hand
     */
    public Card getFromHand(int index) {
        return this.hand.get(index);
    }

    /**
     * Two players are equal if they have the same name
     *
     * @param o the object to be compared
     * @return whether the object equals this player
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Player player = (Player) o;
        return this.name.equals(player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    private void checkRep() {
        if (CHECK_REP) {
            assert this.name != null;
            assert this.hand != null;
            assert this.collected != null;
            assert this.shown != null;
        }
    }
}
