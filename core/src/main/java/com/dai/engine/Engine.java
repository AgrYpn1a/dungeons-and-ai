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
        if(instance == null) {
            instance = new Engine();
        }

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

    private SpriteBatch mainBatch;

    // Engine without rendering
    public Engine() {
        mainBatch = null;
        tickables = new ArrayList<>();

        layerEntities = new HashMap<>();
        layerViewports = new HashMap<>();

        for(int i=0; i<layers.length; i++) {
            Layer layer = layers[i];
            layerEntities.put(layer, new ArrayList<>(1000));
            layerViewports.put(layer, Optional.empty());
        }

        instance = this;
    }

    public Engine(SpriteBatch mainBatch) {
        this();
        this.mainBatch = mainBatch;
    }

    public void init() {}

    public void setMainBatch(SpriteBatch mainBatch) {
        this.mainBatch = mainBatch;
    }

    public SpriteBatch getMainBatch() { return mainBatch; }

    public void render(float dt) {
        // Headless mode
        if(mainBatch == null) {
            return;
        }

        for(int i=0; i<layers.length; i++) {
            Layer layer = layers[i];
            Optional<Viewport> viewport = layerViewports.get(layer);
            List<Entity> entities = layerEntities.get(layer);
            // logger.info("Rendering entities from  " + layer + " # " + entities.size());

            if(viewport.get() != null && entities.size() > 0) {
                viewport.get().apply();
                mainBatch.setProjectionMatrix(viewport.get().getCamera().combined);

                for(int j=0; j<entities.size(); j++) {
                    if(entities.get(j).shouldRender()) {
                        entities.get(j).render(mainBatch, dt);
                    }
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
