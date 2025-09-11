package com.dai.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.dai.PlayerPawn;
import com.dai.engine.Engine;
import com.dai.PlayerController.PlayerData;
import com.dai.network.*;
import com.dai.world.World;
import com.dai.world.Pawn.PawnData;

public final class GameMatch extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(GameMatch.class);

    public static final int NUM_OF_MATCH_PLAYERS = 2;

    public static enum EGameState {
        Pending,
        InProgress,
        Ended
    }

    private final class MatchPlayer {
        public Socket client;
        public PlayerPawn player;
        public ObjectInputStream in;
        public ObjectOutputStream out;
    }

    private EGameState state = EGameState.Pending;
    private final List<MatchPlayer> players;

    private Registry registry;
    private NetworkGameServer netGameServer;

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
                try {
                    startMatch();
                } catch(Exception e) {
                    logger.error("Failed to start match: " + e.getMessage());
                }
            }

            /** Verify that the players have not dropped */
            try {
                for(MatchPlayer mp : players) {
                    Object message = mp.in.readObject();
                    logger.info("Client sent: " + message.toString());
                }
            } catch(Exception e) {
                logger.error("Client has dropped, the game match will now end. The error is: " + e.getMessage());
                hasDroppedClients = true;

                // Close the remaining connections
                try {
                    for(MatchPlayer mp : players) { mp.client.close(); }
                } catch(Exception e1) {}
            }
        }

        logger.info("Thread has exited run() method");
        state = EGameState.Ended;
    }

    public EGameState getGameState() { return this.state; }

    private void startMatch() throws RemoteException {
        MatchPlayer p1 = players.get(0);
        MatchPlayer p2 = players.get(1);

        /** Initialize network stuff */
        registry = LocateRegistry.createRegistry(16000);

        netGameServer = (NetworkGameServer) NetworkGameServer.getInstance();
        registry.rebind(NetworkGameServer.class.getSimpleName(), netGameServer);

        try {
            // Notify both players of connection
            p1.out.writeByte(EDAIProtocol.Connected.value);
            p1.out.flush();

            p2.out.writeByte(EDAIProtocol.Connected.value);
            p2.out.flush();
        } catch(Exception e) { logger.error(e.getMessage()); }

        state = EGameState.InProgress;
    }
}
