package com.dai.game;

import com.badlogic.gdx.graphics.Texture;
import com.dai.DAIServer;
import com.dai.TextureManager;
import com.dai.network.DAINetwork;
import com.dai.world.World;

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
    public void create() {
        super.create();

        // TODO: We do not want TextureManager on server
        TextureManager.getInstance().setTexture(new Texture("selenasdungeon32x32.png"));
        World.getInstance().init();
    }

    @Override
    public void render() {
        super.render();
    }
}
