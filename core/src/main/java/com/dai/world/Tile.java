package com.dai.world;

import java.io.Serializable;
import java.util.Optional;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.dai.TextureManager;
import com.dai.ai.ITraversable;
import com.dai.engine.Entity;
import com.dai.engine.RenderComponent;
import com.dai.network.NetworkManager;

public class Tile extends Entity implements ITraversable, Serializable {

    public static enum TileType {
        Ground,
        Hole,
        Rock,
        Moss
    }

    public static class TileData implements Serializable {
        public Vector2 position;
        public TileType type;
    }

    private TileType type = TileType.Ground;
    private RenderComponent renderer;

    public Tile(TextureRegion texture, Vector2 position) {
        super();

        transform.setPosition(position);
        renderer = this.AddComponent(new RenderComponent(texture));

        // TODO
        // Disable rendering on server
        // if(NetworkManager.isServer()) {
        //     this.setShouldRender(false);
        // }
    }

    public Tile(TileData data) {
        super();


        // TODO
        // Disable rendering on server
        // if(NetworkManager.isServer()) {
        //     this.setShouldRender(false);
        // }

        renderer = this.AddComponent(new RenderComponent(TextureManager.getInstance().getTile(data.type)));

        transform.setPosition(data.position);
        type = data.type;
    }

    public TileData getTileData() {
        TileData data = new TileData();
        data.position = transform.getPosition();
        data.type = type;

        return data;
    }

    @Override
    public boolean isTraversable() {
        return type == TileType.Ground || type == TileType.Moss;
    }

    @Override
    public int getCostModifier() {
        if(type == TileType.Moss) {
            return 2;
        }

        return 1;
    }

    @Override
    public String toString() {
        return "Tile at (" + this.transform.getPosition().x +  ", " + this.transform.getPosition().y + ")";
    }

    @Override
    public void render(SpriteBatch batch, float deltaTime) {
        if(renderer != null) {
            Vector2 worldPos = World.toWorldPos(this.getTransform().getPosition());
            batch.draw(
                renderer.getTexture(),
                worldPos.x,
                worldPos.y
            );
        }
    }

    public void setType(TileType newType) {
        if(renderer != null) {
            renderer.setTexture(TextureManager.getInstance().getTile(newType));
            type = newType;
        }
    }
}
