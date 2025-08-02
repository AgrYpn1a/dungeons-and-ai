package com.dai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dai.engine.Engine;
import com.dai.engine.Engine.Layer;
import com.dai.engine.Entity;
import com.dai.engine.RenderComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UIManager extends Entity {
    private static final Logger logger = LoggerFactory.getLogger(UIManager.class);

    private static UIManager instance;
    public static UIManager getInstance() {
        if(instance == null) {
            instance = new UIManager();
        }

        return instance;
    }

    private Vector3 mouseInWorld;
    private Vector3 mouseOnScreen;
    private Viewport uiViewport;
    private Viewport viewport;

    // @Override
    // public boolean shouldRender() {
    //     return false;
    // }

    public void init() {}

    @Override
    public void registerEntity() {
        Engine.getInstance().registerEntity(Layer.UI, this);

        if(Engine.getInstance().getLayerViewport(Layer.UI).isPresent()) {
            uiViewport = Engine.getInstance().getLayerViewport(Layer.UI).get();
            viewport = Engine.getInstance().getLayerViewport(Layer.Default).get();
        } else {
            logger.error("UI Layer has not been initialized");
        }

        mouseInWorld = new Vector3();
        mouseOnScreen = new Vector3();
    }

    @Override
    public void render(SpriteBatch batch, float deltaTime) {
        mouseOnScreen.x = Gdx.input.getX();
        mouseOnScreen.y = Gdx.input.getY();
        mouseInWorld = viewport.unproject(mouseOnScreen);
    }

    public Vector3 getMouseWorldPos() { return mouseInWorld; }

    public Vector3 getMouseScreenPos() { return mouseOnScreen; }

    // public Vector3 getMousePos() {
    //     return new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    // }

    // public Vector3 getMousePosInWorld() {
    //     return new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    // }

}
