package com.serverless.dal;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Game is a mutable object representing the state of a card game. The state consists of the Players,
 * the Settings, whose turn it is to play, any cards that have been discarded (if applicable), any cards
 * in the draw pile (if applicable), and a join code unique to each Game.
 *
 * The getters and setters included should not be used; they are included only for DynamoDB support.
 * In particular, setCode() must not be used.
 */

@DynamoDBTable(tableName = "PLACEHOLDER")
public class Game {
    private static final String GAMES_TABLE_NAME = "deck-o-cards-games-table";
    private static final Logger LOG = LogManager.getLogger(Game.class);

    private final DynamoDBMapper dbMapper;
    private final AmazonDynamoDB ddb;
    private final Random r;

    private final char[] alphabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private String code;                          // the entry code for the game
    private Settings settings;                          // the settings for the game
    private boolean started;                            // whether the game has started

    private List<Player> players;
    private Map<String, Player> playerNames;
    private Map<String, Integer> playerIndices;

    // important general game data

    private String nextPlayer;                          // the name of the next player to play

    // game data that is only so it gets passed in beans
    private Card cardPlayed;                            // the last card played
    private WayToPlay way;                              // the way that cardPlayed was played
    private String lastPlayer;                          // who was the last player to play
    private String pass;                                // if the last card was passed, who it was passed to

    private Suit trump;

    // fields for games with trick enabled

    private List<Card> trick;                           // the cards that have been played on the current trick, in order
    private List<Player> trickPlayers;                  // the players who have played on the current trick, in order
    private int count;                                  // the number of players who have played on the current trick

    // for if draw is enabled

    private List<Card> draw;                            // the cards currently on the draw pile

    // for if discard is enabled

    private List<Card> discard;

    /**
     * Constructs a game with a join code but no players, no settings, and no discard or draw piles
     */
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
        playerNames = new HashMap<>();
        playerIndices = new HashMap<>();
        started = false;
        trick = new ArrayList<>();
        trickPlayers = new ArrayList<>();
        count = 0;
        discard = new ArrayList<>();
        draw = new ArrayList<>();
//        cardPlayed = null;
//        way = null;
        code = generateCode();
    }

    @DynamoDBTypeConvertedEnum
    @DynamoDBTypeConverted(converter = SuitConverter.class)
    @DynamoDBAttribute(attributeName = "trump")
    public Suit getTrump() { return this.trump; }
    public void setTrump(Suit s) { this.trump = s; }

    @DynamoDBTypeConverted(converter = PlayerListConverter.class)
    @DynamoDBAttribute(attributeName = "players")
    public List<Player> getPlayers() { return this.players; }
    public void setPlayers(List<Player> players) {
        this.players = players;
        playerNames = new HashMap<>();
        playerIndices = new HashMap<>();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            playerNames.put(p.getName(), p);
            playerIndices.put(p.getName(), i);
        }
    }

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

    @DynamoDBTypeConverted(converter = PlayerListConverter.class)
    @DynamoDBAttribute(attributeName = "trick order")
    public List<Player> getTrickPlayers() { return this.trickPlayers; }
    public void setTrickPlayers(List<Player> trickPlayers) { this.trickPlayers = trickPlayers; }

    @DynamoDBAttribute(attributeName = "count")
    public int getCount() { return this.count; }
    public void setCount(int count) { this.count = count; }

    @DynamoDBAttribute(attributeName = "next_player")
    public String getNextPlayer() { return nextPlayer; }
    public void setNextPlayer(String nextPlayer) {
        this.nextPlayer = nextPlayer;
    }

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

    @DynamoDBAttribute(attributeName = "passed_to")
    public String getPass() { return this.pass; }
    public void setPass(String name) { this.pass = name; }

    @DynamoDBAttribute(attributeName = "last_player")
    public String getLastPlayer() { return this.lastPlayer; }
    public void setLastPlayer(String name) { this.lastPlayer = name; }

    /**
     * gets the entry code for this game
     *
     * @return the entry code
     */
    @DynamoDBHashKey(attributeName = "code")
    @JsonProperty("code")
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    @DynamoDBTypeConverted(converter = SettingsConverter.class)
    @DynamoDBAttribute(attributeName = "settings")
    public Settings getSettings() { return settings; }
    public void setSettings(Settings s) { this.settings = s; }

    /**
     * Retrieves the game with the given code from the DynamoDB database; returns null
     * if it is not found
     * @param code the entry code for the desired game - cannot be null;
     * @return the game with this code, or null if not found
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
     * returns a list of all games currently in existence
     * @return a list of all the games
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

    /**
     * returns the player who should take their turn next
     * @return the player whose turn it is
     */
    @JsonIgnore
    @DynamoDBIgnore
    public Player getWhoseTurn() {
        return getPlayer(nextPlayer);
    }

    /**
     * adds the player to this game
     * @throws IllegalStateException if game has started
     * @param name the name of the player to be added - no other player can have this name
     */
    public void joinGame(String name) {
        if (started) {
            LOG.error(name + "attempted to join game after it started");
            throw new IllegalStateException("cannot join game after it has started");
        }
        if (playerNames.containsKey(name)) {
            throw new IllegalArgumentException("another player already has this name");
        }
        LOG.info("Player " + name + " added");
        Player p = new Player(name);
        players.add(p);
        playerNames.put(name, p);
        playerIndices.put(name, players.size() - 1);
        if (players.size() == 1) {
            nextPlayer = name;
        }
    }

    /**
     * Updates the game, moving cards into players' hands or discard piles as specified by the parameter how
     * Sets whose turn to play it is to the player immediately after the player who played
     * If there is a draw pile and it is empty, shuffles and restarts it
     * If there are tricks and the number of cards on the trick equals the number of players, assigns the trick
     * to the player who played the highest card on it - see collectTrick documentation for details
     *
     * @throws IllegalStateException if the game has not started yet
     * @throws IllegalArgumentException if the way the card was played has not been enabled for this game
     * or if the player who took the turn does not have the card that was played in their hand and the card
     * is supposed to be played from their hand (how = DISCARD, PASS, SHOW, or TRICK)
     * @param played the name of the player who took a turn
     * @param card the card that was played
     * @param how the way that it was played
     * @param passedTo if the card was passed, the name of the player it was passed to - cannot be null if how is PASS
     */
    public void takeTurn(String played, Card card, WayToPlay how, String passedTo) {
        Player p = getPlayer(played);
        Player pass = getPlayer(passedTo);
        if (!started) {
            LOG.error("attempted to take turn before game started");
            throw new IllegalStateException("Game has not started yet");
        }
        if ((!settings.getSkip() && how.equals(WayToPlay.SKIP)) ||
                (!settings.getDiscard() && how.equals(WayToPlay.DISCARD)) ||
                (!settings.getPass() && how.equals(WayToPlay.PASS)) ||
                (!settings.getShow() && how.equals(WayToPlay.SHOW)) ||
                (!settings.getTrick() && how.equals(WayToPlay.TRICK)) ||
                (!settings.isDraw() && how.equals(WayToPlay.DRAW))) {
            LOG.error(how.toString() + " is not enabled");
            throw new IllegalArgumentException(how.toString() + " is not enabled");
        }
        if (how.equals(WayToPlay.PASS) && pass == null) {
            LOG.error("player to whom to pass not specified");
            throw new IllegalArgumentException("Please specify to whom to pass");
        }
        if ((how.equals(WayToPlay.DISCARD) || how.equals(WayToPlay.TRICK)
                || how.equals(WayToPlay.PASS) || how.equals(WayToPlay.SHOW)) && !p.getHand().contains(card)) {
            LOG.error(p.getName() + " does not have " + card.toString());
            throw new IllegalArgumentException("Player " + p.getName() + " does not have " + card.toString());
        }
        cardPlayed = card;
        way = how;
        this.pass = passedTo;
        this.lastPlayer = played;
        if (!(how.equals(WayToPlay.DRAW) || how.equals(WayToPlay.PICKUP))) {
            p.getHand().remove(card);
        }
        int nextIndex;
        if (playerIndices.get(p.getName()) == players.size() - 1) {
            nextIndex = 0;
        } else {
            nextIndex = playerIndices.get(p.getName()) + 1;
        }
        nextPlayer = players.get(nextIndex).getName();
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
        if (settings.isDraw() && draw.size() == 0 && discard.size() > 0) {
            Deck.shuffle(discard);
            draw = discard;
            discard = new ArrayList<>();
        }
    }

    /**
     * Deals the deck and assigns each player a hand; if cards are left over, assigns them to the draw pile
     *
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
     * Deals the deck and assigns each player a hand, beginning with the player
     * immediately after the dealer; if cards are left over, assigns them to the draw pile
     * Sets the next player to be the player immediately after the dealer
     *
     * @param dealer the name of the player who dealt - must be in the game
     * @throws IllegalStateException if the game has not started yet
     */
    public void deal(String dealer) {
        if (!started) {
            throw new IllegalStateException("Game has not started yet");
        }
        Player p = playerNames.get(dealer);
        if (!players.contains(p)) {
            throw new IllegalArgumentException("Player is not in the game");
        }
        Deck deck = getDeck();
        List<List<Card>> deal = deck.deal(settings.getCardsPer());
        int index = playerIndices.get(dealer);
        int numPlayers = players.size();
        for (int i = 0; i < numPlayers; i++) {
            players.get((index + i + 1) % numPlayers).setHand(deal.get(i));
        }
        if (deal.size() > numPlayers) {
            draw = deal.get(numPlayers);
        }
        if (index != numPlayers - 1) {
            nextPlayer = players.get(index + 1).getName();
        } else {
            nextPlayer = players.get(0).getName();
        }
    }

    /**
     * Returns a new Deck as specified by the settings (poker, pinochle, or euchre)
     *
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

    /**
     * Retrieves the hand of the player
     *
     * @throws IllegalStateException if game has not started yet
     * @param name the name of the player whose hand is to be found
     * @return the hand of the player, or null if the player is not found
     */
    @JsonIgnore
    @DynamoDBIgnore
    public List<Card> getHand(String name) {
        if (!started) {
            throw new IllegalStateException("Game has not started yet");
        }
        return getPlayer(name).getHand();
    }

    /**
     * Starts the game
     *
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
     * Compares two cards to determine whichever is higher. Trump suit always wins; within suits,
     * higher rank wins; if the cards are different suits and neither is trump, c1 wins
     *
     * @param c1 the card that was played first of the two
     * @param c2 the card that was played second of the two
     * @return the card that is greater of the two
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
     * Awards the cards on a trick to the player with the highest card - the highest trump wins.
     * If there is no trump on the trick, the card of the highest rank and of the suit that was
     * played first wins.
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
        nextPlayer = trickPlayers.get(index).getName();
        trick = new ArrayList<>();
        trickPlayers = new ArrayList<>();
        count = 0;
    }

    /**
     * Gets the player with the given name
     *
     * @param name the name of the player to be found
     * @return the player with the given name
     */
    @JsonIgnore
    @DynamoDBIgnore
    public Player getPlayer(String name) {
        return playerNames.get(name);
    }

    /**
     * Sets the settings
     *
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
    }

    /**
     * Sets a player's score to a certain number of points
     *
     * @throws IllegalStateException if game has not started yet
     * @param name the name of the player whose score is to be set - must be in this game
     * @param points the number of points to be set (can be positive or negative)
     */
    public void score(String name, int points) {
        Player p = getPlayer(name);
        if (p == null) {
            throw new IllegalArgumentException("Player does not exist: " + name);
        }
        if (!started) {
            throw new IllegalStateException("Game has not started yet");
        }
        p.setPoints(points);
    }

    /**
     * Adds a player to a team
     *
     * @param name the name of the player to be added - must be part of this game
     * @param team the team they are joining
     */
    public void joinTeam(String name, String team) {
        Player p = getPlayer(name);
        if (p == null) {
            throw new IllegalArgumentException("Player does not exist: " + name);
        }
        if (started) {
            throw new IllegalStateException("Cannot join team after game has started");
        }
        p.setTeamName(team);
    }

    /**
     * Gets the names of the players in this game
     *
     * @return a list of the players' names
     */
    @JsonIgnore
    @DynamoDBIgnore
    public List<String> getPlayerNames() {
        return new ArrayList<>(this.playerNames.keySet());
    }
}