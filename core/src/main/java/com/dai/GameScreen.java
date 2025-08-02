package com.dai;

import java.util.Optional;
import java.util.stream.Stream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dai.engine.Engine;
import com.dai.engine.Entity;
import com.dai.engine.RenderComponent;
import com.dai.engine.Engine.Layer;
import com.dai.world.World;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {

    private static final int SCREEN_WIDTH = 1024;
    private static final int SCREEN_HEIGHT= 760;

    SpriteBatch batch;
    Texture img;
    TextureRegion region;

    OrthographicCamera camera;
    OrthographicCamera uiCamera;
    Viewport viewport;
    Viewport uiViewport;

    BitmapFont font;
    Engine engine;

    @Override
    public void show() {
        batch = new SpriteBatch();
        img = new Texture("tileset.png");
        region = new TextureRegion(img, 0, 0, 8, 8);

        camera = new OrthographicCamera();
        uiCamera = new OrthographicCamera();

        viewport = new ScreenViewport(camera);
        uiViewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, uiCamera);

        camera.position.set(
            ((World.WORLD_SIZE * World.TILE_SIZE) / 2),
            ((World.WORLD_SIZE * World.TILE_SIZE) / 2),
            0
        );
        camera.zoom = World.CAMERA_ZOOM;
        camera.update();
        uiCamera.update();
        uiCamera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);;

        font = new BitmapFont();
        engine = new Engine(batch);

        engine.registerViewport(Layer.Default, viewport);
        engine.registerViewport(Layer.Player, viewport);
        engine.registerViewport(Layer.UI, uiViewport);

        PlayerController.getInstance().init();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // camera.update();
        // uiCamera.update();

        /** Tick before rendering */
        engine.tick(delta);

        /** Rendering */
        batch.begin();
        engine.render(delta);

        // batch.setProjectionMatrix(camera.combined);

        // Render world
        // viewport.apply();
        // Stream<Entity> worldEntities = World.getInstance().getEntities();

        // worldEntities
        // .forEach(e -> {
        //     e.tick(delta);

        //     Optional<RenderComponent> r = e.getComponent(RenderComponent.id);
        //     if(r.get() != null) {
        //         Vector2 worldPos = World.toWorldPos(e.getTransform().getPosition());
        //         batch.draw(
        //             r.get().getTexture(),
        //             worldPos.x,
        //             worldPos.y
        //         );
        //     }
        // });

        // Draw some debug text
        uiViewport.apply();
        batch.setProjectionMatrix(uiCamera.combined);

        Vector3 screenCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        Vector3 worldCoords = viewport.unproject(screenCoords);

        font.draw(
            batch,
            "Mouse at (" + worldCoords.x + ", " + worldCoords.y + ")",
            25, Gdx.graphics.getHeight() - 25, 100,
            Align.topLeft,
            false);

        batch.end();

        // viewport.apply();
        // batch.setProjectionMatrix(camera.combined);

        // Entity e = World.getInstance().getEntityAtPoint(worldCoords);
        // if(e != null) {
        //     font.draw(
        //         batch,
        //         e.toString(),
        //         25, uiViewport.getWorldHeight() - 50, 100,
        //         Align.topLeft,
        //         false);

        //     Vector2 pointerInWorld = World.toWorldPos(new Vector2(e.getTransform().getPosition().x, e.getTransform().getPosition().y));
        //     batch.draw(region, pointerInWorld.x, pointerInWorld.y);
        // }
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your screen here. The parameters represent the new window size.
        viewport.update(width, height);
        uiViewport.update(width, height);

        viewport.getCamera().update();
        uiViewport.getCamera().update();

        // batch.setProjectionMatrix(camera.combined);
        // batch.setProjectionMatrix(uiCamera.combined);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
}
