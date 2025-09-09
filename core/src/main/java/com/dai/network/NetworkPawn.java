package com.dai.network;

import java.rmi.RemoteException;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.dai.UIManager;
import com.dai.world.Pawn;
import com.dai.world.Pawn.EPawnState;
import com.dai.world.Pawn.PawnData;

public final class NetworkPawn {

    private final UUID id;
    private UUID ownerId;

    // private PawnData data;

    private Pawn possessedPawn;

    public NetworkPawn(UUID id) throws RemoteException {
        this.id = id;
        // this.data = data;
    }

    public UUID getId() { return id; }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getOwnerId() { return ownerId; }

    public void possessPawn(Pawn possessedPawn) {
        this.possessedPawn = possessedPawn;

        this.possessedPawn.onPositionChanged = this::handlePositionChanged;
        this.possessedPawn.onStateChanged = this::handleStateChanged;
        this.possessedPawn.onDataChanged = this::handleDataChanged;
    }

    // public PawnData getPawnData() { return data; }

    public Pawn getPossessedPawn() { return possessedPawn; }

    /** Observe pawn - server only */
    private void handlePositionChanged(Vector2 newPosition) {
        try {
            INetworkGameServer server = NetworkGameServer.getInstance();
            if(server.isServer()) {
                ((NetworkGameServer)server).updatePawnPosition(this.id, newPosition);
            }
        } catch(Exception e) { /* TODO: Handle gracefully */}
    }

    private void handleStateChanged(EPawnState newState) {
        try {
            INetworkGameServer server = NetworkGameServer.getInstance();
            if(server.isServer()) {
                ((NetworkGameServer)server).updatePawnState(this.id, newState);
            }
        } catch(Exception e) { /* TODO: Handle gracefully */}
    }

    private void handleDataChanged(PawnData newData) {
        try {
            INetworkGameServer server = NetworkGameServer.getInstance();
            if(server.isServer()) {
                ((NetworkGameServer)server).updatePawnData(this.id, newData);
            }
        } catch(Exception e) { /* TODO: Handle gracefully */}
    }
}
