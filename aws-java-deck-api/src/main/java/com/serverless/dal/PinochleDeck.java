package com.serverless.dal;

import java.util.ArrayList;
import java.util.Random;

/**
 * A PinochleDeck is a deck with 48 cards from 9 to Ace, 2 of each in every suit.
 * The cards are ordered as follows (in decreasing order): A, 10, K, Q, J, 9
 */

public class PinochleDeck extends Deck {
    /**
     * constructs a new Pinochle deck - 2 each of 9-A
     */
    public PinochleDeck() {
        super.deck = new ArrayList<>();
        r = new Random();
        for (int i = 9; i <= 13; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 2; k++) {
                    super.deck.add(new Card(Suit.valueOf(j), i));
                }
            }
        }
        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 2; k++) {
                super.deck.add(new Card(Suit.valueOf(j), 1));
            }
        }
        super.ordering = new ArrayList<>();
        super.ordering.add(1);
        super.ordering.add(10);
        for (int i = 13; i >= 11; i--) {
            super.ordering.add(i);
        }
        super.ordering.add(9);
    }

    /**
     * Returns the type of deck
     *
     * @return pinochle
     */
    public DeckType type() {
        return DeckType.PINOCHLE;
    }
}
