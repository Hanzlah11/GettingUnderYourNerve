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
    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont taglineFont;
    private GlyphLayout layout;

    private Rectangle playRect;
    private Rectangle leaderboardRect;
    private Rectangle settingsRect;
    private Vector3 touchVec;

    private MenuBackground menuBg;

    private String selectedTagline;
    private float pulseTime = 0;

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

        font = loadFont("ui/runescape_uf.ttf", 24);
        titleFont = loadFont("ui/runescape_uf.ttf", 54);
        taglineFont = loadFont("ui/runescape_uf.ttf", 18);

        playRect = new Rectangle();
        leaderboardRect = new Rectangle();
        settingsRect = new Rectangle();

        recalcLayout();
        startTitleScreenMusic();
    }

    private void recalcLayout() {
        float worldW = viewport.getWorldWidth();
        float btnWidth = 200f;
        float startX = (worldW - btnWidth) / 2f;

        playRect.set(startX, 160f, btnWidth, 50f);
        leaderboardRect.set(startX, 110f, btnWidth, 50f);
        settingsRect.set(startX, 60f, btnWidth, 50f);
    }

    public MenuBackground getMenuBg() {
        return menuBg;
    }

    @Override
    public void render(float delta) {
        pulseTime += delta;

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchVec);

            if (playRect.contains(touchVec.x, touchVec.y)) {
                AudioManager.playSFX(AudioManager.buttonSound);
                game.setScreen(new EnterNameScreen(game, this));
            } else if (leaderboardRect.contains(touchVec.x, touchVec.y)) {
                AudioManager.playSFX(AudioManager.buttonSound);
                game.setScreen(new LeaderboardScreen(game, this));
            } else if (settingsRect.contains(touchVec.x, touchVec.y)) {
                AudioManager.playSFX(AudioManager.buttonSound);
                game.setScreen(new SettingsScreen(game, this));
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        menuBg.updateAndRender(delta, game.batch);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        game.batch.begin();

        drawCustomTitle();

        drawButton(playRect, "PLAY");
        drawButton(leaderboardRect, "LEADERBOARD");
        drawButton(settingsRect, "SETTINGS");

        game.batch.end();
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
        float titleY = 380f;

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
        taglineFont.draw(game.batch, selectedTagline, (worldW - layout.width) / 2f, titleY - 50f);

        taglineFont.getData().setScale(1.0f);
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
    @Override public void hide() {
    }

    @Override
    public void dispose() {
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (taglineFont != null) taglineFont.dispose();
        if (menuBg != null) menuBg.dispose();
    }
}
