package com.serverless.dal;

public enum Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES;

    /**
     * 0 -> hearts, 1 -> diamonds, 2 -> clubs, 3-> spades
     * @param j indicates which suit to find - must be in [0, 3]
     * @return the suit corresponding to j
     */
    public static Suit valueOf(int j) {
        if (j < 0 || j >= 4) {
            throw new IllegalArgumentException("There are exactly four suits; you asked for " + j);
        }
        if (j == 0) {
            return HEARTS;
        } else if (j == 1) {
            return DIAMONDS;
        } else if (j == 2) {
            return CLUBS;
        } else {
            return SPADES;
        }
    }

    /**
     * @param suit the suit to be converted to an int
     * @return 0 if hearts, 1 if diamonds, 2 if clubs, 3 if spades
     */
    public static int numericalEquivalent(Suit suit) {
        if (suit.equals(HEARTS)) {
            return 0;
        } else if (suit.equals(DIAMONDS)) {
            return 1;
        } else if (suit.equals(CLUBS)) {
            return 2;
        } else {
            return 3;
        }
    }
}
