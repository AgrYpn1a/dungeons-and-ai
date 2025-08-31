package com.dai.network;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Queue;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.dai.PlayerPawn.EPlayerActionResult;

public interface INetworkGameServer extends Remote {

    public void registerClient(INetworkGameClient client) throws RemoteException;

    public boolean isServer() throws RemoteException;

    public boolean isMyTurn(UUID id) throws RemoteException;

    public Queue<Vector2> requestPath(UUID playerId, Vector2 target) throws RemoteException;

    public EPlayerActionResult doAction(UUID playerId, Vector2 target) throws RemoteException;

    public void endTurn(UUID playerId) throws RemoteException;
}
