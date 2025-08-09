package com.dai.ai;

import java.util.Queue;

public interface ISearch {
    public Queue<ITraversable> findPath(ITraversable startNode, ITraversable target);
}
