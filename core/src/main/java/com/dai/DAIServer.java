package com.dai;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dai.server.GameMatch;
import com.dai.server.GameMatch.EGameState;

public class DAIServer {
    private static final Logger logger = LoggerFactory.getLogger(DAIServer.class);
    private static volatile DAIServer instance;

    public static DAIServer getInstance() {
        System.out.println("DAIServer getInstance()");
        if(instance == null) {
            synchronized(DAIServer.class) {
                if(instance == null) {
                    instance = new DAIServer();
                }
            }
        }

        return instance;
    }

    public static final int PORT = 6000;

    private List<GameMatch> matches = new ArrayList<>();
    private BlockingQueue<Socket> playerClients;
    private Thread connectionThread;

    public DAIServer() {
        this.playerClients = new LinkedBlockingQueue<>();
        instance = this;
    }

    public void run() throws Exception {
        ServerSocket ss = new ServerSocket(PORT);
        logger.info("Server running on port " + PORT + " ...");

        /** Connection handler */
        connectionThread = new Thread() {
            @Override
            public void run() {
                try {
                    while(!Thread.currentThread().isInterrupted()) {
                        try {
                            Socket client = ss.accept();
                            playerClients.put(client);

                            logger.info("> Client connected: " + client.getInetAddress());
                        } catch(IOException e) {
                            // TODO: Maybe handle gracefully
                            e.printStackTrace();
                        }
                    }
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        connectionThread.start();

        try {

            /** Match maker */
            while(!Thread.interrupted()) {
                Thread.sleep(2000);

                /** Try to pair players into a match */
                logger.trace("Number of players in lobby: " + playerClients.size());
                logger.trace("Number of active games: " + matches.size());

                if(playerClients.size() >= 2) {
                    Socket c1 = playerClients.take();
                    Socket c2 = playerClients.take();

                    GameMatch match = new GameMatch(c1, c2);
                    match.start();

                    matches.add(match);
                }

                /** Clear ended games */
                int i = 0;
                while(i < matches.size() && !matches.isEmpty()) {
                    if(matches.get(i).getGameState() == EGameState.Ended) {
                        matches.remove(i);
                    }
                    i++;
                }
                // matches = matches.stream().filter(m -> m.getGameState() != EGameState.Ended).toList();
            }
        } catch(Exception e) {
            logger.error("Server terminated with error.");
            logger.error(e.getMessage());
        } finally {
            logger.info("Server stopped gracefully.");
            ss.close();
        }
    }
}
