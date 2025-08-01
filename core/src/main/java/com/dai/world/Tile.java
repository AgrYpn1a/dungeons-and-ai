package com.dai.world;

import java.util.Optional;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.dai.engine.Entity;
import com.dai.engine.RenderComponent;

public class Tile extends Entity {
    public Tile(TextureRegion texture, Vector2 position) {
        super();

        this.transform.setPosition(position);
        this.AddComponent(new RenderComponent(texture));
    }

    @Override
    public String toString() {
        return "Tile at (" + this.transform.getPosition().x +  ", " + this.transform.getPosition().y + ")";
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
