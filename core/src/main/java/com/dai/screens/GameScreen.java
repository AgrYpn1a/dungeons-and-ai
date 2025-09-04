package com.dai.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dai.PlayerController;
import com.dai.PlayerPawn;
import com.dai.UIManager;
import com.dai.engine.Engine;
import com.dai.engine.Engine.Layer;
import com.dai.network.NetworkGameClient;
import com.dai.network.NetworkGameServer;
import com.dai.network.NetworkManager;
import com.dai.world.World;

/** First screen of the application. Displayed after the application is created. */
public final class GameScreen implements Screen {
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

        engine = Engine.getInstance();

        batch = engine.getMainBatch();
        batch.enableBlending();

        img = new Texture("tileset.png");
        region = new TextureRegion(img, 0, 0, 8, 8);

        camera = new OrthographicCamera();
        uiCamera = new OrthographicCamera();

        viewport = new ScreenViewport(camera);
        uiViewport = new ScreenViewport(uiCamera);

        camera.position.set(
            ((World.WORLD_SIZE * World.TILE_SIZE) / 2),
            ((World.WORLD_SIZE * World.TILE_SIZE) / 2),
            0
        );
        camera.zoom = World.CAMERA_ZOOM;
        camera.update();
        uiCamera.update();

        font = new BitmapFont();

        engine.registerViewport(Layer.Default, viewport);
        engine.registerViewport(Layer.Player, viewport);
        engine.registerViewport(Layer.Indicators, viewport);
        engine.registerViewport(Layer.UI, uiViewport);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        /** Rendering */
        batch.begin();

        engine.render(delta);

        // Draw some debug text
        uiViewport.apply();
        batch.setProjectionMatrix(uiCamera.combined);

        Vector3 screenCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        Vector3 worldCoords = viewport.unproject(screenCoords);

        PlayerPawn playerPawn = PlayerController.getInstance().getPlayerPawn();
        if(playerPawn != null) {
            // font.draw(
            //     batch,
            //     String.format("| Action Points = %d", playerPawn.getActionPoints()),
            //     25, Gdx.graphics.getHeight() - 25, 100,
            //     Align.topLeft,
            //     false);

            // font.draw(
            //     batch,
            //     String.format("| Health = %d", playerPawn.getData().health),
            //     25, Gdx.graphics.getHeight() - 50, 100,
            //     Align.topLeft,
            //     false);

            // Render turn information
            boolean myTurn = NetworkManager.isOffline();
            try {
                myTurn = NetworkGameServer.getInstance().isMyTurn(NetworkGameClient.getInstance().getId());
            } catch(Exception e) { /* TODO */ }

            if(myTurn) {
                font.setColor(Color.GREEN);
            } else {
                font.setColor(Color.RED);
            }
            font.draw(
                batch,
                String.format("<TURN: %s", myTurn ? "My>" : "Opponnent's>"),
                (Gdx.graphics.getWidth() - 100) / 2, Gdx.graphics.getHeight() - 15, 100,
                Align.top,
                false);
        }

        // font.draw(
        //     batch,
        //     "Mouse at (" + worldCoords.x + ", " + worldCoords.y + ")",
        //     25, Gdx.graphics.getHeight() - 25, 100,
        //     Align.topLeft,
        //     false);

        // font.draw(
        //     batch,
        //     "UIManager Mouse at (" + UIManager.getInstance().getMouseWorldPos().x + ", " + UIManager.getInstance().getMouseWorldPos().y + ")",
        //      25, Gdx.graphics.getHeight() - 50, 100,
        //     Align.topLeft,
        //     false);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your screen here. The parameters represent the new window size.
        viewport.update(width, height);
        uiViewport.update(width, height, true);

        viewport.getCamera().update();
        uiViewport.getCamera().update();
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
