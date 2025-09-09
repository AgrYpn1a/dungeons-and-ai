package com.dai.world;

import java.io.Serializable;
import java.util.Queue;
import java.util.function.Consumer;

import com.badlogic.gdx.math.Vector2;
import com.dai.UIManager;
import com.dai.engine.Entity;
import com.dai.network.NetworkGameServer;
import com.dai.network.NetworkManager;

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
    private float actionTick = 0f;

    public Pawn(PawnData data) {
        this.data = data;
    }

    public EPawnState getState() { return state; }

    public void setState(EPawnState state) {
        this.state = state;

        if(state == EPawnState.Dead) {
            // TODO: Handle death!
        }

        if(onStateChanged != null) {
            onStateChanged.accept(state);
        }
    }

    public void setData(PawnData data) {
        this.data = data;

        if(onDataChanged != null) {
            onDataChanged.accept(data);
        }
    }

    public PawnData getData() { return this.data; }

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

    public void doDamage(int damage) {
        data.health -= damage;

        /** Notify of changes */
        onDataChanged.accept(data);
    }


    @Override
    public boolean shouldRender() {
        return state != EPawnState.Dead;
    }

    @Override
    public void tick(float deltaTime) {
        /** Process actions */
        // boolean hasAuthority = NetworkManager.isServer() || NetworkManager.isOffline();
        boolean hasAuthority = NetworkManager.isOffline();
        if(hasAuthority && path != null && !path.isEmpty()) {
            if(actionTick >= moveTime) {

                // Execute action
                actionTick = 0f;
                Vector2 targetPos = path.poll();

                /** -> Attack */
                Entity targetEntity = World.getInstance().getEntityAtPoint(targetPos);
                Pawn targetPawn = (Pawn) targetEntity;
                if(targetPawn != null) {
                    // TODO: Maybe calculate damage in a different way
                   int damage = 1 + (int)(Math.random() * 5);
                   targetPawn.doDamage(damage);
                   state = EPawnState.Ready;

                   return;
                }

                /** -> TODO: Loot */

                /** -> Move */
                setPosition(targetPos);

                /** Notify of changes */
                if(onPositionChanged != null) {
                    onPositionChanged.accept(targetPos);
                }

                /** End action processing */
                if(path.isEmpty()) {
                    state = EPawnState.Ready;
                }
            }
        }

        actionTick += deltaTime;
    }
}
