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
import com.dai.TextureManager;
import com.dai.engine.Engine;
import com.dai.engine.Entity;
import com.dai.engine.Engine.Layer;

public class World {
    public static World instance;
    public static World getInstance() {
        if(instance == null) {
            instance = new World();
        }

        return instance;
    }

    public static final int WORLD_SIZE = 16;
    public static final int TILE_SIZE = 32;
    public static final float CAMERA_ZOOM = 1.5f;

    private Tile[][] tiles;

    // private final TextureRegion textureGround;
    // private final TextureRegion textureWallVertical;
    // private final TextureRegion textureWallHorizontal;

    private final Map<Vector2, Entity> entities;

    private World() {
        entities = new HashMap<>();

        this.tiles = new Tile[WORLD_SIZE][WORLD_SIZE];
        for(int y=0; y<WORLD_SIZE; y++) {
            for(int x=0; x<WORLD_SIZE; x++) {
                this.tiles[y][x] = new Tile(
                    TextureManager.getInstance().getGroundTile(),
                    new Vector2(x, y)
                );
            }
        }
    }

    public void init() {}

    public Stream<Entity> getEntities() {
        Stream<Entity> tiles = Arrays.stream(this.tiles).flatMap(Arrays::stream);
        Stream<Entity> worldEntities = entities.values().stream();

        return Stream.concat(tiles, worldEntities);
    }

    public Entity getEntityAtPoint(Vector3 point) {
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
