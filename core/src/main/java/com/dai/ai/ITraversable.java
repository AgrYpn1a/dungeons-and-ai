package com.dai.ai;

import com.badlogic.gdx.math.Vector2;

public interface ITraversable {

    public Vector2 getPosition();

    public boolean isTraversable();

    public float getCostModifier();

}
