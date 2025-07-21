package com.dai;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class DAIGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
