package com.dai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dai.engine.Entity;
import com.dai.engine.RenderComponent;
import com.dai.engine.IComponent.EComponentId;
import com.dai.world.Tile;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {

    private static final int WORLD_WIDTH = 800;
    private static final int WORLD_HEIGHT= 600;

    SpriteBatch batch;
    Texture img;
    TextureRegion region;

    OrthographicCamera camera;
    Viewport viewport;

    @Override
    public void show() {
        batch = new SpriteBatch();
        img = new Texture("tileset.png");
        region = new TextureRegion(img, 0, 0, 8, 8);

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        // camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        camera.zoom = 0.5f;
        camera.update();
    }

    @Override
    public void render(float delta) {
        // ScreenUtils.clear(0.57f, 0.77f, 0.85f, 1);

        camera.update();

        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);

        List<Entity> entities = new ArrayList<>();
        entities.add(new Tile(region, new Vector2(8, 15)));
        entities.add(new Tile(region, new Vector2(2*8, 15)));
        entities.add(new Tile(region, new Vector2(3*8, 15)));

        batch.begin();
        for (Entity entity : entities) {
            Optional<RenderComponent> renderer = entity.getComponent(RenderComponent.id);
            if(renderer.get()!= null) {
                batch.draw(
                        renderer.get().getTexture(),
                        entity.getTransform().getPosition().x,
                        entity.getTransform().getPosition().y);
            }
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your screen here. The parameters represent the new window size.
        viewport.update(width, height);
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
