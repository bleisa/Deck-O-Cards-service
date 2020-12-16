import com.serverless.dal.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

/**
 * Tests the Game class without api endpoints for easier debugging
 */

public class GameDirectTest {
    private static final String PINOCHLE_CODE = "HTLXC"; // for a standard pinochle game with four players
    private static final Random R = new Random();
    private static final String PASS_CODE = "HUAMG"; // for a pass pinochle game with four players

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
        List<String> names = List.of("me1", "me2", "me3", "me4");
        for (String name: names) {
            g.joinGame(name);
        }
        Player me1 = g.getPlayer("me1");
        Player me2 = g.getPlayer("me2");
        Player me3 = g.getPlayer("me3");
        Player me4 = g.getPlayer("me4");
        int[] counts = {16, 8, 16, 8};
        Settings settings = new Settings.SettingsBuilder(counts, DeckType.PINOCHLE)
                .trick(true)
                .aceHigh(true)
                .pass(true)
                .points(true)
                .teams(true)
                .show(true)
                .build();
        g.setUp(settings);
        g.joinTeam(me1.getName(), "We");
        g.joinTeam(me2.getName(), "We");
        g.joinTeam(me3.getName(), "They");
        g.joinTeam(me4.getName(), "They");
        g.save(g);
    }

    @Test
    public void joinGameDirectTest() {
        Game g = new Game().getGame(PINOCHLE_CODE);
        List<String> names = List.of("me1", "me2", "me3", "me4");
        for (String name: names) {
            g.joinGame(name);
        }
        Player me1 = g.getPlayer("me1");
        Player me2 = g.getPlayer("me2");
        Player me3 = g.getPlayer("me3");
        Player me4 = g.getPlayer("me4");
        int[] counts = {12, 12, 12, 12};
        Settings settings = new Settings.SettingsBuilder(counts, DeckType.PINOCHLE)
                .trick(true)
                .aceHigh(true)
                .points(true)
                .teams(true)
                .show(true)
                .build();
        g.setUp(settings);
        g.joinTeam(me1.getName(), "We");
        g.joinTeam(me2.getName(), "We");
        g.joinTeam(me3.getName(), "They");
        g.joinTeam(me4.getName(), "They");
        g.save(g);
    }

    @Test
    public void setSettingsDirectTest() {
        Game g = new Game().getGame(PINOCHLE_CODE);
        int[] counts = {12, 12, 12, 12};
        Settings settings = new Settings.SettingsBuilder(counts, DeckType.PINOCHLE)
                .trick(true)
                .aceHigh(true)
                .points(true)
                .teams(true)
                .show(true)
                .build();
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
        List<Card> hand = g.getHand("me1");
        System.out.println(hand);
    }

    @Ignore
    @Test
    public void takeTurnDirectTest() {
        Game g = new Game().getGame(PINOCHLE_CODE);
        g.takeTurn("me1", new Card(Suit.SPADES, 9), WayToPlay.TRICK, null);
        g.save(g);
    }

    @Test
    public void roundDirectTest() {
        Game g = new Game().getGame(PINOCHLE_CODE);
        g.deal();
        g.save(g);
        Player me = new Player("me1");
        List<Card> hand = g.getHand("me1");
        me.setHand(hand);
        System.out.println("My hand is " + hand);
        Card c = me.getHand().get(R.nextInt(me.getHand().size()));
        System.out.println("I will play the " + c.toString());
        g.takeTurn(me.getName(), c, WayToPlay.TRICK, null);
        hand = g.getHand("me1");
        System.out.println("My hand is now " + hand);
    }

    @Test
    public void passDirectTest() {
        Game g = new Game().getGame(PASS_CODE);
        g.deal();
        g.save(g);
        Player billy = new Player("me1");
        billy.setTeamName("We");
        Player bob = new Player("me2");
        bob.setTeamName("We");
        Player joe = new Player("me3");
        joe.setTeamName("They");
        Player me = new Player("me4");
        me.setTeamName("They");
        List<Player> players = new ArrayList<>();
        players.add(billy);
        players.add(bob);
        players.add(joe);
        players.add(me);
        int[] handCounts = new int[4];
        for (int i = 0; i < 4; i++) {
            Player p = players.get(i);
            List<Card> hand = g.getHand(p.getName());
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
                    g.takeTurn(p.getName(), c, WayToPlay.PASS, teammate.getName());
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
                    g.takeTurn(p.getName(), null, WayToPlay.SKIP, null);
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
        Player me1 = new Player("me1");
        Player me2 = new Player("me2");
        Player me3 = new Player("me3");
        Player me4 = new Player("me4");
        List<Player> players = new ArrayList<>();
        players.add(me1);
        players.add(me2);
        players.add(me3);
        players.add(me4);
        for (Player p: players) {
            List<Card> hand = g.getHand(p.getName());
            p.setHand(hand);
            System.out.println(p.getName() + "'s hand is " + hand);
        }
        g.setTrump(Suit.valueOf(R.nextInt(4)));
        System.out.println(g.getTrump().toString() + " is trump");
        for (int i = 0; i < 4; i++) {
            Player p = g.getWhoseTurn();
            Card c = p.getHand().get(R.nextInt(p.getHand().size()));
            g.takeTurn(p.getName(), c, WayToPlay.SHOW, null);
            System.out.println(p.getName() + " has " + c + " to meld");
        }
        for (int i = 0; i < 4; i++) {
            Player p = g.getWhoseTurn();
            Card c = p.getShown().get(0);
            g.takeTurn(p.getName(), c, WayToPlay.PICKUP, null);
            System.out.println(p.getName() + " picked " + c + " back up");
        }
        for (int i = 0; i < 48; i++) {
            if (i % 4 == 0) {
                System.out.println("\nTrick " + i / 4);
            }
            Player p = g.getWhoseTurn();
            Card c = p.getHand().get(R.nextInt(p.getHand().size()));
            g.takeTurn(p.getName(), c, WayToPlay.TRICK, null);
            System.out.println(p.getName() + " played " + c);
            System.out.println("\tTheir hand is now " + p.getHand());
            System.out.println("\tThey have collected " + p.getCollected());
        }
        System.out.println("Ending test....");
    }
}
