package com.dai;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dai.entities.IndicatorEntity.EIndicator;
import com.dai.world.World;
import com.dai.world.Tile.TileType;

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

        if(indicatorType == EIndicator.PathUnreachableMarker) {
            return new TextureRegion(texture, 2 * World.TILE_SIZE, 2 * World.TILE_SIZE, World.TILE_SIZE, World.TILE_SIZE);
        }

        if(indicatorType == EIndicator.PathTargetMarker) {
            return getIndicator();
        }

        // Empty region as default, for now.
        return new TextureRegion(texture, 3 * World.TILE_SIZE, 0, World.TILE_SIZE, World.TILE_SIZE);
    }

    public TextureRegion getTile(TileType type) {
        switch (type) {
            case Hole:
                return new TextureRegion(texture, 1 * World.TILE_SIZE, 1 * World.TILE_SIZE, World.TILE_SIZE, World.TILE_SIZE);
            case Rock:
                return new TextureRegion(texture, 2 * World.TILE_SIZE, 1 * World.TILE_SIZE, World.TILE_SIZE, World.TILE_SIZE);
            case Moss:
                return new TextureRegion(texture, 4 * World.TILE_SIZE, 1 * World.TILE_SIZE, World.TILE_SIZE, World.TILE_SIZE);

            default:
                return getGroundTile();
        }
    }


    // TODO:

    // public TextureRegion getMoveIndicator() {}

    // public TextureRegion getAttackIndicator() {}
}
