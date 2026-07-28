package Game.GettingUnderYourNerve;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.MainGame.PauseScreen;
import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.MainGame.TitleScreen;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SettingsScreen implements Screen {

    private Main game;
    private Screen parentScreen; // Generically supports TitleScreen, PlayScreen, or PauseScreen
    private Viewport viewport;
    private Vector3 touchVec;

    // --- Textures ---
    private Texture boardTL, boardTC, boardTR, boardCL, boardCC, boardCR, boardBL, boardBC, boardBR;
    private Texture btnL, btnC, btnR;

    // --- Fonts & Layout ---
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout layout;

    // --- UI Layout Bounds ---
    private Rectangle musicSliderBar;
    private Rectangle sfxSliderBar;
    private Rectangle subtitleBtnRect;
    private Rectangle backBtnRect;

    // --- Settings State ---
    private float musicVolume;
    private float sfxVolume;
    private boolean subtitlesEnabled;
    private boolean isDraggingMusic = false;
    private boolean isDraggingSFX = false;

    private Preferences prefs;

    public SettingsScreen(Main game, Screen parentScreen) {
        this.game = game;
        this.parentScreen = parentScreen;
        this.viewport = new ExtendViewport(800, 480);
        this.touchVec = new Vector3();
        this.layout = new GlyphLayout();

        // Pull settings from persistent Preferences config file
        prefs = Gdx.app.getPreferences("GettingUnderYourNerve_Settings");
        musicVolume = prefs.getFloat("musicVolume", 0.5f);
        sfxVolume = prefs.getFloat("sfxVolume", 0.5f);
        subtitlesEnabled = prefs.getBoolean("subtitlesEnabled", true);

        loadAssets();

        // Initialize UI bounding boxes
        musicSliderBar  = new Rectangle();
        sfxSliderBar    = new Rectangle();
        subtitleBtnRect = new Rectangle();
        backBtnRect     = new Rectangle();

        recalcLayout();
    }

    private void recalcLayout() {
        float worldW = viewport.getWorldWidth();
        float centerX = worldW / 2f;

        musicSliderBar.set(centerX - 30f, 290f, 200f, 10f);
        sfxSliderBar.set(centerX - 30f, 220f, 200f, 10f);
        subtitleBtnRect.set(centerX - 30f, 145f, 140f, 38f);
        backBtnRect.set(centerX - 80f, 75f, 160f, 40f);
    }

    private void loadAssets() {
        boardTL = game.assets.manager.get(GameAssetManager.BOARD_TL, Texture.class);
        boardTC = game.assets.manager.get(GameAssetManager.BOARD_TC, Texture.class);
        boardTR = game.assets.manager.get(GameAssetManager.BOARD_TR, Texture.class);
        boardCL = game.assets.manager.get(GameAssetManager.BOARD_CL, Texture.class);
        boardCC = game.assets.manager.get(GameAssetManager.BOARD_CC, Texture.class);
        boardCR = game.assets.manager.get(GameAssetManager.BOARD_CR, Texture.class);
        boardBL = game.assets.manager.get(GameAssetManager.BOARD_BL, Texture.class);
        boardBC = game.assets.manager.get(GameAssetManager.BOARD_BC, Texture.class);
        boardBR = game.assets.manager.get(GameAssetManager.BOARD_BR, Texture.class);

        btnL = game.assets.manager.get(GameAssetManager.BUTTON_L, Texture.class);
        btnC = game.assets.manager.get(GameAssetManager.BUTTON_C, Texture.class);
        btnR = game.assets.manager.get(GameAssetManager.BUTTON_R, Texture.class);

        font = loadFont("ui/runescape_uf.ttf", 24);
        titleFont = loadFont("ui/runescape_uf.ttf", 36);
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (parentScreen instanceof TitleScreen) {
            ((TitleScreen) parentScreen).getMenuBg().updateAndRender(delta, game.batch);
        } else if (parentScreen instanceof PlayScreen) {
            ((PlayScreen) parentScreen).drawWorld(delta);
        } else if (parentScreen instanceof PauseScreen) {
            ((PauseScreen) parentScreen).getPlayScreen().drawWorld(delta);
        }

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        game.batch.begin();
        drawSettingsBoard();
        game.batch.end();
    }

    private void drawSettingsBoard() {
        float worldW = viewport.getWorldWidth();

        float bW = 600f;
        float bH = 380f;
        float corner = 32f;
        float bx = (worldW - bW) / 2f;
        float by = 55f;
        float inW = bW - corner * 2;
        float inH = bH - corner * 2;

        game.batch.draw(boardTL, bx, by + bH - corner, corner, corner);
        game.batch.draw(boardTC, bx + corner, by + bH - corner, inW, corner);
        game.batch.draw(boardTR, bx + bW - corner, by + bH - corner, corner, corner);
        game.batch.draw(boardCL, bx, by + corner, corner, inH);
        game.batch.draw(boardCC, bx + corner, by + corner, inW, inH);
        game.batch.draw(boardCR, bx + bW - corner, by + corner, corner, inH);
        game.batch.draw(boardBL, bx, by, corner, corner);
        game.batch.draw(boardBC, bx + corner, by, inW, corner);
        game.batch.draw(boardBR, bx + bW - corner, by, corner, corner);

        layout.setText(titleFont, "SETTINGS");
        titleFont.draw(game.batch, "SETTINGS", (worldW - layout.width) / 2f, by + bH - 30f);

        float labelX = bx + 50f;
        font.draw(game.batch, "MUSIC VOLUME:", labelX, 298f);
        game.batch.draw(btnC, musicSliderBar.x, musicSliderBar.y, musicSliderBar.width, musicSliderBar.height);
        float musicKnobX = musicSliderBar.x + (musicSliderBar.width * musicVolume) - 10f;
        game.batch.draw(btnC, musicKnobX, musicSliderBar.y - 10f, 20f, 30f);
        font.draw(game.batch, Math.round(musicVolume * 100) + "%", musicSliderBar.x + musicSliderBar.width + 20f, 298f);

        font.draw(game.batch, "SFX VOLUME:", labelX, 228f);
        game.batch.draw(btnC, sfxSliderBar.x, sfxSliderBar.y, sfxSliderBar.width, sfxSliderBar.height);
        float sfxKnobX = sfxSliderBar.x + (sfxSliderBar.width * sfxVolume) - 10f;
        game.batch.draw(btnC, sfxKnobX, sfxSliderBar.y - 10f, 20f, 30f);
        font.draw(game.batch, Math.round(sfxVolume * 100) + "%", sfxSliderBar.x + sfxSliderBar.width + 20f, 228f);

        font.draw(game.batch, "SUBTITLES:", labelX, 170f);
        if (subtitlesEnabled) {
            font.setColor(Color.GREEN);
            drawButton(subtitleBtnRect, "ON");
        } else {
            font.setColor(Color.RED);
            drawButton(subtitleBtnRect, "OFF");
        }
        font.setColor(Color.WHITE);

        drawButton(backBtnRect, "BACK");
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            returnToParent();
            return;
        }

        touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchVec);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (backBtnRect.contains(touchVec.x, touchVec.y)) {
                returnToParent();
                return;
            }

            if (subtitleBtnRect.contains(touchVec.x, touchVec.y)) {
                AudioManager.playSFX(AudioManager.buttonSound);
                subtitlesEnabled = !subtitlesEnabled;
            }

            if (touchVec.y >= musicSliderBar.y - 15 && touchVec.y <= musicSliderBar.y + 25
                && touchVec.x >= musicSliderBar.x && touchVec.x <= musicSliderBar.x + musicSliderBar.width) {
                isDraggingMusic = true;
            }
            if (touchVec.y >= sfxSliderBar.y - 15 && touchVec.y <= sfxSliderBar.y + 25
                && touchVec.x >= sfxSliderBar.x && touchVec.x <= sfxSliderBar.x + sfxSliderBar.width) {
                isDraggingSFX = true;
            }
        }

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (isDraggingMusic) {
                float relativeX = touchVec.x - musicSliderBar.x;
                musicVolume = Math.max(0f, Math.min(1f, relativeX / musicSliderBar.width));
                AudioManager.updateMusicVolume(musicVolume);
            }
            if (isDraggingSFX) {
                float relativeX = touchVec.x - sfxSliderBar.x;
                sfxVolume = Math.max(0f, Math.min(1f, relativeX / sfxSliderBar.width));
                AudioManager.updateSFXVolume(sfxVolume);
            }
        } else {
            isDraggingMusic = false;
            isDraggingSFX = false;
        }
    }

    private void returnToParent() {
        AudioManager.playSFX(AudioManager.buttonSound);
        prefs.putFloat("musicVolume", musicVolume);
        prefs.putFloat("sfxVolume", sfxVolume);
        prefs.putBoolean("subtitlesEnabled", subtitlesEnabled);
        prefs.flush();

        // Ensure real-time global sync across all screens
        AudioManager.updateMusicVolume(musicVolume);
        AudioManager.updateSFXVolume(sfxVolume);

        game.setScreen(parentScreen);
    }

    private void drawButton(Rectangle rect, String label) {
        game.batch.draw(btnL, rect.x, rect.y, 20, rect.height);
        game.batch.draw(btnC, rect.x + 20, rect.y, rect.width - 40, rect.height);
        game.batch.draw(btnR, rect.x + rect.width - 20, rect.y, 20, rect.height);

        layout.setText(font, label);
        font.draw(game.batch, label, rect.x + (rect.width - layout.width) / 2f, rect.y + (rect.height + layout.height) / 2f);
    }

    private BitmapFont loadFont(String filename, int size) {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(filename));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = size; p.color = Color.WHITE; p.borderWidth = 1.5f; p.borderColor = new Color(0f, 0f, 0f, 0.6f);
            BitmapFont f = gen.generateFont(p); gen.dispose(); return f;
        } catch (Exception e) { return new BitmapFont(); }
    }

    @Override
    public void show() {
        if (parentScreen instanceof TitleScreen) {
            ((TitleScreen) parentScreen).getMenuBg().resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (parentScreen instanceof TitleScreen) {
            ((TitleScreen) parentScreen).getMenuBg().resize(width, height);
        }
        recalcLayout();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
    }
}
