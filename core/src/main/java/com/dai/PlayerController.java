package com.dai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dai.ai.AStar;
import com.dai.ai.ISearch;
import com.dai.ai.ITraversable;
import com.dai.engine.Engine;
import com.dai.engine.Engine.Layer;
import com.dai.entities.IndicatorEntity.EIndicator;
import com.dai.network.*;
import com.dai.pools.IndicatorsPool;
import com.dai.engine.Entity;
import com.dai.engine.ITickable;
import com.dai.engine.RenderComponent;
import com.dai.world.World;
import com.dai.world.Pawn.EPawnState;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlayerController implements ITickable {
    private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);

    public static class PlayerData implements Serializable {
        public UUID id;
        public String name;
        public Vector2 spawnPos;
    }

    private static PlayerController instance;
    public static PlayerController getInstance() {
        if(instance == null) {
            return new PlayerController();
        }

        return instance;
    }

    private final PlayerInput input;
    private final Entity actionIndicator;

    private IndicatorsPool pathMarkerPool = new IndicatorsPool(EIndicator.PathMarker);
    private List<Entity> cachedPathMarkers = new ArrayList<>();

    private ISearch search;
    private PlayerPawn myPlayerPawn;
    private PlayerData playerData;



    /** Gameplay */
    private Vector2 target = null;
    private Queue<Vector2> path = null;

    /** Network */
    // private Registry registry;
    private INetworkGameServer  networkGame;

    public PlayerController() {
        input = new PlayerInput(
                    (mousePos) -> {
                        processMainAction(mousePos);
                    });

        actionIndicator = new Entity() {
            @Override
            public void registerEntity() {
                Engine.getInstance().registerEntity(Layer.Indicators, this);

                this.AddComponent(new RenderComponent(TextureManager.getInstance().getIndicator()));
                this.setPosition(new Vector2(1, 1));
            }

            @Override
            public void render(SpriteBatch batch, float deltaTime) {
                Optional<RenderComponent> r = this.getComponent(RenderComponent.id);
                if(r.get() != null) {
                    // Vector2 worldPos = World.toWorldPos(this.getTransform().getPosition());
                    batch.draw(
                        r.get().getTexture(),
                        this.getTransform().getPosition().x,
                        this.getTransform().getPosition().y
                    );
                }
            }
        };

        Engine.getInstance().registerTickable(this);
        Gdx.input.setInputProcessor(input);

        search = new AStar();

        instance = this;
    }

    public void init() {
        logger.info("Initialized with no args (online mode).");
    }

    public void init(PlayerPawn pawn, PlayerData playerData) {
        this.myPlayerPawn = pawn;
        this.playerData = playerData;

        logger.info("Initialized with args (offline mode).");
    }

    public void setPlayerPawn(PlayerPawn pawn) {
        this.myPlayerPawn = pawn;
    }

    public void setPlayerData(PlayerData playerData) {
        this.playerData = playerData;
    }

    public void initNetworking(INetworkGameServer networkGame) throws Exception {
        logger.info("Initialized networking.");

        this.networkGame = networkGame;
    }

    public boolean hasPawn() { return myPlayerPawn != null; }

    @Override
    public void tick(float deltaTime) {
        try {
            if(networkGame != null && !networkGame.isServer()) {
                Vector3 mousePos = UIManager.getInstance().getMouseWorldPos();
                Entity e = World.getInstance().getEntityAtPoint(mousePos);

                // Render action indicator over an entity in the world
                if(e != null) {
                    actionIndicator.setPosition(World.toWorldPos(e.getPosition()));
                }
            }
        } catch(RemoteException e) { logger.error(e.getMessage()); }
    }

    /**
     * A 'main action' is init on mouse button down
     *  */
    public void processMainAction(Vector3 screenMousePos) {
        try {
            if(!canDoAction()) {
                return;
            }

            Vector3 mousePos = UIManager.getInstance().getMouseWorldPos();
            Entity e = World.getInstance().getEntityAtPoint(mousePos);

            /** Confirm action */
            if(target != null && path.contains(e.getPosition())) {
                // TODO
                logger.info("Action confirmed.");
                networkGame.doAction(NetworkGameClient.getInstance().getPlayerId(), target);
                return;
            }

            // Update current target
            target = e.getPosition();

            // logger.info("Is my turn? " + networkGame.isMyTurn(NetworkGameClient.getInstance().getPlayerId()));
            // logger.info("Is server? " + networkGame.isServer());

            path = networkGame.requestPath(NetworkGameClient.getInstance().getPlayerId(), e.getPosition());

            // Make sure we have enough markers
            while(cachedPathMarkers.size() < path.size()) {
                cachedPathMarkers.add(pathMarkerPool.borrowFrom());
            }

            // Clear old path
            for(Entity marker : cachedPathMarkers) {
                marker.setShouldRender(false);
            }

            int i = 0;
            for(Vector2 pos : path) {
                cachedPathMarkers.get(i).setPosition(pos);
                cachedPathMarkers.get(i).setShouldRender(true);
                i++;
            }
        } catch(Exception err) {
            logger.error("Error communicating with NetworkGame: " + err.getMessage());
        }

        // int playerX = (int) myPlayerPawn.getPosition().x;
        // int playerY = (int) myPlayerPawn.getPosition().y;
        // Queue<ITraversable> path = search.findPath(
        //                                 World.getInstance().getTiles()[playerY][playerX],
        //                                 World.getInstance().getTiles()[(int)e.getPosition().y][(int)e.getPosition().x]);

        // // Make sure we have enough markers
        // while(cachedPathMarkers.size() < path.size()) {
        //     cachedPathMarkers.add(pathMarkerPool.borrowFrom());
        // }

        // // Clear old path
        // for(Entity marker : cachedPathMarkers) {
        //     marker.setShouldRender(false);
        // }

        // logger.info(">>> Found path <<<");
        // int i = 0;
        // for(ITraversable t : path) {
        //     cachedPathMarkers.get(i).setPosition(t.getPosition());
        //     cachedPathMarkers.get(i).setShouldRender(true);
        //     i++;
        // }

        // myPlayerPawn.move(path);
    }

    private boolean canDoAction() throws RemoteException {
        // Network checks
        if(networkGame != null
           && !networkGame.isServer()
           && networkGame.isMyTurn(NetworkGameClient.getInstance().getId())) {

            // Actual gameplay checks
            return myPlayerPawn.getState() == EPawnState.Ready;
        }

        return false;
    }

}
