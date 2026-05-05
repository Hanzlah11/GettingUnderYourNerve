package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Cutscenes.BaseCutscene;
import Game.GettingUnderYourNerve.Cutscenes.IntroEncounter;
import Game.GettingUnderYourNerve.GameCam;
import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Map.PlayableMap;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.FileHandler;
import Game.GettingUnderYourNerve.Utilities.WorldContactListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PlayScreen implements Screen {

    private Main game;
    private FileHandler fileHandler;

    // --- Box2D ---
    private World world;
    private Box2DDebugRenderer debugRenderer;

    private Player player;
    private PlayableMap playableMap;

    // --- Camera & Viewport ---
    private Viewport viewport;
    private GameCam cam;
    private Viewport uiViewport;

    private boolean DebugOption = true;

    private final float WORLD_WIDTH  = 800;
    private final float WORLD_HEIGHT = 480;

    private BaseCutscene currentCutscene;

    public PlayScreen(Main game) {
        this.game = game;

        // Physics World
        world = new World(new Vector2(0, -40f), true);

        WorldContactListener contactListener = new WorldContactListener();
        world.setContactListener(contactListener);

        debugRenderer = new Box2DDebugRenderer();
        fileHandler = new FileHandler();

        // Create Player + Map
        player      = new Player(20, game.assets);
        playableMap = new PlayableMap(game.assets);

        // Camera Setup
        cam        = new GameCam();
        viewport   = new FitViewport(WORLD_WIDTH / Main.PPM, WORLD_HEIGHT / Main.PPM, cam.GetCam());
        uiViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);

        // Map Physics initialization
        playableMap.createPhysicsFromMap(world);

        contactListener.setPlayableMap(playableMap);

        // Spawn Player[cite: 21]
        player.SpawnPlayerFromTiled(playableMap.GetMap(), world);
        currentCutscene = new IntroEncounter(this, playableMap.getBatman());
    }

    @Override
    public void show() { }

    // Inside PlayScreen.java

    // 1. Rename your rendering block to a public method
    public void drawWorld(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        viewport.apply();

        // --- Layer 1: Background ---
        game.batch.setProjectionMatrix(cam.GetCam().combined);
        game.batch.begin();
        playableMap.DrawBackGround(game.batch, cam, viewport, delta);
        game.batch.end();

        // --- Layer 2: Tilemap ---
        playableMap.RenderTileMap(cam.GetCam());

        // --- Layer 3: Entities ---
        game.batch.setProjectionMatrix(cam.GetCam().combined);
        game.batch.begin();
        player.Render(game.batch, delta);
        playableMap.DrawElements(game.batch, delta);
        game.batch.end();

        // Optional: Draw debug if enabled
        handleDebugInput();
        if (DebugOption) {
            debugRenderer.render(world, cam.GetCam().combined);
        }
    }

    @Override
    public void render(float delta) {
        // 2. Standard Input handling
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new PauseScreen(game, this, player.getHealth(), player.getScore()));
            return;
        }

        // 3. Logic Updates (Only runs while playing)
        updateLogic(delta);

        // 4. Draw the world
        drawWorld(delta);
    }

    private void updateLogic(float delta) {
        boolean inCutscene = (currentCutscene != null);

        if (inCutscene) {
            currentCutscene.update(delta);
            if (currentCutscene.isFinished()) {
                cam.setPosition(cam.GetCam().position.x, cam.GetCam().position.y);
                currentCutscene = null;
                inCutscene = false; // Reset for the update call below
            }
        } else {
            fileHandler.GetFilInput(player, playableMap);
        }


        // --- 1. CAPTURE STATE BEFORE PHYSICS STEP ---
        boolean wasDead = player.isDead;
        int healthBefore = player.getHealth();

        // Perform physics and player updates
        world.step(1 / 60f, 6, 2);
        player.UpdatePlayer(delta, world, inCutscene);

        // --- 2. RESPAWN DETECTION ---
        // If the player was dead but is now alive, reset map triggers
        if (wasDead && !player.isDead) {
            playableMap.resetTriggers(world);
        }

        // --- 3. SCREEN SHAKE LOGIC ---
        // Only shake if the player is still alive but took damage
        if (!player.isDead && player.getHealth() < healthBefore) {
            int damageTaken = healthBefore - player.getHealth();

            if (damageTaken >= 25) {
                // Heavy hit (e.g., spike, heavy enemy attack)[cite: 15]
                cam.startShake(0.4f, 0.6f);
            } else {
                // Light hit (e.g., minor projectile or graze)[cite: 15]
                cam.startShake(0.2f, 0.4f);
            }
        }

        // --- 4. CAMERA UPDATES ---[cite: 15, 18]
        float worldWidth = playableMap.getMapWidthInMeters();
        float worldHeight = playableMap.getMapHeightInMeters();
        float halfVW = (WORLD_WIDTH / Main.PPM) / 2f;
        float halfVH = (WORLD_HEIGHT / Main.PPM) / 2f;

        // Handle camera behavior if the player is dead[cite: 15]
        if (player.isDead) {
            cam.SetDeathTarget(worldWidth, worldHeight,
                halfVW, halfVH,
                player.spawnX, player.spawnY);
        }

        // Apply standard camera follow and update map elements[cite: 18]
        if (currentCutscene == null) {
            // Standard camera behavior following the player[cite: 24, 26]
            cam.Update(worldWidth, worldHeight, halfVW, halfVH, player.GetXpos(), player.GetYpos());
        } else {
            // Cutscene is manually moving cam.position.x; just update matrices[cite: 24, 25]
            cam.GetCam().update();
        }
        playableMap.UpdateMap(cam.GetCam(), delta, world, player);
    }

    private void handleDebugInput() {
        boolean isCtrlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) ||
            Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            DebugOption = !DebugOption;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        uiViewport.update(width, height, true);
    }

    @Override public void pause()  { }
    @Override public void resume() { }
    @Override public void hide()   { }

    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        player.dispose();
        playableMap.dispose();
    }

    public Player getPlayer() {
        return player;
    }

    public GameCam getCam() { return cam; }

    public PlayableMap getPlayableMap() {
        return playableMap;
    }
}
