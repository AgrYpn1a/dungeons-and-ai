package com.dai;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.dai.engine.RenderComponent;
import com.dai.engine.Engine;
import com.dai.engine.Engine.Layer;
import com.dai.network.DAINetwork;
import com.dai.world.Pawn;
import com.dai.world.World;

public final class PlayerPawn extends Pawn {
    public PlayerPawn(Vector2 pos, boolean isOpponent) {
        super(new PawnData());

        this.setPosition(pos);
        this.AddComponent(new RenderComponent(
            isOpponent
            ? TextureManager.getInstance().getOpponentTexture()
            : TextureManager.getInstance().getPlayerTexture()));
    }

    public PlayerPawn(PawnData data, Vector2 pos, boolean isOpponent) {
        super(data);

        this.setPosition(pos);

        // Setup rendering
        if(!DAINetwork.isServer()) {
            this.AddComponent(new RenderComponent(
                isOpponent
                ? TextureManager.getInstance().getOpponentTexture()
                : TextureManager.getInstance().getPlayerTexture()));
        }
    }

    @Override
    public void registerEntity() {
        Engine.getInstance().registerEntity(Layer.Player, this);
    }

    // public int getId() { return this.id; }

    // @Override
    // public String toString() {
    //     // TODO
    //     return this.data.name;
    // }

    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
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
