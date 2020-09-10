package com.serverless.dal;

import java.util.ArrayList;
import java.util.Random;

public class EuchreDeck extends Deck {
    /**
     * Constructs a new Euchre deck (one each of 9-A)
     */
    public EuchreDeck() {
        super.deck = new ArrayList<>();
        r = new Random();
        for (int i = 9; i <= 13; i++) {
            for (int j = 0; j < 4; j++) {
                super.deck.add(new Card(Suit.valueOf(j), i));
            }
        }
        for (int j = 0; j < 4; j++) {
            super.deck.add(new Card(Suit.valueOf(j), 1));
        }
        super.ordering = new ArrayList<>();
        super.ordering.add(11);
        super.ordering.add(1);
        super.ordering.add(13);
        super.ordering.add(12);
        super.ordering.add(10);
        super.ordering.add(9);
        // TODO: left jack
    }

    public DeckType type() {
        return DeckType.EUCHRE;
    }
}
