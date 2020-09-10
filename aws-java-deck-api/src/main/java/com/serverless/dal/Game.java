package com.serverless.dal;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@DynamoDBTable(tableName = "PLACEHOLDER")
public class Game {
    private static final String GAMES_TABLE_NAME = "deck-o-cards-games-table";
    private final DynamoDBMapper dbMapper;
    private final AmazonDynamoDB ddb;

    private static final Logger LOG = LogManager.getLogger(Game.class);

    private final char[] alphabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private List<Player> players;
    private int nextPlayer;
    private Card cardPlayed;
    private WayToPlay way;
    private String code;
    private Settings settings;
    private Suit trump;

    // fields for games with trick enabled
    private List<Card> trick;
    private List<Player> trickPlayers;
    private int count;

    // for if draw is enabled
    private List<Card> draw;

    // for if discard is enabled
    private List<Card> discard;

    private boolean started;

    private final Random r;

    public Game() {
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(GAMES_TABLE_NAME))
                .build();
        ddb = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .build();
        dbMapper = new DynamoDBMapper(ddb, mapperConfig);
        r = new Random();
        players = new ArrayList<>();
        started = false;
        trick = new ArrayList<>();
        trickPlayers = new ArrayList<>();
        count = 0;
        discard = new ArrayList<>();
        draw = new ArrayList<>();
        cardPlayed = null;
        way = null;
        code = generateCode();
    }

    @DynamoDBTypeConvertedEnum
    @DynamoDBTypeConverted(converter = SuitConverter.class)
    @DynamoDBAttribute(attributeName = "trump")
    public Suit getTrump() { return this.trump; }
    public void setTrump(Suit s) { this.trump = s; }

    @DynamoDBTypeConverted(converter = PlayerConverter.class)
    @DynamoDBAttribute(attributeName = "players")
    public List<Player> getPlayers() { return this.players; }
    public void setPlayers(List<Player> players) { this.players = players; }

    @DynamoDBTypeConverted(converter = CardListConverter.class)
    @DynamoDBAttribute(attributeName = "draw pile")
    public List<Card> getDraw() { return this.draw; }
    public void setDraw(List<Card> draw) { this.draw = draw; }

    @DynamoDBTypeConverted(converter = CardListConverter.class)
    @DynamoDBAttribute(attributeName = "discard pile")
    public List<Card> getDiscard() { return this.discard; }
    public void setDiscard(List<Card> discard) { this.discard = discard; }

    @DynamoDBTypeConverted(converter = CardListConverter.class)
    @DynamoDBAttribute(attributeName = "trick")
    public List<Card> getTrick() { return this.trick; }
    public void setTrick(List<Card> trick) { this.trick = trick; }

    @DynamoDBTypeConverted(converter = PlayerConverter.class)
    @DynamoDBAttribute(attributeName = "trick order")
    public List<Player> getTrickPlayers() { return this.trickPlayers; }
    public void setTrickPlayers(List<Player> trickPlayers) { this.trickPlayers = trickPlayers; }

    @DynamoDBAttribute(attributeName = "count")
    public int getCount() { return this.count; }
    public void setCount(int count) { this.count = count; }

    @DynamoDBAttribute(attributeName = "next_player")
    public int getNextPlayer() { return nextPlayer; }
    public void setNextPlayer(int index) { nextPlayer = index; }

    @DynamoDBAttribute(attributeName = "started")
    public boolean getStarted() { return started; }
    public void setStarted(boolean started) {
        this.started = started;
    }

    @DynamoDBTypeConverted(converter = CardConverter.class)
    @DynamoDBAttribute(attributeName = "card_played")
    public Card getCardPlayed() { return cardPlayed; }
    public void setCardPlayed(Card c) { cardPlayed = c; }

    @DynamoDBTypeConvertedEnum
    @DynamoDBTypeConverted(converter = WayToPlayConverter.class)
    @DynamoDBAttribute(attributeName = "way_played")
    public WayToPlay getWay() { return this.way; }
    public void setWay(WayToPlay way) { this.way = way; }

    @DynamoDBHashKey(attributeName = "code")
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    @DynamoDBTypeConverted(converter = SettingsConverter.class)
    @DynamoDBAttribute(attributeName = "settings")
    public Settings getSettings() { return settings; }
    public void setSettings(Settings s) { this.settings = s; }

    /**
     * Retrieves the game with the given code from the DynamoDB database; returns null
     * if it does not exist
     * @param code the entry code for the desired game - cannot be null;
     * @return the game with this code
     */
    public Game getGame(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Code cannot be null");
        }
        HashMap<String, AttributeValue> av = new HashMap<>();
        av.put(":v1", new AttributeValue().withS(code));
        DynamoDBQueryExpression<Game> queryExp = new DynamoDBQueryExpression<Game>()
                .withKeyConditionExpression("code = :v1")
                .withExpressionAttributeValues(av);

        PaginatedQueryList<Game> result = dbMapper.query(Game.class, queryExp);
        if (result.size() > 0) {
            LOG.info("Game " + code + " found");
            return result.get(0);
        } else {
            LOG.info("Game " + code + " not found");
            return null;
        }
    }

    /**
     * @return a list of all the games;
     */
    public List<Game> listGames() {
        DynamoDBScanExpression scanExp = new DynamoDBScanExpression();
        return dbMapper.scan(Game.class, scanExp);
    }

    /**
     * generates a random code that no other game is using
     * @return the resulting entry code
     */
    private String generateCode() { // only call once, when constructing
        String result;
        do {
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                code.append(alphabet[r.nextInt(alphabet.length)]);
            }
            code = new StringBuilder(code.toString().toUpperCase());
            result = code.toString();
        } while (getGame(result) != null);
        return result;
    }

    /**
     * saves the game
     * @param g the game to be saved
     */
    public void save(Game g) {
        LOG.info("Game " + g.code + " saved");
        dbMapper.save(g);
    }

    /**
     * removes a game from the database
     * @param code the code of the game to be deleted
     * @return whether the game existed
     */
    public Boolean delete(String code) {
        Game g = this.getGame(code);
        if (g != null) {
            LOG.info("Game " + g.code + " deleted");
            dbMapper.delete(g);
            return true;
        } else {
            return false;
        }
    }

    // TODO: note that currently with only remembering index, if client's list is not same order, will be wrong

    /**
     * @return the player whose turn it is
     */
    @JsonIgnore
    @DynamoDBIgnore
    public Player getWhoseTurn() {
        return players.get(nextPlayer);
    }

    /**
     * adds the player to this game
     * @throws IllegalStateException if game has started
     * @param p the player to be added - no other player can have this player's name
     */
    public void joinGame(Player p) {
        if (started) {
            LOG.error(p.getName() + "attempted to join game after it started");
            throw new IllegalStateException("cannot join game after it has started");
        }
        for (Player pl: players) { // speed up using map from names to players (if keyset contains the name, reject)
            if (p.getName().equals(pl.getName())) {
                throw new IllegalArgumentException("another player already has this name");
            }
        }
        LOG.info("Player " + p.getName() + " added");
        players.add(p);
    }

    // TODO: contemplate allowing a list of cards to be passed in rather than a single card
    // (esp. for showing cards)

    /**
     * Records the turn taken in the database
     * @throws IllegalStateException if the game has not started yet
     * @throws IllegalArgumentException if the way the card was played has not been enabled for this game
     * or if how is TRICK the player whose turn it is does not have the card that was played
     * @param p the player who played
     * @param card the card that was played
     * @param how the way that it was played
     * @param pass if the card was passed, the player it was passed to - cannot be null if how is PASS
     */
    public void takeTurn(Player p, Card card, WayToPlay how, Player pass) {
        if (!started) {
            LOG.error("attempted to take turn before game started");
            throw new IllegalStateException("Game has not started yet");
        }
        if ((!settings.getSkipEnabled() && how.equals(WayToPlay.SKIP)) ||
                (!settings.getDiscardEnabled() && how.equals(WayToPlay.DISCARD)) ||
                (!settings.getPassEnabled() && how.equals(WayToPlay.PASS)) ||
                (!settings.getShowEnabled() && how.equals(WayToPlay.SHOW)) ||
                (!settings.getTrickEnabled() && how.equals(WayToPlay.TRICK)) ||
                (!settings.isDrawEnabled() && how.equals(WayToPlay.DRAW))) {
            LOG.error(how.toString() + " is not enabled");
            throw new IllegalArgumentException(how.toString() + " is not enabled");
        }
        if (how.equals(WayToPlay.PASS) && pass == null) {
            LOG.error("player to whom to pass not specified");
            throw new IllegalArgumentException("Please specify to whom to pass");
        }
        if (how.equals(WayToPlay.TRICK) && !p.getHand().contains(card)) {
            LOG.error(p.getName() + " does not have " + card.toString());
            throw new IllegalArgumentException("Player " + p.getName() + " does not have " + card.toString());
        }
        if (!p.equals(getWhoseTurn())) {
            LOG.error("not " + p.getName() + "'s turn");
            throw new IllegalArgumentException("It is not " + p.getName() + "'s turn");
        }
        cardPlayed = card;
        way = how;
        if (!(how.equals(WayToPlay.DRAW) || how.equals(WayToPlay.PICKUP))) {
            p.getHand().remove(card);
        }
        if (nextPlayer == players.size() - 1) {
            nextPlayer = 0;
        } else {
            nextPlayer++;
        }
        if (how.equals(WayToPlay.PASS)) {
            pass.getHand().add(card);
            if (LOG.getLevel().equals(Level.DEBUG)) {
                LOG.debug(p.getName() + " passed " + card + " to " + pass.getName());
            }
        }
        if (how.equals(WayToPlay.TRICK)) {
            trick.add(card);
            trickPlayers.add(p);
            count++;
            if (LOG.getLevel().equals(Level.DEBUG)) {
                LOG.debug(p.getName() + " played " + card + " in a trick");
            }
            if (count == settings.getCardsPerTrick()) {
                collectTrick();
            }
        }
        if (how.equals(WayToPlay.SHOW)) {
            p.getShown().add(card);
        }
        if (how.equals(WayToPlay.DISCARD)) {
            discard.add(card);
        }
        if (how.equals(WayToPlay.DRAW)) {
            Card c = draw.remove(draw.size() - 1);
            p.getHand().add(c);
        }
        if (how.equals(WayToPlay.PICKUP)) {
            p.getShown().remove(card);
            p.getHand().add(card);
        }
        if (settings.isDrawEnabled() && draw.size() == 0 && discard.size() > 0) {
            Deck.shuffle(discard);
            draw = discard;
            discard = new ArrayList<>();
        }
    }

    /**
     * deals the deck and assigns each player a hand; if cards are left over, assigns
     * them to the draw pile
     * @throws IllegalStateException if the game has not started yet
     */
    public void deal() {
        if (!started) {
            throw new IllegalStateException("Game has not started yet");
        }
        Deck deck = getDeck();
        List<List<Card>> deal = deck.deal(settings.getCardsPer());
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setHand(deal.get(i));
        }
        if (deal.size() > players.size()) {
            draw = deal.get(players.size());
        }
    }

    /**
     * @return the type of deck this game uses
     */
    @JsonIgnore
    @DynamoDBIgnore
    private Deck getDeck() {
        if (settings.getDeckType() == DeckType.POKER) {
            return new PokerDeck(settings.isAceHigh());
        } else if (settings.getDeckType() == DeckType.PINOCHLE) {
            return new PinochleDeck();
        } else {
            return new EuchreDeck();
        }
    }

    // actually not a necessary method
    // also could be sped up using map
    /**
     * retrieves the hand of the player (so it can be updated in the param player object)
     * @throws IllegalStateException if game has not started yet
     * @param p the player whose hand is to be found - cannot be null
     * @return the hand of the player, or null if the player is not found
     */
    @JsonIgnore
    @DynamoDBIgnore
    public List<Card> getHand(Player p) {
        if (!started) {
            throw new IllegalStateException("Game has not started yet");
        }
        for (Player player : players) {
            if (player.equals(p)) {
                return player.getHand();
            }
        }
        return null;
    }

    /**
     * starts the game
     * @throws IllegalStateException if settings are still null, i.e. have not been set yet
     * @throws IllegalStateException if the number of players in settings does not match the number
     * of players in the game
     */
    public void startGame() {
        if (settings == null) {
            throw new IllegalStateException("Settings must be set before game can start");
        }
        if (settings.getCardsPer().length != players.size()) {
            String message;
            if (settings.getCardsPer().length > players.size()) {
                message = "There are not enough players";
            } else {
                message = "There are too many players";
            }
            throw new IllegalStateException(message);
        }
        setStarted(true);
    }

    /**
     * compares two cards
     * @param c1 the card that was played first of the two
     * @param c2 the card that was played second of the two
     * @return the card that will win; trump suit always wins; within suits, higher rank wins;
     * if the cards are different suits and neither is trump, the first one played (c1) wins
     */
    private Card compare(Card c1, Card c2) {
        if (c1.getSuit().equals(trump) && !c2.getSuit().equals(trump)) {
            return c1;
        } else if (c2.getSuit().equals(trump) && !c1.getSuit().equals(trump)) {
            return c2;
        } else if (c1.getSuit().equals(c2.getSuit()) &&
                getDeck().compareCardValue(c1.getValue(), c2.getValue()) == c1.getValue()) {
            return c1;
        } else if (c1.getSuit().equals(c2.getSuit()) &&
                getDeck().compareCardValue(c1.getValue(), c2.getValue()) == c2.getValue()) {
            return c2;
        } else {
            return c1;
        }
    }

    /**
     * awards the cards on a trick to the player with the highest card
     */
    private void collectTrick() {
        int index;
        Card c = compare(trick.get(0), trick.get(1));
        if (c.equals(trick.get(0))) {
            index = 0;
        } else {
            index = 1;
        }
        for (int i = 2; i < trick.size(); i++) {
            Card oldC = c;
            c = compare(c, trick.get(i));
            if (!oldC.equals(c)) {
                index = i;
            }
        }
        getPlayer(trickPlayers.get(index).getName()).getCollected().addAll(trick);
        if (LOG.getLevel().equals(Level.DEBUG)) {
            LOG.debug(trickPlayers.get(index).getName() + " collected the trick: " + trick);
        }
        for (int i = 0; i < players.size(); i++) { // speed up using map
            if (players.get(i).equals(trickPlayers.get(index))) {
                nextPlayer = i;
                break;
            }
        }
        trick = new ArrayList<>();
        trickPlayers = new ArrayList<>();
        count = 0;
    }

    /**
     * @param name the name of the player to be found
     * @return the player with the given name
     */
    @JsonIgnore
    @DynamoDBIgnore
    public Player getPlayer(String name) {
        for (Player p: players) { // USE MAP
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * sets settings and initializes all necessary fields depending on the settings
     * @throws IllegalStateException if game has started already
     * @param s the settings for the game - cannot be null
     */
    public void setUp(Settings s) {
        if (s == null) {
            throw new IllegalArgumentException("Settings cannot be null");
        }
        if (started) {
            throw new IllegalStateException("Settings cannot be changed after game is started");
        }
        setSettings(s);
        if (s.getTrickEnabled()) {
            trick = new ArrayList<>();
            trickPlayers = new ArrayList<>();
            count = 0;
        }
        if (s.getDiscardEnabled()) {
            discard = new ArrayList<>();
        }
    }

    /**
     * adds the given number of points to the player's score
     * @throws IllegalStateException if game has not started yet
     * @param p the player whose score is to be added to - cannot be null
     * @param points the number of points to be added (can be positive or negative)
     */
    public void score(Player p, int points) {
        if (p == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (!started) {
            throw new IllegalStateException("Game has not started yet");
        }
        p.addPoints(points);
    }

    /**
     * adds a player to a team
     * @param p the player to be added
     * @param team the team they are joining
     */
    public void joinTeam(Player p, String team) {
        if (p == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (started) {
            throw new IllegalStateException("Cannot join team after game has started");
        }
        p.setTeamName(team);
    }
}