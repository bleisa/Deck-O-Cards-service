import com.serverless.dal.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

/**
 * Tests the Game class without api endpoints for easier debugging
 */

public class GameDirectTest {
    private static final String PINOCHLE_CODE = "AGZPV"; // for a standard pinochle game with four players
    private static final Random R = new Random();
    private static final String PASS_CODE = "JPFPW"; // for a pass pinochle game with six players

    @Ignore
    @Test
    public void createGameDirectTest() {
        Game g = new Game();
        g.save(g);
        System.out.println(g.getCode());
    }

    @Test
    public void joinPassGameDirectTest() {
        Game g = new Game().getGame(PASS_CODE);
        List<Player> players = new ArrayList<>();
        Player abby = new Player("Abby");
        Player alan = new Player("Alan");
        Player emma = new Player("Emma");
        Player henry = new Player("Henry");
        players.add(abby);
        players.add(alan);
        players.add(emma);
        players.add(henry);
        for (Player p: players) {
            g.joinGame(p);
        }
        int[] counts = {16, 8, 16, 8};
        Settings settings = new Settings(counts, DeckType.PINOCHLE, true, false, true, true,
                true, false, true, false, true,4);
        g.setUp(settings);
        g.joinTeam(abby, "We");
        g.joinTeam(alan, "We");
        g.joinTeam(emma, "They");
        g.joinTeam(henry, "They");
        g.save(g);
    }

    @Test
    public void joinGameDirectTest() {
        Game g = new Game().getGame(PINOCHLE_CODE);
        List<Player> players = new ArrayList<>();
        Player abby = new Player("Abby");
        Player alan = new Player("Alan");
        Player emma = new Player("Emma");
        Player henry = new Player("Henry");
        players.add(abby);
        players.add(alan);
        players.add(emma);
        players.add(henry);
        for (Player p: players) {
            g.joinGame(p);
        }
        int[] counts = {12, 12, 12, 12};
        Settings settings = new Settings(counts, DeckType.PINOCHLE, false, false, true, false,
                true, false, true, false, true,4);
        g.setUp(settings);
        g.joinTeam(abby, "We");
        g.joinTeam(alan, "We");
        g.joinTeam(emma, "They");
        g.joinTeam(henry, "They");
        g.save(g);
    }

    @Test
    public void setSettingsDirectTest() {
        Game g = new Game().getGame(PINOCHLE_CODE);
        int[] counts = {12, 12, 12, 12};
        Settings settings = new Settings(counts, DeckType.PINOCHLE, false, false, true, false,
                true, false, true, false, true,4);
        g.setSettings(settings);
        g.save(g);
    }

    @Test
    public void startPinochleGameDirectTest() {
        Game g = new Game().getGame(PINOCHLE_CODE);
        g.startGame();
        g.save(g);
    }

    @Test
    public void startPassGameDirectTest() {
        Game g = new Game().getGame(PASS_CODE);
        g.startGame();
        g.save(g);
    }

    @Test
    public void dealHandDirectTest() {
        Game g = new Game().getGame(PINOCHLE_CODE);
        g.deal();
        g.save(g);
    }

    @Test
    public void getHandDirectTest() {
        Game g = new Game().getGame(PINOCHLE_CODE);
        List<Card> hand = g.getHand(new Player("Abby"));
        System.out.println(hand);
    }

    @Ignore
    @Test
    public void takeTurnDirectTest() {
        Game g = new Game().getGame(PINOCHLE_CODE);
        g.takeTurn(g.getPlayer("Henry"), new Card(Suit.SPADES, 9), WayToPlay.TRICK, null);
        g.save(g);
    }

    @Test
    public void roundDirectTest() {
        Game g = new Game().getGame(PINOCHLE_CODE);
        g.deal();
        g.save(g);
        Player me = new Player("Abby");
        List<Card> hand = g.getHand(me);
        me.setHand(hand);
        System.out.println("Abby's hand is " + hand);
        Card c = me.getHand().get(R.nextInt(me.getHand().size()));
        System.out.println("I will play the " + c.toString());
        g.takeTurn(me, c, WayToPlay.TRICK, null);
        hand = g.getHand(me);
        System.out.println("My hand is now " + hand);
    }

    @Test
    public void passDirectTest() {
        Game g = new Game().getGame(PASS_CODE);
        g.deal();
        g.save(g);
        Player abby = new Player("Abby");
        abby.setTeamName("We");
        Player alan = new Player("Alan");
        alan.setTeamName("We");
        Player emma = new Player("Emma");
        emma.setTeamName("They");
        Player henry = new Player("Henry");
        henry.setTeamName("They");
        List<Player> players = new ArrayList<>();
        players.add(abby);
        players.add(alan);
        players.add(emma);
        players.add(henry);
        int[] handCounts = new int[4];
        for (int i = 0; i < 4; i++) {
            Player p = players.get(i);
            List<Card> hand = g.getHand(p);
            p.setHand(hand);
            handCounts[i] = p.getHand().size();
            System.out.println(p.getName() + "'s hand is " + hand);
        }
        System.out.println("Beginning passing....");
        while (handCounts[0] > 12 || handCounts[2] > 12) {
            for (Player p : players) {
                if (g.getWhoseTurn().equals(p) && p.getHand().size() > 12) {
                    Player teammate = null;
                    System.out.println(p.getName() + " is passing....");
                    Card c = p.getHand().get(R.nextInt(p.getHand().size()));
                    for (Player p2: players) {
                        if (!p2.equals(p) && p2.getTeamName().equals(p.getTeamName())) {
                            teammate = p2;
                            break;
                        }
                    }
                    assert teammate != null;
                    g.takeTurn(p, c, WayToPlay.PASS, teammate);
                    for (int i = 0; i < players.size(); i++) {
                        Player pl = players.get(i);
                        if (pl.equals(p)) {
                            handCounts[i]--;
                        } else if (pl.equals(teammate)) {
                            handCounts[i]++;
                        }
                    }
                    teammate.setHand(g.getPlayer(teammate.getName()).getHand());
                    System.out.println(teammate.getName() + "'s hand is now " + teammate.getHand());
                    p.setHand(g.getPlayer(p.getName()).getHand());
                    System.out.println(p.getName() + "'s hand is now " + p.getHand());
                } else if (g.getWhoseTurn().equals(p)){
                    g.takeTurn(p, null, WayToPlay.SKIP, null);
                    System.out.println(p.getName() + " is skipping their turn");
                }
            }
        }
    }

    @Test
    public void handDirectTest() {
        Game g = new Game().getGame(PINOCHLE_CODE);
        g.deal();
        g.save(g);
        Player abby = new Player("Abby");
        Player alan = new Player("Alan");
        Player emma = new Player("Emma");
        Player henry = new Player("Henry");
        List<Player> players = new ArrayList<>();
        players.add(abby);
        players.add(alan);
        players.add(emma);
        players.add(henry);
        for (Player p: players) {
            List<Card> hand = g.getHand(p);
            p.setHand(hand);
            System.out.println(p.getName() + "'s hand is " + hand);
        }
        g.setTrump(Suit.valueOf(R.nextInt(4)));
        System.out.println(g.getTrump().toString() + " is trump");
        for (int i = 0; i < 4; i++) {
            Player p = g.getWhoseTurn();
            Card c = p.getHand().get(R.nextInt(p.getHand().size()));
            g.takeTurn(p, c, WayToPlay.SHOW, null);
            System.out.println(p.getName() + " has " + c + " to meld");
        }
        for (int i = 0; i < 4; i++) {
            Player p = g.getWhoseTurn();
            Card c = p.getShown().get(0);
            g.takeTurn(p, c, WayToPlay.PICKUP, null);
            System.out.println(p.getName() + " picked " + c + " back up");
        }
        for (int i = 0; i < 48; i++) {
            if (i % 4 == 0) {
                System.out.println("\nTrick " + i / 4);
            }
            Player p = g.getWhoseTurn();
            Card c = p.getHand().get(R.nextInt(p.getHand().size()));
            g.takeTurn(p, c, WayToPlay.TRICK, null);
            System.out.println(p.getName() + " played " + c);
            System.out.println("\tTheir hand is now " + p.getHand());
            System.out.println("\tThey have collected " + p.getCollected());
        }
        System.out.println("Ending test....");
    }
}
