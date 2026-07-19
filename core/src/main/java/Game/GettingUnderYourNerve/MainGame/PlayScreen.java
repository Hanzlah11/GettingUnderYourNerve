package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Cutscenes.*;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameCam;
import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Map.PlayableMap;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.WorldContactListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PlayScreen implements Screen {

    private Main game;

    // --- Save Slot Data ---
    private String playerName;
    private int slotIndex;
    private float startX;
    private float startY;

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

    Music currentTrack = null;

    // Updated Constructor to receive save data
    public PlayScreen(Main game, String playerName, int slotIndex, float startX, float startY, int levelNumber) {
        this(game, playerName, slotIndex, startX, startY, levelNumber, false);
        startLevelMusic();
    }

    public PlayScreen(Main game, String playerName, int slotIndex, float startX, float startY, int levelNumber, boolean isPostBattle) {
        this.game = game;
        this.playerName = playerName;
        this.slotIndex = slotIndex;
        this.startX = startX;
        this.startY = startY;
        this.levelNumber = levelNumber;
        this.isPostBattle = isPostBattle;

        startLevelMusic();

        this.WORLD_WIDTH  = (levelNumber == 0) ? 400 : 800;
        this.WORLD_HEIGHT = (levelNumber == 0) ? 240 : 480;

        world = new World(new Vector2(0, -40f), true);

        WorldContactListener contactListener = new WorldContactListener();
        world.setContactListener(contactListener);

        debugRenderer = new Box2DDebugRenderer();

        player      = new Player(20, game.assets);
        playableMap = new PlayableMap(game.assets, levelNumber);

        cam        = new GameCam();
        viewport   = new ExtendViewport(WORLD_WIDTH / Main.PPM, WORLD_HEIGHT / Main.PPM, cam.GetCam());
        uiViewport = new ExtendViewport(800, 480);

        playableMap.createPhysicsFromMap(world);
        contactListener.setPlayableMap(playableMap);

        // Spawn player normally from Tiled
        player.SpawnPlayerFromTiled(playableMap.GetMap(), world);

        // Immediately overwrite coordinates with Save Data (if X/Y are not 0)
        player.setPlayerData(this.playerName, this.slotIndex, this.startX, this.startY, this.levelNumber);

        // --- LEVEL TRIGGER LOGIC ---
        if (this.levelNumber == 0) {
            currentCutscene = new PrologueCutscene(this);
        } else if (this.levelNumber == 3 && !this.isPostBattle) {
            currentCutscene = new BossCutscene(this, playableMap.getBatman());
        } else if (this.levelNumber == 3 && this.isPostBattle) {
            currentCutscene = new AvengersCutscene(this, playableMap.getBatman());
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

        game.setScreen(new PokemonBattleScreen(game, playerName, slotIndex, startX, startY, this.levelNumber));
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

            if (levelNumber == 0) {
                cam.GetCam().position.x = com.badlogic.gdx.math.MathUtils.clamp(
                    cam.GetCam().position.x, halfVW, worldWidth - halfVW);
                cam.GetCam().position.y = com.badlogic.gdx.math.MathUtils.clamp(
                    cam.GetCam().position.y, halfVH, worldHeight - halfVH);
            }

            if (currentCutscene.isFinished()) {
                if (levelNumber == 0) {
                    // Update next level transition with save variables
                    game.setScreen(new PlayScreen(game, playerName, slotIndex, 0, 0, 1));
                    this.dispose();
                    return false;
                }

                cam.setPosition(cam.GetCam().position.x, cam.GetCam().position.y);
                currentCutscene = null;
                inCutscene = false;
            }
        }

        int healthBefore = player.getHealth();
        world.step(1 / 60f, 6, 2);
        boolean wasDead = player.isDead;

        if (player.isDead && levelNumber == 3 && isPostBattle) {
            System.out.println("GAME OVER. BATMAN WINS.");
            Gdx.app.exit();
            return false;
        }

        player.UpdatePlayer(delta, world, inCutscene);

        if (!inCutscene && playableMap.isFlagReached()) {
            // Update next level transition with save variables
            game.setScreen(new PlayScreen(game, playerName, slotIndex, 0, 0, levelNumber + 1));
            this.dispose();
            return false;
        }

        if (wasDead && !player.isDead) {
            playableMap.resetTriggers(world);
        }

        if (!player.isDead && player.getHealth() < healthBefore) {
            int damageTaken = healthBefore - player.getHealth();
            cam.startShake(damageTaken >= 25 ? 0.4f : 0.2f, damageTaken >= 25 ? 0.6f : 0.4f);
        }

        if (player.isDead) {
            // Updated death logic to rely on the Player's checkpoint variables
            cam.SetDeathTarget(worldWidth, worldHeight, halfVW, halfVH, player.checkpointX, player.checkpointY);
        }

        if (currentCutscene == null) {
            cam.Update(worldWidth, worldHeight, halfVW, halfVH, player.GetXpos(), player.GetYpos());
        } else {
            cam.GetCam().update();
        }

        playableMap.UpdateMap(cam.GetCam(), delta, world, player);

        // --- FOOTBALL ENCOUNTER CAPTURE ---
        if (playableMap.pendingMinigameMatch) {
            playableMap.pendingMinigameMatch = false; // Reset map event flag
            if (currentTrack != null) currentTrack.pause(); // Pause level music tracks cleanly[cite: 9]

            // Swap screens and save current platformer context to return later[cite: 8]
            game.setScreen(new FootballMinigameScreen(game, this));
            return false; // Halts additional logic updates on this frame instance[cite: 9]
        }

        return true;
    }

    private void handleDebugInput() {
        if ((Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
            && Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            DebugOption = !DebugOption;
        }
    }

    private void startLevelMusic() {
        switch (levelNumber) {
            case 1:  currentTrack = AudioManager.level1Music;   break;
            case 2:  currentTrack = AudioManager.level2Music;   break;
            case 3:  currentTrack = AudioManager.bossArenaMusic; break;
            default: currentTrack = null;   break;
        }

        if (currentTrack != AudioManager.elevatorMusic) AudioManager.elevatorMusic.stop();
        if (currentTrack != AudioManager.level1Music) AudioManager.level1Music.stop();
        if (currentTrack != AudioManager.level2Music) AudioManager.level2Music.stop();
        if (currentTrack != AudioManager.bossArenaMusic) AudioManager.bossArenaMusic.stop();

        if (currentTrack != null) {
            currentTrack.setLooping(true);
            currentTrack.setVolume(0.15f);
            currentTrack.play();
        }
    }

    public void increaseLevelAudio(float volume) {
        if(currentTrack != null)
            currentTrack.setVolume(volume);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        uiViewport.update(width, height, true);
    }

    @Override public void pause()  { }
    @Override public void resume() { }
    @Override public void hide()   { }
    @Override public void show() {}

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
