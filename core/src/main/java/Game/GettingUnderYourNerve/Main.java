package Game.GettingUnderYourNerve;

import Game.GettingUnderYourNerve.Enemies.Enemy;
import Game.GettingUnderYourNerve.Enemies.Shell;
import Game.GettingUnderYourNerve.Map.PlayableMap;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import Game.GettingUnderYourNerve.Utilities.WorldContactListener;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
    // --- 1. Box2D & Scaling Variables ---
    private World world;
    private Box2DDebugRenderer debugRenderer;
    public static final float PPM = 32f; // Pixels Per Meter
    Player player;
    PlayableMap playableMap;

    public GameAssetManager assets;

    // --- 2. Graphics Variables ---
    private SpriteBatch batch;

    // --- 3. Camera & Viewport ---
    private Viewport viewport;
    private GameCam cam;

    private final float WORLD_WIDTH = 800;
    private final float WORLD_HEIGHT = 480;

    // --- COLLISION BITS ---
    public static final short BIT_NONE = 0;
    public static final short GROUND_BIT = 1;
    public static final short PLAYER_BIT = 2;
    public static final short ENEMY_BIT = 4;
    public static final short PROJECTILE_BIT = 8;
    public static final short WATER_BIT = 16;
    public static final short COIN_BIT = 32;   // ADDED
    public static final short POTION_BIT = 64; // ADDED

    Enemy enemy;

    @Override
    public void create() {
        world = new World(new Vector2(0, -40f), true);
        world.setContactListener(new WorldContactListener());
        debugRenderer = new Box2DDebugRenderer();
        AudioManager.load();

        assets = new GameAssetManager();
        assets.loadAllAssets();
        assets.manager.finishLoading();

        // FIX: Pass assets into the Player constructor
        player = new Player(20, assets);
        playableMap = new PlayableMap(assets);

        batch = new SpriteBatch();

        cam =  new GameCam();
        viewport = new FitViewport(WORLD_WIDTH / PPM, WORLD_HEIGHT / PPM, cam.GetCam());

        playableMap.createPhysicsFromMap(world);

        player.SpawnPlayerFromTiled(playableMap.GetMap(), world);

        // FIX: Pass assets into the enemy spawner
        enemy = spawnOneShell(playableMap.GetMap(), world, assets);
    }

    // FIX: Updated method signature to accept assets
    public Shell spawnOneShell(TiledMap map, World world, GameAssetManager assets) {
        MapLayer layer = map.getLayers().get("Shell");

        if (layer != null && layer.getObjects().getCount() > 0) {
            MapObject obj = layer.getObjects().get(0);
            float x = obj.getProperties().get("x", Float.class);
            float y = obj.getProperties().get("y", Float.class);

            // Pass assets to the Shell
            return new Shell(world, x, y, assets);
        }

        return null;
    }

    // ... [render, resize, dispose remain EXACTLY the same] ...


    @Override
    public void render() {
        // --- 1. UPDATE PHYSICS ---
        world.step(1 / 60f, 6, 2);
        player.UpdatePlayer(world);

        enemy.updateEnemy(Gdx.graphics.getDeltaTime(), player);

        // --- 4. CAMERA FOLLOW ---

        float WorldWidth = playableMap.getMapWidthInMeters();
        float WorldHeight = playableMap.getMapHeightInMeters();

        float halfViewportWidth = (WORLD_WIDTH / PPM) / 2f;
        float halfViewportHeight = (WORLD_HEIGHT / PPM) / 2f;

        cam.Update(WorldWidth, WorldHeight, halfViewportWidth, halfViewportHeight, player.GetXpos(), player.GetYpos());


        float dt = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        viewport.apply();

        batch.setProjectionMatrix(cam.GetCam().combined);
        batch.begin();
        playableMap.DrawBackGround(batch, cam, viewport, dt);
        batch.end();

        playableMap.UpdateMap(cam.GetCam(), dt, world);

        batch.begin();

        player.Render(batch, dt);
        playableMap.DrawElements(batch);
        enemy.render(dt, batch);

        batch.end();

        // The magical debug renderer (draws the physics boxes)
        debugRenderer.render(world, cam.GetCam().combined);
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
