package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import Game.GettingUnderYourNerve.Utilities.MenuBackground;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

    // --- LIVE BACKGROUND HELPER ---
    private MenuBackground menuBg;

    // --- RANDOMIZED TAGLINE SYSTEM ---
    private String selectedTagline;
    private float pulseTime = 0;

    private final String[] taglines = {
        "WITH GREAT GAME COMES GREAT RAGE - SOME RANDOM IDIOT",
        "TRY NOT TO CRY",
        "YOU COULD NOT LIVE WITH YOUR OWN FAILURE, WHERE DID THAT BRING YOU, BACK TO ME",
        "TOTALLY FAMILY FRIENDLY",
        "STUDENT ID: 24I-0537 APPROVED!" // A secret easter egg!
    };

    Music currentTrack;
    public TitleScreen(Main game) {
        this.game = game;
        viewport = new ExtendViewport(800, 480);
        touchVec = new Vector3();
        layout = new GlyphLayout();

        // Initialize the live map background
        menuBg = new MenuBackground();

        // Choose a random tagline once when the title screen is created
        selectedTagline = taglines[MathUtils.random(0, taglines.length - 1)];

        btnL = game.assets.manager.get(GameAssetManager.BUTTON_L, Texture.class);
        btnC = game.assets.manager.get(GameAssetManager.BUTTON_C, Texture.class);
        btnR = game.assets.manager.get(GameAssetManager.BUTTON_R, Texture.class);

        font = loadFont("ui/runescape_uf.ttf", 24);
        titleFont = loadFont("ui/runescape_uf.ttf", 54);
        taglineFont = loadFont("ui/runescape_uf.ttf", 18); // Smaller font for longer quotes

        float startX = (800 - 200) / 2f;
        playRect = new Rectangle(startX, 160f, 200f, 50f);
        leaderboardRect = new Rectangle(startX, 110f, 200f, 50f);
        settingsRect = new Rectangle(startX, 60f, 200f, 50f);

        startTitleScreenMusic();
    }

    public MenuBackground getMenuBg() {
        return menuBg;
    }

    @Override
    public void render(float delta) {
        pulseTime += delta; // Track time to drive the pulsing animation

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
                // Switches to the persistent SettingsScreen while sharing this title screen context
                game.setScreen(new SettingsScreen(game, this));
            }
        }

        // Clear Screen Buffer
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Render the moving Tilemap Background
        menuBg.updateAndRender(delta, game.batch);

        // 2. Render UI Buttons, Title, and Tagline on top
        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        game.batch.begin();

        // Draw Title & Pulsing Minecraft-style Tagline
        drawCustomTitle();

        drawButton(playRect, "PLAY");
        drawButton(leaderboardRect, "LEADERBOARD");
        drawButton(settingsRect, "SETTINGS");

        game.batch.end();
    }

    /**
     * Calculates segments to keep the dynamic white-orange-white title and its pulsing tagline centered.
     */
    private void drawCustomTitle() {
        String seg1 = "GETTING ";
        String seg2 = "UNDER ";
        String seg3 = "YOUR NERVE";

        // Calculate individual widths for accurate spacing
        layout.setText(titleFont, seg1);
        float w1 = layout.width;

        layout.setText(titleFont, seg2);
        float w2 = layout.width;

        layout.setText(titleFont, seg3);
        float w3 = layout.width;

        float totalWidth = w1 + w2 + w3;
        float startX = (800 - totalWidth) / 2f;
        float titleY = 380f;

        // Segment 1: GETTING (White)
        titleFont.setColor(Color.WHITE);
        titleFont.draw(game.batch, seg1, startX, titleY);

        // Segment 2: UNDER (Orange)
        titleFont.setColor(new Color(1.0f, 0.5f, 0.0f, 1.0f));
        titleFont.draw(game.batch, seg2, startX + w1, titleY);

        // Segment 3: YOUR NERVE (White)
        titleFont.setColor(Color.WHITE);
        titleFont.draw(game.batch, seg3, startX + w1 + w2, titleY);

        // --- DRAW MINECRAFT-STYLE PULSING TAGLINE ---
        // Calculate a scale factor using a sine wave to create a smooth pulsing bounce
        float scale = 1.0f + 0.08f * MathUtils.sin(pulseTime * 5f);
        taglineFont.getData().setScale(scale);

        layout.setText(taglineFont, selectedTagline);

        // Dynamic yellow-gold shade to mimic splash text
        taglineFont.setColor(new Color(1.0f, 0.85f, 0.0f, 1.0f));

        // Center dynamically taking the scale factor into account
        taglineFont.draw(game.batch, selectedTagline, (800 - layout.width) / 2f, titleY - 50f);

        // Reset scale factor back to normal for future frames
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

    void startTitleScreenMusic()
    {
        currentTrack = AudioManager.elevatorMusic;
        currentTrack.setLooping(true);
        currentTrack.setVolume(0.5f);
        currentTrack.play();
    }

    void stopTitleScreenMusic()
    {
        if(currentTrack.isPlaying())
            currentTrack.stop();
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        menuBg.resize(width, height);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (taglineFont != null) taglineFont.dispose();
        if (menuBg != null) menuBg.dispose();
    }
}
