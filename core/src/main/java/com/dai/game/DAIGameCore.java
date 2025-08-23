package com.dai.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.dai.engine.Engine;
import com.dai.world.World;

public abstract class DAIGameCore extends Game {

    protected Engine engine;

    @Override
    public void create() {
        engine = Engine.getInstance();
        engine.init();

        // World.getInstance().init();
    }

    @Override
    public void dispose() {}

    /** Main game loop */
    @Override
    public void render() {
        super.render();

        float delta = Gdx.graphics.getDeltaTime();
        engine.tick(delta);
    }
}
