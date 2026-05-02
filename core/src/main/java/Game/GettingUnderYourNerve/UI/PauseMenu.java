package Game.GettingUnderYourNerve.UI;

import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class PauseMenu {

    // ---------------------------------------------------------------
    // State
    // ---------------------------------------------------------------
    private boolean paused = false;

    // ---------------------------------------------------------------
    // Screen size
    // ---------------------------------------------------------------
    private float screenW, screenH;

    // ---------------------------------------------------------------
    // Board layout constants
    // ---------------------------------------------------------------
    private static final float BOARD_CORNER = 32f;
    private static final float BOARD_EDGE_H = 32f;
    private static final float BOARD_EDGE_V = 32f;
    private static final float BOARD_WIDTH  = 320f;
    private static final float BOARD_HEIGHT = 260f;

    // Push board down so banner sits on its top-left naturally
    private static final float BOARD_OFFSET_Y = HudBanner.BANNER_HEIGHT / 2f;

    // ---------------------------------------------------------------
    // Button layout constants
    // ---------------------------------------------------------------
    private static final float BTN_CORNER_W = 20f;
    private static final float BTN_HEIGHT   = 40f;
    private static final float BTN_WIDTH    = 200f;
    private static final float BTN_GAP      = 18f;

    // ---------------------------------------------------------------
    // Board textures
    // ---------------------------------------------------------------
    private Texture boardTL, boardTC, boardTR;
    private Texture boardCL, boardCC, boardCR;
    private Texture boardBL, boardBC, boardBR;

    // ---------------------------------------------------------------
    // Button textures
    // ---------------------------------------------------------------
    private Texture btnL, btnC, btnR;

    // ---------------------------------------------------------------
    // Font
    // ---------------------------------------------------------------
    private BitmapFont font;
    private GlyphLayout layout;

    // ---------------------------------------------------------------
    // Button hit-boxes
    // ---------------------------------------------------------------
    private Rectangle resumeRect = new Rectangle();
    private Rectangle helpRect   = new Rectangle();
    private Rectangle howToRect  = new Rectangle();

    // ---------------------------------------------------------------
    // Touch
    // ---------------------------------------------------------------
    private Vector3 touchVec = new Vector3();

    // ---------------------------------------------------------------
    // Screen-space matrix
    // ---------------------------------------------------------------
    private final Matrix4 screenMatrix = new Matrix4();

    // ---------------------------------------------------------------
    // HudBanner — created, loaded, and disposed entirely by PauseMenu
    // ---------------------------------------------------------------
    private final HudBanner hudBanner = new HudBanner();

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public PauseMenu() {
        layout = new GlyphLayout();
    }

    // ---------------------------------------------------------------
    // loadAssets — call once after assets.manager.finishLoading()
    // ---------------------------------------------------------------
    public void loadAssets(GameAssetManager assets) {
        boardTL = load(assets, GameAssetManager.BOARD_TL);
        boardTC = load(assets, GameAssetManager.BOARD_TC);
        boardTR = load(assets, GameAssetManager.BOARD_TR);
        boardCL = load(assets, GameAssetManager.BOARD_CL);
        boardCC = load(assets, GameAssetManager.BOARD_CC);
        boardCR = load(assets, GameAssetManager.BOARD_CR);
        boardBL = load(assets, GameAssetManager.BOARD_BL);
        boardBC = load(assets, GameAssetManager.BOARD_BC);
        boardBR = load(assets, GameAssetManager.BOARD_BR);

        btnL = load(assets, GameAssetManager.BUTTON_L);
        btnC = load(assets, GameAssetManager.BUTTON_C);
        btnR = load(assets, GameAssetManager.BUTTON_R);

        font = loadFont("ui/runescape_uf.ttf", 18);

        // Banner loaded here — Main knows nothing about it
        hudBanner.loadAssets(assets);
    }

    // ---------------------------------------------------------------
    // resize — call from Main.resize() and once after create()
    // ---------------------------------------------------------------
    public void resize(int width, int height) {
        screenW = width;
        screenH = height;
        recalcLayout();
        hudBanner.resize(width, height);
    }

    // ---------------------------------------------------------------
    // handleInput — call at top of Main.render()
    // ---------------------------------------------------------------
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            paused = !paused;
        }
    }

    public boolean isPaused() { return paused; }

    // ---------------------------------------------------------------
    // render
    //   health — player's current health (int)
    //   score  — player's current score  (int)
    //
    // In Main.render():
    //   if (pauseMenu.isPaused()) {
    //       pauseMenu.render(batch, player.getHealth(), player.getScore());
    //   }
    // ---------------------------------------------------------------
    public void render(SpriteBatch batch, int health, int score) {
        if (!paused) return;

        handleMenuInput();

        screenMatrix.setToOrtho2D(0, 0, screenW, screenH);
        batch.setProjectionMatrix(screenMatrix);

        float bx = (screenW - BOARD_WIDTH)  / 2f;
        float by = (screenH - BOARD_HEIGHT) / 2f - BOARD_OFFSET_Y;

        float innerW = BOARD_WIDTH  - BOARD_CORNER * 2;
        float innerH = BOARD_HEIGHT - BOARD_CORNER * 2;

        batch.begin();

        // ---- Board (9-patch) ----
        drawTex(batch, boardTL, bx,                              by + BOARD_HEIGHT - BOARD_CORNER,  BOARD_CORNER, BOARD_CORNER);
        drawTex(batch, boardTC, bx + BOARD_CORNER,               by + BOARD_HEIGHT - BOARD_EDGE_H,  innerW,       BOARD_EDGE_H);
        drawTex(batch, boardTR, bx + BOARD_WIDTH - BOARD_CORNER, by + BOARD_HEIGHT - BOARD_CORNER,  BOARD_CORNER, BOARD_CORNER);

        drawTex(batch, boardCL, bx,                              by + BOARD_CORNER,                 BOARD_EDGE_V, innerH);
        drawTex(batch, boardCC, bx + BOARD_CORNER,               by + BOARD_CORNER,                 innerW,       innerH);
        drawTex(batch, boardCR, bx + BOARD_WIDTH - BOARD_EDGE_V, by + BOARD_CORNER,                 BOARD_EDGE_V, innerH);

        drawTex(batch, boardBL, bx,                              by,                                BOARD_CORNER, BOARD_CORNER);
        drawTex(batch, boardBC, bx + BOARD_CORNER,               by,                                innerW,       BOARD_EDGE_H);
        drawTex(batch, boardBR, bx + BOARD_WIDTH - BOARD_CORNER, by,                                BOARD_CORNER, BOARD_CORNER);

        // ---- "PAUSED" title ----
        font.setColor(Color.WHITE);
        String title = "PAUSED";
        layout.setText(font, title);
        font.draw(batch, title,
            bx + (BOARD_WIDTH - layout.width) / 2f,
            by + BOARD_HEIGHT - BOARD_CORNER - 0f);

        // ---- Buttons ----
        drawButton(batch, resumeRect, "Resume");
        drawButton(batch, helpRect,   "Help");
        drawButton(batch, howToRect,  "How to Play");

        batch.end();

        // ---- Banner: attach to board's top-left edge, then render ----
        hudBanner.attachToBoard(bx - 200, by, BOARD_HEIGHT);
        hudBanner.render(batch, health, score);
    }

    // ---------------------------------------------------------------
    // dispose — cleans up everything including the banner
    // ---------------------------------------------------------------
    public void dispose() {
        if (font != null) font.dispose();
        hudBanner.dispose();
    }

    // ================================================================
    // Private helpers
    // ================================================================

    private void recalcLayout() {
        float bx = (screenW - BOARD_WIDTH)  / 2f;
        float by = (screenH - BOARD_HEIGHT) / 2f - BOARD_OFFSET_Y;

        float totalBtns = BTN_HEIGHT * 3 + BTN_GAP * 2;
        float startY    = by + (BOARD_HEIGHT - totalBtns) / 2f;
        float btnX      = bx + (BOARD_WIDTH  - BTN_WIDTH) / 2f;

        howToRect .set(btnX, startY,                               BTN_WIDTH, BTN_HEIGHT);
        helpRect  .set(btnX, startY + BTN_HEIGHT + BTN_GAP,        BTN_WIDTH, BTN_HEIGHT);
        resumeRect.set(btnX, startY + (BTN_HEIGHT + BTN_GAP) * 2f, BTN_WIDTH, BTN_HEIGHT);
    }

    private void handleMenuInput() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;

        touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        float tx = touchVec.x;
        float ty = screenH - touchVec.y;

        if      (resumeRect.contains(tx, ty)) paused = false;
        else if (helpRect  .contains(tx, ty)) onHelp();
        else if (howToRect .contains(tx, ty)) onHowToPlay();
    }

    private void drawButton(SpriteBatch batch, Rectangle rect, String label) {
        float x = rect.x, y = rect.y, w = rect.width, h = rect.height;

        drawTex(batch, btnL, x,                    y, BTN_CORNER_W,          h);
        drawTex(batch, btnC, x + BTN_CORNER_W,     y, w - BTN_CORNER_W * 2, h);
        drawTex(batch, btnR, x + w - BTN_CORNER_W, y, BTN_CORNER_W,          h);

        layout.setText(font, label);
        font.setColor(Color.WHITE);
        font.draw(batch, label,
            x + (w - layout.width)  / 2f,
            y + (h + layout.height) / 2f);
    }

    private void drawTex(SpriteBatch batch, Texture tex,
                         float x, float y, float w, float h) {
        if (tex == null) return;
        batch.draw(tex, x, y, w, h);
    }

    private Texture load(GameAssetManager assets, String path) {
        if (path == null || path.isEmpty()) {
            Gdx.app.log("PauseMenu", "Empty path, skipping.");
            return null;
        }
        if (!assets.manager.isLoaded(path, Texture.class)) {
            Gdx.app.log("PauseMenu", "Not loaded: " + path);
            return null;
        }
        return assets.manager.get(path, Texture.class);
    }

    private BitmapFont loadFont(String filename, int size) {
        try {
            FreeTypeFontGenerator gen =
                new FreeTypeFontGenerator(Gdx.files.internal(filename));
            FreeTypeFontGenerator.FreeTypeFontParameter p =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
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

    private void onHelp()      { Gdx.app.log("PauseMenu", "Help pressed");        }
    private void onHowToPlay() { Gdx.app.log("PauseMenu", "How To Play pressed"); }
}
