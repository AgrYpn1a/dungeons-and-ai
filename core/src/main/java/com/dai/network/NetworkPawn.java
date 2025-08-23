package com.dai.network;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.UUID;

import com.dai.world.Pawn;
import com.dai.world.Pawn.PawnData;

public final class NetworkPawn implements Serializable {

    private UUID id;
    // private PawnData data;

    private Pawn possessedPawn;

    public NetworkPawn(UUID id) throws RemoteException {
        this.id = id;
        // this.data = data;
    }

    public UUID getId() { return id; }

    public void possessPawn(Pawn possessedPawn) {
        this.possessedPawn = possessedPawn;
    }

    // public PawnData getPawnData() { return data; }

    public Pawn getPossessedPawn() { return possessedPawn; }

    // TODO: Orders
}
