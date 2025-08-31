package com.dai;

import java.util.Optional;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.dai.engine.RenderComponent;
import com.dai.engine.Engine;
import com.dai.engine.Engine.Layer;
import com.dai.network.NetworkManager;
import com.dai.world.Pawn;
import com.dai.world.World;

public final class PlayerPawn extends Pawn {

    public static enum EPlayerActionResult {
        Success,
        Failed,
        NotEnoughActionPoints
    }

    /** Stats */
    private int actionPoints = 0;

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
        if(!NetworkManager.isServer()) {
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

    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
    }

    @Override
    public void render(SpriteBatch batch, float deltaTime) {
        Optional<RenderComponent> r = this.getComponent(RenderComponent.id);
        if(!r.isEmpty())  {
            Vector2 worldPos = World.toWorldPos(this.getTransform().getPosition());
            batch.draw(
                r.get().getTexture(),
                worldPos.x,
                worldPos.y
            );
        }
    }

    public int getActionPoints() { return actionPoints; }
    public boolean consumeActionPoints(int points) {
        int newPoints = actionPoints - points;
        if(newPoints >= 0) {
            actionPoints = newPoints;
            return true;
        }

        return false;
    }

    // public boolean (int cost) {
    //     return actionPoints
    // }

}
