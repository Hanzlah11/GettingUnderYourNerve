package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.SettingsScreen;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import Game.GettingUnderYourNerve.Utilities.MenuBackground;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class TitleScreen implements Screen {

    private Main game;
    private Viewport viewport;
    private Texture btnL, btnC, btnR;
    private Texture boardTL, boardTC, boardTR, boardCL, boardCC, boardCR, boardBL, boardBC, boardBR;

    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont taglineFont;
    private GlyphLayout layout;

    private Rectangle playRect;
    private Rectangle leaderboardRect;
    private Rectangle settingsRect;
    private Rectangle exitRect;

    private Rectangle confirmYesRect;
    private Rectangle confirmNoRect;
    private boolean showExitConfirmation = false;

    private Vector3 touchVec;

    private MenuBackground menuBg;

    private String selectedTagline;
    private float pulseTime = 0;
    private int selectedButtonIndex = 0; // 0: Play, 1: Leaderboard, 2: Settings, 3: Exit

    private final String[] taglines = {
        "WITH GREAT GAME COMES GREAT RAGE ~ SOME RANDOM IDIOT",
        "TRY NOT TO CRY",
        "YOU COULD NOT LIVE WITH YOUR OWN FAILURE, WHERE DID THAT BRING YOU, BACK TO ME",
        "TOTALLY FAMILY FRIENDLY",
        "STUDENT ID: 24I-0537 APPROVED!"
    };

    Music currentTrack;

    public TitleScreen(Main game) {
        this.game = game;
        this.viewport = new ExtendViewport(800, 480);
        this.touchVec = new Vector3();
        this.layout = new GlyphLayout();

        menuBg = new MenuBackground();
        selectedTagline = taglines[MathUtils.random(0, taglines.length - 1)];

        btnL = game.assets.manager.get(GameAssetManager.BUTTON_L, Texture.class);
        btnC = game.assets.manager.get(GameAssetManager.BUTTON_C, Texture.class);
        btnR = game.assets.manager.get(GameAssetManager.BUTTON_R, Texture.class);

        boardTL = game.assets.manager.get(GameAssetManager.BOARD_TL, Texture.class);
        boardTC = game.assets.manager.get(GameAssetManager.BOARD_TC, Texture.class);
        boardTR = game.assets.manager.get(GameAssetManager.BOARD_TR, Texture.class);
        boardCL = game.assets.manager.get(GameAssetManager.BOARD_CL, Texture.class);
        boardCC = game.assets.manager.get(GameAssetManager.BOARD_CC, Texture.class);
        boardCR = game.assets.manager.get(GameAssetManager.BOARD_CR, Texture.class);
        boardBL = game.assets.manager.get(GameAssetManager.BOARD_BL, Texture.class);
        boardBC = game.assets.manager.get(GameAssetManager.BOARD_BC, Texture.class);
        boardBR = game.assets.manager.get(GameAssetManager.BOARD_BR, Texture.class);

        font = loadFont("ui/runescape_uf.ttf", 24);
        titleFont = loadFont("ui/runescape_uf.ttf", 54);
        taglineFont = loadFont("ui/runescape_uf.ttf", 18);

        playRect = new Rectangle();
        leaderboardRect = new Rectangle();
        settingsRect = new Rectangle();
        exitRect = new Rectangle();

        confirmYesRect = new Rectangle();
        confirmNoRect = new Rectangle();

        recalcLayout();
        startTitleScreenMusic();
    }

    private void recalcLayout() {
        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();
        float btnWidth = 200f;
        float startX = (worldW - btnWidth) / 2f;

        // Position 4 menu buttons cleanly stacked
        playRect.set(startX, 185f, btnWidth, 45f);
        leaderboardRect.set(startX, 135f, btnWidth, 45f);
        settingsRect.set(startX, 85f, btnWidth, 45f);
        exitRect.set(startX, 35f, btnWidth, 45f);

        // Exit confirmation modal bounds
        float modalX = (worldW - 400f) / 2f;
        float modalY = (worldH - 220f) / 2f;
        confirmYesRect.set(modalX + 40f, modalY + 30f, 130f, 40f);
        confirmNoRect.set(modalX + 230f, modalY + 30f, 130f, 40f);
    }

    public MenuBackground getMenuBg() {
        return menuBg;
    }

    @Override
    public void render(float delta) {
        pulseTime += delta;

        handleInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        menuBg.updateAndRender(delta, game.batch);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        game.batch.begin();

        drawCustomTitle();

        drawButton(playRect, "PLAY", selectedButtonIndex == 0);
        drawButton(leaderboardRect, "LEADERBOARD", selectedButtonIndex == 1);
        drawButton(settingsRect, "SETTINGS", selectedButtonIndex == 2);
        drawButton(exitRect, "EXIT", selectedButtonIndex == 3);

        if (showExitConfirmation) {
            drawExitConfirmationModal();
        }

        game.batch.end();
    }

    private void handleInput() {
        // Optional Back key/Escape shortcut
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            showExitConfirmation = !showExitConfirmation;
            AudioManager.playSFX(AudioManager.buttonSound);
            return;
        }

        if (showExitConfirmation) {
            if (Gdx.input.justTouched()) {
                touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                viewport.unproject(touchVec);

                if (confirmYesRect.contains(touchVec.x, touchVec.y)) {
                    AudioManager.playSFX(AudioManager.buttonSound);
                    Gdx.app.exit();
                } else if (confirmNoRect.contains(touchVec.x, touchVec.y)) {
                    AudioManager.playSFX(AudioManager.buttonSound);
                    showExitConfirmation = false;
                }
            }
            return;
        }

        // Keyboard Navigation (PC)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedButtonIndex = (selectedButtonIndex + 3) % 4;
            AudioManager.playSFX(AudioManager.buttonSound);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedButtonIndex = (selectedButtonIndex + 1) % 4;
            AudioManager.playSFX(AudioManager.buttonSound);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            triggerSelectedAction(selectedButtonIndex);
        }

        // Direct Touch / Mouse Tap (Android & PC)
        if (Gdx.input.justTouched()) {
            touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchVec);

            if (playRect.contains(touchVec.x, touchVec.y)) {
                selectedButtonIndex = 0;
                triggerSelectedAction(0);
            } else if (leaderboardRect.contains(touchVec.x, touchVec.y)) {
                selectedButtonIndex = 1;
                triggerSelectedAction(1);
            } else if (settingsRect.contains(touchVec.x, touchVec.y)) {
                selectedButtonIndex = 2;
                triggerSelectedAction(2);
            } else if (exitRect.contains(touchVec.x, touchVec.y)) {
                selectedButtonIndex = 3;
                triggerSelectedAction(3);
            }
        }
    }

    private void triggerSelectedAction(int index) {
        AudioManager.playSFX(AudioManager.buttonSound);
        switch (index) {
            case 0:
                game.setScreen(new EnterNameScreen(game, this));
                break;
            case 1:
                game.setScreen(new LeaderboardScreen(game, this));
                break;
            case 2:
                game.setScreen(new SettingsScreen(game, this));
                break;
            case 3:
                showExitConfirmation = true;
                break;
        }
    }

    private void drawExitConfirmationModal() {
        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();

        float mW = 400f;
        float mH = 220f;
        float corner = 24f;
        float mx = (worldW - mW) / 2f;
        float my = (worldH - mH) / 2f;
        float inW = mW - corner * 2;
        float inH = mH - corner * 2;

        game.batch.draw(boardTL, mx, my + mH - corner, corner, corner);
        game.batch.draw(boardTC, mx + corner, my + mH - corner, inW, corner);
        game.batch.draw(boardTR, mx + mW - corner, my + mH - corner, corner, corner);
        game.batch.draw(boardCL, mx, my + corner, corner, inH);
        game.batch.draw(boardCC, mx + corner, my + corner, inW, inH);
        game.batch.draw(boardCR, mx + mW - corner, my + corner, corner, inH);
        game.batch.draw(boardBL, mx, my, corner, corner);
        game.batch.draw(boardBC, mx + corner, my, inW, corner);
        game.batch.draw(boardBR, mx + mW - corner, my, corner, corner);

        layout.setText(font, "Are you tilted?");
        font.setColor(Color.WHITE);
        font.draw(game.batch, "Are you tilted?", mx + (mW - layout.width) / 2f, my + mH - 50f);

        drawButton(confirmYesRect, "YES", false);
        drawButton(confirmNoRect, "NO", true);
    }

    private void drawCustomTitle() {
        float worldW = viewport.getWorldWidth();

        String seg1 = "GETTING ";
        String seg2 = "UNDER ";
        String seg3 = "YOUR NERVE";

        layout.setText(titleFont, seg1);
        float w1 = layout.width;

        layout.setText(titleFont, seg2);
        float w2 = layout.width;

        layout.setText(titleFont, seg3);
        float w3 = layout.width;

        float totalWidth = w1 + w2 + w3;
        float startX = (worldW - totalWidth) / 2f;
        float titleY = 390f;

        titleFont.setColor(Color.WHITE);
        titleFont.draw(game.batch, seg1, startX, titleY);

        titleFont.setColor(new Color(1.0f, 0.5f, 0.0f, 1.0f));
        titleFont.draw(game.batch, seg2, startX + w1, titleY);

        titleFont.setColor(Color.WHITE);
        titleFont.draw(game.batch, seg3, startX + w1 + w2, titleY);

        float scale = 1.0f + 0.08f * MathUtils.sin(pulseTime * 5f);
        taglineFont.getData().setScale(scale);

        layout.setText(taglineFont, selectedTagline);

        taglineFont.setColor(new Color(1.0f, 0.85f, 0.0f, 1.0f));
        taglineFont.draw(game.batch, selectedTagline, (worldW - layout.width) / 2f, titleY - 45f);

        taglineFont.getData().setScale(1.0f);
    }

    private void drawButton(Rectangle rect, String label, boolean isSelected) {
        Color btnTint = isSelected ? new Color(1f, 0.9f, 0.4f, 1f) : Color.WHITE;
        game.batch.setColor(btnTint);

        game.batch.draw(btnL, rect.x, rect.y, 20, rect.height);
        game.batch.draw(btnC, rect.x + 20, rect.y, rect.width - 40, rect.height);
        game.batch.draw(btnR, rect.x + rect.width - 20, rect.y, 20, rect.height);

        game.batch.setColor(Color.WHITE);

        layout.setText(font, label);
        font.setColor(isSelected ? Color.YELLOW : Color.WHITE);
        font.draw(game.batch, label,
            rect.x + (rect.width - layout.width) / 2f,
            rect.y + (rect.height + layout.height) / 2f);
    }

    private BitmapFont loadFont(String filename, int size) {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(filename));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = size;
            p.color = Color.WHITE;
            p.borderWidth = 1.5f;
            p.borderColor = new Color(0f, 0f, 0f, 0.6f);
            BitmapFont f = gen.generateFont(p);
            gen.dispose();
            return f;
        } catch (Exception e) {
            return new BitmapFont();
        }
    }

    void startTitleScreenMusic() {
        currentTrack = AudioManager.elevatorMusic;
        if (currentTrack != null) {
            currentTrack.setLooping(true);

            Preferences prefs = Gdx.app.getPreferences("GettingUnderYourNerve_Settings");
            float userVolume = prefs.getFloat("musicVolume", 0.5f);

            currentTrack.setVolume(userVolume);

            if (userVolume > 0f) {
                if (!currentTrack.isPlaying()) {
                    currentTrack.play();
                }
            } else {
                currentTrack.pause();
            }
        }
    }

    void stopTitleScreenMusic() {
        if (currentTrack != null && currentTrack.isPlaying())
            currentTrack.stop();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        menuBg.resize(width, height);
        recalcLayout();
    }

    @Override public void show() {
        startTitleScreenMusic();
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (taglineFont != null) taglineFont.dispose();
        if (menuBg != null) menuBg.dispose();
    }
}
