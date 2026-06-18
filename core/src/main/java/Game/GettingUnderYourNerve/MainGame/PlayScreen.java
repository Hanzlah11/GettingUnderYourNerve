package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Cutscenes.*;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameCam;
import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Map.PlayableMap;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.FileHandler;
import Game.GettingUnderYourNerve.Utilities.WorldContactListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
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

    // --- Cutscene & Level State ---
    private BaseCutscene currentCutscene;
    private int levelNumber;
    private boolean isPostBattle = false;

    // --- Save Limiter (From Version 2) ---
    private int quickSavesUsed = 0;
    private final int MAX_SAVES = 3;

    public PlayScreen(Main game, int levelNumber)
    {
        this(game, levelNumber, false);
        startLevelMusic();
    }

    public PlayScreen(Main game, int levelNumber, boolean isPostBattle) {
        this.game = game;
        this.levelNumber = levelNumber;
        this.isPostBattle = isPostBattle;

        startLevelMusic();

        // Dynamic world sizing based on level
        this.WORLD_WIDTH  = (levelNumber == 0) ? 400 : 800;
        this.WORLD_HEIGHT = (levelNumber == 0) ? 240 : 480;

        world = new World(new Vector2(0, -40f), true);

        WorldContactListener contactListener = new WorldContactListener();
        world.setContactListener(contactListener);

        debugRenderer = new Box2DDebugRenderer();
        fileHandler = new FileHandler();

        player      = new Player(20, game.assets);
        playableMap = new PlayableMap(game.assets, levelNumber);

        cam        = new GameCam();
        viewport   = new FitViewport(WORLD_WIDTH / Main.PPM, WORLD_HEIGHT / Main.PPM, cam.GetCam());
        uiViewport = new FitViewport(800, 480);

        playableMap.createPhysicsFromMap(world);
        contactListener.setPlayableMap(playableMap);

        player.SpawnPlayerFromTiled(playableMap.GetMap(), world);

        // --- LEVEL TRIGGER LOGIC ---
        if (this.levelNumber == 0) {
            currentCutscene = new PrologueCutscene(this);
        } else if (this.levelNumber == 3 && !this.isPostBattle) {
            currentCutscene = new BossCutscene(this, playableMap.getBatman());
        } else if (this.levelNumber == 3 && this.isPostBattle) {
            currentCutscene = new AvengersCutscene(this, playableMap.getBatman()); // Contingency Phase
        } else {
            currentCutscene = new IntroEncounter(this, playableMap.getBatman());
        }
    }

    public Main getGame() { return game; }

    public void startPokemonBattle() {
        AudioManager.elevatorMusic.pause();
        AudioManager.level1Music.pause();
        AudioManager.level2Music.pause();
        AudioManager.bossArenaMusic.pause();

        game.setScreen(new PokemonBattleScreen(game));
    }

    public World getWorld() { return world; }

    public void drawWorld(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        viewport.apply();

        game.batch.setProjectionMatrix(cam.GetCam().combined);
        game.batch.begin();
        playableMap.DrawBackGround(game.batch, cam, viewport, delta);
        game.batch.end();

        playableMap.RenderTileMap(cam.GetCam());

        game.batch.setProjectionMatrix(cam.GetCam().combined);
        game.batch.begin();
        player.Render(game.batch, delta);
        playableMap.DrawElements(game.batch, delta);

        // Renders Pokeballs or other cutscene overlays
        if (currentCutscene != null) currentCutscene.render(game.batch);

        game.batch.end();

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

        if (!updateLogic(delta)) return;

        drawWorld(delta);
    }

    private boolean updateLogic(float delta) {
        boolean inCutscene = (currentCutscene != null);

        float worldWidth = playableMap.getMapWidthInMeters();
        float worldHeight = playableMap.getMapHeightInMeters();
        float halfVW = (WORLD_WIDTH / Main.PPM) / 2f;
        float halfVH = (WORLD_HEIGHT / Main.PPM) / 2f;

        if (inCutscene) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                currentCutscene.skip();
            }
            if (!currentCutscene.isFinished()) {
                currentCutscene.update(delta);
            }

            // Camera clamping for Prologue
            if (levelNumber == 0) {
                cam.GetCam().position.x = com.badlogic.gdx.math.MathUtils.clamp(
                    cam.GetCam().position.x, halfVW, worldWidth - halfVW);
                cam.GetCam().position.y = com.badlogic.gdx.math.MathUtils.clamp(
                    cam.GetCam().position.y, halfVH, worldHeight - halfVH);
            }

            if (currentCutscene.isFinished()) {
                // If the prologue just finished, go to Level 1
                if (levelNumber == 0) {
                    game.setScreen(new PlayScreen(game, 1));
                    this.dispose();
                    return false;
                }

                cam.setPosition(cam.GetCam().position.x, cam.GetCam().position.y);
                currentCutscene = null;
                inCutscene = false;
            }
        } else {
            // INPUT HANDLING
            boolean isCtrlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

            if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                if (quickSavesUsed < MAX_SAVES) {
                    String playerSaveFile = "SavedFiles/" + EnterNameScreen.globalPlayerName + ".json";
                    fileHandler.saveGameState(player, playableMap, playerSaveFile);
                    quickSavesUsed++;
                }
            }

            if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.L)) {
                game.setScreen(new LoadScreen(game, this));
                return false;
            }
        }

        int healthBefore = player.getHealth();
        boolean wasDead = player.isDead;

        world.step(1 / 60f, 6, 2);

        // --- LEVEL 3 TERMINATION (Contingency Ending) ---
        if (player.isDead && levelNumber == 3 && isPostBattle) {
            System.out.println("GAME OVER. BATMAN WINS.");
            Gdx.app.exit();
            return false;
        }

        player.UpdatePlayer(delta, world, inCutscene);

        // --- FLAG TRANSITION ---
        if (!inCutscene && playableMap.isFlagReached()) {
            game.setScreen(new PlayScreen(game, levelNumber + 1));
            this.dispose();
            return false;
        }

        // Standard Respawns
        if (wasDead && !player.isDead) {
            playableMap.resetTriggers(world);
        }

        // Damage Effects
        if (!player.isDead && player.getHealth() < healthBefore) {
            int damageTaken = healthBefore - player.getHealth();
            cam.startShake(damageTaken >= 25 ? 0.4f : 0.2f, damageTaken >= 25 ? 0.6f : 0.4f);
        }

        if (player.isDead) {
            cam.SetDeathTarget(worldWidth, worldHeight, halfVW, halfVH, player.spawnX, player.spawnY);
        }

        if (currentCutscene == null) {
            cam.Update(worldWidth, worldHeight, halfVW, halfVH, player.GetXpos(), player.GetYpos());
        } else {
            cam.GetCam().update();
        }

        playableMap.UpdateMap(cam.GetCam(), delta, world, player);
        return true;
    }

    private void handleDebugInput() {
        if ((Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
            && Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            DebugOption = !DebugOption;
        }
    }

    public void executeLoad(String saveName) {
        String playerSaveFile = "SavedFiles/" + saveName + ".json";
        fileHandler.loadGameState(player, playableMap, playerSaveFile);
        EnterNameScreen.globalPlayerName = saveName;
    }

    private void startLevelMusic() {
        Music currentTrack;

        switch (levelNumber) {
            case 0:  currentTrack = AudioManager.level1Music; break;
            case 1:  currentTrack = AudioManager.level1Music;   break;
            case 2:  currentTrack = AudioManager.level2Music;   break;
            case 3:  currentTrack = AudioManager.bossArenaMusic; break;
            default: currentTrack = AudioManager.level1Music;   break;
        }

        if (currentTrack != AudioManager.elevatorMusic) AudioManager.elevatorMusic.stop();
        if (currentTrack != AudioManager.level1Music) AudioManager.level1Music.stop();
        if (currentTrack != AudioManager.level2Music) AudioManager.level2Music.stop();
        if (currentTrack != AudioManager.bossArenaMusic) AudioManager.bossArenaMusic.stop();

        currentTrack.setLooping(true);
        if(levelNumber == 0 || levelNumber == 3)
            currentTrack.setVolume(0.05f);
        else
            currentTrack.setVolume(0.15f);

        currentTrack.play();
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
    public void show() {}

    @Override
    public void dispose() {
        playableMap.dispose();
        player.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
        if (world != null) world.dispose();
    }

    public Player getPlayer() { return player; }
    public GameCam getCam() { return cam; }
    public PlayableMap getPlayableMap() { return playableMap; }
}
