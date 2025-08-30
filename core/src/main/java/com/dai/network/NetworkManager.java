package com.dai.network;

import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dai.game.DAIGameClient;

public final class NetworkManager {
    private static final Logger logger = LoggerFactory.getLogger(NetworkManager.class);

    // TODO: Maybe we do not need this to be a singleton
    private static NetworkManager instance;
    public static NetworkManager getInstance() {
        if(instance == null) {
            instance = new NetworkManager();
        }

        return instance;
    }

    public static boolean isServer() {
        if(DAIGameClient.isOffline()) {
            return false;
        }

        try {
            INetworkGameServer server = NetworkGameServer.getInstance();
            return server.isServer();
        } catch (RemoteException e) {
            logger.error("Failed calling isServer(): " + e.getMessage());
            return false;
        }
    }

    public static boolean isOffline() {
        return DAIGameClient.isOffline();
    }
}
