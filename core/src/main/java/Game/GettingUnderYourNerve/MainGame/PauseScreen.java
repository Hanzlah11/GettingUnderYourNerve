package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.SettingsScreen;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PauseScreen implements Screen {

    private final Main game;
    private final PlayScreen playScreen;
    private final int health, score;

    private Viewport uiViewport;
    private static final float BOARD_WIDTH    = 290f;
    private static final float BOARD_HEIGHT   = 290f;
    private static final float BOARD_CORNER   = 32f;
    private static final float BOARD_EDGE_H   = 32f;
    private static final float BOARD_EDGE_V   = 32f;
    private static final float BOARD_OFFSET_Y = 25f;

    private static final float BTN_CORNER_W   = 20f;
    private static final float BTN_HEIGHT     = 32f;
    private static final float BTN_WIDTH      = 210f;
    private static final float BTN_GAP        = 6f;

    private Texture boardTL, boardTC, boardTR, boardCL, boardCC, boardCR, boardBL, boardBC, boardBR;
    private Texture btnL, btnC, btnR;
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout layout;

    private Rectangle resumeRect     = new Rectangle();
    private Rectangle settingsRect   = new Rectangle();
    private Rectangle helpRect       = new Rectangle();
    private Rectangle cheatsRect     = new Rectangle();
    private Rectangle checkpointRect = new Rectangle();
    private Rectangle titleScreenRect= new Rectangle();
    private Vector3 touchVec         = new Vector3();

    private boolean isRickrolling = false;
    private float rickTimer = 0;
    private Animation<TextureRegion> rickAnim;

    private final HudBanner hudBanner = new HudBanner();

    public PauseScreen(Main game, PlayScreen playScreen, int health, int score) {
        AudioManager.pauseAll();
        this.game = game;
        this.playScreen = playScreen;
        this.health = health;
        this.score = score;
        this.layout = new GlyphLayout();
        this.uiViewport = new ExtendViewport(800, 480);

        loadAssets(game.assets);
        recalcLayout();
    }

    public PlayScreen getPlayScreen() {
        return playScreen;
    }

    private void loadAssets(GameAssetManager assets) {
        boardTL = assets.manager.get(GameAssetManager.BOARD_TL, Texture.class);
        boardTC = assets.manager.get(GameAssetManager.BOARD_TC, Texture.class);
        boardTR = assets.manager.get(GameAssetManager.BOARD_TR, Texture.class);
        boardCL = assets.manager.get(GameAssetManager.BOARD_CL, Texture.class);
        boardCC = assets.manager.get(GameAssetManager.BOARD_CC, Texture.class);
        boardCR = assets.manager.get(GameAssetManager.BOARD_CR, Texture.class);
        boardBL = assets.manager.get(GameAssetManager.BOARD_BL, Texture.class);
        boardBC = assets.manager.get(GameAssetManager.BOARD_BC, Texture.class);
        boardBR = assets.manager.get(GameAssetManager.BOARD_BR, Texture.class);

        btnL = assets.manager.get(GameAssetManager.BUTTON_L, Texture.class);
        btnC = assets.manager.get(GameAssetManager.BUTTON_C, Texture.class);
        btnR = assets.manager.get(GameAssetManager.BUTTON_R, Texture.class);

        font      = loadFont("ui/runescape_uf.ttf", 18);
        titleFont = loadFont("ui/runescape_uf.ttf", 26);

        Texture sheet = assets.manager.get(GameAssetManager.RICK_SHEET, Texture.class);
        int frameWidth = 240;
        int frameHeight = 135;
        int cropX = 30;
        int cropWidth = 180;

        Array<TextureRegion> frames = new Array<>();
        int count = 0;
        int rows = sheet.getHeight() / frameHeight;
        int cols = sheet.getWidth() / frameWidth;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (count++ < 84) {
                    TextureRegion croppedFrame = new TextureRegion(
                        sheet,
                        c * frameWidth + cropX,
                        r * frameHeight,
                        cropWidth,
                        frameHeight
                    );
                    frames.add(croppedFrame);
                }
            }
        }
        rickAnim = new Animation<>(1/12f, frames);
        hudBanner.loadAssets(assets);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (isRickrolling) {
                stopRickroll();
            } else {
                resumeGame();
            }
            return;
        }

        playScreen.drawWorld(delta);

        uiViewport.apply();
        game.batch.setProjectionMatrix(uiViewport.getCamera().combined);

        float worldW = uiViewport.getWorldWidth();
        float worldH = uiViewport.getWorldHeight();

        float bx = (worldW - BOARD_WIDTH) / 2f;
        float by = (worldH - BOARD_HEIGHT) / 2f - BOARD_OFFSET_Y;

        float innerW = BOARD_WIDTH - BOARD_CORNER * 2;
        float innerH = BOARD_HEIGHT - BOARD_CORNER * 2;

        game.batch.begin();

        drawTex(game.batch, boardCC, bx + BOARD_CORNER, by + BOARD_CORNER, innerW, innerH);

        if (isRickrolling) {
            rickTimer += delta;
            TextureRegion frame = rickAnim.getKeyFrame(rickTimer);
            game.batch.draw(frame, bx + BOARD_CORNER, by + BOARD_CORNER, innerW, innerH);

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || rickAnim.isAnimationFinished(rickTimer)) {
                stopRickroll();
            }
        } else {
            handleMenuInput();
            drawButton(game.batch, resumeRect, "Resume", true);
            drawButton(game.batch, settingsRect, "Settings", true);
            drawButton(game.batch, helpRect, "Need Some Help?", true);

            boolean inCutscene = playScreen.isInCutscene();
            drawButton(game.batch, cheatsRect, "Cheat Codes", !inCutscene);
            drawButton(game.batch, checkpointRect, "Last Checkpoint", !inCutscene);

            drawButton(game.batch, titleScreenRect, "Title Screen", true);
        }

        drawTex(game.batch, boardTL, bx,                              by + BOARD_HEIGHT - BOARD_CORNER, BOARD_CORNER, BOARD_CORNER);
        drawTex(game.batch, boardTC, bx + BOARD_CORNER,               by + BOARD_HEIGHT - BOARD_EDGE_H,  innerW,       BOARD_EDGE_H);
        drawTex(game.batch, boardTR, bx + BOARD_WIDTH - BOARD_CORNER, by + BOARD_HEIGHT - BOARD_CORNER, BOARD_CORNER, BOARD_CORNER);
        drawTex(game.batch, boardCL, bx,                              by + BOARD_CORNER,                 BOARD_EDGE_V, innerH);
        drawTex(game.batch, boardCR, bx + BOARD_WIDTH - BOARD_EDGE_V, by + BOARD_CORNER,                 BOARD_EDGE_V, innerH);
        drawTex(game.batch, boardBL, bx,                              by,                                BOARD_CORNER, BOARD_CORNER);
        drawTex(game.batch, boardBC, bx + BOARD_CORNER,               by,                                innerW,       BOARD_EDGE_H);
        drawTex(game.batch, boardBR, bx + BOARD_WIDTH - BOARD_CORNER, by,                                BOARD_CORNER, BOARD_CORNER);

        game.batch.end();

        hudBanner.attachToBoard(bx - 160, by + 10, BOARD_HEIGHT);
        hudBanner.render(game.batch, health, score, uiViewport.getCamera().combined);
    }

    private void resumeGame() {
        stopRickroll();
        AudioManager.resumeAll();

        float musicVol = Gdx.app.getPreferences("GettingUnderYourNerve_Settings").getFloat("musicVolume", 0.5f);
        if (playScreen != null && playScreen.getPlayableMap() != null) {
            playScreen.updateCurrentTrackVolume(musicVol);
        }

        game.setScreen(playScreen);
    }

    private void stopRickroll() {
        isRickrolling = false;
        rickTimer = 0;
        if (AudioManager.rickMusic != null && AudioManager.rickMusic.isPlaying()) {
            AudioManager.rickMusic.stop();
        }
    }

    private void handleMenuInput() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        uiViewport.unproject(touchVec);

        if (resumeRect.contains(touchVec.x, touchVec.y)) {
            AudioManager.playSFX(AudioManager.buttonSound);
            resumeGame();
        } else if (settingsRect.contains(touchVec.x, touchVec.y)) {
            AudioManager.playSFX(AudioManager.buttonSound);
            game.setScreen(new SettingsScreen(game, this));
        } else if (helpRect.contains(touchVec.x, touchVec.y)) {
            AudioManager.playSFX(AudioManager.buttonSound);
            stopRickroll();
            Gdx.net.openURI("https://www.jellybyteofficial.me/HowToPlayGettingUnderYourNerve");
        } else if (cheatsRect.contains(touchVec.x, touchVec.y)) {
            if (playScreen.isInCutscene()) {
                return;
            }
            AudioManager.playSFX(AudioManager.buttonSound);
            isRickrolling = true;
            rickTimer = 0;

            float userVolume = Gdx.app.getPreferences("GettingUnderYourNerve_Settings").getFloat("musicVolume", 0.5f);
            AudioManager.rickMusic.setVolume(userVolume > 0f ? userVolume : 0.8f);
            AudioManager.rickMusic.play();
        } else if (checkpointRect.contains(touchVec.x, touchVec.y)) {
            if (playScreen.isInCutscene()) {
                return;
            }
            AudioManager.playSFX(AudioManager.buttonSound);
            playScreen.getPlayer().Respawn();
            resumeGame();
            dispose();
        } else if (titleScreenRect.contains(touchVec.x, touchVec.y)) {
            AudioManager.playSFX(AudioManager.buttonSound);
            stopRickroll();
            if (playScreen != null) {
                playScreen.dispose();
            }
            AudioManager.syncMusicVolume();
            game.setScreen(new TitleScreen(game));
            dispose();
        }
    }

    private void drawButton(SpriteBatch batch, Rectangle rect, String label, boolean enabled) {
        Color origColor = batch.getColor().cpy();
        if (!enabled) {
            batch.setColor(0.5f, 0.5f, 0.5f, 0.7f);
        }

        drawTex(batch, btnL, rect.x, rect.y, BTN_CORNER_W, rect.height);
        drawTex(batch, btnC, rect.x + BTN_CORNER_W, rect.y, rect.width - BTN_CORNER_W * 2, rect.height);
        drawTex(batch, btnR, rect.x + rect.width - BTN_CORNER_W, rect.y, BTN_CORNER_W, rect.height);

        layout.setText(font, label);
        if (!enabled) font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, label, rect.x + (rect.width - layout.width)/2f, rect.y + (rect.height + layout.height)/2f);
        if (!enabled) font.setColor(Color.WHITE);

        batch.setColor(origColor);
    }

    private void drawTex(SpriteBatch batch, Texture tex, float x, float y, float w, float h) {
        if (tex != null) batch.draw(tex, x, y, w, h);
    }

    private void recalcLayout() {
        float worldW = uiViewport.getWorldWidth();
        float worldH = uiViewport.getWorldHeight();

        float bx = (worldW - BOARD_WIDTH) / 2f;
        float by = (worldH - BOARD_HEIGHT) / 2f - BOARD_OFFSET_Y;

        float totalBtns = (BTN_HEIGHT * 6) + (BTN_GAP * 5);
        float startY = by + (BOARD_HEIGHT - totalBtns) / 2f;
        float btnX = bx + (BOARD_WIDTH - BTN_WIDTH) / 2f;

        titleScreenRect.set(btnX, startY, BTN_WIDTH, BTN_HEIGHT);
        checkpointRect.set(btnX, startY + (BTN_HEIGHT + BTN_GAP), BTN_WIDTH, BTN_HEIGHT);
        cheatsRect.set(btnX, startY + (BTN_HEIGHT + BTN_GAP) * 2f, BTN_WIDTH, BTN_HEIGHT);
        helpRect.set(btnX, startY + (BTN_HEIGHT + BTN_GAP) * 3f, BTN_WIDTH, BTN_HEIGHT);
        settingsRect.set(btnX, startY + (BTN_HEIGHT + BTN_GAP) * 4f, BTN_WIDTH, BTN_HEIGHT);
        resumeRect.set(btnX, startY + (BTN_HEIGHT + BTN_GAP) * 5f, BTN_WIDTH, BTN_HEIGHT);
    }

    private BitmapFont loadFont(String filename, int size) {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(filename));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size        = size;
            p.color       = Color.WHITE;
            p.borderWidth = 1.5f;
            p.borderColor = new Color(0f, 0f, 0f, 0.6f);
            BitmapFont f  = gen.generateFont(p);
            gen.dispose();
            return f;
        } catch (Exception e) {
            Gdx.app.error("PauseMenu", "Font load failed: " + e.getMessage());
            return new BitmapFont();
        }
    }

    @Override public void show() { }

    @Override
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
        playScreen.resize(width, height);
        recalcLayout();
    }

    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        hudBanner.dispose();
    }
}
