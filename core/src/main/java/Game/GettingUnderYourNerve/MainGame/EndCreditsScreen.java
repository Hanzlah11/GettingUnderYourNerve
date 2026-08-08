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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class EndCreditsScreen implements Screen {

    private Main game;
    private Viewport viewport;
    private GlyphLayout layout;
    private ShapeRenderer shapeRenderer;

    private BitmapFont headerFont;
    private BitmapFont bodyFont;
    private BitmapFont teaserFont;
    private BitmapFont sequelFont;

    private Music creditsMusic;

    private boolean isTeaserPhase = true;
    private float teaserTimer = 0f;
    private float teaser1Alpha = 0f;
    private float teaser2Alpha = 0f;
    private float teaser3Alpha = 0f;

    private float scrollY = -50f;
    private final float SCROLL_SPEED = 45f;
    private float screenFadeAlpha = 1f;
    private float exitFadeAlpha = 0f;

    private boolean isExiting = false;
    private float exitTimer = 0f;
    private final float FADE_DURATION = 1.0f;

    private float totalCreditsHeight = 0f;
    private final float LINE_SPACING = 35f;
    private final float SECTION_SPACING = 70f;

    private static final Color COLOR_BG = new Color(0.05f, 0.05f, 0.07f, 1f);
    private static final Color COLOR_GOLD_HEADER = new Color(1.0f, 0.85f, 0.0f, 1f);
    private static final Color COLOR_WHITE_TEXT = new Color(0.95f, 0.95f, 0.95f, 1f);

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
        this.viewport = new FitViewport(800, 480);
        this.layout = new GlyphLayout();
        this.shapeRenderer = new ShapeRenderer();

        loadAssets();
        calculateCreditsHeight();
    }

    private void loadAssets() {
        headerFont = loadFont("ui/runescape_uf.ttf", 28, true, COLOR_GOLD_HEADER);
        bodyFont   = loadFont("ui/runescape_uf.ttf", 22, false, COLOR_WHITE_TEXT);
        teaserFont = loadFont("ui/runescape_uf.ttf", 26, true, COLOR_WHITE_TEXT);
        sequelFont = loadFont("ui/runescape_uf.ttf", 36, true, COLOR_GOLD_HEADER);

        creditsMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/sounds/UI/end_credits_theme.mp3"));
        creditsMusic.setLooping(false);

        float musicVolume = Gdx.app.getPreferences("GettingUnderYourNerve_Settings").getFloat("musicVolume", 0.5f);
        creditsMusic.setVolume(musicVolume);
        creditsMusic.play();
    }

    private void calculateCreditsHeight() {
        totalCreditsHeight = 0f;
        for (CreditLine line : creditsData) {
            totalCreditsHeight += line.isHeader ? SECTION_SPACING : LINE_SPACING;
        }
    }

    @Override
    public void render(float delta) {
        if (updateTimersAndTransitions(delta)) {
            return;
        }

        Gdx.gl.glClearColor(COLOR_BG.r, COLOR_BG.g, COLOR_BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        game.batch.begin();

        if (isTeaserPhase) {
            if (teaser1Alpha > 0f) {
                teaserFont.setColor(1f, 1f, 1f, teaser1Alpha);
                teaserFont.draw(game.batch, "CAPTAIN CLOWN NOSE", 0f, 310f, 800f, Align.center, true);
            }

            if (teaser2Alpha > 0f) {
                teaserFont.setColor(0.8f, 0.8f, 0.8f, teaser2Alpha);
                teaserFont.draw(game.batch, "WILL RETURN IN", 0f, 260f, 800f, Align.center, true);
            }

            if (teaser3Alpha > 0f) {
                sequelFont.setColor(COLOR_GOLD_HEADER.r, COLOR_GOLD_HEADER.g, COLOR_GOLD_HEADER.b, teaser3Alpha);
                sequelFont.draw(game.batch, "GETTING UNDER YOUR NERVE II", 0f, 200f, 800f, Align.center, true);
            }
        } else {
            float currentYOffset = scrollY;
            for (CreditLine line : creditsData) {
                BitmapFont activeFont = line.isHeader ? headerFont : bodyFont;
                currentYOffset -= line.isHeader ? SECTION_SPACING : LINE_SPACING;

                if (currentYOffset > -40f && currentYOffset < 520f) {
                    activeFont.draw(game.batch, line.text, 0f, currentYOffset, 800f, Align.center, true);
                }
            }
        }

        game.batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (screenFadeAlpha > 0.01f) {
            shapeRenderer.setColor(new Color(0f, 0f, 0f, screenFadeAlpha));
            shapeRenderer.rect(0, 0, 800f, 480f);
        } else if (isExiting) {
            shapeRenderer.setColor(new Color(0f, 0f, 0f, exitFadeAlpha));
            shapeRenderer.rect(0, 0, 800f, 480f);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        if (!isExiting && (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))) {
            if (isTeaserPhase) {
                isTeaserPhase = false;
                scrollY = -50f;
            } else {
                isExiting = true;
                exitTimer = 0f;
            }
        }
    }

    private boolean updateTimersAndTransitions(float dt) {
        if (isExiting) {
            exitTimer += dt;
            float progress = Math.min(1f, exitTimer / FADE_DURATION);
            exitFadeAlpha = progress;

            if (creditsMusic != null && creditsMusic.isPlaying()) {
                float baseVol = Gdx.app.getPreferences("GettingUnderYourNerve_Settings").getFloat("musicVolume", 0.5f);
                creditsMusic.setVolume(baseVol * (1f - progress));
            }

            if (progress >= 1f) {
                game.setScreen(new TitleScreen(game));
                dispose();
                return true;
            }
            return false;
        }

        if (screenFadeAlpha > 0f) {
            screenFadeAlpha = Math.max(0f, screenFadeAlpha - (dt / FADE_DURATION));
        }

        if (isTeaserPhase) {
            teaserTimer += dt;

            teaser1Alpha = Math.min(1f, teaserTimer / 1.0f);

            if (teaserTimer >= 2.5f) {
                teaser2Alpha = Math.min(1f, (teaserTimer - 2.5f) / 1.0f);
            }

            if (teaserTimer >= 5.0f) {
                teaser3Alpha = Math.min(1f, (teaserTimer - 5.0f) / 1.0f);
            }

            if (teaserTimer >= 5.5f) {
                float fadeOutProgress = (teaserTimer - 5.5f) / 1.0f;
                float currentAlpha = Math.max(0f, 1f - fadeOutProgress);
                teaser1Alpha = currentAlpha;
                teaser2Alpha = currentAlpha;
                teaser3Alpha = currentAlpha;

                if (teaserTimer >= 6.5f) {
                    isTeaserPhase = false;
                    scrollY = -50f;
                }
            }
            return false;
        }

        scrollY += SCROLL_SPEED * dt;

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
        if (teaserFont != null) teaserFont.dispose();
        if (sequelFont != null) sequelFont.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();

        if (creditsMusic != null) {
            creditsMusic.stop();
            creditsMusic.dispose();
        }
    }

    private static class CreditLine {
        String text;
        boolean isHeader;

        public CreditLine(String text, boolean isHeader) {
            this.text = text;
            this.isHeader = isHeader;
        }
    }
}
