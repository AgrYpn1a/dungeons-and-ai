package com.dai.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dai.Player;

public final class GameMatch extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(GameMatch.class);

    public static enum EGameState {
        Pending,
        Started,
        Ended
    }

    private final class MatchPlayer {
        public Socket client;
        public Player player;
        public ObjectInputStream in;
    }

    private EGameState state = EGameState.Pending;
    private final List<MatchPlayer> players;

    public GameMatch(Socket c1, Socket c2) throws IOException {
        players = new ArrayList<>(2);

        MatchPlayer mp1 = new MatchPlayer();
        mp1.client = c1;
        mp1.in = new ObjectInputStream(c1.getInputStream());
        MatchPlayer mp2 = new MatchPlayer();
        mp2.client = c2;
        mp2.in = new ObjectInputStream(c2.getInputStream());

        players.add(mp1);
        players.add(mp2);

        // If server crashes, matches should close too
        setDaemon(true);
    }

    @Override
    public void run() {
        boolean hasDroppedClients = false;

        while(!Thread.currentThread().isInterrupted() && !hasDroppedClients) {
            try { Thread.sleep(500); } catch(Exception e) {}

            // TODO: Actually start the match
            // logger.info("Matchmaking in progress for " + players.size() + " players...");

            /** Verify that the players have not dropped */
            try {
                for(MatchPlayer mp : players) {
                    Object message = mp.in.readObject();
                    logger.info("Client sent: " + message.toString());
                }
            } catch(Exception e) {
                logger.error("Client has dropped, the game match will now end.");
                hasDroppedClients = true;

                // Close the remaining connections
                try {
                    for(MatchPlayer mp : players) { mp.client.close(); }
                } catch(Exception e1) {}
            }
        }

        logger.info("Thread has exicted run() method");
        state = EGameState.Ended;
    }

    public EGameState getGameState() { return this.state; }
}
