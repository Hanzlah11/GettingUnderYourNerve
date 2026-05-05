package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PauseScreen implements Screen {

    private final Main game;
    private final PlayScreen playScreen; // Reference to resume the game
    private final int health, score;

    // --- UI Layout ---
    private Viewport uiViewport;
    private static final float BOARD_WIDTH  = 320f;
    private static final float BOARD_HEIGHT = 260f;
    private static final float BOARD_CORNER = 32f;
    private static final float BOARD_EDGE_H = 32f;
    private static final float BOARD_EDGE_V = 32f;
    private static final float BOARD_OFFSET_Y = HudBanner.BANNER_HEIGHT / 2f;

    private static final float BTN_CORNER_W = 20f;
    private static final float BTN_HEIGHT   = 40f;
    private static final float BTN_WIDTH    = 200f;
    private static final float BTN_GAP      = 18f;

    // --- Textures & Fonts ---
    private Texture boardTL, boardTC, boardTR, boardCL, boardCC, boardCR, boardBL, boardBC, boardBR;
    private Texture btnL, btnC, btnR;
    private BitmapFont font;
    private GlyphLayout layout;

    // --- Interactive Elements ---
    private Rectangle resumeRect = new Rectangle();
    private Rectangle helpRect   = new Rectangle();
    private Rectangle cheatsRect = new Rectangle();
    private Rectangle menuRect   = new Rectangle();
    private Vector3 touchVec     = new Vector3();

    // --- Prank Logic ---
    private boolean isRickrolling = false;
    private float rickTimer = 0;
    private Animation<TextureRegion> rickAnim;
    private static final float RICK_DRAW_W = 300f;
    private static final float RICK_DRAW_H = 220f;

    private final HudBanner hudBanner = new HudBanner();

    public PauseScreen(Main game, PlayScreen playScreen, int health, int score) {
        this.game = game;
        this.playScreen = playScreen;
        this.health = health;
        this.score = score;
        this.layout = new GlyphLayout();
        this.uiViewport = new FitViewport(800, 480);

        loadAssets(game.assets);
        recalcLayout();

        // --- PAUSE WORLD AUDIO ON ENTRY ---
        pauseWorldAudio();
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

        // Reusing your font loading logic from Source 18
        font = loadFont("ui/runescape_uf.ttf", 18);

        // Rickroll Setup
        Texture sheet = assets.manager.get(GameAssetManager.RICK_SHEET, Texture.class);
        TextureRegion[][] tmp = TextureRegion.split(sheet, 240, 135);
        Array<TextureRegion> frames = new Array<>();
        int count = 0;
        for (int r = 0; r < tmp.length; r++) {
            for (int c = 0; c < tmp[r].length; c++) {
                if (count++ < 84) frames.add(tmp[r][c]);
            }
        }
        rickAnim = new Animation<>(1/12f, frames);
        hudBanner.loadAssets(assets);
    }

    @Override
    public void render(float delta) {
        // 1. Input & World Rendering (Keep as is)[cite: 14]
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            resumeGame();
            return;
        }
        playScreen.drawWorld(delta);

        // 2. UI Setup
        uiViewport.apply();
        game.batch.setProjectionMatrix(uiViewport.getCamera().combined);

        float bx = (uiViewport.getWorldWidth() - BOARD_WIDTH) / 2f;
        float by = (uiViewport.getWorldHeight() - BOARD_HEIGHT) / 2f - BOARD_OFFSET_Y;
        float innerW = BOARD_WIDTH - BOARD_CORNER * 2;
        float innerH = BOARD_HEIGHT - BOARD_CORNER * 2;

        game.batch.begin();

        // --- STEP A: Draw the center backing first ---
        drawTex(game.batch, boardCC, bx + BOARD_CORNER, by + BOARD_CORNER, innerW, innerH);

        // --- STEP B: Draw Rickroll OR Buttons in the middle ---
        if (isRickrolling) {
            rickTimer += delta;
            TextureRegion frame = rickAnim.getKeyFrame(rickTimer);
            // Squeezed dimensions center the video inside the frame
            game.batch.draw(frame, bx + (BOARD_WIDTH - RICK_DRAW_W)/2f, by + (BOARD_HEIGHT - RICK_DRAW_H)/2f, RICK_DRAW_W, RICK_DRAW_H);

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) stopRickroll();
            if (rickAnim.isAnimationFinished(rickTimer)) stopRickroll();
        } else {
            handleMenuInput(); // Process button clicks
            drawButton(game.batch, resumeRect, "Resume");
            drawButton(game.batch, helpRect, "Need Some Help?");
            drawButton(game.batch, cheatsRect, "Cheat Codes");
            drawButton(game.batch, menuRect, "Return to Menu");
        }

        // --- STEP C: Draw the Wooden Frame ON TOP of the video ---
        // This act as a physical "window" that hides the video overflow[cite: 13]
        drawTex(game.batch, boardTL, bx,                              by + BOARD_HEIGHT - BOARD_CORNER, BOARD_CORNER, BOARD_CORNER);
        drawTex(game.batch, boardTC, bx + BOARD_CORNER,               by + BOARD_HEIGHT - BOARD_EDGE_H,  innerW,       BOARD_EDGE_H);
        drawTex(game.batch, boardTR, bx + BOARD_WIDTH - BOARD_CORNER, by + BOARD_HEIGHT - BOARD_CORNER, BOARD_CORNER, BOARD_CORNER);
        drawTex(game.batch, boardCL, bx,                              by + BOARD_CORNER,                 BOARD_EDGE_V, innerH);
        drawTex(game.batch, boardCR, bx + BOARD_WIDTH - BOARD_EDGE_V, by + BOARD_CORNER,                 BOARD_EDGE_V, innerH);
        drawTex(game.batch, boardBL, bx,                              by,                                BOARD_CORNER, BOARD_CORNER);
        drawTex(game.batch, boardBC, bx + BOARD_CORNER,               by,                                innerW,       BOARD_EDGE_H);
        drawTex(game.batch, boardBR, bx + BOARD_WIDTH - BOARD_CORNER, by,                                BOARD_CORNER, BOARD_CORNER);

        game.batch.end();

        // --- STEP D: Fixed HUD Visibility ---
        // Remove the !isRickrolling check so the HUD always renders[cite: 13]
        hudBanner.attachToBoard(bx - 180, by + 25, BOARD_HEIGHT);
        hudBanner.render(game.batch, health, score, uiViewport.getCamera().combined);
    }

    private void resumeGame() {
        stopRickroll();
        resumeWorldAudio();
        game.setScreen(playScreen); // Swap back to the existing play state[cite: 15]
    }

    private void stopRickroll() {
        isRickrolling = false;
        rickTimer = 0;
        if (AudioManager.rickMusic.isPlaying()) AudioManager.rickMusic.stop();
    }

    private void handleMenuInput() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        uiViewport.unproject(touchVec);

        if (resumeRect.contains(touchVec.x, touchVec.y)) resumeGame();
        else if (helpRect.contains(touchVec.x, touchVec.y)) Gdx.app.exit();
        else if (cheatsRect.contains(touchVec.x, touchVec.y)) {
            isRickrolling = true;
            AudioManager.rickMusic.play();
        }
    }

    // Reuse your helper methods for drawing buttons and tex from Source 18...
    private void drawButton(SpriteBatch batch, Rectangle rect, String label) {
        drawTex(batch, btnL, rect.x, rect.y, BTN_CORNER_W, rect.height);
        drawTex(batch, btnC, rect.x + BTN_CORNER_W, rect.y, rect.width - BTN_CORNER_W * 2, rect.height);
        drawTex(batch, btnR, rect.x + rect.width - BTN_CORNER_W, rect.y, BTN_CORNER_W, rect.height);
        layout.setText(font, label);
        font.draw(batch, label, rect.x + (rect.width - layout.width)/2f, rect.y + (rect.height + layout.height)/2f);
    }

    private void drawTex(SpriteBatch batch, Texture tex, float x, float y, float w, float h) {
        if (tex != null) batch.draw(tex, x, y, w, h);
    }

    private void recalcLayout() {
        float bx = (800 - BOARD_WIDTH) / 2f;
        float by = (480 - BOARD_HEIGHT) / 2f - BOARD_OFFSET_Y;
        float totalBtns = (BTN_HEIGHT * 4) + (BTN_GAP * 3);
        float startY = by + (BOARD_HEIGHT - totalBtns) / 2f;
        float btnX = bx + (BOARD_WIDTH - BTN_WIDTH) / 2f;
        menuRect.set(btnX, startY, BTN_WIDTH, BTN_HEIGHT);
        cheatsRect.set(btnX, startY + BTN_HEIGHT + BTN_GAP, BTN_WIDTH, BTN_HEIGHT);
        helpRect.set(btnX, startY + (BTN_HEIGHT + BTN_GAP) * 2f, BTN_WIDTH, BTN_HEIGHT);
        resumeRect.set(btnX, startY + (BTN_HEIGHT + BTN_GAP) * 3f, BTN_WIDTH, BTN_HEIGHT);
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

    private void pauseWorldAudio() {
        // 1. Use pause() instead of stop() to keep loops alive in memory
        AudioManager.crabPatrol.pause();
        AudioManager.crabAttack.pause();
        AudioManager.crabChaseShout.pause();

        // Optional: Pause one-shots as well so they don't finish during the menu
        AudioManager.shellShoot.pause();
        AudioManager.projectileBreak.pause();

        // 2. Room for Game Music:
        // if (AudioManager.gameMusic != null && AudioManager.gameMusic.isPlaying()) {
        //     AudioManager.gameMusic.setVolume(0.3f);
        // }
    }

    private void resumeWorldAudio() {
        AudioManager.crabPatrol.resume();
        AudioManager.crabAttack.resume();
        AudioManager.crabChaseShout.resume();
        AudioManager.shellShoot.resume();
        AudioManager.projectileBreak.resume();

        // Restore Game Music volume
        // if (AudioManager.gameMusic != null) AudioManager.gameMusic.setVolume(1.0f);
    }

    @Override public void show() { }
    @Override
    public void resize(int width, int height) {
        // 1. Update the UI viewport (centers the wooden board)
        uiViewport.update(width, height, true);

        // 2. CRITICAL: Update the world viewport in the background screen!
        // This calls PlayScreen.resize(), which handles viewport.update(width, height, false);[cite: 12, 13]
        playScreen.resize(width, height);
    }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override public void dispose() { if (font != null) font.dispose(); hudBanner.dispose(); }
}
