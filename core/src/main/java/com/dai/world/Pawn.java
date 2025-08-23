package com.dai.world;

import java.io.Serializable;
import java.util.Queue;

import com.dai.ai.ITraversable;
import com.dai.engine.Entity;

public class Pawn extends Entity {

    public static enum EPawnState {
        Ready,
        Busy,
        Dead
    }

    public static class PawnData implements Serializable {
        public int health = 10;
    }

    protected PawnData data;
    protected EPawnState state;
    protected Queue<ITraversable> path;

    /** Movement mechanics */
    private float moveTime = 0.5f;
    private float moveTick = 0f;

    public Pawn(PawnData data) {
        this.data = data;
    }

    public EPawnState getState() { return state; }

    public void move(Queue<ITraversable> path) {
       this.state = EPawnState.Busy;
       this.path = path;
    }

    public void doDamage(int damage) {
        this.data.health -= damage;
    }

    public boolean isDead() {
        return this.data.health <= 0;
    }

    @Override
    public void tick(float deltaTime) {
        /** Movement mechanics */
        if(path != null && !path.isEmpty()) {
            if(moveTick >= moveTime) {
                moveTick = 0f;
                setPosition(path.poll().getPosition());

                if(path.isEmpty()) {
                    state = EPawnState.Ready;
                }
            }
        }

        moveTick += deltaTime;
    }
}
