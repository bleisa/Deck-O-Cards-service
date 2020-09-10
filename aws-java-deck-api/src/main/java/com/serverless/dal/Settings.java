package com.serverless.dal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Settings {
    private final int[] cardsPer;
    private DeckType deckType;
    private final boolean skipEnabled;
    private final boolean discardEnabled;
    private final boolean trickEnabled;
    private final boolean passEnabled;
    private final boolean showEnabled;
    private final boolean aceHigh;
    private final boolean teamsEnabled;
    private final boolean drawEnabled;
    private final boolean pointsEnabled;

    private final int cardsPerTrick;

    // TODO: jokers in poker

    public Settings(@JsonProperty("cardsPer") int[] cardsPer, @JsonProperty("deckType") DeckType deckType,
                    @JsonProperty("skipEnabled") boolean skip, @JsonProperty("discardEnabled") boolean discard,
                    @JsonProperty("trickEnabled") boolean trickEnabled, @JsonProperty("passEnabled") boolean pass,
                    @JsonProperty("showEnabled") boolean show, @JsonProperty("aceHigh") boolean aceHigh,
                    @JsonProperty("teamsEnabled") boolean teams, @JsonProperty("drawEnabled") boolean draw,
                    @JsonProperty("pointsEnabled") boolean points, @JsonProperty("cardsPerTrick") int cardsPerTrick) {
        if (discard && trickEnabled) {
            throw new IllegalArgumentException("both discard and trick cannot be enabled");
        }
        if (cardsPer == null || cardsPer.length == 0) {
            throw new IllegalArgumentException("cardsPer cannot be null or empty");
        }
        this.cardsPer = cardsPer;
        this.deckType = deckType;
        this.skipEnabled = skip;
        this.discardEnabled = discard;
        this.trickEnabled = trickEnabled;
        this.passEnabled = pass;
        this.showEnabled = show;
        this.aceHigh = aceHigh;
        this.cardsPerTrick = cardsPerTrick;
        this.teamsEnabled = teams;
        this.drawEnabled = draw;
        this.pointsEnabled = points;
    }

    public boolean isAceHigh() { return aceHigh; }

    public int[] getCardsPer() { return cardsPer; }

    public DeckType getDeckType() { return deckType; }
    public void setDeckType(DeckType type) { this.deckType = type; }

    public boolean getSkipEnabled() { return skipEnabled; }

    public boolean getDiscardEnabled() { return discardEnabled; }

    public boolean getTrickEnabled() { return trickEnabled; }

    public boolean getPassEnabled() { return passEnabled; }

    public boolean getShowEnabled() { return showEnabled; }

    public int getCardsPerTrick() { return cardsPerTrick; }

    public boolean isTeamsEnabled() { return teamsEnabled; }

    public boolean isDrawEnabled() { return drawEnabled; }

    public boolean isPointsEnabled() { return pointsEnabled; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settings settings = (Settings) o;
        return skipEnabled == settings.skipEnabled &&
                discardEnabled == settings.discardEnabled &&
                trickEnabled == settings.trickEnabled &&
                passEnabled == settings.passEnabled &&
                showEnabled == settings.showEnabled &&
                aceHigh == settings.aceHigh &&
                teamsEnabled == settings.teamsEnabled &&
                drawEnabled == settings.drawEnabled &&
                pointsEnabled == settings.pointsEnabled &&
                cardsPerTrick == settings.cardsPerTrick &&
                Arrays.equals(cardsPer, settings.cardsPer) &&
                deckType == settings.deckType;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(deckType, skipEnabled, discardEnabled, trickEnabled, passEnabled, showEnabled, aceHigh, teamsEnabled, drawEnabled, pointsEnabled, cardsPerTrick);
        result = 31 * result + Arrays.hashCode(cardsPer);
        return result;
    }
}
