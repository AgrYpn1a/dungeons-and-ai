package com.dai.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dai.engine.Engine;
import com.dai.engine.Entity;
import com.dai.engine.Engine.Layer;

public class World {

    public static final int WORLD_SIZE = 32;
    public static final int TILE_SIZE = 8;
    public static final float CAMERA_ZOOM = 0.3f;

    public static World instance;
    private Tile[][] tiles;

    private final Texture worldTexture;
    private final TextureRegion textureGround;
    private final TextureRegion textureWallVertical;
    private final TextureRegion textureWallHorizontal;

    private final Map<Vector2, Entity> entities;

    private World() {
        // entities = Collections.synchronizedList(new ArrayList<>(50));
        entities = new HashMap<>();

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
                    // new Vector2(x * TILE_SIZE, y * TILE_SIZE)
                    new Vector2(x, y)
                );
                Engine.getInstance().registerEntity(Layer.Default, this.tiles[y][x]);
            }
        }
    }

    public static World getInstance() {
        if(instance == null) {
            instance = new World();
        }

        return instance;
    }

    public Stream<Entity> getEntities() {
        Stream<Entity> tiles = Arrays.stream(this.tiles).flatMap(Arrays::stream);
        Stream<Entity> worldEntities = entities.values().stream();

        return Stream.concat(tiles, worldEntities);
    }

    public Entity getEntityAtPoint(Vector3 point) {
        // int x = Math.round((point.x - TILE_SIZE / 2) / TILE_SIZE);
        // int y = Math.round((point.y - TILE_SIZE / 2) / TILE_SIZE);

        Vector2 gridPos = toGridPos(point);
        int x = (int)gridPos.x;
        int y = (int)gridPos.y;

        if(y >= tiles.length || x >= tiles[0].length || y < 0 || x < 0) {
            return null;
        }

        return tiles[y][x];
    }

    public synchronized void spawn(Entity e, Vector2 pos) {
        entities.put(pos, e);
        Engine.getInstance().registerEntity(Layer.Player, e);
    }

    public static Vector2 toWorldPos(Vector2 pos) {
        return new Vector2(pos.x * TILE_SIZE, pos.y * TILE_SIZE);
    }

    public static Vector2 toGridPos(Vector3 point) {
        int x = Math.round((point.x - TILE_SIZE / 2) / TILE_SIZE);
        int y = Math.round((point.y - TILE_SIZE / 2) / TILE_SIZE);

        return new Vector2(x, y);
    }
}
