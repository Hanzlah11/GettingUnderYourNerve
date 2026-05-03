package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.GameCam;
import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Map.PlayableMap;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.UI.PauseMenu;
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

    // Reference to the main game object so we can access the SpriteBatch & Assets
    private Main game;

    FileHandler fileHandler;

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

    // --- Pause Menu ---
    private PauseMenu pauseMenu;

    public PlayScreen(Main game) {
        this.game = game;

        // Physics World
        world = new World(new Vector2(0, -40f), true);

        WorldContactListener contactListener = new WorldContactListener();
        world.setContactListener(contactListener);

        debugRenderer = new Box2DDebugRenderer();
        fileHandler = new FileHandler();

        // Pause Menu
        pauseMenu = new PauseMenu();
        pauseMenu.loadAssets(game.assets);
        pauseMenu.resize((int) WORLD_WIDTH, (int) WORLD_HEIGHT);

        // Create Player + Map
        player      = new Player(20, game.assets);
        playableMap = new PlayableMap(game.assets);

        // Camera
        cam        = new GameCam();
        viewport   = new FitViewport(WORLD_WIDTH / Main.PPM, WORLD_HEIGHT / Main.PPM, cam.GetCam());
        uiViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);

        // Map Physics
        playableMap.createPhysicsFromMap(world);

        // Wire map into contact listener AFTER map is fully built
        contactListener.setPlayableMap(playableMap);

        // Spawn Player
        player.SpawnPlayerFromTiled(playableMap.GetMap(), world);
    }

    @Override
    public void show() {
        // Called when this screen becomes the active screen
    }

    @Override
    public void render(float delta) {

        pauseMenu.handleInput();

        if (!pauseMenu.isPaused()) {
            fileHandler.GetFilInput(player, playableMap);

            // Note: We use the 'delta' passed in by the render method instead of Gdx.graphics.getDeltaTime()
            world.step(1 / 60f, 6, 2);

            boolean wasDead = player.isDead;
            player.UpdatePlayer(delta, world);
            if (wasDead && !player.isDead) {
                playableMap.resetTriggers(world);
            }

            float worldWidth  = playableMap.getMapWidthInMeters();
            float worldHeight = playableMap.getMapHeightInMeters();

            float halfViewportWidth  = (WORLD_WIDTH  / Main.PPM) / 2f;
            float halfViewportHeight = (WORLD_HEIGHT / Main.PPM) / 2f;

            if (player.isDead) {
                cam.SetDeathTarget(worldWidth, worldHeight,
                    halfViewportWidth, halfViewportHeight,
                    player.spawnX, player.spawnY);
            }

            cam.Update(
                worldWidth, worldHeight,
                halfViewportWidth, halfViewportHeight,
                player.GetXpos(), player.GetYpos()
            );

            playableMap.UpdateMap(cam.GetCam(), delta, world, player);
        }

        // --- Always render ---
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        viewport.apply();

        // Notice how we use 'game.batch' to access the SpriteBatch from Main
        game.batch.setProjectionMatrix(cam.GetCam().combined);
        game.batch.begin();
        playableMap.DrawBackGround(game.batch, cam, viewport, delta);
        game.batch.end();

        playableMap.RenderTileMap(cam.GetCam());

        game.batch.setProjectionMatrix(cam.GetCam().combined);
        game.batch.begin();
        player.Render(game.batch, delta);
        playableMap.DrawElements(game.batch, delta);
        game.batch.end();

        boolean isCtrlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) ||
            Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            DebugOption = !DebugOption;
        }

        if (DebugOption) {
            debugRenderer.render(world, cam.GetCam().combined);
        }

        // --- Pause menu on top ---
        if (pauseMenu.isPaused()) {
            uiViewport.apply();
            pauseMenu.render(game.batch, player.getHealth(), player.getScore(), uiViewport);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        uiViewport.update(width, height, true);
        pauseMenu.resize((int) WORLD_WIDTH, (int) WORLD_HEIGHT);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        // Dispose of level-specific things here
        world.dispose();
        debugRenderer.dispose();
        player.dispose();
        playableMap.dispose();
        pauseMenu.dispose();
    }
}
