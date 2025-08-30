package com.dai.world;

import java.io.Serializable;
import java.util.Queue;
import java.util.function.Consumer;

import com.badlogic.gdx.math.Vector2;
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
    protected EPawnState state = EPawnState.Ready;
    protected Queue<Vector2> path;

    public Consumer<EPawnState> onStateChanged;
    public Consumer<PawnData> onDataChanged;
    public Consumer<Vector2> onPositionChanged;

    /** Movement mechanics */
    private float moveTime = 0.5f;
    private float moveTick = 0f;

    public Pawn(PawnData data) {
        this.data = data;
    }

    public EPawnState getState() { return state; }

    public void setState(EPawnState state) {
        this.state = state;

        if(state == EPawnState.Dead) {
            // TODO: Handle death!
        }
    }

    public void setData(PawnData data) {
        this.data = data;
    }

    public void move(Queue<Vector2> path) {
       this.state = EPawnState.Busy;
       this.path = path;
    }

    // public void doDamage(int damage) {
    //     this.data.health -= damage;
    // }

    // public boolean isDead() {
    //     return this.data.health <= 0;
    // }

    @Override
    public void tick(float deltaTime) {
        /** Movement mechanics */
        if(path != null && !path.isEmpty()) {
            if(moveTick >= moveTime) {

                // Move to position
                moveTick = 0f;
                Vector2 newPosition = path.poll();
                setPosition(newPosition);
                onPositionChanged.accept(newPosition);

                if(path.isEmpty()) {
                    state = EPawnState.Ready;
                }
            }
        }

        moveTick += deltaTime;
    }
}
