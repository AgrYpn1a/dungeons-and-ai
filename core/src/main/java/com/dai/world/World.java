package com.dai.world;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Stream;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dai.TextureManager;
import com.dai.ai.AStar;
import com.dai.ai.ISearch;
import com.dai.ai.ITraversable;
import com.dai.engine.Entity;
import com.dai.world.Tile.TileData;
import com.dai.world.Tile.TileType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class World {
    private static final Logger logger = LoggerFactory.getLogger(World.class);

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
    private boolean isInit = false;

    // private final TextureRegion textureGround;
    // private final TextureRegion textureWallVertical;
    // private final TextureRegion textureWallHorizontal;

    private final Map<Vector2, Entity> entities;

    private World() {
        entities = new HashMap<>();
    }

    public void init() {
        generateWorld();
    }

    public Tile[][] getTiles() { return tiles; }

    public boolean isInit() { return isInit; }

    public void generateWorld() {
        tiles = new Tile[WORLD_SIZE][WORLD_SIZE];

        for(int y=0; y<WORLD_SIZE; y++) {
            for(int x=0; x<WORLD_SIZE; x++) {
                Tile tile = new Tile(
                    TextureManager.getInstance().getGroundTile(),
                    new Vector2(x, y)
                );
                tiles[y][x] = tile;
            }
        }

        /*
        * A very primitive random tile generator.
        *
        * TODO: If there's enough time turn this into something
        * that actually works properly.
        */

        /** Generate moss paths */
        ISearch search = new AStar(tiles);
        Queue<Vector2> mossPathA = search.findPath(tiles[1][1], tiles[12][8]);
        for(Vector2 pos : mossPathA) {
            tiles[(int) pos.y][(int) pos.x].setType(TileType.Moss);
        }

        Queue<Vector2> mossPathB = search.findPath(tiles[1][12], tiles[12][3]);
        for(Vector2 pos : mossPathB) {
            tiles[(int) pos.y][(int) pos.x].setType(TileType.Moss);
        }

        /** Generate holes and rocks */
        // TODO: Move this into its own config ?
        final int maxHoles = 12;
        final int maxRocks = 6;

        int holesCount = 12;
        int rockCount = 6;

        for(int y=0; y<WORLD_SIZE; y++) {
            for(int x=0; x<WORLD_SIZE; x++) {
                Tile tile = tiles[y][x];

                // Make sure we do not generate near the edges
                if(x > 3 && y > 3) {
                    // float mossFactor = (float) mossCount / maxMossTiles;
                    float holeFactor = (float) holesCount / maxHoles;
                    float rockFactor = (float) rockCount / maxRocks;

                    float random = (float) Math.random();

                    if(holeFactor >= rockFactor) {
                        if(holeFactor > random) {
                            tile.setType(TileType.Hole);
                            holesCount--;
                        }
                    } else if(rockFactor >= holeFactor) {
                        if(rockFactor > random) {
                            tile.setType(TileType.Rock);
                            rockCount--;
                        }
                    }
                }
            }
        }

        isInit = true;
    }

    public Stream<Entity> getEntities() {
        Stream<Entity> worldEntities = entities.values().stream();
        return worldEntities;
    }

    public Tile getTileAtPoint(Vector2 point) {
        int x = (int)point.x;
        int y = (int)point.y;

        if(y >= tiles.length || x >= tiles[0].length || y < 0 || x < 0) {
            return null;
        }

        return tiles[y][x];
    }

    public ITraversable getTraversableAtPoint(Vector2 point) {
        // Vector2 gridPos = toGridPos(new Vector3(point.x, point.y, 0));

        int x = (int)point.x;
        int y = (int)point.y;

        if(y >= tiles.length || x >= tiles[0].length || y < 0 || x < 0) {
            return null;
        }

        return (ITraversable)tiles[y][x];
    }

    // TODO: Should we maybe sync the whole entities map?
    public Entity getEntityAtPoint(Vector3 point) {
        Vector2 v2Pos = new Vector2(point.x, point.y);

        for(Entity e : entities.values()) {
            if(e.getPosition().equals(v2Pos)) {
                return e;
            }
        }

        return null;
    }

    public Entity getEntityAtPoint(Vector2 point) {
        for(Entity e : entities.values()) {
            if(e.getPosition().equals(point)) {
                return e;
            }
        }

        return null;
    }

    public List<ITraversable> getNeighbours(ITraversable node) {
        List<ITraversable> neighbours = new LinkedList<>();

        int x = (int)node.getPosition().x;
        int y = (int)node.getPosition().y;

        // Check north
        if(y+1 >= 0 && y+1 < tiles.length) {
            neighbours.add(tiles[y+1][x]);
        }

        // Check south
        if(y-1 >= 0 && y-1 < tiles.length) {
            neighbours.add(tiles[y-1][x]);
        }

        // Check east
        if(x+1 >= 0 && x+1 < tiles[0].length) {
            neighbours.add(tiles[y][x+1]);
        }

        // Check west
        if(x-1 >= 0 && x-1 < tiles[0].length) {
            neighbours.add(tiles[y][x-1]);
        }

        return neighbours;
    }

    public synchronized void spawn(Entity e, Vector2 pos) {
        entities.put(pos, e);
    }

    public int getPathCost(Queue<Vector2> path) {
        int cost = 0;

        for(Vector2 pos : path) {
            Tile t = getTileAtPoint(pos);
            int currCost = 1 * t.getCostModifier();
            cost += currCost;
        }

        return cost;
    }

    /** Coordinates */
    public static Vector2 toWorldPos(Vector2 pos) {
        return new Vector2(pos.x * TILE_SIZE, pos.y * TILE_SIZE);
    }

    public static Vector2 worldToGrid(Vector3 point) {
        int x = Math.round((point.x - TILE_SIZE / 2) / TILE_SIZE);
        int y = Math.round((point.y - TILE_SIZE / 2) / TILE_SIZE);

        return new Vector2(x, y);
    }

    /** World data sharing */
    public TileData[][] exportWorld() {
        TileData[][] data = new TileData[tiles.length][tiles[0].length];

        for(int y=0; y<WORLD_SIZE; y++) {
            for(int x=0; x<WORLD_SIZE; x++) {
                data[y][x] = tiles[y][x].getTileData();
            }
        }

        return data;
    }

    public void importWorld(TileData[][] data) {
        tiles = new Tile[WORLD_SIZE][WORLD_SIZE];

        for(int y=0; y<WORLD_SIZE; y++) {
            for(int x=0; x<WORLD_SIZE; x++) {
                Tile tile = new Tile(data[y][x]);

                // We need to replace old tiles
                if(tiles[y][x] != null) {
                    tiles[y][x].destroy();
                }

                tiles[y][x] = tile;
            }
        }

        isInit = true;
    }
}
