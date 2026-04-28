package Game.GettingUnderYourNerve;

import Game.GettingUnderYourNerve.Enemies.Enemy;
import Game.GettingUnderYourNerve.Enemies.Shell;
import Game.GettingUnderYourNerve.Utilities.WorldContactListener;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
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

    // --- 2. Graphics Variables ---
    private SpriteBatch batch;

    // --- 3. Camera & Viewport ---
    private Viewport viewport;
    private GameCam cam;

    private final float WORLD_WIDTH = 800;
    private final float WORLD_HEIGHT = 480;

    public static final short GROUND_BIT = 1;
    public static final short PLAYER_BIT = 2;
    public static final short ENEMY_BIT = 4;
    public static final short BIT_NONE = 0;

    Enemy enemy;


    @Override
    public void create() {
        // Setup Physics World (Gravity pulling down on Y axis)
        world = new World(new Vector2(0, -40f), true);
        world.setContactListener(new WorldContactListener());
        debugRenderer = new Box2DDebugRenderer();
        player = new Player(20);
        playableMap = new PlayableMap();

        // Setup Graphics
        batch = new SpriteBatch();

        // Setup Camera (Viewport is scaled to meters)
        cam =  new GameCam();
        viewport = new FitViewport(WORLD_WIDTH / PPM, WORLD_HEIGHT / PPM, cam.GetCam());

        // 1. Generate Static Collision Walls
        playableMap.createPhysicsFromMap(world);

        // 2. Spawn Dynamic Player
        player.SpawnPlayerFromTiled(playableMap.GetMap(), world);
        enemy = spawnOneShell(playableMap.GetMap(), world);

    }


    public Shell spawnOneShell(TiledMap map, World world) {
        // 1. Grab the "Shell" layer
        MapLayer layer = map.getLayers().get("Shell");

        // 2. Check if the layer exists and has at least one object
        if (layer != null && layer.getObjects().getCount() > 0) {
            // Get only the first object (index 0)
            MapObject obj = layer.getObjects().get(0);

            float x = obj.getProperties().get("x", Float.class);
            float y = obj.getProperties().get("y", Float.class);

            // Return just this one shell
            return new Shell(world, x, y);
        }

        return null; // No layer or no objects found
    }


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

        // --- 5. RENDERING ---
        // Changed to a dark blue clear color. If you see this color, the game is drawing properly!
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        viewport.apply();

        float dt = Gdx.graphics.getDeltaTime();
        playableMap.UpdateMap(cam.GetCam(), dt, world);

        batch.setProjectionMatrix(cam.GetCam().combined);
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
    }
}
