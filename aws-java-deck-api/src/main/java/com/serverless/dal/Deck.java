package com.serverless.dal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class Deck implements Serializable {
    protected static Random r;
    protected List<Card> deck;
    protected List<Integer> ordering;
    // speed up potential: replace with Map<Integer, Integer>: essentially map from value of card
    // to its order. Then comparing cards will be faster

    /**
     * shuffles the deck
     */
    public void shuffle() { // modern Fisher-Yates shuffle from Wikipedia
        shuffle(deck);
    }

    /**
     * shuffles a subset of the deck
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
     * shuffles and deals the deck, giving the ith player the number of cards at the ith index of cards
     * @throws IllegalArgumentException if attempting to deal more cards than are in the deck
     * @param cards the numbers of cards to be dealt to each player
     * @return a list of hands
     */
    public List<List<Card>> deal(int[] cards) {
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
        return deal;
    }

    /**
     * @return the number of cards in the deck
     */
    public int size() {
        return deck.size();
    }

    /**
     * @return the type of deck (as an enum)
     */
    abstract public DeckType type();

    /**
     * compares two card values and returns the greater of the two based on the ordering of
     * the deck
     * @param v1 the value of one card
     * @param v2 the value of another card
     * @return whichever is greater in this deck, or -1 if neither is found
     */
    public int compareCardValue(int v1, int v2) {
        for (int i : ordering) {
            if (i == v1) {
                return v1;
            } else if (i == v2) {
                return v2;
            }
        }
        return -1;
    }
}
