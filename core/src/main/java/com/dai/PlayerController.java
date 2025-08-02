package com.dai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dai.engine.Engine;
import com.dai.engine.Engine.Layer;
import com.dai.engine.Entity;
import com.dai.engine.ITickable;
import com.dai.engine.RenderComponent;
import com.dai.world.World;

import java.util.Optional;

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
    }

    /**
     * A 'main action' is init on mouse button down
     *  */
    public void processMainAction(Vector2 mousePos) {
        logger.info("LMB pressed " + "(" + mousePos.x + "," + mousePos.y + ")");
    }

}
