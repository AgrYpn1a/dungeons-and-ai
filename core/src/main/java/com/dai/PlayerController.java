package com.dai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.dai.engine.Engine;
import com.dai.engine.ITickable;

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

    public PlayerController() {
        input = new PlayerInput(
                    (mousePos) -> {
                        processMainAction(mousePos);
                    });

        instance = this;
        Engine.getInstance().registerTickable(this);
        Gdx.input.setInputProcessor(input);
    }

    public void init() {
        logger.info("Initialized.");
    }

    @Override
    public void tick(float deltaTime) { }

    /**
     * A 'main action' is init on mouse button down
     *  */
    public void processMainAction(Vector2 mousePos) {
        logger.info("LMB pressed " + "(" + mousePos.x + "," + mousePos.y + ")");
    }

}
