package com.dai.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

public final class Engine {
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

    private Map<Layer, Optional<Viewport>> layerViewports;
    private Map<Layer, ArrayList<Entity>> layerEntities;

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


    public void registerTickable(ITickable t) {
        tickables.add(t);
    }

    public Optional<Viewport> getLayerViewport(Layer layer) {
        return layerViewports.get(layer);
    }

    /**
     * Use this method to register new entity.
     *
     * @param layer
     * @param entity
    */
    public synchronized void registerEntity(Layer layer, Entity entity) {
        // TODO: Do we need queue for adding entites here as well?
        layerEntities.get(layer).add(entity);
        tickables.add(entity);
    }

    public synchronized void destroyEntity(Layer layer, Entity entity) {
        // TODO: Need to add queue for destroying entities
        layerEntities.get(layer).remove(entity);
        tickables.remove(entity);
    }
}
