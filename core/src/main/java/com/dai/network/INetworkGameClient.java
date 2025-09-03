package com.dai.network;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.dai.world.Pawn.EPawnState;
import com.dai.world.Pawn.PawnData;
import com.dai.world.Tile.TileData;

public interface INetworkGameClient extends Remote {

    public UUID getId() throws RemoteException;

    public void onGenerateWorld(TileData[][] data) throws RemoteException;

	public void onSpawnPawn(UUID netPawnId, Vector2 location, UUID playerId, boolean isPlayer) throws RemoteException;

    public void onPawnPositionChange(UUID netPawnId, Vector2 position) throws RemoteException;

    public void onPawnStateChange(UUID netPawnId, EPawnState state) throws RemoteException;

    public void onPawnDataChange(UUID netPawnId, PawnData state) throws RemoteException;

    public void onPlayerPawnActionPointsChange(UUID netPawnId, int deltaPoints) throws RemoteException;
}
