package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class EndCreditsScreen implements Screen {

    private Main game;
    private Viewport viewport;
    private GlyphLayout layout;
    private ShapeRenderer shapeRenderer;

    // --- Fonts ---
    private BitmapFont headerFont;
    private BitmapFont bodyFont;

    // --- Audio Assets ---
    private Music creditsMusic;

    // --- Scroll & Fade Timers ---
    private float scrollY = -50f;          // Starts just below the screen field view boundary
    private final float SCROLL_SPEED = 45f; // Pixels per second
    private float screenFadeAlpha = 1f;     // Starts completely black and fades in the scene
    private float exitFadeAlpha = 0f;       // Transitions to black when exiting

    private boolean isExiting = false;
    private float exitTimer = 0f;
    private final float FADE_DURATION = 1.0f;

    private float totalCreditsHeight = 0f;
    private final float LINE_SPACING = 35f;
    private final float SECTION_SPACING = 70f;

    // --- Structured Credits Data Package ---
    private final CreditLine[] creditsData = {
        new CreditLine("GETTING UNDER YOUR NERVE", true),
        new CreditLine("A JellyByte Studios Production", false),
        new CreditLine("Copyright 2026 - All Rights Reserved", false),
        new CreditLine("Build Pipeline: Stable v1.0.0", false),

        new CreditLine("DIRECTED & PROGRAMMED BY", true),
        new CreditLine("Mohib Sardar", false),
        new CreditLine("Hanzlah", false),

        new CreditLine("CORE GAME ENGINE ARCHITECTURE", true),
        new CreditLine("Lead Framework Systems -- Mohib Sardar", false),
        new CreditLine("State Machine Engine Design -- Hanzlah", false),
        new CreditLine("Screen Lifecycle Coordinator -- Mohib Sardar", false),
        new CreditLine("Batch Sprite Optimizer -- Hanzlah", false),

        new CreditLine("LEVEL DESIGN & INTENTIONAL TROLLS", true),
        new CreditLine("Chief Spatial Shift Architect -- Mohib Sardar", false),
        new CreditLine("Director of Unfair Spikes -- Hanzlah", false),
        new CreditLine("Fake Collision Bounds Specialist -- Mohib Sardar", false),
        new CreditLine("Rage-Quit Analytics Lead -- Hanzlah", false),
        new CreditLine("Hidden Trap Deployment Engine -- Mohib Sardar", false),

        new CreditLine("POKEMON BATTLE SUBSYSTEM", true),
        new CreditLine("Turn-Based Core Logic -- Mohib Sardar", false),
        new CreditLine("Health Bar Dynamic Vector Renderer -- Hanzlah", false),
        new CreditLine("Dialogue Label Layout Parser -- Mohib Sardar", false),
        new CreditLine("Skin Stylesheet Definitions -- Hanzlah", false),
        new CreditLine("Batman AI Modeling & Sarcasm Move Matrix -- Mohib Sardar", false),

        new CreditLine("ART DIRECTION & ANIMATION PIPELINE", true),
        new CreditLine("Lead Pixel Artist -- JellyByte Studios Visuals", false),
        new CreditLine("Sprite Sheet Slicer & Frame Sequencer -- Mohib Sardar", false),
        new CreditLine("Texture Memory Culling & VRAM Management -- Hanzlah", false),
        new CreditLine("Pikachu Frame Alignment Coordinator -- Mohib Sardar", false),
        new CreditLine("Charizard Flame Particle Supervisor -- Hanzlah", false),

        new CreditLine("AUDIO & TRACK SOUNDSCAPES", true),
        new CreditLine("Spatial Radar Engine -- JellyByte Audio Lab", false),
        new CreditLine("Music Compression Specialist -- 660KB OGG Core", false),
        new CreditLine("Loop Interlock Logic Engineer -- Mohib Sardar", false),
        new CreditLine("SFX Dynamic Volume Balancer -- Hanzlah", false),

        new CreditLine("QUALITY ASSURANCE & STRESS TESTING", true),
        new CreditLine("Lead Crash Investigation -- Mohib Sardar", false),
        new CreditLine("Frame Control Pointer Analyst -- Hanzlah", false),
        new CreditLine("Disposed Asset Lifecycle Evaluator -- Mohib Sardar", false),
        new CreditLine("Keyboard Durability Inspector -- Hanzlah", false),

        new CreditLine("DEDICATED INTERNAL BETA TESTERS", true),
        new CreditLine("Test Subject #0537", false),
        new CreditLine("Test Subject #1138", false),
        new CreditLine("Test Subject #2401", false),
        new CreditLine("The Unfortunate Spike-Tester Crew", false),
        new CreditLine("The Blind Invisible-Wall Walkers", false),
        new CreditLine("The Pitfall Trapped Volunteers", false),
        new CreditLine("The Sarcasm Move Spam Evaluators", false),

        new CreditLine("EXTERNAL DEPENDENCIES & TOOLKITS", true),
        new CreditLine("Powered by the LibGDX Framework", false),
        new CreditLine("LWJGL Native Desktop Backend", false),
        new CreditLine("FreeType Extension Font Rasterizer", false),
        new CreditLine("OpenGL Graphics Execution Layer", false),
        new CreditLine("OpenMPT Track Modulator System", false),
        new CreditLine("Audacity Mono Compression Engine", false),

        new CreditLine("SPECIAL GLOBAL THANKS", true),
        new CreditLine("FAST-NUCES Islamabad School of Computing", false),
        new CreditLine("Late-Night Coffee Makers & Academic Escape Pods", false),
        new CreditLine("Git Version History Stack Traces", false),
        new CreditLine("Our Target Hardware's GPU Context Drivers", false),
        new CreditLine("And Every Single Player Bold Enough to Face the Trolls", false),

        new CreditLine("CONGRATULATIONS", true),
        new CreditLine("YOU ACTUALLY ESCAPED THE FRUSTRATION MATRIX", false),
        new CreditLine("YOUR NERVES OF STEEL REMAIN ABSOLUTELY INTACT", false),

        new CreditLine("JELLYBYTE STUDIOS -- SIGNING OFF", true),
        new CreditLine("THANK YOU FOR PLAYING!", false)
    };

    public EndCreditsScreen(Main game) {
        this.game = game;
        this.viewport = new ExtendViewport(800, 480);
        this.layout = new GlyphLayout();
        this.shapeRenderer = new ShapeRenderer();

        loadAssets();
        calculateCreditsHeight();
    }

    private void loadAssets() {
        headerFont = loadFont("ui/runescape_uf.ttf", 28, true, new Color(1.0f, 0.85f, 0.0f, 1f));
        bodyFont = loadFont("ui/runescape_uf.ttf", 22, false, Color.WHITE);

        // Load your Stranger Things-inspired synth anthem track
        creditsMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/sounds/UI/end_credits_theme.mp3"));
        creditsMusic.setLooping(false);

        float musicVolume = Gdx.app.getPreferences("GettingUnderYourNerve_Settings").getFloat("musicVolume", 0.5f);
        creditsMusic.setVolume(musicVolume);
        creditsMusic.play();
    }

    private void calculateCreditsHeight() {
        // Run a safe calculation pre-pass to track the total vertical footprint of the text array
        totalCreditsHeight = 0f;
        for (CreditLine line : creditsData) {
            if (line.isHeader) {
                totalCreditsHeight += SECTION_SPACING;
            } else {
                totalCreditsHeight += LINE_SPACING;
            }
        }
    }

    @Override
    public void render(float delta) {
        // --- SAFE CRASH-PROOF CONTROL FLOW ENGINE ---
        if (updateTimersAndTransitions(delta)) {
            return; // Break execution frame instantly if screen changes to prevent native memory pointer faults
        }

        // Deep cinematic background slate
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.01f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        game.batch.begin();

        // RENDER SCROLLING CREDITS TEXT
        float currentYOffset = scrollY;
        for (CreditLine line : creditsData) {
            BitmapFont activeFont = line.isHeader ? headerFont : bodyFont;

            // Advance positioning baselines dynamically depending on entry classification tags
            currentYOffset -= line.isHeader ? SECTION_SPACING : LINE_SPACING;

            // Culling optimization: Only render lines that fall within the visible window space boundaries
            if (currentYOffset > -40f && currentYOffset < 520f) {
                activeFont.draw(game.batch, line.text, 0f, currentYOffset, 800f, Align.center, true);
            }
        }

        game.batch.end();

        // RENDER FADE OVERLAYS
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (screenFadeAlpha > 0.01f) {
            // Initial boot intro fade-in covering mask layer
            shapeRenderer.setColor(new Color(0.01f, 0.01f, 0.01f, screenFadeAlpha));
            shapeRenderer.rect(0, 0, 800f, 480f);
        } else if (isExiting) {
            // Screen exit fade-out covering mask layer
            shapeRenderer.setColor(new Color(0.01f, 0.01f, 0.01f, exitFadeAlpha));
            shapeRenderer.rect(0, 0, 800f, 480f);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Allow players to skip the crawl at any point to go back to the menu loop
        if (!isExiting && (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))) {
            isExiting = true;
            exitTimer = 0f;
        }
    }

    private boolean updateTimersAndTransitions(float dt) {
        if (isExiting) {
            exitTimer += dt;
            float progress = Math.min(1f, exitTimer / FADE_DURATION);
            exitFadeAlpha = progress;

            // Dim music volume downward linearly during the exit sequence transition window
            if (creditsMusic != null && creditsMusic.isPlaying()) {
                float baseVol = Gdx.app.getPreferences("GettingUnderYourNerve_Settings").getFloat("musicVolume", 0.5f);
                creditsMusic.setVolume(baseVol * (1f - progress));
            }

            if (progress >= 1f) {
                game.setScreen(new TitleScreen(game));
                dispose();
                return true; // Screen transition verified successfully
            }
            return false;
        }

        // Handle structural entry introduction fades
        if (screenFadeAlpha > 0f) {
            screenFadeAlpha = Math.max(0f, screenFadeAlpha - (dt / FADE_DURATION));
        }

        // Increment the upward scroll positions continuously
        scrollY += SCROLL_SPEED * dt;

        // Auto-exit script processing once the last element finishes crawling off the top viewport boundary limits
        if (scrollY > (480f + totalCreditsHeight + 100f)) {
            isExiting = true;
            exitTimer = 0f;
        }

        return false;
    }

    private BitmapFont loadFont(String filename, int size, boolean hasOutline, Color targetColor) {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(filename));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = size;
            p.color = targetColor;
            if (hasOutline) {
                p.borderWidth = 1.5f;
                p.borderColor = new Color(0f, 0f, 0f, 0.8f);
            }
            BitmapFont f = gen.generateFont(p);
            gen.dispose();
            return f;
        } catch (Exception e) {
            return new BitmapFont();
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (headerFont != null) headerFont.dispose();
        if (bodyFont != null) bodyFont.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();

        if (creditsMusic != null) {
            creditsMusic.stop();
            creditsMusic.dispose();
        }
    }

    // --- Data Model Struct Isolate ---
    private static class CreditLine {
        String text;
        boolean isHeader;

        public CreditLine(String text, boolean isHeader) {
            this.text = text;
            this.isHeader = isHeader;
        }
    }
}
