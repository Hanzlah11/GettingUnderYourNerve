package Game.GettingUnderYourNerve.UI;

import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

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
    private Rectangle cheatsRect = new Rectangle();
    private Rectangle menuRect   = new Rectangle();

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

    private boolean isRickrolling = false;
    private float rickTimer = 0;
    private Animation<TextureRegion> rickAnim;

    // Squeezed dimensions: 280x210 fits perfectly in your 320x260 board
    private static final float RICK_DRAW_W = 280f;
    private static final float RICK_DRAW_H = 210f;


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

        Texture sheet = assets.manager.get(GameAssetManager.RICK_SHEET, Texture.class);
        // Split into 240x135 frames
        TextureRegion[][] tmp =
            TextureRegion.split(sheet, 240, 135);

        Array<TextureRegion> frames = new Array<>();
        int count = 0;
        for (int row = 0; row < tmp.length; row++) {
            for (int col = 0; col < tmp[row].length; col++) {
                if (count < 84) { // Only take the 84 frames you have
                    frames.add(tmp[row][col]);
                    count++;
                }
            }
        }
        rickAnim = new com.badlogic.gdx.graphics.g2d.Animation<>(1/12f, frames);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            // Ensure that whenever we unpause (regardless of how), the prank is reset
            if (!paused) {
                stopRickroll();
            }
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
    public void render(SpriteBatch batch, int health, int score, Viewport uiViewport) {
        if (!paused) return;

        if (isRickrolling) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                stopRickroll();
            }
        } else {
            handleMenuInput(uiViewport);
        }

        batch.setProjectionMatrix(uiViewport.getCamera().combined);

        float bx = (uiViewport.getWorldWidth() - BOARD_WIDTH)  / 2f;
        float by = (uiViewport.getWorldHeight() - BOARD_HEIGHT) / 2f - BOARD_OFFSET_Y;

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

        if (isRickrolling) {
            rickTimer += Gdx.graphics.getDeltaTime();
            com.badlogic.gdx.graphics.g2d.TextureRegion frame = rickAnim.getKeyFrame(rickTimer);

            // Center the squeezed 280x210 video
            float rx = bx + (BOARD_WIDTH - RICK_DRAW_W) / 2f;
            float ry = by + (BOARD_HEIGHT - RICK_DRAW_H) / 2f;
            batch.draw(frame, rx, ry, RICK_DRAW_W, RICK_DRAW_H);

            if (rickAnim.isAnimationFinished(rickTimer)) {
                isRickrolling = false;
                AudioManager.rickMusic.stop();
            }
        }
        else {
            // ---- "PAUSED" title ----
            font.setColor(Color.WHITE);
            String title = "PAUSED";
            layout.setText(font, title);
            font.draw(batch, title,
                bx + (BOARD_WIDTH - layout.width) / 2f,
                by + BOARD_HEIGHT - BOARD_CORNER - 0f);

            // ---- Buttons ----
            drawButton(batch, resumeRect, "Resume");
            drawButton(batch, helpRect, "Need Some Help?");
            drawButton(batch, cheatsRect, "Cheat Codes");
            drawButton(batch, menuRect, "Return to Menu");
        }
        batch.end();

        // ---- Banner: attach to board's top-left edge, then render ----

        hudBanner.attachToBoard(bx - 180, by + 25, BOARD_HEIGHT);
        hudBanner.render(batch, health, score, uiViewport.getCamera().combined);

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

        // Adjusted for 4 buttons and 3 gaps
        float totalBtns = (BTN_HEIGHT * 4) + (BTN_GAP * 3);
        float startY    = by + (BOARD_HEIGHT - totalBtns) / 2f;
        float btnX      = bx + (BOARD_WIDTH  - BTN_WIDTH) / 2f;

        // Position buttons from bottom to top
        menuRect  .set(btnX, startY,                               BTN_WIDTH, BTN_HEIGHT);
        cheatsRect.set(btnX, startY + BTN_HEIGHT + BTN_GAP,        BTN_WIDTH, BTN_HEIGHT);
        helpRect  .set(btnX, startY + (BTN_HEIGHT + BTN_GAP) * 2f, BTN_WIDTH, BTN_HEIGHT);
        resumeRect.set(btnX, startY + (BTN_HEIGHT + BTN_GAP) * 3f, BTN_WIDTH, BTN_HEIGHT);
    }

    private void handleMenuInput(Viewport uiViewport) {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;

        touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        uiViewport.unproject(touchVec);

        float tx = touchVec.x;
        float ty = touchVec.y;

        if      (resumeRect.contains(tx, ty)) paused = false;
        else if (helpRect  .contains(tx, ty)) onHelp();
        else if (cheatsRect.contains(tx, ty)) onCheatCodes();
        else if (menuRect  .contains(tx, ty)) onReturnToMenu();
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

    private void stopRickroll() {
        isRickrolling = false;
        rickTimer = 0;
        if (AudioManager.rickMusic != null && AudioManager.rickMusic.isPlaying()) {
            AudioManager.rickMusic.stop();
        }
    }

    private void onHelp() {
        // TROLL: Instantly terminate the game as requested
        Gdx.app.exit();
    }
    private void onCheatCodes() {
        isRickrolling = true; // Troll 2: The Rickroll
        rickTimer = 0;
        AudioManager.rickMusic.play();
    }
    private void onReturnToMenu()  { Gdx.app.log("PauseMenu", "Returning to Menu..."); }
}
