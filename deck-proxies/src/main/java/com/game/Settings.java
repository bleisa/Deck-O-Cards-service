package com.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * A Settings object stores a variety of settings for a card game.
 *
 * Required:    handCounts: representing the number of cards each player should be dealt
 *              deckType:   the type of deck the game is using
 *
 * Optional:    skipEnabled:    allows a player to skip their turn
 *              discardEnabled: allows a player to discard a card from their hand
 *              trickEnabled:   allows a player to play a card to a trick from their hand
 *                                  (a trick is a collection of cards that will be collected by one player)
 *              passEnabled:    allows a player to give a card to another player
 *              showEnabled:    allows a player to show a card from their hand to all other players
 *              drawEnabled:    allows a player to draw a card from a draw pile
 *              aceHigh:        if the deck is a poker deck, whether aces are high
 *              teamsEnabled:   allows players to be on teams
 *              pointsEnabled:  whether the game is being scored
 *
 * For all optional settings except discardEnabled, the default is false.
 * Tricks and discarding cannot be enabled at the same time.
 */
@JsonDeserialize(builder = Settings.SettingsBuilder.class)
public final class Settings {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final int[] cardsPer;
    private final DeckType deckType;
    private final boolean skip;
    private final boolean discard;
    private final boolean trick;
    private final boolean pass;
    private final boolean show;
    private final boolean aceHigh;
    private final boolean teams;
    private final boolean draw;
    private final boolean points;

    private final int cardsPerTrick;

    // TODO: jokers in poker

    private Settings(SettingsBuilder builder) {
        this.cardsPer = builder.cardsPer;
        this.deckType = builder.deckType;
        this.skip = builder.skip;
        this.discard = builder.discard;
        this.trick = builder.trick;
        this.pass = builder.pass;
        this.show = builder.show;
        this.aceHigh = builder.aceHigh;
        this.cardsPerTrick = builder.cardsPerTrick;
        this.teams = builder.teams;
        this.draw = builder.draw;
        this.points = builder.points;
    }

    public boolean isAceHigh() { return aceHigh; }

    public int[] getCardsPer() { return cardsPer; }

    public DeckType getDeckType() { return deckType; }

    public boolean getSkip() { return skip; }

    public boolean getDiscard() { return discard; }

    public boolean getTrick() { return trick; }

    public boolean getPass() { return pass; }

    public boolean getShow() { return show; }

    public int getCardsPerTrick() { return cardsPerTrick; }

    public boolean isTeams() { return teams; }

    public boolean isDraw() { return draw; }

    public boolean isPoints() { return points; }

    public String toJson() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this);
    }

    public static Settings fromJson(String s) throws IOException {
        return MAPPER.readValue(s, Settings.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settings settings = (Settings) o;
        return skip == settings.skip &&
                discard == settings.discard &&
                trick == settings.trick &&
                pass == settings.pass &&
                show == settings.show &&
                aceHigh == settings.aceHigh &&
                teams == settings.teams &&
                draw == settings.draw &&
                points == settings.points &&
                cardsPerTrick == settings.cardsPerTrick &&
                Arrays.equals(cardsPer, settings.cardsPer) &&
                deckType == settings.deckType;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(deckType, skip, discard, trick, pass, show, aceHigh, teams, draw, points, cardsPerTrick);
        result = 31 * result + Arrays.hashCode(cardsPer);
        return result;
    }

    /**
     * A builder class for this object
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class SettingsBuilder {
        // necessary
        private int[] cardsPer;
        private DeckType deckType;

        // defaults
        private boolean skip = false;
        private boolean discard = true;
        private boolean trick = false;
        private boolean pass = false;
        private boolean show = false;
        private boolean aceHigh = false;
        private boolean teams = false;
        private boolean draw = false;
        private boolean points = false;
        @JsonProperty("cardsPerTrick")
        private int cardsPerTrick = 0;

        /**
         * Base for the builder
         *
         * @param handCounts represents the number of cards each player should be dealt;
         *                   the ith player will be dealt handCounts[i] cards
         * @param type      the type of deck this game will use
         */
        public SettingsBuilder(@JsonProperty("cardsPer") int[] handCounts, @JsonProperty("deckType") DeckType type) {
            if (handCounts == null || handCounts.length == 0) {
                throw new IllegalArgumentException("cardsPer cannot be null or empty");
            }
            this.cardsPer = handCounts;
            this.deckType = type;
        }

        /**
         * sets skipEnabled
         *
         * @param skip whether skipping turns is enabled
         * @return this for chaining
         */
        public SettingsBuilder skip(boolean skip) {
            this.skip = skip;
            return this;
        }

        /**
         * sets discardEnabled
         *
         * @param discard whether discarding to a discard pile is enabled
         * @return this for chaining
         */
        public SettingsBuilder discard(boolean discard) {
            this.discard = discard;
            this.trick = false;
            return this;
        }

        /**
         * sets trickEnabled
         *
         * @param trick whether playing to a trick is enabled
         * @return this for chaining
         */
        public SettingsBuilder trick(boolean trick) {
            this.trick = trick;
            this.discard = false;
            return this;
        }

        /**
         * sets passEnabled
         *
         * @param pass whether giving a card to another player is enabled
         * @return this for chaining
         */
        public SettingsBuilder pass(boolean pass) {
            this.pass = pass;
            return this;
        }

        /**
         * sets showEnabled
         *
         * @param show whether showing a card to another player is enabled
         * @return this for chaining
         */
        public SettingsBuilder show(boolean show) {
            this.show = show;
            return this;
        }

        /**
         * sets aceHigh
         *
         * @param aceHigh whether aces are high or low, if deck is poker
         * @return this for chaining
         */
        public SettingsBuilder aceHigh(boolean aceHigh) {
            this.aceHigh = aceHigh;
            return this;
        }

        /**
         * sets teamsEnabled
         *
         * @param teams whether players are on teams
         * @return this for chaining
         */
        public SettingsBuilder teams(boolean teams) {
            this.teams = teams;
            return this;
        }

        /**
         * sets drawEnabled
         *
         * @param draw whether drawing from a draw pile is enabled
         * @return this for chaining
         */
        public SettingsBuilder draw(boolean draw) {
            this.draw = draw;
            return this;
        }

        /**
         * sets pointsEnabled
         *
         * @param points whether the game is keeping score
         * @return this for chaining
         */
        public SettingsBuilder points(boolean points) {
            this.points = points;
            return this;
        }

        /**
         * sets the number of cards each player is dealt
         *
         * @param counts an array representing the number of cards to be dealt to each player
         * @return this for chaining
         */
        @JsonIgnore
        public SettingsBuilder counts(int[] counts) {
            if (counts == null || counts.length == 0) {
                throw new IllegalArgumentException("cardsPer cannot be null or empty");
            }
            this.cardsPer = counts;
            return this;
        }

        /**
         * sets the deck type
         *
         * @param type the type of deck to be used
         * @return this for chaining
         */
        @JsonIgnore
        public SettingsBuilder deckType(DeckType type) {
            this.deckType = type;
            return this;
        }

        /**
         * builds the Settings object
         *
         * @return a new Settings object using the settings selected, or defaults if not selected
         */
        public Settings build() {
            if (trick) {
                this.cardsPerTrick = this.cardsPer.length;
            } else {
                this.cardsPerTrick = 0;
            }
            return new Settings(this);
        }
    }
}
