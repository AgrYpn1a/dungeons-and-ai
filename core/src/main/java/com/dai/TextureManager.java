package com.dai;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dai.entities.IndicatorEntity.EIndicator;
import com.dai.world.World;

public final class TextureManager {
    private static TextureManager instance;

    public static TextureManager getInstance() {
        if(instance == null) {
            instance = new TextureManager();
        }

        return instance;
    }

    private Texture texture;

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public TextureRegion getGroundTile() {
        return new TextureRegion(texture, 0, 1 * World.TILE_SIZE, World.TILE_SIZE, World.TILE_SIZE);
    }

    public TextureRegion getPlayerTexture() {
        return new TextureRegion(texture, 1 * World.TILE_SIZE, 0, World.TILE_SIZE, World.TILE_SIZE);
    }

    public TextureRegion getOpponentTexture() {
        return new TextureRegion(texture, 0, 0, World.TILE_SIZE, World.TILE_SIZE);
    }

    public TextureRegion getEnemyTexture() {
        // return new TextureRegion(texture, 1 * World.TILE_SIZE, 2 * World.TILE_SIZE, World.TILE_SIZE, World.TILE_SIZE);
        return new TextureRegion(texture, 0, 1 * World.TILE_SIZE, World.TILE_SIZE, World.TILE_SIZE);
    }

    public TextureRegion getIndicator() {
        return new TextureRegion(texture, 0, 2 * World.TILE_SIZE, World.TILE_SIZE, World.TILE_SIZE);
    }

    public TextureRegion getIndicator(EIndicator indicatorType) {
        if(indicatorType == EIndicator.PathMarker) {
            return new TextureRegion(texture, 3 * World.TILE_SIZE, 2 * World.TILE_SIZE, World.TILE_SIZE, World.TILE_SIZE);
        }

        // Empty region as default, for now.
        return new TextureRegion(texture, 3 * World.TILE_SIZE, 0, World.TILE_SIZE, World.TILE_SIZE);
    }


    // TODO:

    // public TextureRegion getMoveIndicator() {}

    // public TextureRegion getAttackIndicator() {}
}
