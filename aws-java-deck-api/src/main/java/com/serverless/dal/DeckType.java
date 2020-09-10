package com.serverless.dal;

public enum DeckType {
    POKER, PINOCHLE, EUCHRE;

    public static int validSize(DeckType type) {
        if (type == POKER) {
            return 52;
        } else if (type == PINOCHLE) {
            return 48;
        } else {
            return 24;
        }
    }
}
