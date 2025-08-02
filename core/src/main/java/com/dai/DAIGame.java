package com.dai;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.dai.PlayerPawn.PlayerData;
import com.dai.network.NetworkListener;
import com.dai.network.NetworkListener.NetworkData;
import com.dai.server.EDAIProtocol;
import com.dai.world.World;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class DAIGame extends Game {
    Socket ss;
    PlayerPawn player;
    NetworkListener listener;

    ObjectOutputStream out;
    ObjectInputStream in;

    Queue<NetworkData> messageQueue;

    private static final Logger logger = LoggerFactory.getLogger(DAIGame.class);

    private boolean isOfflineMode;

    public DAIGame(boolean isOfflineMode) {
        super();

        this.isOfflineMode = isOfflineMode;
    }

    public boolean getIsOfflineMode() { return isOfflineMode; }

    @Override
    public void create() {

        if(!isOfflineMode) {
            try {
                // Connect to the server
                ss = new Socket("localhost", DAIServer.PORT);

                out = new ObjectOutputStream (ss.getOutputStream());
                in = new ObjectInputStream(ss.getInputStream());
                messageQueue = new ConcurrentLinkedQueue<>();

                /** Let the connection run in the background */
                listener = new NetworkListener(in, messageQueue);
                listener.setDaemon(true);
                listener.start();

            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                // try { ss.close(); } catch(Exception e) {}
            }
        }

        /** Initialize managers */
        // TextureManager.getInstance().setTexture(new Texture("tileset.png"));
        TextureManager.getInstance().setTexture(new Texture("selenasdungeon32x32.png"));

        /** Init the libgdx game screen */
        setScreen(new GameScreen());

        /** Initialise for offline mode */
        if(isOfflineMode) {
            PlayerPawn player = new PlayerPawn(new PlayerData(), new Vector2(0, 0));
            World.getInstance().spawn(player, new Vector2(0, 0));
        }
    }

    @Override
    public void dispose() {
        try {
            logger.info("Closing streams.");
            out.close();
            in.close();
            ss.close();
            listener.interrupt();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render() {
        super.render();

        if(!isOfflineMode) {
            NetworkData message = messageQueue.poll();
            if(message != null) {
                handleMessage(message);
            }
        }
    }

    private void handleMessage(NetworkData nData) {
        EDAIProtocol message = nData.type;
        Object data = nData.data;

        // TODO: Separate enemy spawn logic
        if(message == EDAIProtocol.SpawnPlayer || message == EDAIProtocol.SpawnEnemy) {
            PlayerData pData = (PlayerData) data;
            if(pData != null) {
                PlayerPawn player = new PlayerPawn(pData, pData.spawnPos);
                World.getInstance().spawn(player, pData.spawnPos);
            } else {
                // TODO: Possibly handle wrong message, but should never happen!
            }
        }
    }
}
