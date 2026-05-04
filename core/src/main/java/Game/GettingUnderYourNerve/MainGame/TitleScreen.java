package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

public class TitleScreen implements Screen {

    private Main game;

    // Viewport to scale the title screen nicely
    private Viewport viewport;
    private final float WORLD_WIDTH = 800;
    private final float WORLD_HEIGHT = 480;

    // Textures
    private Texture background;
    private Texture btnL, btnC, btnR;

    // Font
    private BitmapFont font;
    private GlyphLayout layout;

    // Buttons
    private Rectangle playRect;
    private Rectangle settingsRect;
    private Vector3 touchVec;

    // Layout Constants
    private static final float BTN_CORNER_W = 20f;
    private static final float BTN_HEIGHT = 50f;
    private static final float BTN_WIDTH = 200f;

    public TitleScreen(Main game) {
        this.game = game;

        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        touchVec = new Vector3();
        layout = new GlyphLayout();

        // Load Textures from AssetManager
        background = game.assets.manager.get(GameAssetManager.TITLE_BG, Texture.class);
        btnL = game.assets.manager.get(GameAssetManager.BUTTON_L, Texture.class);
        btnC = game.assets.manager.get(GameAssetManager.BUTTON_C, Texture.class);
        btnR = game.assets.manager.get(GameAssetManager.BUTTON_R, Texture.class);

        // Load Font
        font = loadFont("ui/runescape_uf.ttf", 24);
        playRect = new Rectangle(200f, 50f, BTN_WIDTH, BTN_HEIGHT);
        settingsRect = new Rectangle(400f, 50f, BTN_WIDTH, BTN_HEIGHT);
    }

    @Override
    public void render(float delta) {
        handleInput();

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        game.batch.begin();

        // 1. Draw the background image to cover the whole viewport
        game.batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // 2. Draw Buttons
        drawButton(playRect, "PLAY");
        drawButton(settingsRect, "SETTINGS");

        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchVec);

            if (playRect.contains(touchVec.x, touchVec.y)) {
                // GO TO GAME!
                game.setScreen(new PlayScreen(game));
                dispose(); // Clean up title screen memory
            } else if (settingsRect.contains(touchVec.x, touchVec.y)) {
                // Does nothing right now, as requested!
                System.out.println("Settings Button Clicked");
            }
        }
    }

    private void drawButton(Rectangle rect, String label) {
        float x = rect.x, y = rect.y, w = rect.width, h = rect.height;

        // Draw 9-patch style pieces based on your PauseMenu code
        game.batch.draw(btnL, x, y, BTN_CORNER_W, h);
        game.batch.draw(btnC, x + BTN_CORNER_W, y, w - BTN_CORNER_W * 2, h);
        game.batch.draw(btnR, x + w - BTN_CORNER_W, y, BTN_CORNER_W, h);

        // Draw Text Centered
        layout.setText(font, label);
        font.setColor(Color.WHITE);
        font.draw(game.batch, label,
            x + (w - layout.width) / 2f,
            y + (h + layout.height) / 2f);
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
            Gdx.app.error("TitleScreen", "Font load failed: " + e.getMessage());
            return new BitmapFont();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (font != null) font.dispose();
    }
}
