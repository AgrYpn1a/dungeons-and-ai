package com.dai.entities;

import com.dai.engine.Entity;
import com.dai.engine.RenderComponent;
import com.dai.world.World;

import java.util.Optional;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.dai.TextureManager;
import com.dai.engine.Engine;
import com.dai.engine.Engine.Layer;

public final class IndicatorEntity extends Entity {

    public static enum EIndicator {
        PathMarker,
        PathTargetMarker,
        PathUnreachableMarker
    }

    private EIndicator indicatorType;

    public IndicatorEntity(EIndicator indicatorType) {
        this.indicatorType = indicatorType;
        this.AddComponent(new RenderComponent(
            TextureManager.getInstance().getIndicator(indicatorType)
        ));
    }

    public EIndicator getIndicatorType() { return indicatorType; }

    public void changeType(EIndicator indicatorType) {

        // A slight optimization
        if(this.indicatorType == indicatorType) {
            return;
        }

        Optional<RenderComponent> r = this.getComponent(RenderComponent.id);
        if(!r.isEmpty()) {
            r.get().setTexture(TextureManager.getInstance().getIndicator(indicatorType));
            this.indicatorType = indicatorType;
        }
    }

    @Override
    public void registerEntity() {
        Engine.getInstance().registerEntity(Layer.Indicators, this);
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
