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
    private Screen parentScreen;
    private Viewport viewport;
    private Vector3 touchVec;

    private Texture boardTL, boardTC, boardTR, boardCL, boardCC, boardCR, boardBL, boardBC, boardBR;
    private Texture btnL, btnC, btnR;

    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout layout;

    private Rectangle musicSliderBar = new Rectangle();
    private Rectangle sfxSliderBar   = new Rectangle();
    private Rectangle subtitleBtnRect= new Rectangle();
    private Rectangle backBtnRect    = new Rectangle();

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
        this.viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.touchVec = new Vector3();
        this.layout = new GlyphLayout();

        prefs = Gdx.app.getPreferences("GettingUnderYourNerve_Settings");
        musicVolume = prefs.getFloat("musicVolume", 0.5f);
        sfxVolume = prefs.getFloat("sfxVolume", 0.5f);
        subtitlesEnabled = prefs.getBoolean("subtitles", true);

        loadAssets();
        recalcLayout();
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

    private void recalcLayout() {
        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();

        float bW = 600f;
        float bH = 360f;
        float bx = (worldW - bW) / 2f;
        float by = (worldH - bH) / 2f;

        musicSliderBar.set(bx + 230, by + 220, 220, 10);
        sfxSliderBar.set(bx + 230, by + 160, 220, 10);
        subtitleBtnRect.set(bx + 230, by + 95, 120, 32);
        backBtnRect.set(bx + (bW - 150f) / 2f, by + 25, 150, 36);
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
        float worldH = viewport.getWorldHeight();

        float bW = 600f;
        float bH = 360f;
        float corner = 32f;
        float bx = (worldW - bW) / 2f;
        float by = (worldH - bH) / 2f;
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
        titleFont.draw(game.batch, "SETTINGS", bx + (bW - layout.width) / 2f, by + bH - 25);

        font.draw(game.batch, "MUSIC VOLUME:", bx + 40, by + 232);
        game.batch.draw(btnC, musicSliderBar.x, musicSliderBar.y, musicSliderBar.width, musicSliderBar.height);
        float musicKnobX = musicSliderBar.x + (musicSliderBar.width * musicVolume) - 10;
        game.batch.draw(btnC, musicKnobX, musicSliderBar.y - 10, 20, 30);
        font.draw(game.batch, Math.round(musicVolume * 100) + "%", musicSliderBar.x + musicSliderBar.width + 15, by + 232);

        font.draw(game.batch, "SFX VOLUME:", bx + 40, by + 172);
        game.batch.draw(btnC, sfxSliderBar.x, sfxSliderBar.y, sfxSliderBar.width, sfxSliderBar.height);
        float sfxKnobX = sfxSliderBar.x + (sfxSliderBar.width * sfxVolume) - 10;
        game.batch.draw(btnC, sfxKnobX, sfxSliderBar.y - 10, 20, 30);
        font.draw(game.batch, Math.round(sfxVolume * 100) + "%", sfxSliderBar.x + sfxSliderBar.width + 15, by + 172);

        font.draw(game.batch, "SUBTITLES:", bx + 40, by + 118);
        drawButton(subtitleBtnRect, subtitlesEnabled ? "ON" : "OFF");

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
                prefs.putBoolean("subtitles", subtitlesEnabled);
                prefs.flush();
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

                prefs.putFloat("musicVolume", musicVolume);
                prefs.flush();
                AudioManager.updateMusicVolume(musicVolume);

                if (parentScreen instanceof PauseScreen) {
                    PlayScreen ps = ((PauseScreen) parentScreen).getPlayScreen();
                    if (ps != null) {
                        ps.updateCurrentTrackVolume(musicVolume);
                    }
                } else if (parentScreen instanceof TitleScreen) {
                    if (AudioManager.elevatorMusic != null) {
                        if (musicVolume > 0f) {
                            if (!AudioManager.elevatorMusic.isPlaying()) {
                                AudioManager.elevatorMusic.play();
                            }
                            AudioManager.elevatorMusic.setVolume(musicVolume);
                        } else {
                            AudioManager.elevatorMusic.pause();
                        }
                    }
                }
            }
            if (isDraggingSFX) {
                float relativeX = touchVec.x - sfxSliderBar.x;
                sfxVolume = Math.max(0f, Math.min(1f, relativeX / sfxSliderBar.width));

                prefs.putFloat("sfxVolume", sfxVolume);
                prefs.flush();
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
        prefs.putBoolean("subtitles", subtitlesEnabled);
        prefs.flush();

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
        musicVolume = prefs.getFloat("musicVolume", 0.5f);
        sfxVolume = prefs.getFloat("sfxVolume", 0.5f);
        subtitlesEnabled = prefs.getBoolean("subtitles", true);
        AudioManager.updateMusicVolume(musicVolume);
        AudioManager.updateSFXVolume(sfxVolume);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        if (parentScreen != null) {
            parentScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        recalcLayout();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (parentScreen != null) {
            parentScreen.resize(width, height);
        }
        recalcLayout();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
    }
}
