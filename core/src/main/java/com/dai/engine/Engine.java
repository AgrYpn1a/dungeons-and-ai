package com.dai.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.dai.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Engine {
    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    private static Engine instance;

    public static Engine getInstance() {
        // if(instance == null) {
        //     return new Engine();
        // }

        return instance;
    }

    public enum Layer {
        Default,
        Player,
        UI
    }

    private final Map<Layer, Viewport> layerViewports;
    private final Map<Layer, ArrayList<Entity>> layerEntities;

    private ArrayList<ITickable> tickables;

    private Layer[] layers = new Layer[] {
        Layer.Default,
        Layer.Player,
        Layer.UI
    };

    private final SpriteBatch mainBatch;

    public Engine(SpriteBatch mainBatch) {
        this.mainBatch = mainBatch;

        tickables = new ArrayList<>();

        layerEntities = new HashMap<>();
        layerViewports = new HashMap<>();

        for(int i=0; i<layers.length; i++) {
            Layer layer = layers[i];
            layerEntities.put(layer, new ArrayList<>(1000));
        }

        instance = this;
    }

    public void render(float dt) {
        for(int i=0; i<layers.length; i++) {
            Layer layer = layers[i];
            Viewport viewport = layerViewports.get(layer);
            List<Entity> entities = layerEntities.get(layer);

            if(viewport != null && entities.size() > 0) {
                viewport.apply();
                mainBatch.setProjectionMatrix(viewport.getCamera().combined);

                for(int j=0; j<entities.size(); j++) {
                    entities.get(j).render(mainBatch, dt);
                }

                // We need to flush here, in order not to mix viewports
                mainBatch.flush();
            }
        }
    }

    public void tick(float dt) {
        for(ITickable t : tickables) { t.tick(dt); }
    }

    public void registerViewport(Layer layer, Viewport viewport) {
        layerViewports.put(layer, viewport);
    }

    public void registerEntity(Layer layer, Entity entity) {
        layerEntities.get(layer).add(entity);
        tickables.add(entity);
    }

    public void registerTickable(ITickable t) {
        tickables.add(t);
    }
}
