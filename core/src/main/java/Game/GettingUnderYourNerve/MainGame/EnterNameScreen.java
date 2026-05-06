package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class EnterNameScreen implements Screen, InputProcessor {

    private Main game;
    private Viewport viewport;
    private Texture background;

    // Board Textures
    private Texture boardTL, boardTC, boardTR;
    private Texture boardCL, boardCC, boardCR;
    private Texture boardBL, boardBC, boardBR;

    // Button Textures
    private Texture btnL, btnC, btnR;

    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout layout;

    private Rectangle backRect;
    private Rectangle startRect;
    private Vector3 touchVec;

    // Name Entry Logic
    private StringBuilder playerName;
    public static String globalPlayerName = "Player"; // Accessible globally by your FileHandler later

    public EnterNameScreen(Main game) {
        this.game = game;
        viewport = new FitViewport(800, 480);
        touchVec = new Vector3();
        layout = new GlyphLayout();
        playerName = new StringBuilder();

        background = game.assets.manager.get(GameAssetManager.TITLE_BG, Texture.class);

        // Load Board
        boardTL = game.assets.manager.get(GameAssetManager.BOARD_TL, Texture.class);
        boardTC = game.assets.manager.get(GameAssetManager.BOARD_TC, Texture.class);
        boardTR = game.assets.manager.get(GameAssetManager.BOARD_TR, Texture.class);
        boardCL = game.assets.manager.get(GameAssetManager.BOARD_CL, Texture.class);
        boardCC = game.assets.manager.get(GameAssetManager.BOARD_CC, Texture.class);
        boardCR = game.assets.manager.get(GameAssetManager.BOARD_CR, Texture.class);
        boardBL = game.assets.manager.get(GameAssetManager.BOARD_BL, Texture.class);
        boardBC = game.assets.manager.get(GameAssetManager.BOARD_BC, Texture.class);
        boardBR = game.assets.manager.get(GameAssetManager.BOARD_BR, Texture.class);

        // Load Buttons
        btnL = game.assets.manager.get(GameAssetManager.BUTTON_L, Texture.class);
        btnC = game.assets.manager.get(GameAssetManager.BUTTON_C, Texture.class);
        btnR = game.assets.manager.get(GameAssetManager.BUTTON_R, Texture.class);

        font = loadFont("ui/runescape_uf.ttf", 24);
        titleFont = loadFont("ui/runescape_uf.ttf", 36);

        backRect = new Rectangle(200, 100, 150, 40);
        startRect = new Rectangle(450, 100, 150, 40);
    }

    @Override
    public void show() {
        // Set this screen to capture keyboard input
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        // Handle Mouse Clicks
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchVec);

            if (backRect.contains(touchVec.x, touchVec.y)) {
                AudioManager.buttonSound.play();
                Gdx.input.setInputProcessor(null); // Clear input!
                game.setScreen(new TitleScreen(game));
                dispose();
            } else if (startRect.contains(touchVec.x, touchVec.y)) {
                AudioManager.buttonSound.play();
                startGame();
            }
        }

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        game.batch.begin();

        // 1. Background
        game.batch.draw(background, 0, 0, 800, 480);

        // 2. The Board UI
        float BOARD_WIDTH = 500f;
        float BOARD_HEIGHT = 200f;
        float BOARD_CORNER = 32f;
        float bx = (800 - BOARD_WIDTH) / 2f;
        float by = 180f; // Shifted up slightly
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

        // 3. Text & Name Display
        layout.setText(titleFont, "ENTER YOUR NAME");
        titleFont.draw(game.batch, "ENTER YOUR NAME", (800 - layout.width) / 2f, 340);

        // --- NEW NON-JITTERY CURSOR LOGIC ---
        // Measure ONLY the player's name to find the true center
        layout.setText(font, playerName.toString());
        float nameWidth = layout.width;
        float startX = (800 - nameWidth) / 2f;

        // Draw the actual name perfectly centered
        font.draw(game.batch, playerName.toString(), startX, 260);

        // Draw the blinking cursor right at the end of the name
        if (System.currentTimeMillis() / 500 % 2 == 0) {
            font.draw(game.batch, "_", startX + nameWidth, 260);
        }

        // 4. Buttons
        drawButton(backRect, "BACK");
        drawButton(startRect, "START");

        game.batch.end();
    }

    private void drawButton(Rectangle rect, String label) {
        game.batch.draw(btnL, rect.x, rect.y, 20, rect.height);
        game.batch.draw(btnC, rect.x + 20, rect.y, rect.width - 40, rect.height);
        game.batch.draw(btnR, rect.x + rect.width - 20, rect.y, 20, rect.height);

        layout.setText(font, label);
        font.setColor(Color.WHITE);
        font.draw(game.batch, label,
            rect.x + (rect.width - layout.width) / 2f,
            rect.y + (rect.height + layout.height) / 2f);
    }

    private void startGame() {
        if (playerName.length() > 0) {
            globalPlayerName = playerName.toString().trim();
        }
        Gdx.input.setInputProcessor(null); // CRITICAL: Stop listening to keys before switching screens
        game.setScreen(new PlayScreen(game, 0));
        dispose();
    }

    // --- InputProcessor Methods for Typing ---
    @Override
    public boolean keyTyped(char character) {
        // Backspace
        if (character == '\b' && playerName.length() > 0) {
            playerName.setLength(playerName.length() - 1);
        }
        // Enter / Return
        else if (character == '\r' || character == '\n') {
            AudioManager.buttonSound.play();
            startGame();
        }
        // Valid characters (Letters, Numbers, Space) up to 12 chars
        else if (Character.isLetterOrDigit(character) || character == ' ') {
            if (playerName.length() < 12) {
                playerName.append(character);
            }
        }
        return true;
    }

    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }

    private BitmapFont loadFont(String filename, int size) {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(filename));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = size; p.color = Color.WHITE; p.borderWidth = 1.5f; p.borderColor = new Color(0f, 0f, 0f, 0.6f);
            BitmapFont f = gen.generateFont(p); gen.dispose(); return f;
        } catch (Exception e) { return new BitmapFont(); }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
    }
}
