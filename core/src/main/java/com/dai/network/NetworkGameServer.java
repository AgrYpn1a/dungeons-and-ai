package com.dai.network;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.dai.PlayerPawn;
import com.dai.ai.AStar;
import com.dai.ai.ISearch;
import com.dai.ai.ITraversable;
import com.dai.server.GameMatch;
import com.dai.world.Pawn.EPawnState;
import com.dai.world.Pawn.PawnData;
import com.dai.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetworkGameServer extends UnicastRemoteObject implements INetworkGameServer {
    private static final Logger logger = LoggerFactory.getLogger(NetworkGameServer.class);
	private final UUID id;
	private UUID currentPlayer = null;

	private volatile Map<UUID, INetworkGameClient> clients = new HashMap<>();
	private List<NetworkPawn> netPawns = new LinkedList<>();

    private ISearch search;

    private static NetworkGameServer instance;
    public static NetworkGameServer getInstance() throws RemoteException {
        if(instance == null) {
            instance = new NetworkGameServer();
        }

        return instance;
    }

    private NetworkGameServer() throws RemoteException {
		// This id may be used for server ownede entities, such as NPCs
		this.id = UUID.randomUUID();
		search = new AStar();
	}

	/** Remote logic  */
	@Override
	public boolean isMyTurn(UUID id) throws RemoteException {
        return currentPlayer.equals(id);
	}

	@Override
	public synchronized void registerClient(INetworkGameClient client) throws RemoteException {
		if(clients.containsKey(client.getId())) {
			// TODO: Something bad is going on
			logger.error("NetworkGameClient with id already registered. Problematic id: " + client.getId());
			return;
		}

		// Assign first connected player as the current player
		if(currentPlayer == null) {
			currentPlayer = client.getId();
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
				UUID p1Id = (UUID)clients.keySet().toArray()[0];
				NetworkPawn netPawn1 = new NetworkPawn(p1NetId);
				netPawn1.setOwnerId(p1Id);
				spawnPawn(
					netPawn1,
					new Vector2(0, 0),
					p1Id,
					true);

				UUID p2Id = (UUID)clients.keySet().toArray()[1];
				NetworkPawn netPawn2 = new NetworkPawn(p2NetId);
				netPawn2.setOwnerId(p2Id);
				spawnPawn(
					netPawn2 ,
					new Vector2(World.WORLD_SIZE - 1, World.WORLD_SIZE - 1),
					p2Id,
					true);
			} catch(Exception e) {
				logger.error(e.getMessage());
			}
		}

	}

	@Override
    public Queue<Vector2> requestPath(UUID playerId, Vector2 target) throws RemoteException {
		Optional<NetworkPawn> pawn = netPawns.stream().filter(p -> p.getOwnerId().equals(playerId)).findFirst();
		NetworkPawn netPawn = pawn.get();
		logger.info("Requested pawn by player " + playerId + " is " + pawn);
		if(netPawn != null) {
			if(netPawn.getPossessedPawn().getState() == EPawnState.Ready) {
				Vector2 pawnPos = netPawn.getPossessedPawn().getPosition();
				Queue<Vector2> path = search.findPath(
									World.getInstance().getTiles()[(int)pawnPos.y][(int)pawnPos.x],
									World.getInstance().getTiles()[(int)target.y][(int)target.x]);
				// pawn.get().getPossessedPawn().move(path);
				return path;
			}

			return null;
		} else {
			// TODO: Should probably kill the game?
			logger.error("Unable to find players pawn. This is critical.");
		}

		return null;
	}

	@Override
    public void doAction(UUID playerId, Vector2 target) throws RemoteException {
		Optional<NetworkPawn> pawn = netPawns.stream().filter(p -> p.getOwnerId().equals(playerId)).findFirst();
		NetworkPawn netPawn = pawn.get();

		Queue<Vector2> path = requestPath(playerId, target);
		netPawn.getPossessedPawn().move(path);
	}

	@Override
    public boolean isServer() throws RemoteException {
		try {
			RemoteServer.getClientHost();
			return false;
		} catch(ServerNotActiveException e) {
			// In case this fails, we're on server!
			return true;
		}
	}

	/** Server only  */
	public void spawnPawn(NetworkPawn netPawn, Vector2 location, UUID playerId, boolean isPlayer) {
		logger.info("Called spawnPawn");

		if(isPlayer) {
            PlayerPawn pawnPlayer = new PlayerPawn(new PawnData(), location, false);
			pawnPlayer.setShouldRender(false);

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

	// TODO ???
	public boolean hasAuthority(UUID playerId, UUID pawnId) {
		NetworkPawn pawn = netPawns.stream()
			.filter(p -> p.getId() == pawnId)
			.findFirst().get();

		return pawn != null && pawn.getOwnerId() == playerId;
	}

    public void updatePawnPosition(UUID netPawnId, Vector2 newPosition) throws RemoteException {
		try {
			for(Map.Entry<UUID, INetworkGameClient> entry : clients.entrySet()) {
				INetworkGameClient client = entry.getValue();
				client.onPawnPositionChange(netPawnId, newPosition);
			}
		} catch(Exception e) {
			logger.error("[updatePawnPosition] " + e.getMessage());
		}
	}

    public void updatePawnState(UUID netPawnId, EPawnState newState) throws RemoteException {
		try {
			for(Map.Entry<UUID, INetworkGameClient> entry : clients.entrySet()) {
				INetworkGameClient client = entry.getValue();
				client.onPawnStateChange(netPawnId, newState);
			}
		} catch(Exception e) {
			logger.error("[updatePawnPosition] " + e.getMessage());
		}
	}

    public void updatePawnData(UUID netPawnId, PawnData newPawnData) throws RemoteException {
		try {
			for(Map.Entry<UUID, INetworkGameClient> entry : clients.entrySet()) {
				INetworkGameClient client = entry.getValue();
				client.onPawnDataChange(netPawnId, newPawnData);
			}
		} catch(Exception e) {
			logger.error("[updatePawnPosition] " + e.getMessage());
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
