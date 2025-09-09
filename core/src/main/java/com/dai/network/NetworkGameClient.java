package com.dai.network;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.dai.PlayerController;
import com.dai.PlayerPawn;
import com.dai.UIManager;
import com.dai.world.Pawn.EPawnState;
import com.dai.world.Pawn.PawnData;
import com.dai.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dai.world.Tile.TileData;

public final class NetworkGameClient extends UnicastRemoteObject implements INetworkGameClient {
    private static final Logger logger = LoggerFactory.getLogger(NetworkGameClient.class);
    private final UUID id;
    private List<NetworkPawn> netPawns = new LinkedList<>();

    private static NetworkGameClient instance;
    public static NetworkGameClient getInstance() throws RemoteException {
        if(instance == null) {
            instance = new NetworkGameClient();
        }

        return instance;
    }

    private NetworkGameClient() throws RemoteException {
        this.id = UUID.randomUUID();
    }

    public UUID getPlayerId() { return id; }

	@Override
	public void onGenerateWorld(TileData[][] data) throws RemoteException {
        World.getInstance().importWorld(data);
	}

	@Override
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
                netPawn.setOwnerId(playerId);
                PlayerController.getInstance().setPlayerPawn(pawnPlayer);
                UIManager.getInstance().setPlayerPawn(pawnPlayer);
            } else {
                // Initialise opponent
                PlayerPawn pawnPlayer = new PlayerPawn(new PawnData(), location, true);
                World.getInstance().spawn(pawnPlayer, location);

                // Make sure network pawn is controlling pawn
                netPawn.possessPawn(pawnPlayer);
                netPawn.setOwnerId(playerId);
                PlayerController.getInstance().setOpponentPawn(pawnPlayer);
                UIManager.getInstance().setOpponentPawn(pawnPlayer);
            }
        } else {
            // TODO: Spawn NPC pawn
        }
    }

	@Override
	public UUID getId() throws RemoteException {
        return this.id;
	}

	@Override
    public void onPawnPositionChange(UUID netPawnId, Vector2 position) throws RemoteException {
        NetworkPawn netPawn = netPawns.stream()
            .filter(p -> p.getId().equals(netPawnId))
            .findFirst().get();
        netPawn.getPossessedPawn().setPosition(position);
    }

	@Override
    public void onPawnStateChange(UUID netPawnId, EPawnState state) throws RemoteException {
        NetworkPawn netPawn = netPawns.stream()
            .filter(p -> p.getId().equals(netPawnId))
            .findFirst().get();
        netPawn.getPossessedPawn().setState(state);
    }

	@Override
    public void onPawnDataChange(UUID netPawnId, PawnData newData) throws RemoteException {
        NetworkPawn netPawn = netPawns.stream()
            .filter(p -> p.getId().equals(netPawnId))
            .findFirst().get();

        /** When my pawn's data has changed */
        NetworkPawn myPawn = netPawns.stream()
            .filter(np -> np.getOwnerId().equals(id))
            .findFirst().get();

        PawnData currData = netPawn.getPossessedPawn().getData();
        int damage = Math.abs(newData.health - currData.health);
        boolean hasDied = false;

        if(newData.health <= 0) {
            /** Death blow */
            if(myPawn.getId().equals(netPawnId)) {
                // UIManager.getInstance().spawnFloatingText(String.format("You died! Game over :(", damage), Color.RED);
                UIManager.getInstance().displayLoseMessage();
            } else {
                /** When opponent's pawn data has changed */
                // UIManager.getInstance().spawnFloatingText(String.format("You dealt final blow! Victory is ours!"), Color.GREEN);
                UIManager.getInstance().displayVictoryMessage();
            }

            hasDied = true;
        }

        if(myPawn.getId().equals(netPawnId)) {
            UIManager.getInstance().spawnFloatingText(String.format("-%d Hit", damage), Color.RED);
        } else {
            /** When opponent's pawn data has changed */
            UIManager.getInstance().spawnFloatingText(String.format("+%d Hit", damage), Color.GREEN);
        }

        // Finally update the data
        netPawn.getPossessedPawn().setData(newData);
        if(hasDied) {
            netPawn.getPossessedPawn().setState(EPawnState.Dead);
        }
    }

    public void onPlayerPawnActionPointsChange(UUID netPawnId, int deltaPoints) throws RemoteException {
        NetworkPawn netPawn = netPawns.stream()
            .filter(p -> p.getId().equals(netPawnId))
            .findFirst().get();

        PlayerPawn playerPawn = (PlayerPawn) netPawn.getPossessedPawn();
        playerPawn.consumeActionPoints(deltaPoints);
    }
}
