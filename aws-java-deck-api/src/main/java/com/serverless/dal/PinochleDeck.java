package com.serverless.dal;

import java.util.ArrayList;
import java.util.Random;

public class PinochleDeck extends Deck {
    /**
     * constructs a new Pinochle deck - 2 each of 9-A
     */
    public PinochleDeck() {
        super.deck = new ArrayList<>();
        super.r = new Random();
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

    public DeckType type() {
        return DeckType.PINOCHLE;
    }
}
