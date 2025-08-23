package com.dai.network;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.dai.PlayerController;
import com.dai.PlayerPawn;
import com.dai.world.Pawn.PawnData;
import com.dai.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetworkGameClient extends UnicastRemoteObject implements INetworkGameClient {
    private static final Logger logger = LoggerFactory.getLogger(NetworkGameClient.class);
    private final UUID id;
    private List<NetworkPawn> netPawns = new LinkedList<>();

    public NetworkGameClient() throws RemoteException {
        this.id = UUID.randomUUID();
    }

	@Override
	public void onSpawnPawn(NetworkPawn netPawn, Vector2 location) throws RemoteException {
        // PlayerPawn pawnPlayer = new PlayerPawn(netPawn.getPawnData(), false);
        // World.getInstance()
    }

	@Override
	public void onSpawnPawn(NetworkPawn netPawn, Vector2 location, UUID playerId, boolean isPlayer) throws RemoteException {

        // logger.info("onSpawnPawn called.");

        // // Spawn player
        // if(isPlayer && id == playerId) {
        //     PlayerPawn pawnPlayer = new PlayerPawn(netPawn.getPawnData(), location, false);
        //     World.getInstance().spawn(pawnPlayer, location);
        // } else if(isPlayer) {
        //     // Spawn opponent player
        //     PlayerPawn pawnOpponent = new PlayerPawn(netPawn.getPawnData(), location, true);
        //     World.getInstance().spawn(pawnOpponent, location);
        // }
    }

	public void onSpawnPawn(UUID netPawnId, Vector2 location, UUID playerId, boolean isPlayer) throws RemoteException {
        logger.info("[id:" + id + "] " + "onSpawnPawn called with netPawnId: " + netPawnId + ", playerId " + playerId);

        if(isPlayer) {
            NetworkPawn netPawn = new NetworkPawn(netPawnId);
            netPawns.add(netPawn);

            // Initialise player
            if(playerId.equals(id)) {
                PlayerPawn pawnPlayer = new PlayerPawn(new PawnData(), location, false);
                World.getInstance().spawn(pawnPlayer, location);

                // Make sure network pawn is controlling pawn
                netPawn.possessPawn(pawnPlayer);
                PlayerController.getInstance().setPlayerPawn(pawnPlayer);
            } else {
                // Initialise opponent
                PlayerPawn pawnPlayer = new PlayerPawn(new PawnData(), location, true);
                World.getInstance().spawn(pawnPlayer, location);

                // Make sure network pawn is controlling pawn
                netPawn.possessPawn(pawnPlayer);
            }
        } else {
            // TODO: Spawn NPC pawn
        }
    }

	@Override
	public UUID getId() throws RemoteException {
        return this.id;
	}
}
