package com.dai.ai;

import java.util.Queue;

import com.badlogic.gdx.math.Vector2;

public interface ISearch {
    public Queue<Vector2> findPath(ITraversable startNode, ITraversable target);
}
