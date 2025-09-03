package com.dai.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.dai.DAIServer;
import com.dai.PlayerController;
import com.dai.PlayerController.PlayerData;
import com.dai.PlayerPawn;
import com.dai.TextureManager;
import com.dai.UIManager;
import com.dai.network.INetworkGameClient;
import com.dai.network.INetworkGameServer;
import com.dai.network.NetworkGameClient;
import com.dai.network.NetworkGameServer;
import com.dai.network.NetworkListener;
import com.dai.network.NetworkManager;
import com.dai.network.NetworkListener.NetworkData;
import com.dai.network.NetworkPawn;
import com.dai.screens.GameScreen;
import com.dai.server.EDAIProtocol;
import com.dai.world.Pawn.PawnData;
import com.dai.world.World;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;

public final class DAIGameClient extends DAIGameCore {
    private static final Logger logger = LoggerFactory.getLogger(DAIGameClient.class.getSimpleName());
    private GameScreen scrMainGame;

    /** Offline mode */
    private static boolean isOfflineMode = true;
    public static boolean isOffline() { return isOfflineMode; }

    /** Net logic */
    private Socket ss;
    private PlayerPawn offlinePlayer;
    private NetworkListener listener;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private Queue<NetworkData> messageQueue;

    public DAIGameClient(boolean isOfflineMode) {
        super();

        DAIGameClient.isOfflineMode = isOfflineMode;
    }

    @Override
    public void create() {
        super.create();

        logger.info("Calling create()");
        System.out.println("Calling create()");

        TextureManager.getInstance().setTexture(new Texture("selenasdungeon32x32.png"));

        // We will not init world unless in offline mode,
        // instead it should be imported from server
        if(isOfflineMode) {
            World.getInstance().init();
        }
        PlayerController.getInstance().init();

        engine.setMainBatch(new SpriteBatch());

        /** Init the libgdx game screen */
        scrMainGame = new GameScreen();
        setScreen(scrMainGame);

        UIManager.getInstance().init();

        logger.info("create() called successfully.");

        // TODO: Move this out
        if(!isOfflineMode) {
            try {
                // Connect to the server
                ss = new Socket("localhost", DAIServer.PORT);

                out = new ObjectOutputStream (ss.getOutputStream());
                in = new ObjectInputStream(ss.getInputStream());
                messageQueue = new ConcurrentLinkedQueue<>();

                /** Let the connection run in the background */
                listener = new NetworkListener(in, out, messageQueue);
                listener.setDaemon(true);
                listener.start();

            } catch(Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            } finally {
                // try { ss.close(); } catch(Exception e) {}
            }
        }

        /** Setup offline-mode */
        if(isOfflineMode) {
            Vector2 location = new Vector2(0, 0);
            PlayerPawn pawnPlayer = new PlayerPawn(new PawnData(), location, false);
            World.getInstance().spawn(pawnPlayer, location);

            PlayerController.getInstance().setPlayerPawn(pawnPlayer);
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        try {
            ss.close();
            logger.info("dispose() called successfully.");
        } catch(Exception e) {
            logger.error("dispose() called with exception: " + e.getMessage());
        }
    }

    /** Main game loop */
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

    /*
     * !IMPORTANT! TODO:
     *
     * Let's introduce a NetworkManager to handle all networked objects.
     *
     */
    // private Registry registry;
    private INetworkGameServer netGameServer;
    private INetworkGameClient netGameClient;

    private void handleMessage(NetworkData nData) {
        EDAIProtocol message = nData.type;
        logger.info("[handleMessage] -> Got " + message);
        System.out.println("[handleMessage] -> Got " + message);

        /** This should be the first message coming from the server */
        if(message == EDAIProtocol.Connected) {
            try {

                // Connect to registry
                // registry = LocateRegistry.getRegistry( 16000);
                // logger.info(registry.toString());

                // Get remote NetworkGame
                // netGameServer = (INetworkGameServer) registry.lookup(NetworkGameServer.class.getSimpleName());

                logger.info("[EDAIProtocol.Connected] -> Trying to get NetworkGameServer and NetworkGameClient instances.");
                netGameServer = NetworkGameServer.getInstance();
                netGameClient = NetworkGameClient.getInstance();

                netGameServer.registerClient(netGameClient);

                PlayerController.getInstance().initNetworking(netGameServer);
            } catch(Exception e) {
                logger.error(e.getMessage());
            }
        }
    }
}
