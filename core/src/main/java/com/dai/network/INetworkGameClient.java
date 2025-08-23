package com.dai.network;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;

public interface INetworkGameClient extends Remote {

    public UUID getId() throws RemoteException;

    public void onSpawnPawn(NetworkPawn netPawn, Vector2 location) throws RemoteException;

	public void onSpawnPawn(NetworkPawn netPawn, Vector2 location, UUID playerId, boolean isPlayer) throws RemoteException;

	public void onSpawnPawn(UUID netPawnId, Vector2 location, UUID playerId, boolean isPlayer) throws RemoteException;
}
