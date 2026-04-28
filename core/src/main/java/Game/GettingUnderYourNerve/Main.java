package Game.GettingUnderYourNerve;

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


    @Override
    public void create() {
        // Setup Physics World (Gravity pulling down on Y axis)
        world = new World(new Vector2(0, -40f), true);
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                // Grab the UserData we attached to the bodies
                Object objA = contact.getFixtureA().getUserData();
                Object objB = contact.getFixtureB().getUserData();

                // Check if the collision is between a Player and a Coin
                if (objA instanceof Player && objB instanceof Coin) {
                    ((Coin) objB).onCollect((Player) objA);
                } else if (objB instanceof Player && objA instanceof Coin) {
                    ((Coin) objA).onCollect((Player) objB);
                }
            }

            @Override
            public void endContact(Contact contact) {}
            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
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
    }


    @Override
    public void render() {
        // --- 1. UPDATE PHYSICS ---
        world.step(1 / 60f, 6, 2);
        player.UpdatePlayer(world);

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

        // 1. Get the correct frame from the player's state machine
        TextureRegion currentFrame = player.GetCurrentFrame(dt);

        // 2. Define the exact size you want the sprite to appear on screen.
        // If your Box2D body is 1 meter wide (32/PPM), you want the sprite to be slightly larger
        // to cover the hitbox nicely. Captain Clown Nose is usually drawn at roughly 2 meters wide to account for his sword swing.
        float spriteDrawWidth = 64f / Player.PPM;
        float spriteDrawHeight = 64f / Player.PPM;

        // 3. Draw the frame, forcing it to scale to your defined width/height
        batch.draw(currentFrame,
            player.GetXpos() - (spriteDrawWidth / 2f),  // Center X
            player.GetYpos() - (spriteDrawHeight / 2f), // Center Y
            spriteDrawWidth,
            spriteDrawHeight);

        playableMap.DrawElements(batch);

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
