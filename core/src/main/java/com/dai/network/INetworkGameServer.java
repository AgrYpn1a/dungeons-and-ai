package com.dai.network;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;

public interface INetworkGameServer extends Remote {

    public void registerClient(INetworkGameClient client) throws RemoteException;

    public boolean isMyTurn(UUID id) throws RemoteException;

    /** DEPRECATED */

    // public void addNetworkPawn(INetworkPawn netPawn) throws RemoteException;
    // public void addNetworkPlayerPawn(INetworkPawn netPawn) throws RemoteException;

    /**
     * This method is used to request potential path data for the current player.
     *
     * @param target
     * @return
     * @throws RemoteException
    */
    // public Queue<ITraversable> requestPath(ITraversable target) throws RemoteException;

    /**
     * This method will actually move the player, and return true if the movement is possible.
     *
     * @param target
     * @return
     * @throws RemoteException
    */
    // public boolean requestMove(ITraversable target) throws RemoteException;
}
