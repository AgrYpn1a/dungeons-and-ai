package com.dai.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.dai.engine.Entity;
import com.dai.engine.RenderComponent;

public class Tile extends Entity {
    public Tile(TextureRegion texture, Vector2 position) {
        super();

        // this.texture = texture;
        this.transform.setPosition(position);

        this.AddComponent(new RenderComponent(texture));
    }
}
