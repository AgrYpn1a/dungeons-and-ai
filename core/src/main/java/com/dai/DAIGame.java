package com.dai;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.badlogic.gdx.Game;
import com.dai.Player.PlayerData;
import com.dai.server.EDAIProtocol;
import com.dai.server.ServerConnection;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class DAIGame extends Game {
    Socket ss;
    Player player;
    ServerConnection serverConnection;
    ObjectOutputStream out;

    private static final Logger logger = LoggerFactory.getLogger(DAIGame.class);

    @Override
    public void create() {

        try {
            /** Connect to the server */
            ss = new Socket("localhost", DAIServer.PORT);

            out = new ObjectOutputStream (ss.getOutputStream());
            serverConnection = new ServerConnection(out);

            /** Init player */
            PlayerData data = new PlayerData();
            data.name = "Player " + Math.round(Math.random() * 1000);
            player = new Player(data);

            out.writeObject(EDAIProtocol.PlayerConnect.name());
            out.writeObject(data);

            /** Let the connection run in the background */
            serverConnection.setDaemon(true);
            serverConnection.start();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            // try { ss.close(); } catch(Exception e) {}
        }

        /** Init the libgdx game screen */
        setScreen(new GameScreen());
    }

    @Override
    public void dispose() {
        try {
            logger.info("Closing streams.");
            out.close();
            ss.close();
            serverConnection.interrupt();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
