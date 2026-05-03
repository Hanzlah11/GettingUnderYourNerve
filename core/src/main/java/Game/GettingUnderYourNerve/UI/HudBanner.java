package Game.GettingUnderYourNerve.UI;

import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;

/**
 * HudBanner — owned and managed entirely by PauseMenu.
 * Main.java never touches this class directly.
 *
 * Banner sprite layout (2 columns x 3 rows):
 *   TL | TR
 *   CL | CR
 *   BL | BR
 *
 * Displays "Stats" heading, player Health and Score.
 * Positioned by PauseMenu via attachToBoard() so it sits on the
 * top-left corner of the pause board with a small overlap.
 */
public class HudBanner {

    // ---------------------------------------------------------------
    // Banner size — adjust to fit your sprites
    // ---------------------------------------------------------------
    public static final float BANNER_WIDTH  = 350f;
    public static final float BANNER_HEIGHT = 150f;

    // Pixels the banner overlaps the board's top edge
    public static final float OVERLAP = HudBanner.BANNER_HEIGHT - 80f;

    private static final float HALF_W   = BANNER_WIDTH / 2f;
    private static final float CORNER_H = 32f;
    private static final float CENTER_H = BANNER_HEIGHT - CORNER_H * 2;

    // ---------------------------------------------------------------
    // Position — set by attachToBoard()
    // ---------------------------------------------------------------
    private float bannerX, bannerY;
    private float screenW, screenH;

    // ---------------------------------------------------------------
    // Textures
    // ---------------------------------------------------------------
    private Texture tl, tr;
    private Texture cl, cr;
    private Texture bl, br;

    // ---------------------------------------------------------------
    // Font
    // ---------------------------------------------------------------
    private BitmapFont font;
    private GlyphLayout layout;

    // ---------------------------------------------------------------
    // Screen-space matrix (own copy — never mutates PauseMenu's)
    // ---------------------------------------------------------------
    private final Matrix4 screenMatrix = new Matrix4();

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public HudBanner() {
        layout = new GlyphLayout();
    }

    // ---------------------------------------------------------------
    // loadAssets — called by PauseMenu.loadAssets()
    // ---------------------------------------------------------------
    public void loadAssets(GameAssetManager assets) {
        tl = get(assets, GameAssetManager.BANNER_TL);
        tr = get(assets, GameAssetManager.BANNER_TR);
        cl = get(assets, GameAssetManager.BANNER_CL);
        cr = get(assets, GameAssetManager.BANNER_CR);
        bl = get(assets, GameAssetManager.BANNER_BL);
        br = get(assets, GameAssetManager.BANNER_BR);

        font = loadFont("ui/runescape_uf.ttf", 25);
    }

    // ---------------------------------------------------------------
    // resize — called by PauseMenu.resize()
    // ---------------------------------------------------------------
    public void resize(int width, int height) {
        screenW = width;
        screenH = height;
    }

    // ---------------------------------------------------------------
    // attachToBoard — called by PauseMenu.render() every frame.
    // Positions the banner so its bottom edge overlaps the board's
    // top edge by OVERLAP pixels.
    //
    //   boardX — left edge of the pause board  (screen pixels)
    //   boardY — bottom edge of the pause board (screen pixels)
    //   boardH — total height of the pause board
    // ---------------------------------------------------------------
    public void attachToBoard(float boardX, float boardY, float boardH) {
        bannerX = boardX;
        bannerY = boardY + boardH - OVERLAP;
    }

    // ---------------------------------------------------------------
    // render — called by PauseMenu.render() after attachToBoard()
    // ---------------------------------------------------------------
    public void render(SpriteBatch batch, int health, int score, Matrix4 projectionMatrix) {
        batch.setProjectionMatrix(projectionMatrix);

        float x = bannerX;
        float y = bannerY;

        batch.begin();

        // --- Top row ---
        drawTex(batch, tl, x,          y + CENTER_H + CORNER_H, HALF_W, CORNER_H);
        drawTex(batch, tr, x + HALF_W, y + CENTER_H + CORNER_H, HALF_W, CORNER_H);

        // --- Middle row ---
        drawTex(batch, cl, x,          y + CORNER_H,            HALF_W, CENTER_H);
        drawTex(batch, cr, x + HALF_W, y + CORNER_H,            HALF_W, CENTER_H);

        // --- Bottom row ---
        drawTex(batch, bl, x,          y,                        HALF_W, CORNER_H);
        drawTex(batch, br, x + HALF_W, y,                        HALF_W, CORNER_H);

        // --- "Stats" heading ---
        font.setColor(new Color(0.35f, 0.18f, 0f, 1f));
        String heading = "Stats";
        layout.setText(font, heading);
        font.draw(batch, heading,
            x + (BANNER_WIDTH - layout.width - 10) / 2f, y + BANNER_HEIGHT - 35f);          // ← was: - CORNER_H / 2f

        // --- Health ---
        font.setColor(Color.WHITE);
        font.draw(batch, "HP:    " + health,
            x + (BANNER_WIDTH - layout.width - 30) / 2f, y + BANNER_HEIGHT - 65f);         // ← was: CORNER_H + CENTER_H - 8f

        // --- Score ---
        font.draw(batch, "Score: " + score,
            x + (BANNER_WIDTH - layout.width - 30) / 2f, y + BANNER_HEIGHT - 95f);

        batch.end();
    }

    // ---------------------------------------------------------------
    // dispose — called by PauseMenu.dispose()
    // ---------------------------------------------------------------
    public void dispose() {
        if (font != null) font.dispose();
    }

    // ================================================================
    // Private helpers
    // ================================================================

    private void drawTex(SpriteBatch batch, Texture tex,
                         float x, float y, float w, float h) {
        if (tex == null) return;
        batch.draw(tex, x, y, w, h);
    }

    private Texture get(GameAssetManager assets, String path) {
        if (path == null || path.isEmpty()) {
            Gdx.app.log("HudBanner", "Empty path, skipping.");
            return null;
        }
        if (!assets.manager.isLoaded(path, Texture.class)) {
            Gdx.app.log("HudBanner", "Not loaded: " + path);
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
            p.borderWidth = 1f;
            p.borderColor = new Color(0f, 0f, 0f, 0.5f);
            BitmapFont f  = gen.generateFont(p);
            gen.dispose();
            return f;
        } catch (Exception e) {
            Gdx.app.error("HudBanner", "Font load failed: " + e.getMessage());
            return new BitmapFont();
        }
    }
}
