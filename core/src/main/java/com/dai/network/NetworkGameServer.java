package com.dai.network;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Stream;

import com.badlogic.gdx.math.Vector2;
import com.dai.PlayerPawn;
import com.dai.PlayerPawn.EPlayerActionResult;
import com.dai.ai.AStar;
import com.dai.ai.ISearch;
import com.dai.common.Config;
import com.dai.engine.Entity;
import com.dai.server.GameMatch;
import com.dai.world.Pawn;
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

	/** Used on client to fetch remote reference */
	private static Registry registry;

    private static INetworkGameServer instance;
    public static INetworkGameServer getInstance() throws RemoteException {
        if(instance == null) {

			if(NetworkManager.isOffline()) {
				instance = new NetworkGameServer();
			} else{
				try {
					registry = LocateRegistry.getRegistry(16000);
					INetworkGameServer netInstance = (INetworkGameServer) registry.lookup(NetworkGameServer.class.getSimpleName());
					instance = netInstance;

					logger.info("Fetched NetworkGameServer instance on client from RMI Registry.");
				} catch(Exception e) {
					instance = new NetworkGameServer();
					logger.info("Created NetworkGameServer instance on server.");

					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
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
				INetworkGameClient client1 = (INetworkGameClient) clients.values().toArray()[0];
				UUID p1Id = (UUID)clients.keySet().toArray()[0];
				NetworkPawn netPawn1 = new NetworkPawn(p1NetId);
				netPawn1.setOwnerId(p1Id);
				spawnPawn(
					netPawn1,
					new Vector2(0, 0),
					p1Id,
					true);

				INetworkGameClient client2 = (INetworkGameClient) clients.values().toArray()[1];
				UUID p2Id = (UUID)clients.keySet().toArray()[1];
				NetworkPawn netPawn2 = new NetworkPawn(p2NetId);
				netPawn2.setOwnerId(p2Id);
				spawnPawn(
					netPawn2 ,
					new Vector2(World.WORLD_SIZE - 1, World.WORLD_SIZE - 1),
					p2Id,
					true);


				client1.onGenerateWorld(World.getInstance().exportWorld());
				client2.onGenerateWorld(World.getInstance().exportWorld());
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
    public EPlayerActionResult doAction(UUID playerId, Vector2 target) throws RemoteException {
		/** Not current players turn */
		if(!currentPlayer.equals(playerId)) {
			return EPlayerActionResult.Failed;
		}

		Optional<NetworkPawn> pawn = netPawns.stream().filter(p -> p.getOwnerId().equals(playerId)).findFirst();
		NetworkPawn netPawn = pawn.get();

		/** Find path */
		Queue<Vector2> path = requestPath(playerId, target);

		if(path == null || (path != null && path.size() == 0)) {
			return EPlayerActionResult.Failed;
		}

		/**
		 *
		 * We're observing target element and calculating possible actions
		 * 1. Move
		 * 2. Loot
		 * 3. Attack
		 *
		 **/
		Entity targetEntity = World.getInstance().getEntityAtPoint(target);
		PlayerPawn playerPawn = (PlayerPawn) netPawn.getPossessedPawn();

		/** Movement */
		if(targetEntity == null) {
			int pathCost = World.getInstance().getPathCost(path);

			if(playerPawn.consumeActionPoints(pathCost)) {
				// Notify of action points consumed
				for(Map.Entry<UUID, INetworkGameClient> entry : clients.entrySet()) {
					INetworkGameClient client = entry.getValue();
					client.onPlayerPawnActionPointsChange(netPawn.getId(), pathCost);
				}

				netPawn.getPossessedPawn().move(path);
				return EPlayerActionResult.Success;
			}
		} else {
			Pawn targetPawn = (Pawn) targetEntity;

			/** Attack */
			if(targetPawn != null) {
				// Attack without movement
				int attackCost = 2;
				if(path.size() == 1 && playerPawn.consumeActionPoints(attackCost)) {
					// Notify of action points consumed
					for(Map.Entry<UUID, INetworkGameClient> entry : clients.entrySet()) {
						INetworkGameClient client = entry.getValue();
						client.onPlayerPawnActionPointsChange(netPawn.getId(), attackCost);
					}

					netPawn.getPossessedPawn().move(path);
					return EPlayerActionResult.Success;
				} else {
					// TODO: Move first then attack
				}
			}
		}

		return EPlayerActionResult.NotEnoughActionPoints;
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

	@Override
    public void endTurn(UUID playerId) throws RemoteException {
		// TODO: Check if player's pawn is currently busy, and prevent
		// end turn until pawn has finished any actions.
		if(currentPlayer.equals(playerId)) {
			Optional<Entry<UUID, INetworkGameClient>> nextPlayer = clients.entrySet().stream()
				.filter(entry -> !entry.getKey().equals(playerId))
				.findFirst();

			currentPlayer = nextPlayer.get().getKey();

			Optional<NetworkPawn> pawn = netPawns.stream()
				.filter(p -> p.getOwnerId().equals(currentPlayer))
				.findFirst();
			NetworkPawn nextPlayersNetPawn = pawn.get();
			PlayerPawn nextPlayersPawn = (PlayerPawn) nextPlayersNetPawn.getPossessedPawn();
			nextPlayersPawn.consumeActionPoints(-Config.POINTS_PER_TURN);

			// Notify of action points consumed
			for(Map.Entry<UUID, INetworkGameClient> entry : clients.entrySet()) {
				INetworkGameClient client = entry.getValue();
				// Sending negative points will increment them instead
				client.onPlayerPawnActionPointsChange(nextPlayersNetPawn.getId(), -Config.POINTS_PER_TURN);
			}
		}
	}

	/** Server only  */
	public void spawnPawn(NetworkPawn netPawn, Vector2 location, UUID playerId, boolean isPlayer) {
		logger.info("Called spawnPawn");

		if(isPlayer) {
            PlayerPawn pawnPlayer = new PlayerPawn(new PawnData(), location, false);
			pawnPlayer.setShouldRender(false);

			World.getInstance().spawn(pawnPlayer, location);

			netPawn.possessPawn(pawnPlayer);
			netPawns.add(netPawn);

			try {
				for(Map.Entry<UUID, INetworkGameClient> entry : clients.entrySet()) {
					INetworkGameClient client = entry.getValue();
					client.onSpawnPawn(netPawn.getId(), location, playerId, isPlayer);
				}

				// Initialise action points for first player
				if(playerId.equals(currentPlayer)) {
					pawnPlayer.consumeActionPoints(-Config.POINTS_PER_TURN);

					for(Map.Entry<UUID, INetworkGameClient> entry : clients.entrySet()) {
						INetworkGameClient currClient = entry.getValue();
						// Sending negative points will increment them instead
						currClient.onPlayerPawnActionPointsChange(netPawn.getId(), -Config.POINTS_PER_TURN);
					}
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
			logger.error("[updatePawnState] " + e.getMessage());
		}
	}

    public void updatePawnData(UUID netPawnId, PawnData newPawnData) throws RemoteException {
		try {
			for(Map.Entry<UUID, INetworkGameClient> entry : clients.entrySet()) {
				INetworkGameClient client = entry.getValue();
				client.onPawnDataChange(netPawnId, newPawnData);
			}
		} catch(Exception e) {
			logger.error("[updatePawnData] " + e.getMessage());
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
