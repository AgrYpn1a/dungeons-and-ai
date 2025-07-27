package com.dai.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.dai.Player;
import com.dai.Player.PlayerData;
import com.dai.world.World;

public final class GameMatch extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(GameMatch.class);

    public static enum EGameState {
        Pending,
        InProgress,
        Ended
    }

    private final class MatchPlayer {
        public Socket client;
        public Player player;
        public ObjectInputStream in;
        public ObjectOutputStream out;
    }

    private EGameState state = EGameState.Pending;
    private final List<MatchPlayer> players;

    public GameMatch(Socket c1, Socket c2) throws IOException {
        players = new ArrayList<>(2);

        MatchPlayer mp1 = new MatchPlayer();
        mp1.client = c1;
        mp1.in = new ObjectInputStream(c1.getInputStream());
        mp1.out = new ObjectOutputStream(c1.getOutputStream());
        MatchPlayer mp2 = new MatchPlayer();
        mp2.client = c2;
        mp2.in = new ObjectInputStream(c2.getInputStream());
        mp2.out = new ObjectOutputStream(c2.getOutputStream());

        players.add(mp1);
        players.add(mp2);

        // If server crashes, matches should close too
        setDaemon(true);
    }

    @Override
    public void run() {
        boolean hasDroppedClients = false;

        while(
            !Thread.currentThread().isInterrupted()
            && !hasDroppedClients
            && state != EGameState.Ended) {

            try { Thread.sleep(500); } catch(Exception e) {}

            /** Initialise match */
            if(state == EGameState.Pending) {
                startMatch();
            }

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

    private void startMatch() {
        /** 1. Create players and send to clients */
        /** 2.  */
        PlayerData p1Data = new PlayerData();
        PlayerData p2Data = new PlayerData();

        p1Data.name = "Player 1";
        p1Data.spawnPos = new Vector2(0, 0);

        p2Data.name = "Player 2";
        p2Data.spawnPos = new Vector2(World.WORLD_SIZE - 1, World.WORLD_SIZE - 1);

        MatchPlayer p1 = players.get(0);
        MatchPlayer p2 = players.get(1);

        try {
            p1.out.writeByte(EDAIProtocol.SpawnPlayer.value);
            p1.out.writeObject(p1Data);
            p1.out.flush();

            p1.out.writeByte(EDAIProtocol.SpawnEnemy.value);
            p1.out.writeObject(p2Data);
            p1.out.flush();

            p2.out.writeByte(EDAIProtocol.SpawnPlayer.value);
            p2.out.writeObject(p2Data);
            p2.out.flush();

            p2.out.writeByte(EDAIProtocol.SpawnEnemy.value);
            p2.out.writeObject(p1Data);
            p2.out.flush();
        } catch(Exception e) { logger.error(e.getMessage()); }

        state = EGameState.InProgress;
    }
}
