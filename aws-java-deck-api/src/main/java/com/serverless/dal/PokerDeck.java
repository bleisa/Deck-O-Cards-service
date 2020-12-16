package com.serverless.dal;

import java.util.ArrayList;
import java.util.Random;

/**
 * A PokerDeck is a deck with 52 cards from Ace to King, one of each in every suit.
 * Ace can be high or low; otherwise, cards are ordered in increasing numerical order,
 * ending with 10, J, Q, K (optional A).
 */

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
        checkRep();
    }

    /**
     * Returns the type of deck
     *
     * @return Poker
     */
    public DeckType type() {
        return DeckType.POKER;
    }

    private void checkRep() {
        assert super.deck != null;
        assert super.ordering != null;
    }
}
