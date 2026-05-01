package Game.GettingUnderYourNerve;

import Game.GettingUnderYourNerve.Enemies.Crab;
import Game.GettingUnderYourNerve.Enemies.Enemy;
import Game.GettingUnderYourNerve.Enemies.Shell;
import Game.GettingUnderYourNerve.Map.PlayableMap;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.FileHandler;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import Game.GettingUnderYourNerve.Utilities.WorldContactListener;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {
    FileHandler fileHandler;

    // --- Box2D & Scaling ---
    private World world;
    private Box2DDebugRenderer debugRenderer;
    public static final float PPM = 32f;

    private Player player;
    private PlayableMap playableMap;
    private Enemy enemy;

    // --- Assets ---
    public GameAssetManager assets;

    // --- Graphics ---
    private SpriteBatch batch;

    // --- Camera & Viewport ---
    private Viewport viewport;
    private GameCam cam;

    private boolean DebugOption = true;

    private final float WORLD_WIDTH = 800;
    private final float WORLD_HEIGHT = 480;

    // --- Collision Bits ---
    public static final short BIT_NONE = 0;
    public static final short GROUND_BIT = 1;
    public static final short PLAYER_BIT = 2;
    public static final short ENEMY_BIT = 4;
    public static final short PROJECTILE_BIT = 8;
    public static final short WATER_BIT = 16;
    public static final short COIN_BIT = 32;
    public static final short POTION_BIT = 64;

    @Override
    public void create() {

        // Physics World
        world = new World(new Vector2(0, -40f), true);
        world.setContactListener(new WorldContactListener());
        debugRenderer = new Box2DDebugRenderer();

        // Audio
        AudioManager.load();

        fileHandler = new FileHandler(); // Initialize FileHandler

        // Assets
        assets = new GameAssetManager();
        assets.loadAllAssets();
        assets.manager.finishLoading();

        // Create Player + Map
        player = new Player(20, assets);
        playableMap = new PlayableMap(assets);

        // Graphics
        batch = new SpriteBatch();

        // Camera
        cam = new GameCam();
        viewport = new FitViewport(WORLD_WIDTH / PPM, WORLD_HEIGHT / PPM, cam.GetCam());

        // Map Physics
        playableMap.createPhysicsFromMap(world);

        // Spawn Player
        player.SpawnPlayerFromTiled(playableMap.GetMap(), world);

        // Spawn Enemy (Crab from Crab layer)
        enemy = spawnOneCrab(playableMap.GetMap(), world, assets);
    }

    // Spawn Crab
    public Shell spawnOneCrab(TiledMap map, World world, GameAssetManager assets) {

        MapLayer layer = map.getLayers().get("Shell");

        if (layer != null && layer.getObjects().getCount() > 0) {

            MapObject obj = layer.getObjects().get(0);

            float x = obj.getProperties().get("x", Float.class);
            float y = obj.getProperties().get("y", Float.class);

            return new Shell(world, x, y, assets);
        }

        return null;
    }

    @Override
    public void render() {

        fileHandler.GetFilInput(player);

        // --- Physics Update ---
        world.step(1 / 60f, 6, 2);
        float dt = Gdx.graphics.getDeltaTime();

        player.UpdatePlayer(dt, world);

        enemy.updateEnemy(Gdx.graphics.getDeltaTime(), player);

        // --- Camera Follow ---
        float worldWidth = playableMap.getMapWidthInMeters();
        float worldHeight = playableMap.getMapHeightInMeters();

        float halfViewportWidth = (WORLD_WIDTH / PPM) / 2f;
        float halfViewportHeight = (WORLD_HEIGHT / PPM) / 2f;

        if (player.isDead) {
            cam.SetDeathTarget(worldWidth, worldHeight,
                halfViewportWidth, halfViewportHeight,
                player.spawnX, player.spawnY);
        }

        cam.Update(
            worldWidth,
            worldHeight,
            halfViewportWidth,
            halfViewportHeight,
            player.GetXpos(),
            player.GetYpos()
        );


        // --- Clear Screen ---
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        viewport.apply();

        // --- Draw Background ---
        batch.setProjectionMatrix(cam.GetCam().combined);
        batch.begin();
        playableMap.DrawBackGround(batch, cam, viewport, dt);
        batch.end();

        // --- Update Map ---
        playableMap.UpdateMap(cam.GetCam(), dt, world);

        // --- Draw Foreground ---
        batch.begin();

        player.Render(batch, dt);
        playableMap.DrawElements(batch);

        if (enemy != null)
            enemy.render(dt, batch);

        batch.end();

        boolean isCtrlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) ||  Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        if (isCtrlPressed &&  Gdx.input.isKeyJustPressed(Input.Keys.D)){
            DebugOption = !DebugOption;
        }

        // --- Debug Renderer ---
        if(DebugOption){
        debugRenderer.render(world, cam.GetCam().combined);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {

        batch.dispose();
        world.dispose();
        debugRenderer.dispose();

        player.dispose();
        playableMap.dispose();

        AudioManager.dispose();
    }
}
