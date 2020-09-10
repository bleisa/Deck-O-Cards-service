package com.serverless.dal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.*;

public class Player implements Serializable {
    private List<Card> hand;
    private final String name;
    private List<Card> collected;
    private List<Card> shown;
    private int points;
    private String teamName;

    /**
     * Constructs a new Player object
     * @param name the name of the player - cannot be null or contain /
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
    }


    /**
     * @return the name of the player
     */
    public String getName() { return name; }

    /**
     * Setter for field hand
     * @param hand the list of cards that the hand is set to
     */
    public void setHand(List<Card> hand) { this.hand = hand; }

    /**
     * @return the hand of this player
     */
    public List<Card> getHand() { return hand; }

    /**
     * @return a list of the cards that have been collected by this player
     */
    public List<Card> getCollected() {return collected; }

    /**
     * Setter for field collected
     * @param collected the list of cards for collected to be set to
     */
    public void setCollected(List<Card> collected) { this.collected = collected; }

    /**
     * @return the list of cards currently "owned" by this player but being shown to all
     * other players
     */
    public List<Card> getShown() { return shown; }

    /**
     * Sets the list of cards currently being shown by this player
     * @param shown the list of cards to be set to
     */
    public void setShown(List<Card> shown) { this.shown = shown; }

    /**
     * @return the number of points this player has
     */
    public int getPoints() { return points; }

    /**
     * sets this player's points
     * @param points the number of points to be given to this player
     */
    public void setPoints(int points) { this.points = points; }

    /**
     * adds points to this player's current points
     * @param points the number of points to be given to this player
     */
    public void addPoints(int points) { this.points += points; }

    /**
     * @return the name of the team this player is on; null if none
     */
    public String getTeamName() { return teamName; }

    /**
     * sets this player's team
     * @param teamName the name of the team this player is joining
     */
    public void setTeamName(String teamName) { this.teamName = teamName; }

    /**
     * two players are equal if they have the same name
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
        return Objects.hash(hand, name);
    }
}
