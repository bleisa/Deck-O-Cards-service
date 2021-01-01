package com.game;

/**
 * DeckType is an enum representing the type of a Deck (Poker, Pinochle, or Euchre)
 */

public enum DeckType {
    POKER, PINOCHLE, EUCHRE;

    public static int numCards(DeckType type) {
        if (type == POKER) {
            return 52;
        } else if (type == PINOCHLE) {
            return 48;
        } else {
            return 24;
        }
    }
}
