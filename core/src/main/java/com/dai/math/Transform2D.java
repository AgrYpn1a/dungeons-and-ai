package com.dai.math;

import com.badlogic.gdx.math.Vector2;

public class Transform2D {
    private Vector2 position;

    protected Transform2D() {
        this.position = new Vector2(0, 0);
    }

    public Vector2 getPosition() { return position; }
    public void setPosition(Vector2 position) { this.position = position; }
}
