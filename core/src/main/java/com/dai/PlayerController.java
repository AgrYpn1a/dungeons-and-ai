package com.dai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dai.PlayerInput.EInputAction;
import com.dai.ai.AStar;
import com.dai.ai.ISearch;
import com.dai.ai.ITraversable;
import com.dai.engine.Engine;
import com.dai.engine.Engine.Layer;
import com.dai.entities.IndicatorEntity;
import com.dai.entities.IndicatorEntity.EIndicator;
import com.dai.network.*;
import com.dai.pools.IndicatorsPool;
import com.dai.engine.Entity;
import com.dai.engine.ITickable;
import com.dai.engine.RenderComponent;
import com.dai.world.World;
import com.dai.world.Pawn.EPawnState;
import com.dai.world.Tile;

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
    private List<IndicatorEntity> cachedPathMarkers = new ArrayList<>();

    private ISearch search;

    private PlayerPawn myPlayerPawn;
    private PlayerPawn opponentPawn;

    /** Gameplay */
    private Vector2 target = null;
    private Queue<Vector2> path = null;

    /** Network */
    // private Registry registry;
    private INetworkGameServer  networkGame;

    public PlayerController() {
        input = new PlayerInput();

        input.registerInput(EInputAction.Main, this::processMainAction);
        input.registerInput(EInputAction.EndTurn, this::processEndTurn);

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

        if(NetworkManager.isOffline()) {
            search = new AStar();
        }

        instance = this;
    }

    public void init() {
        logger.info("Initialized with no args (online mode).");
    }

    public void init(PlayerPawn pawn, PlayerData playerData) {
        this.myPlayerPawn = pawn;
        // this.playerData = playerData;

        logger.info("Initialized with args (offline mode).");
    }

    public void setPlayerPawn(PlayerPawn pawn) {
        myPlayerPawn = pawn;

        if(NetworkManager.isOffline()) {
            myPlayerPawn.consumeActionPoints(-Integer.MAX_VALUE);
        }
    }

    public boolean hasPawn() { return myPlayerPawn != null; }

    public void setOpponentPawn(PlayerPawn pawn) { opponentPawn = pawn; }

    public PlayerPawn getPlayerPawn() { return myPlayerPawn; }

    public PlayerPawn getOpponentPawn() { return opponentPawn; }

    public void setPlayerData(PlayerData playerData) {
        // this.playerData = playerData;
    }

    public void initNetworking(INetworkGameServer networkGame) throws Exception {
        logger.info("Initialized networking.");

        this.networkGame = networkGame;
    }

    @Override
    public void tick(float deltaTime) {
        if(!World.getInstance().isInit()) {
            return;
        }

        // Networking not initialised yet and not in offline mode!
        if(!NetworkManager.isOffline() && networkGame == null) {
            return;
        }

        if(!NetworkManager.isServer()) {
            Vector3 mousePos = UIManager.getInstance().getMouseWorldPos();
            Tile tile = World.getInstance().getTileAtPoint(mousePos);

            // Render action indicator over an entity in the world
            if(tile != null) {
                actionIndicator.setPosition(World.toWorldPos(tile.getPosition()));
            }
        }
    }

    private boolean canDoAction() {
        try {
            // Network checks
            if(NetworkManager.isOffline() || !NetworkManager.isServer() && networkGame.isMyTurn(NetworkGameClient.getInstance().getId())) {

                // Actual gameplay checks
                return myPlayerPawn.getState() == EPawnState.Ready;
            }
        } catch(RemoteException e) {
            logger.error(e.getMessage());
            return false;
        }

        return false;
    }

    /** Renders current path */
    private void renderPath() {
        // Make sure we have enough markers
        while(cachedPathMarkers.size() < path.size()) {
            cachedPathMarkers.add(pathMarkerPool.borrowFrom());
        }

        // Clear old path
        for(Entity marker : cachedPathMarkers) {
            marker.setShouldRender(false);
        }

        int i = 0;
        int costSoFar = 0;
        for(Vector2 pos : path) {
            cachedPathMarkers.get(i).setPosition(pos);
            cachedPathMarkers.get(i).setShouldRender(true);
            cachedPathMarkers.get(i).changeType(EIndicator.PathMarker);

            // Calculate cost and update path markers
            ITraversable t = World.getInstance().getTraversableAtPoint(pos);
            costSoFar += 1 * t.getCostModifier();

            if(costSoFar > myPlayerPawn.getActionPoints()) {
                cachedPathMarkers.get(i).changeType(EIndicator.PathUnreachableMarker);
            }

            if(i == path.size() - 1) {
                cachedPathMarkers.get(i).changeType(EIndicator.PathTargetMarker);
            }

            i++;
        }
    }

    /** ====================
     *  Processing actions
     *  ====================
     *  */
    public void processMainAction(Vector3 screenMousePos) {
        if(!canDoAction()) {
            return;
        }

        logger.info("Processing MAIN_ACTION");

        Vector3 mousePos = UIManager.getInstance().getMouseWorldPos();
        Entity e = World.getInstance().getTileAtPoint(mousePos);

        if(NetworkManager.isOffline()) {
            int playerX = (int) myPlayerPawn.getPosition().x;
            int playerY = (int) myPlayerPawn.getPosition().y;
            path = search.findPath(
                World.getInstance().getTiles()[playerY][playerX],
                World.getInstance().getTiles()[(int)e.getPosition().y][(int)e.getPosition().x]);

            renderPath();

            /** Confirm action */
            if(target != null && target.equals(e.getPosition())) {
                myPlayerPawn.move(path);
                // target = null;
            }

            target = e.getPosition();
        } else {
            try {
                /** Confirm action */
                // if(target != null && path.contains(e.getPosition())) {
                //     // TODO
                //     logger.info("Action confirmed.");
                //     networkGame.doAction(NetworkGameClient.getInstance().getPlayerId(), target);
                //     return;
                // }

                path = networkGame.requestPath(NetworkGameClient.getInstance().getPlayerId(), e.getPosition());

                renderPath();

                /** Confirm action */
                if(target != null && target.equals(e.getPosition())) {
                    networkGame.doAction(NetworkGameClient.getInstance().getPlayerId(), target);
                } else {
                    // Update target
                    target = e.getPosition();
                }
            } catch(Exception err) {
                logger.error("Error communicating with NetworkGame: " + err.getMessage());
            }
        }



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

    public void processEndTurn(Integer keycode) {
        try {
            if(canDoAction()) {
                networkGame.endTurn(NetworkGameClient.getInstance().getId());
            }
        } catch(Exception e) {
            logger.error(e.getMessage());
        }
    }

}
