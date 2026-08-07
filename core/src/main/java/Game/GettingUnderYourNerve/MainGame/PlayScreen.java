package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Cutscenes.*;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameCam;
import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Map.PlayableMap;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.WorldContactListener;
import Game.GettingUnderYourNerve.Utilities.FileHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PlayScreen implements Screen {

    private Main game;

    private String playerName;
    private int slotIndex;
    private float startX;
    private float startY;

    private World world;
    private Box2DDebugRenderer debugRenderer;

    private Player player;
    private PlayableMap playableMap;

    private Viewport viewport;
    private GameCam cam;
    private Viewport uiViewport;
    private Matrix4 screenMatrix = new Matrix4();

    private boolean DebugOption = false;
    private float WORLD_WIDTH;
    private float WORLD_HEIGHT;

    private BaseCutscene currentCutscene;
    private int levelNumber;
    private boolean isPostBattle = false;

    private Texture whitePixel;
    private float flashAlpha = 0f;
    private boolean isLevelCompleting = false;
    private float completionTimer = 0f;
    private static final float TOTAL_COMPLETION_TIME = 2.5f;

    private float minigameFlashAlpha = 0f;
    private boolean isMinigameTransitioning = false;

    private float lastPlayerX = 0f;
    private float lastPlayerY = 0f;
    private int lastPlayerHealth = -1;

    private BitmapFont completeFont;
    private GlyphLayout completeLayout;

    Music currentTrack = null;

    public PlayScreen(Main game, String playerName, int slotIndex, float startX, float startY, int levelNumber) {
        this(game, playerName, slotIndex, startX, startY, levelNumber, false);
    }

    public PlayScreen(Main game, String playerName, int slotIndex, float startX, float startY, int levelNumber, boolean isPostBattle) {
        this.game = game;
        this.playerName = playerName;
        this.slotIndex = slotIndex;
        this.startX = startX;
        this.startY = startY;
        this.levelNumber = levelNumber;
        this.isPostBattle = isPostBattle;

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
        uiViewport = new FitViewport(800, 480);

        completeLayout = new GlyphLayout();
        completeFont = loadFont("ui/runescape_uf.ttf", 38);

        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fill();
        whitePixel = new Texture(pix);
        pix.dispose();

        playableMap.createPhysicsFromMap(world);
        contactListener.setPlayableMap(playableMap);

        player.SpawnPlayerFromTiled(playableMap.GetMap(), world);
        player.setPlayerData(this.playerName, this.slotIndex, this.startX, this.startY, this.levelNumber);

        lastPlayerX = player.GetXpos();
        lastPlayerY = player.GetYpos();
        lastPlayerHealth = player.getHealth();

        cam.setPosition(player.GetXpos(), player.GetYpos());
        cam.GetCam().update();

        boolean isLoadedCheckpoint = (this.startX > 0f || this.startY > 0f)
            && (Math.abs(this.startX - player.spawnX) > 0.1f || Math.abs(this.startY - player.spawnY) > 0.1f);

        if (this.levelNumber == 0) {
            currentCutscene = new PrologueCutscene(this);
        } else if (this.levelNumber == 3 && !this.isPostBattle) {
            currentCutscene = new BossCutscene(this, playableMap.getBatman());
        } else if (this.levelNumber == 3 && this.isPostBattle) {
            currentCutscene = new AvengersCutscene(this, playableMap.getBatman());
        } else {
            if (isLoadedCheckpoint) {
                currentCutscene = null;
            } else {
                currentCutscene = new IntroEncounter(this, playableMap.getBatman());
            }
        }

        startLevelMusic();

        if (currentCutscene == null) {
            float worldWidth = playableMap.getMapWidthInMeters();
            float worldHeight = playableMap.getMapHeightInMeters();
            float halfVW = (viewport.getWorldWidth()) / 2f;
            float halfVH = (viewport.getWorldHeight()) / 2f;
            cam.Update(worldWidth, worldHeight, halfVW, halfVH, player.GetXpos(), player.GetYpos());
        }
    }

    private BitmapFont loadFont(String filename, int size) {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(filename));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = size;
            p.color = new Color(1.0f, 0.85f, 0.0f, 1.0f);
            p.borderWidth = 2.5f;
            p.borderColor = new Color(0f, 0f, 0f, 0.9f);
            BitmapFont f = gen.generateFont(p);
            gen.dispose();
            return f;
        } catch (Exception e) {
            return new BitmapFont();
        }
    }

    public Main getGame() { return game; }

    public boolean isInCutscene() {
        return currentCutscene != null;
    }

    public Music getCurrentTrack() {
        return currentTrack;
    }

    public void updateCurrentTrackVolume(float volume) {
        if (currentTrack != null) {
            if (volume > 0f) {
                if (!currentTrack.isPlaying()) {
                    currentTrack.play();
                }
                currentTrack.setVolume(volume);
            } else {
                currentTrack.pause();
            }
        }
    }

    public void startPokemonBattle() {
        AudioManager.elevatorMusic.pause();
        AudioManager.level1Music.pause();
        AudioManager.level2Music.pause();
        AudioManager.bossArenaMusic.pause();

        game.setScreen(new PokemonBattleScreen(game, playerName, slotIndex, startX, startY, this.levelNumber));
    }

    public World getWorld() { return world; }

    public void drawWorld(float delta) {
        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1);
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

        float currentFlashAlpha = Math.max(flashAlpha, minigameFlashAlpha);
        if (currentFlashAlpha > 0f && whitePixel != null) {
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            screenMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.batch.setProjectionMatrix(screenMatrix);
            game.batch.begin();

            Color c = game.batch.getColor().cpy();
            game.batch.setColor(1f, 1f, 1f, currentFlashAlpha);
            game.batch.draw(whitePixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.batch.setColor(c);

            game.batch.end();
        }

        if (flashAlpha > 0f && completeFont != null) {
            uiViewport.apply();
            game.batch.setProjectionMatrix(uiViewport.getCamera().combined);
            game.batch.begin();

            String completeText = "LEVEL " + levelNumber + " COMPLETED!";
            completeLayout.setText(completeFont, completeText);

            float fontX = (uiViewport.getWorldWidth() - completeLayout.width) / 2f;
            float fontY = (uiViewport.getWorldHeight() + completeLayout.height) / 2f;

            completeFont.draw(game.batch, completeText, fontX, fontY);

            game.batch.end();
        }

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

        if (currentTrack != null && !isLevelCompleting) {
            float userVolume = Gdx.app.getPreferences("GettingUnderYourNerve_Settings").getFloat("musicVolume", 0.5f);
            if (userVolume <= 0f) {
                if (currentTrack.isPlaying()) {
                    currentTrack.pause();
                }
            } else {
                currentTrack.setVolume(inCutscene ? userVolume * 0.3f : userVolume);
            }
        }

        float worldWidth = playableMap.getMapWidthInMeters();
        float worldHeight = playableMap.getMapHeightInMeters();
        float halfVW = (viewport.getWorldWidth()) / 2f;
        float halfVH = (viewport.getWorldHeight()) / 2f;

        if (inCutscene) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                currentCutscene.skip();
            }
            if (!currentCutscene.isFinished()) {
                currentCutscene.update(delta);
            }

            if (levelNumber == 0) {
                cam.GetCam().position.x = MathUtils.clamp(cam.GetCam().position.x, halfVW, Math.max(halfVW, worldWidth - halfVW));
                cam.GetCam().position.y = MathUtils.clamp(cam.GetCam().position.y, halfVH, Math.max(halfVH, worldHeight - halfVH));
            }

            if (currentCutscene.isFinished()) {
                if (levelNumber == 0) {
                    game.setScreen(new PlayScreen(game, playerName, slotIndex, 0, 0, 1));
                    this.dispose();
                    return false;
                }

                cam.setPosition(cam.GetCam().position.x, cam.GetCam().position.y);
                currentCutscene = null;
                inCutscene = false;
            }
        }

        boolean wasDead = player.isDead;

        if (!isLevelCompleting && !isMinigameTransitioning) {
            world.step(1 / 60f, 6, 2);
        }

        if (player.isDead && levelNumber == 3 && isPostBattle) {
            if (currentTrack != null) currentTrack.stop();
            if (AudioManager.bossArenaMusic != null) AudioManager.bossArenaMusic.stop();

            if (playerName != null && !playerName.trim().isEmpty() && player != null) {
                FileHandler.saveScore(playerName, player.getScore());
            }

            game.setScreen(new EndCreditsScreen(game));
            this.dispose();
            return false;
        }

        player.UpdatePlayer(delta, world, inCutscene || isLevelCompleting || isMinigameTransitioning);

        float currX = player.GetXpos();
        float currY = player.GetYpos();
        int currHealth = player.getHealth();

        boolean respawnedByTeleport = (Math.abs(currX - lastPlayerX) > 5f || Math.abs(currY - lastPlayerY) > 5f) && currHealth >= lastPlayerHealth;
        boolean respawnedByHealth = (lastPlayerHealth <= 0 && currHealth > 0);

        if ((wasDead && !player.isDead) || respawnedByTeleport || respawnedByHealth) {
            playableMap.resetTriggers(world);
        }

        lastPlayerX = currX;
        lastPlayerY = currY;
        lastPlayerHealth = currHealth;

        if (!inCutscene && (playableMap.isFlagReached() || isLevelCompleting)) {
            isLevelCompleting = true;
            completionTimer += delta;

            if (player != null && player.getPlayerBody() != null) {
                player.getPlayerBody().setLinearVelocity(0, 0);
            }

            if (currentTrack != null && currentTrack.isPlaying()) {
                float userVol = Gdx.app.getPreferences("GettingUnderYourNerve_Settings").getFloat("musicVolume", 0.5f);
                float fadeRatio = Math.max(0f, 1.0f - (completionTimer / 1.0f));
                currentTrack.setVolume(userVol * fadeRatio);
                if (fadeRatio <= 0f) {
                    currentTrack.pause();
                }
            }

            flashAlpha = Math.min(1.0f, completionTimer / 0.5f);

            if (completionTimer >= TOTAL_COMPLETION_TIME) {
                int nextLevel = levelNumber + 1;
                if (player != null) player.saveSlotIndex = -1;
                if (world != null) world.setContactListener(null);

                if (slotIndex >= 0) {
                    FileHandler.saveSlot(slotIndex, playerName, 0, 0, nextLevel);
                }

                if (playerName != null && !playerName.trim().isEmpty() && player != null) {
                    FileHandler.saveScore(playerName, player.getScore());
                }

                game.setScreen(new PlayScreen(game, playerName, slotIndex, 0, 0, nextLevel));
                this.dispose();
                return false;
            }
        }

        if (!player.isDead && player.getHealth() < lastPlayerHealth) {
            int damageTaken = lastPlayerHealth - player.getHealth();
            cam.startShake(damageTaken >= 25 ? 0.4f : 0.2f, damageTaken >= 25 ? 0.6f : 0.4f);
        }

        if (player.isDead) {
            cam.SetDeathTarget(worldWidth, worldHeight, halfVW, halfVH, player.checkpointX, player.checkpointY);
        }

        if (currentCutscene == null) {
            if (levelNumber == 3 && isPostBattle) {
                cam.GetCam().update();
            } else {
                cam.Update(worldWidth, worldHeight, halfVW, halfVH, player.GetXpos(), player.GetYpos());
            }
        } else {
            cam.GetCam().update();
        }

        if (!isLevelCompleting && !isMinigameTransitioning) {
            playableMap.UpdateMap(cam.GetCam(), delta, world, player);
        }

        if (playableMap.pendingMinigameMatch || isMinigameTransitioning) {
            playableMap.pendingMinigameMatch = false;
            isMinigameTransitioning = true;
            minigameFlashAlpha = Math.min(1.0f, minigameFlashAlpha + (delta * 2.5f));

            if (minigameFlashAlpha >= 1.0f) {
                if (currentTrack != null) currentTrack.pause();
                isMinigameTransitioning = false;
                minigameFlashAlpha = 0f;

                game.setScreen(new FootballMinigameScreen(game, this));
                return false;
            }
        }

        return true;
    }

    private void handleDebugInput() {
        boolean ctrlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
        boolean shiftPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        boolean altPressed = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);

        if (ctrlPressed && shiftPressed && altPressed && Gdx.input.isKeyJustPressed(Input.Keys.D)) {
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

        if (AudioManager.elevatorMusic != null) AudioManager.elevatorMusic.stop();
        if (AudioManager.level1Music != null) AudioManager.level1Music.stop();
        if (AudioManager.level2Music != null) AudioManager.level2Music.stop();
        if (AudioManager.bossArenaMusic != null) AudioManager.bossArenaMusic.stop();

        if (currentTrack != null) {
            float userVolume = Gdx.app.getPreferences("GettingUnderYourNerve_Settings").getFloat("musicVolume", 0.5f);
            boolean inCutscene = (currentCutscene != null);

            currentTrack.setLooping(true);
            currentTrack.setVolume(inCutscene ? userVolume * 0.3f : userVolume);

            if (userVolume > 0f) {
                currentTrack.play();
            } else {
                currentTrack.pause();
            }
        }
    }

    public int getSlotIndex() { return slotIndex; }

    public void increaseLevelAudio(float volume) {
        if (currentTrack != null) currentTrack.setVolume(volume);
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
        if (currentTrack != null) {
            currentTrack.stop();
        }
        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
        if (completeFont != null) {
            completeFont.dispose();
            completeFont = null;
        }
        playableMap.dispose();
        player.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
        if (world != null) world.dispose();
    }

    public Player getPlayer() { return player; }
    public GameCam getCam() { return cam; }
    public PlayableMap getPlayableMap() { return playableMap; }
}
