package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;

public class HudBanner {

    public static final float BANNER_WIDTH  = 350f;
    public static final float BANNER_HEIGHT = 150f;

    public static final float OVERLAP = HudBanner.BANNER_HEIGHT - 80f;

    private static final float HALF_W   = BANNER_WIDTH / 2f;
    private static final float CORNER_H = 32f;
    private static final float CENTER_H = BANNER_HEIGHT - CORNER_H * 2;

    private float bannerX, bannerY;
    private float screenW, screenH;

    private Texture tl, tr;
    private Texture cl, cr;
    private Texture bl, br;

    private BitmapFont font;
    private GlyphLayout layout;

    private final Matrix4 screenMatrix = new Matrix4();

    public HudBanner() {
        layout = new GlyphLayout();
    }

    public void loadAssets(GameAssetManager assets) {
        tl = get(assets, GameAssetManager.BANNER_TL);
        tr = get(assets, GameAssetManager.BANNER_TR);
        cl = get(assets, GameAssetManager.BANNER_CL);
        cr = get(assets, GameAssetManager.BANNER_CR);
        bl = get(assets, GameAssetManager.BANNER_BL);
        br = get(assets, GameAssetManager.BANNER_BR);

        font = loadFont("ui/runescape_uf.ttf", 25);
    }

    public void resize(int width, int height) {
        screenW = width;
        screenH = height;
    }

    public void attachToBoard(float boardX, float boardY, float boardH) {
        bannerX = boardX;
        bannerY = boardY + boardH - OVERLAP;
    }

    public void render(SpriteBatch batch, int health, int score, Matrix4 projectionMatrix) {
        batch.setProjectionMatrix(projectionMatrix);

        float x = bannerX;
        float y = bannerY;

        batch.begin();

        drawTex(batch, tl, x,          y + CENTER_H + CORNER_H, HALF_W, CORNER_H);
        drawTex(batch, tr, x + HALF_W, y + CENTER_H + CORNER_H, HALF_W, CORNER_H);

        drawTex(batch, cl, x,          y + CORNER_H,            HALF_W, CENTER_H);
        drawTex(batch, cr, x + HALF_W, y + CORNER_H,            HALF_W, CENTER_H);

        drawTex(batch, bl, x,          y,                        HALF_W, CORNER_H);
        drawTex(batch, br, x + HALF_W, y,                        HALF_W, CORNER_H);

        font.setColor(new Color(0.35f, 0.18f, 0f, 1f));
        String heading = "Stats";
        layout.setText(font, heading);
        font.draw(batch, heading,
            x + (BANNER_WIDTH - layout.width - 10) / 2f, y + BANNER_HEIGHT - 35f);

        font.setColor(Color.WHITE);
        font.draw(batch, "HP:    " + health,
            x + (BANNER_WIDTH - layout.width - 30) / 2f, y + BANNER_HEIGHT - 65f);

        font.draw(batch, "Score: " + score,
            x + (BANNER_WIDTH - layout.width - 30) / 2f, y + BANNER_HEIGHT - 95f);

        batch.end();
    }

    public void dispose() {
        if (font != null) font.dispose();
    }

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
