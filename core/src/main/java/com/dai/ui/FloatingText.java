package com.dai.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.dai.common.Config;

public class FloatingText {
    private BitmapFont font;
    private String text;
    private Color color;

    private float offset;

    public FloatingText(String text, Color color) {
        this.text = text;
        this.color = color;

        offset = Config.UI_FLOATING_TEXT_START_Y;
    }

    public void uiRender(SpriteBatch batch, float deltaTime) {
        if(font == null) {
            font = new BitmapFont();
        }

        if(offset > 0) {
            font.setColor(color);

            float scale = 2 * (offset / Config.UI_FLOATING_TEXT_START_Y);
            font.getData().setScale(scale);

            int width = text.length() * Config.UI_CHAR_WIDTH;
            font.draw(
                batch,
                text,
                (Gdx.graphics.getWidth() + width) / 2, Gdx.graphics.getHeight() - offset, width,
                Align.center,
                false);
            font.setColor(Color.WHITE);

            offset -= deltaTime * Config.UI_FLOATING_TEXT_SPEED;
        }
    }
}
