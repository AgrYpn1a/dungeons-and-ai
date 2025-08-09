package com.dai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dai.ai.AStar;
import com.dai.ai.ISearch;
import com.dai.ai.ITraversable;
import com.dai.engine.Engine;
import com.dai.engine.Engine.Layer;
import com.dai.entities.IndicatorEntity.EIndicator;
import com.dai.pools.IndicatorsPool;
import com.dai.engine.Entity;
import com.dai.engine.ITickable;
import com.dai.engine.RenderComponent;
import com.dai.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlayerController implements ITickable {
    private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);

    private static PlayerController instance;
    public static PlayerController getInstance() {
        if(instance == null) {
            return new PlayerController();
        }

        return instance;
    }

    private final PlayerInput input;
    private final Entity actionIndicator;

    private IndicatorsPool pathMarkerPool = new IndicatorsPool(EIndicator.PathMarker);
    private List<Entity> cachedPathMarkers = new ArrayList<>();

    private ISearch search = new AStar();

    public PlayerController() {
        input = new PlayerInput(
                    (mousePos) -> {
                        processMainAction(mousePos);
                    });

        actionIndicator = new Entity() {
            @Override
            public void registerEntity() {
                Engine.getInstance().registerEntity(Layer.Indicators, this);

                this.AddComponent(new RenderComponent(TextureManager.getInstance().getIndicator()));
                this.setPosition(new Vector2(1, 1));
            }

            @Override
            public void render(SpriteBatch batch, float deltaTime) {
                Optional<RenderComponent> r = this.getComponent(RenderComponent.id);
                if(r.get() != null) {
                    // Vector2 worldPos = World.toWorldPos(this.getTransform().getPosition());
                    batch.draw(
                        r.get().getTexture(),
                        this.getTransform().getPosition().x,
                        this.getTransform().getPosition().y
                    );
                }
            }
        };

        Engine.getInstance().registerTickable(this);
        Gdx.input.setInputProcessor(input);

        instance = this;
    }

    public void init() {
        logger.info("Initialized.");
    }

    @Override
    public void tick(float deltaTime) {
        Vector3 mousePos = UIManager.getInstance().getMouseWorldPos();
        Entity e = World.getInstance().getEntityAtPoint(mousePos);

        // Render action indicator over an entity in the world
        if(e != null) {
            actionIndicator.setPosition(World.toWorldPos(e.getPosition()));
        }

        // logger.info("+ Cached markers: " + cachedPathMarkers.size());
        // for(int i=0; i<cachedPathMarkers.size(); i++) {
        //     logger.info(cachedPathMarkers.get(i).getPosition().toString());
        // }
    }

    /**
     * A 'main action' is init on mouse button down
     *  */
    public void processMainAction(Vector3 screenMousePos) {
        Vector3 mousePos = UIManager.getInstance().getMouseWorldPos();
        Entity e = World.getInstance().getEntityAtPoint(mousePos);
        // Vector2 worldPos = World.toWorldPos(e.getPosition());

        // logger.info("lmb pressed at" + "(" + mousePos.x + "," + mousePos.y + ")");
        // logger.info("entity at" + "(" + e.getPosition().x + "," + e.getPosition().y + ")");

        Queue<ITraversable> path = search.findPath(
                                        World.getInstance().getTiles()[0][0],
                                        World.getInstance().getTiles()[(int)e.getPosition().y][(int)e.getPosition().x]);

        // Make sure we have enough markers
        while(cachedPathMarkers.size() < path.size()) {
            cachedPathMarkers.add(pathMarkerPool.borrowFrom());
        }

        // Clear old path
        for(Entity marker : cachedPathMarkers) {
            marker.setShouldRender(false);
        }

        logger.info(">>> Found path <<<");
        int i = 0;
        for(ITraversable t : path) {
            cachedPathMarkers.get(i).setPosition(t.getPosition());
            cachedPathMarkers.get(i).setShouldRender(true);
            i++;
        }
    }

}
