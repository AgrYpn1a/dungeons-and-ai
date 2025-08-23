package com.dai.network;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.dai.PlayerPawn;
import com.dai.ai.ITraversable;
import com.dai.server.GameMatch;
import com.dai.world.Pawn.PawnData;
import com.dai.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetworkGameServer extends UnicastRemoteObject implements INetworkGameServer {
    private static final Logger logger = LoggerFactory.getLogger(NetworkGameServer.class);

	private Map<UUID, INetworkGameClient> clients = new HashMap<>();
	private List<NetworkPawn> netPawns = new LinkedList<>();

    public NetworkGameServer() throws RemoteException {}

	/** Remote logic  */
	@Override
	public boolean isMyTurn(UUID id) throws RemoteException {
        return true;
	}

	@Override
	public synchronized void registerClient(INetworkGameClient client) throws RemoteException {
		if(clients.containsKey(client.getId())) {
			// TODO: Something bad is going on
			logger.error("NetworkGameClient with id already registered. Problematic id: " + client.getId());
			return;
		}

		// Register client
		clients.put(client.getId(), client);

		logger.info("NetworkGameClient successfully registered with id: " + client.getId() + " # of clients " + clients.entrySet().size());

		// Spawn players when all clients are connected
		if(clients.entrySet().size() == GameMatch.NUM_OF_MATCH_PLAYERS) {
			logger.info("Spawning players...");

			UUID p1NetId = UUID.randomUUID();
			// PawnData p1PawnData = new PawnData();
			// p1PawnData .health = 10;

			UUID p2NetId = UUID.randomUUID();
			// PawnData p2PawnData = new PawnData();
			// p2PawnData.health = 10;

			try {
				spawnPawn(
					new NetworkPawn(p1NetId),
					new Vector2(0, 0),
					(UUID)clients.keySet().toArray()[0],
					true);

				spawnPawn(
					new NetworkPawn(p2NetId),
					new Vector2(World.WORLD_SIZE - 1, World.WORLD_SIZE - 1),
					(UUID)clients.keySet().toArray()[1],
					true);
			} catch(Exception e) {
				logger.error(e.getMessage());
			}
		}

	}

	/** Server only  */
	public void spawnPawn(NetworkPawn netPawn, Vector2 location, UUID playerId, boolean isPlayer) {
		logger.info("Called spawnPawn");

		if(isPlayer) {
            PlayerPawn pawnPlayer = new PlayerPawn(new PawnData(), location, false);

			netPawn.possessPawn(pawnPlayer);
			netPawns.add(netPawn);

			// Spawn player in the world
            // World.getInstance().spawn(pawnPlayer, location);

			try {
				for(Map.Entry<UUID, INetworkGameClient> entry : clients.entrySet()) {
					INetworkGameClient client = entry.getValue();
					client.onSpawnPawn(netPawn.getId(), location, playerId, isPlayer);
				}
			} catch(Exception e) {}
		}
	}

	// @Override
	// public void addNetworkPlayerPawn(INetworkPawn netPawn) throws RemoteException {
	// 	// NetworkPlayer netPlayer = new NetworkPlayer();
	// 	// netPlayer.id = netPawn.getId();
	// 	// netPlayer.pawn = netPawn;

	// 	// players.add(netPlayer);
	// }

	// @Override
	// public void addNetworkPawn(INetworkPawn netPawn) throws RemoteException {
	// 	netPawns.add(netPawn);
	// }

	// @Override
	// public Queue<ITraversable> requestPath(ITraversable target) throws RemoteException {
	// 	// TODO Auto-generated method stub
	// 	throw new UnsupportedOperationException("Unimplemented method 'requestPath'");
	// }

	// @Override
	// public boolean requestMove(ITraversable target) throws RemoteException {
	// 	// TODO Auto-generated method stub
	// 	throw new UnsupportedOperationException("Unimplemented method 'requestMove'");
	// }


}
