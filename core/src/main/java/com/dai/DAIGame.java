package com.dai;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.dai.PlayerController.PlayerData;
import com.dai.engine.Engine;
import com.dai.network.INetworkGameClient;
import com.dai.network.INetworkGameServer;
import com.dai.network.NetworkGameClient;
import com.dai.network.NetworkGameServer;
import com.dai.network.NetworkListener;
import com.dai.network.NetworkListener.NetworkData;
import com.dai.network.NetworkPawn;
import com.dai.screens.GameScreen;
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

        /** Initialise core singletons and managers */
        Engine.getInstance().init();
        World.getInstance().init();
        UIManager.getInstance().init();
        PlayerController.getInstance().init();

        /** Initialise for offline mode */
        if(isOfflineMode) {
            PlayerPawn offlinePlayer = new PlayerPawn(new Vector2(0, 0), false);
            World.getInstance().spawn(offlinePlayer, new Vector2(0, 0));
            PlayerController.getInstance().setPlayerPawn(offlinePlayer);
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

    /*
     * !IMPORTANT! TODO:
     *
     * Let's introduce a NetworkManager to handle all networked objects.
     *
     */
    private List<NetworkPawn> netPawns = new LinkedList<>();
    private Registry registry;
    private INetworkGameServer netGameServer;
    private INetworkGameClient netGameClient;

    private void handleMessage(NetworkData nData) {
        EDAIProtocol message = nData.type;
        Object data = nData.data;

        /** This should be the first message coming from the server */
        if(message == EDAIProtocol.Connected) {
            try {

                // Connect to registry
                registry = LocateRegistry.getRegistry( 16000);
                logger.info(registry.toString());

                // Get remote NetworkGame
                netGameServer = (INetworkGameServer) registry.lookup(NetworkGameServer.class.getSimpleName());
                netGameClient = NetworkGameClient.getInstance();

                netGameServer.registerClient(netGameClient);

                // PlayerController.getInstance().initNetworking(networkGame);
            } catch(Exception e) {
                logger.error(e.getMessage());
            }
        }
        else if(message == EDAIProtocol.SpawnPlayer) {
            PlayerData pData = (PlayerData) data;
            if(pData != null) {
                PlayerPawn pawnPlayer = new PlayerPawn(pData.spawnPos, false);
                World.getInstance().spawn(pawnPlayer, pData.spawnPos);

                PlayerController.getInstance().setPlayerPawn(pawnPlayer);
                PlayerController.getInstance().setPlayerData(pData);

                // Create my network pawn
                // try {
                    // NetworkPawn netPawnPlayer = new NetworkPawn(pawnPlayer);
                    // netPawns.add(netPawnPlayer);
                    // UnicastRemoteObject.exportObject(netPawnPlayer, 0);
                // } catch(RemoteException e) {
                //     logger.error("Failed to init NetworkPawn: " + e.getMessage());
                // }
            } else {
                // TODO: Possibly handle wrong message, but should never happen!
            }
        }
        // else if(message == EDAIProtocol.SpawnEnemy) {
        //     PlayerData pData = (PlayerData) data;
        //     if(pData != null) {
        //         PlayerPawn pawnOpponent = new PlayerPawn(pData.spawnPos, true);
        //         World.getInstance().spawn(pawnOpponent, pData.spawnPos);

        //         // Create opponent network pawn
        //         try {
        //             // NetworkPawn netPawnOpponent = new NetworkPawn(pawnOpponent);
        //             // netPawns.add(netPawnOpponent);
        //         } catch(RemoteException e) {
        //             logger.error("Failed to init NetworkPawn: " + e.getMessage());
        //         }
        //     } else {
        //         // TODO: Possibly handle wrong message, but should never happen!
        //     }
        // }
    }
}
