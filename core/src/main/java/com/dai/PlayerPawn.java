package com.dai;

import java.io.Serializable;
import java.util.Optional;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.dai.engine.Entity;
import com.dai.engine.RenderComponent;
import com.dai.engine.Engine;
import com.dai.engine.Engine.Layer;
import com.dai.world.World;

public final class PlayerPawn extends Entity {

    private static int _id = 0;
    private int id;

    public static class PlayerData implements Serializable {
        public String name;
        public Vector2 spawnPos;
    }

    private PlayerData data;

    public PlayerPawn(PlayerData data, Vector2 pos, boolean isOpponent) {
        super();

        this.data = data;
        this.id = _id++;

        this.setPosition(pos);
        this.AddComponent(new RenderComponent(
            isOpponent
            ? TextureManager.getInstance().getOpponentTexture()
            : TextureManager.getInstance().getPlayerTexture()));
    }

    @Override
    public void registerEntity() {
        Engine.getInstance().registerEntity(Layer.Player, this);
    }

    public int getId() { return this.id; }

    @Override
    public String toString() {
        // TODO
        return this.data.name;
    }

    @Override
    public void tick(float deltaTime) {

    }

    @Override
    public void render(SpriteBatch batch, float deltaTime) {
        Optional<RenderComponent> r = this.getComponent(RenderComponent.id);
        if(r.get() != null) {
            Vector2 worldPos = World.toWorldPos(this.getTransform().getPosition());
            batch.draw(
                r.get().getTexture(),
                worldPos.x,
                worldPos.y
            );
        }
    }

}
