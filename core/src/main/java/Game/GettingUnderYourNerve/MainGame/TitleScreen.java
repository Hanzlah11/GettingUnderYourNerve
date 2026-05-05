//TitleScreen.java
package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
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
    private Viewport viewport;
    private Texture background;
    private Texture btnL, btnC, btnR;
    private BitmapFont font;
    private GlyphLayout layout;

    private Rectangle playRect;
    private Rectangle leaderboardRect;
    private Rectangle settingsRect;
    private Vector3 touchVec;

    public TitleScreen(Main game) {
        this.game = game;
        viewport = new FitViewport(800, 480);
        touchVec = new Vector3();
        layout = new GlyphLayout();

        background = game.assets.manager.get(GameAssetManager.TITLE_BG, Texture.class);
        btnL = game.assets.manager.get(GameAssetManager.BUTTON_L, Texture.class);
        btnC = game.assets.manager.get(GameAssetManager.BUTTON_C, Texture.class);
        btnR = game.assets.manager.get(GameAssetManager.BUTTON_R, Texture.class);

        font = loadFont("ui/runescape_uf.ttf", 24);

        float startX = (800 - 200) / 2f;
        playRect = new Rectangle(startX, 160f, 200f, 50f);
        leaderboardRect = new Rectangle(startX, 110f, 200f, 50f);
        settingsRect = new Rectangle(startX, 60f, 200f, 50f);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchVec);

            if (playRect.contains(touchVec.x, touchVec.y)) {
                AudioManager.buttonSound.play(); // PLAY SOUND
                game.setScreen(new EnterNameScreen(game));
                dispose();
            } else if (leaderboardRect.contains(touchVec.x, touchVec.y)) {
                AudioManager.buttonSound.play(); // PLAY SOUND
                game.setScreen(new LeaderboardScreen(game));
                dispose();
            } else if (settingsRect.contains(touchVec.x, touchVec.y)) {
                AudioManager.buttonSound.play(); // PLAY SOUND
                System.out.println("Settings Button Clicked");
            }
        }

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, 800, 480);

        drawButton(playRect, "PLAY");
        drawButton(leaderboardRect, "LEADERBOARD");
        drawButton(settingsRect, "SETTINGS");

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

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (font != null) font.dispose();
    }
}
