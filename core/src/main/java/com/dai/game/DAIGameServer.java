package com.dai.game;

import com.dai.DAIServer;
import com.dai.network.DAINetwork;

public final class DAIGameServer extends DAIGameCore {
    private DAINetwork network;
    private DAIServer server;
    private Thread thrServer;

    public DAIGameServer() {
        super();

        network = new DAINetwork(true);
        server = DAIServer.getInstance();
        thrServer = new Thread(server);
        thrServer.setDaemon(true);
        thrServer.start();
    }

    @Override
    public void render() {
        super.render();
    }
}
