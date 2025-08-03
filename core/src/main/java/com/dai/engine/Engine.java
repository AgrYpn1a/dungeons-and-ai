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
        Indicators,
        UI
    }

    private final Map<Layer, Optional<Viewport>> layerViewports;
    private final Map<Layer, ArrayList<Entity>> layerEntities;

    private ArrayList<ITickable> tickables;

    private Layer[] layers = new Layer[] {
        Layer.Default,
        Layer.Player,
        Layer.Indicators,
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

    public void init() {}

    public void render(float dt) {
        for(int i=0; i<layers.length; i++) {
            Layer layer = layers[i];
            Optional<Viewport> viewport = layerViewports.get(layer);
            List<Entity> entities = layerEntities.get(layer);

            if(viewport.get() != null && entities.size() > 0) {
                viewport.get().apply();
                mainBatch.setProjectionMatrix(viewport.get().getCamera().combined);

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
        layerViewports.put(layer, Optional.of(viewport));
    }

    public void registerEntity(Layer layer, Entity entity) {
        layerEntities.get(layer).add(entity);
        tickables.add(entity);
    }

    public void registerTickable(ITickable t) {
        tickables.add(t);
    }

    public Optional<Viewport> getLayerViewport(Layer layer) {
        return layerViewports.get(layer);
    }
}
