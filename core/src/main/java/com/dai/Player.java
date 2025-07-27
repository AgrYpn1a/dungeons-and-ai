package com.dai;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;
import com.dai.engine.Entity;
import com.dai.engine.RenderComponent;

public final class Player extends Entity {

    private static int _id = 0;
    private int id;

    public static class PlayerData implements Serializable {
        public String name;
        public Vector2 spawnPos;
    }

    private PlayerData data;

    public Player(PlayerData data, Vector2 pos) {
        super();

        this.data = data;
        this.id = _id++;

        this.setPosition(pos);
        this.AddComponent(new RenderComponent(
            TextureManager.getInstance().getPlayerTexture()));
    }

    public int getId() { return this.id; }

    @Override
    public String toString() {
        // TODO
        return this.data.name;
    }

}
