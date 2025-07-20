package com.dai.world;

import java.util.Arrays;
import java.util.stream.Stream;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dai.engine.Entity;

public class World {

    public static final int WORLD_SIZE = 16;
    public static final int TILE_SIZE = 8;

    public static World instance;
    private Tile[][] tiles;

    private final Texture worldTexture;
    private final TextureRegion textureGround;
    private final TextureRegion textureWallVertical;
    private final TextureRegion textureWallHorizontal;

    private World() {
        this.worldTexture = new Texture("tileset.png");
        this.textureGround = new TextureRegion(
            this.worldTexture,
            3*TILE_SIZE,
            6*TILE_SIZE,
            TILE_SIZE,
            TILE_SIZE
        );
        this.textureWallHorizontal = new TextureRegion(
            this.worldTexture,
            4*TILE_SIZE,
            4*TILE_SIZE,
            TILE_SIZE,
            TILE_SIZE
        );
        this.textureWallVertical = new TextureRegion(
            this.worldTexture,
            4*TILE_SIZE,
            5*TILE_SIZE,
            TILE_SIZE,
            TILE_SIZE
        );

        this.tiles = new Tile[WORLD_SIZE][WORLD_SIZE];
        for(int y=0; y<WORLD_SIZE; y++) {
            for(int x=0; x<WORLD_SIZE; x++) {

                // Top wall
                // if(y == 0) {
                //     this.tiles[y][x] = new Tile(
                //         textureWallHorizontal,
                //         new Vector2(x * TILE_SIZE, y * TILE_SIZE)
                //     );
                //     continue;
                // }

                // if(y == WORLD_SIZE-1) {
                //     this.tiles[y][x] = new Tile(
                //         textureWallHorizontal,
                //         new Vector2(x * TILE_SIZE, y * TILE_SIZE)
                //     );
                //     continue;
                // }

                // // Side walls
                // if(x == 0) {
                //     this.tiles[y][x] = new Tile(
                //         textureWallVertical,
                //         new Vector2(x * TILE_SIZE, y * TILE_SIZE)
                //     );
                //     continue;
                // }

                // if(x == WORLD_SIZE-1) {
                //     this.tiles[y][x] = new Tile(
                //         textureWallVertical,
                //         new Vector2(x * TILE_SIZE, y * TILE_SIZE)
                //     );
                //     continue;
                // }

                this.tiles[y][x] = new Tile(
                    textureGround,
                    new Vector2(x * TILE_SIZE, y * TILE_SIZE)
                );
            }
        }
    }

    public static World getInstance() {
        if(instance == null) {
            instance = new World();
        }

        return instance;
    }

    // TODO: If world will have more entities, we will
    // merge them together here.
    public Stream<Entity> getEntities() {
        return Arrays.stream(this.tiles).flatMap(Arrays::stream);
    }

    public Entity getEntityAtPoint(Vector3 point) {
        int x = Math.round((point.x - TILE_SIZE / 2) / TILE_SIZE);
        int y = Math.round((point.y - TILE_SIZE / 2) / TILE_SIZE);

        if(y >= tiles.length || x >= tiles[0].length || y < 0 || x < 0) {
            return null;
        }

        return tiles[y][x];
    }
}
