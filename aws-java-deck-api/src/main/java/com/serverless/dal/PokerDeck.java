package com.serverless.dal;

import java.util.ArrayList;
import java.util.Random;

public class PokerDeck extends Deck {

    /**
     * constructs a new Poker deck - one each of A-K
     */
    public PokerDeck(boolean aceHigh) {
        super.deck = new ArrayList<>();
        r = new Random();
        for (int i = 1; i <= 13; i++) {
            for (int j = 0; j < 4; j++) {
                super.deck.add(new Card(Suit.valueOf(j), i));
            }
        }
        super.ordering = new ArrayList<>();
        if (aceHigh) {
            super.ordering.add(1);
            for (int i = 13; i >= 2; i--) {
                super.ordering.add(i);
            }
        } else {
            for (int i = 13; i >= 1; i--) {
                super.ordering.add(i);
            }
        }
    }

    public DeckType type() {
        return DeckType.POKER;
    }
}
