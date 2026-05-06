package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Cutscenes.BaseCutscene;
import Game.GettingUnderYourNerve.Cutscenes.IntroEncounter;
import Game.GettingUnderYourNerve.Cutscenes.PrologueCutscene;
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

    private float WORLD_WIDTH;
    private float WORLD_HEIGHT;

    private BaseCutscene currentCutscene;
    private int levelNumber;

    public PlayScreen(Main game, int levelNumber) {
        this.game = game;
        this.levelNumber = levelNumber;
        // Physics World

        this.WORLD_WIDTH  = (levelNumber == 0) ? 400 : 800;
        this.WORLD_HEIGHT = (levelNumber == 0) ? 240 : 480;

        world = new World(new Vector2(0, -40f), true);

        WorldContactListener contactListener = new WorldContactListener();
        world.setContactListener(contactListener);

        debugRenderer = new Box2DDebugRenderer();
        fileHandler = new FileHandler();

        // Create Player + Map
        player      = new Player(20, game.assets);
        playableMap = new PlayableMap(game.assets, levelNumber);

        // Camera Setup
        cam        = new GameCam();
        viewport   = new FitViewport(WORLD_WIDTH / Main.PPM, WORLD_HEIGHT / Main.PPM, cam.GetCam());
        uiViewport = new FitViewport(800, 480);

        // Map Physics initialization
        playableMap.createPhysicsFromMap(world);

        contactListener.setPlayableMap(playableMap);

        // Spawn Player
        player.SpawnPlayerFromTiled(playableMap.GetMap(), world);
        if (this.levelNumber == 0) {
            currentCutscene = new PrologueCutscene(this);
        }
        else
            currentCutscene = new IntroEncounter(this, playableMap.getBatman());
    }

    @Override
    public void show() { }

    public World getWorld() { return world; }
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new PauseScreen(game, this, player.getHealth(), player.getScore()));
            return;
        }

        // CRITICAL FIX: If updateLogic returns false, the map was deleted. Abort drawing![cite: 16]
        if (!updateLogic(delta)) {
            return;
        }

        drawWorld(delta);
    }

    private boolean updateLogic(float delta) {
        boolean inCutscene = (currentCutscene != null);

        // --- 4. CAMERA UPDATES ---
        float worldWidth = playableMap.getMapWidthInMeters();
        float worldHeight = playableMap.getMapHeightInMeters();
        float halfVW = (WORLD_WIDTH / Main.PPM) / 2f;
        float halfVH = (WORLD_HEIGHT / Main.PPM) / 2f;

        if (inCutscene) {
            currentCutscene.update(delta);
            if (levelNumber == 0) {
                cam.GetCam().position.x = com.badlogic.gdx.math.MathUtils.clamp(
                    cam.GetCam().position.x, halfVW, worldWidth - halfVW);

                cam.GetCam().position.y = com.badlogic.gdx.math.MathUtils.clamp(
                    cam.GetCam().position.y, halfVH, worldHeight - halfVH);
            }
            if (currentCutscene.isFinished()) {
                // If the prologue just finished, shift to Level 1![cite: 21]
                if (levelNumber == 0) {
                    game.setScreen(new PlayScreen(game, 1));
                    this.dispose(); // CRITICAL: prevent memory leaks
                    return false;
                }

                cam.setPosition(cam.GetCam().position.x, cam.GetCam().position.y);
                currentCutscene = null;
                inCutscene = false;
            }
        } else {
            // --- RE-ADDED: QUICK SAVE & QUICK LOAD HOTKEYS ---
            boolean isCtrlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

            if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                String playerSaveFile = "SavedFiles/" + EnterNameScreen.globalPlayerName + ".json";
                fileHandler.saveGameState(player, playableMap, playerSaveFile);
            }

            if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.L)) {
                game.setScreen(new LoadScreen(game, this));
                return false; // Stop updating for this frame while we switch to the Load menu
            }
            // -------------------------------------------------
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
                // Heavy hit (e.g., spike, heavy enemy attack)
                cam.startShake(0.4f, 0.6f);
            } else {
                // Light hit (e.g., minor projectile or graze)
                cam.startShake(0.2f, 0.4f);
            }
        }



        // Handle camera behavior if the player is dead
        if (player.isDead) {
            cam.SetDeathTarget(worldWidth, worldHeight,
                halfVW, halfVH,
                player.spawnX, player.spawnY);
        }

        // Apply standard camera follow and update map elements
        if (currentCutscene == null) {
            // Standard camera behavior following the player
            cam.Update(worldWidth, worldHeight, halfVW, halfVH, player.GetXpos(), player.GetYpos());
        } else {
            // Cutscene is manually moving cam.position.x; just update matrices
            cam.GetCam().update();
        }
        playableMap.UpdateMap(cam.GetCam(), delta, world, player);
        return true;
    }

    private void handleDebugInput() {
        boolean isCtrlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) ||
            Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            DebugOption = !DebugOption;
        }
    }

    // --- RE-ADDED: Helper method called by LoadScreen when the player confirms a load ---
    public void executeLoad(String saveName) {
        String playerSaveFile = "SavedFiles/" + saveName + ".json";
        fileHandler.loadGameState(player, playableMap, playerSaveFile);

        // Ensure future Quick Saves overwrite the newly loaded file!
        EnterNameScreen.globalPlayerName = saveName;
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
        // --- RE-ADDED: Safe dispose order to prevent C++ EXCEPTION_ACCESS_VIOLATION crashes ---
        playableMap.dispose();
        player.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
        if (world != null) world.dispose();
    }

    public Player getPlayer() {
        return player;
    }

    public GameCam getCam() { return cam; }

    public PlayableMap getPlayableMap() {
        return playableMap;
    }
}
