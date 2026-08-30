package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

public class DifficultyScreen implements Screen {

    private Main game;

    private String playerName;
    private int slotIndex;
    private float startX;
    private float startY;
    private int startLevel;

    private TitleScreen titleScreen;

    private Viewport viewport;

    private Texture boardTL, boardTC, boardTR, boardCL, boardCC, boardCR, boardBL, boardBC, boardBR;
    private Texture btnL, btnC, btnR;

    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout layout;

    private Rectangle classicRect;
    private Rectangle nightmareRect;
    private Rectangle backRect;
    private Vector3 touchVec;

    private int selectedOptionIndex = 0; // 0: Classic, 1: Nightmare

    public static boolean isNightmareMode = false;

    public DifficultyScreen(Main game, TitleScreen titleScreen, String playerName, int slotIndex, float startX, float startY, int startLevel) {
        this.game = game;
        this.titleScreen = titleScreen;
        this.playerName = playerName;
        this.slotIndex = slotIndex;
        this.startX = startX;
        this.startY = startY;
        this.startLevel = startLevel;

        viewport = new ExtendViewport(800, 480);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        touchVec = new Vector3();
        layout = new GlyphLayout();

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

        classicRect = new Rectangle();
        nightmareRect = new Rectangle();
        backRect = new Rectangle();

        recalcLayout();
    }

    private void recalcLayout() {
        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();
        float centerX = worldW / 2f;

        classicRect.set(centerX - 190f, 150f, 180f, 40f);
        nightmareRect.set(centerX + 10f, 150f, 180f, 40f);

        backRect.set(20f, worldH - 60f, 140f, 40f);
    }

    @Override
    public void show() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        if (titleScreen != null) {
            titleScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        recalcLayout();
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (titleScreen != null && titleScreen.getMenuBg() != null) {
            titleScreen.getMenuBg().updateAndRender(delta, game.batch);
        }

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        float worldW = viewport.getWorldWidth();

        game.batch.begin();

        float BOARD_WIDTH = 500f;
        float BOARD_HEIGHT = 200f;
        float BOARD_CORNER = 32f;
        float bx = (worldW - BOARD_WIDTH) / 2f;
        float by = 120f;
        float innerW = BOARD_WIDTH - BOARD_CORNER * 2;
        float innerH = BOARD_HEIGHT - BOARD_CORNER * 2;

        game.batch.draw(boardTL, bx, by + BOARD_HEIGHT - BOARD_CORNER, BOARD_CORNER, BOARD_CORNER);
        game.batch.draw(boardTC, bx + BOARD_CORNER, by + BOARD_HEIGHT - BOARD_CORNER, innerW, BOARD_CORNER);
        game.batch.draw(boardTR, bx + BOARD_WIDTH - BOARD_CORNER, by + BOARD_HEIGHT - BOARD_CORNER, BOARD_CORNER, BOARD_CORNER);
        game.batch.draw(boardCL, bx, by + BOARD_CORNER, BOARD_CORNER, innerH);
        game.batch.draw(boardCC, bx + BOARD_CORNER, by + BOARD_CORNER, innerW, innerH);
        game.batch.draw(boardCR, bx + BOARD_WIDTH - BOARD_CORNER, by + BOARD_CORNER, BOARD_CORNER, innerH);
        game.batch.draw(boardBL, bx, by, BOARD_CORNER, BOARD_CORNER);
        game.batch.draw(boardBC, bx + BOARD_CORNER, by, innerW, BOARD_CORNER);
        game.batch.draw(boardBR, bx + BOARD_WIDTH - BOARD_CORNER, by, BOARD_CORNER, BOARD_CORNER);

        layout.setText(titleFont, "CHOOSE DIFFICULTY");
        titleFont.draw(game.batch, "CHOOSE DIFFICULTY", (worldW - layout.width) / 2f, 280);

        drawButton(classicRect, "CLASSIC", selectedOptionIndex == 0, Color.WHITE);
        drawButton(nightmareRect, "NIGHTMARE", selectedOptionIndex == 1, Color.RED);
        drawButton(backRect, "BACK", false, Color.WHITE);

        game.batch.end();
    }

    private void handleInput() {
        // Keyboard Navigation (PC)
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            selectedOptionIndex = 0;
            AudioManager.playSFX(AudioManager.buttonSound);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            selectedOptionIndex = 1;
            AudioManager.playSFX(AudioManager.buttonSound);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            AudioManager.playSFX(AudioManager.buttonSound);
            isNightmareMode = (selectedOptionIndex == 1);
            startGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            AudioManager.playSFX(AudioManager.buttonSound);
            game.setScreen(new EnterNameScreen(game, titleScreen));
            dispose();
            return;
        }

        // Direct Touch / Mouse Click Selection (Android & PC)
        if (Gdx.input.justTouched()) {
            touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchVec);

            if (backRect.contains(touchVec.x, touchVec.y)) {
                AudioManager.playSFX(AudioManager.buttonSound);
                game.setScreen(new EnterNameScreen(game, titleScreen));
                dispose();
                return;
            }

            if (classicRect.contains(touchVec.x, touchVec.y)) {
                AudioManager.playSFX(AudioManager.buttonSound);
                selectedOptionIndex = 0;
                isNightmareMode = false;
                startGame();
            } else if (nightmareRect.contains(touchVec.x, touchVec.y)) {
                AudioManager.playSFX(AudioManager.buttonSound);
                selectedOptionIndex = 1;
                isNightmareMode = true;
                startGame();
            }
        }
    }

    private void drawButton(Rectangle rect, String label, boolean isSelected, Color textColor) {
        Color btnTint = isSelected ? new Color(1f, 0.9f, 0.4f, 1f) : Color.WHITE;
        game.batch.setColor(btnTint);

        game.batch.draw(btnL, rect.x, rect.y, 20, rect.height);
        game.batch.draw(btnC, rect.x + 20, rect.y, rect.width - 40, rect.height);
        game.batch.draw(btnR, rect.x + rect.width - 20, rect.y, 20, rect.height);

        game.batch.setColor(Color.WHITE);

        layout.setText(font, label);
        font.setColor(isSelected ? Color.YELLOW : textColor);
        font.draw(game.batch, label,
            rect.x + (rect.width - layout.width) / 2f,
            rect.y + (rect.height + layout.height) / 2f);
    }

    private void startGame() {
        titleScreen.stopTitleScreenMusic();

        int levelToLoad = (startLevel < 0) ? 0 : startLevel;

        game.setScreen(new PlayScreen(game, playerName, slotIndex, startX, startY, levelToLoad));
        dispose();
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
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (titleScreen != null) {
            titleScreen.resize(width, height);
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
