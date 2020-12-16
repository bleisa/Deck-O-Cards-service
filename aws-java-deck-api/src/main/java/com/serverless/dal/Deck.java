package com.serverless.dal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A Deck is an abstract representation of a deck of playing cards. This class is designed
 * to be subclassed.
 */

public abstract class Deck {
    private static final boolean checkRep = false;
    protected static Random r;
    protected List<Card> deck;
    protected List<Integer> ordering;
    // speed up potential: replace with Map<Integer, Integer>: essentially map from value of card
    // to its order. Then comparing cards will be faster

    // Abstraction Function: this.deck is the list of cards in the deck.
    //                       this.ordering is the order in which cards are considered
    //                              to be greater than each other; if the value of card A
    //                              is after the value of card B in this.ordering, then
    //                              A > B

    // Representation Invariant: this.deck != null
    //                           this.ordering != null

    /**
     * shuffles the deck
     */
    public void shuffle() { // modern Fisher-Yates shuffle from Wikipedia
        checkRep();
        shuffle(deck);
        checkRep();
    }

    /**
     * shuffles a subset of a deck
     *
     * @param subset the list of cards to be shuffled
     */
    public static void shuffle(List<Card> subset) {
        int n = subset.size() - 1;
        for (int i = n; i > 0; i--) {
            int index = r.nextInt(i);
            Card last = subset.remove(i);
            Card swap = subset.remove(index);
            subset.add(index, last);
            subset.add(swap);
        }
    }

    /**
     * Shuffles and deals the deck into cards.length hands. The ith hand has cards[i] cards in it.
     * If there are cards left over, puts them in the last index of the returned list for a draw pile.
     *
     * @throws IllegalArgumentException if attempting to deal more cards than are in the deck
     * @param cards the numbers of cards to be dealt to each player
     * @return a list of hands
     */
    public List<List<Card>> deal(int[] cards) {
        checkRep();
        int total = 0;
        for (int i: cards) {
            total += i;
        }
        if (total > deck.size()) {
            throw new IllegalArgumentException();
        }
        shuffle();
        List<List<Card>> deal = new ArrayList<>();
        int used = 0;
        for (int i: cards) { // O(N) time where N is the number of cards in the deck
            List<Card> hand = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                hand.add(deck.get(used + j));
            }
            used += i;
            deal.add(hand);
        }
        List<Card> remaining = new ArrayList<>();
        for (int i = used; i < size(); i++) {
            remaining.add(deck.get(i));
        }
        deal.add(remaining);
        checkRep();
        return deal;
    }

    /**
     * Returns the size of the deck
     *
     * @return the number of cards in the deck
     */
    public int size() {
        checkRep();
        return deck.size();
    }

    /**
     * Returns the type of the deck - to be implemented by children
     *
     * @return the type of deck (as an enum)
     */
    abstract public DeckType type();

    /**
     * Compares two card values and returns the greater of the two based on the ordering of
     * the deck
     *
     * @param v1 the value of one card
     * @param v2 the value of another card
     * @return whichever is greater in this deck, or -1 if neither is found
     */
    public int compareCardValue(int v1, int v2) {
        checkRep();
        for (int i : ordering) {
            if (i == v1) {
                return v1;
            } else if (i == v2) {
                return v2;
            }
        }
        checkRep();
        return -1;
    }

    private void checkRep() {
        if (checkRep) {
            assert this.deck != null;
            assert this.ordering != null;
        }
    }
}
