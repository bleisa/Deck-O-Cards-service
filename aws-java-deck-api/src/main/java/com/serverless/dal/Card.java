package com.serverless.dal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * A simple Card class.
 */
public class Card implements Serializable {
    private final int value;
    private final Suit suit;

    /**
     * constructs a new card
     * @param suit the suit of the card - cannot be null
     * @param value the numerical value of the card - must be between 0 and 13, inclusive
     */
    public Card(@JsonProperty("suit")Suit suit, @JsonProperty("value") int value) {
        if (value <= 0 || value > 13) {
            throw new IllegalArgumentException("Rank must be between 1 and 13, inclusive");
        }
        if (suit == null) {
            throw new IllegalArgumentException("Suit cannot be null");
        }
        this.value = value;
        this.suit = suit;
    }

    /**
     * @return the numerical value of the card
     */
    public int getValue() {
        return value;
    }

    /**
     * @return the suit of the card
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * @return the card as a string of the form "value of suit"
     */
    @Override
    public String toString() {
        return "" + value + " of " + suit.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return value == card.value &&
                suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, suit);
    }
}
