package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LoadingScreen implements Screen {

    private Main game;
    private Viewport viewport;
    private GlyphLayout layout;

    // --- Fonts ---
    private BitmapFont font;
    private BitmapFont titleFont;

    // --- State Machine & Timers ---
    private float stateTime = 0f;
    private float slideTimer = 0f;

    // Updated Indices: -1 = JellyByte Logo Animation, 0+ = Text Acknowledgments
    private int currentSlideIndex = -1;

    // Transition states: 0 = Fade In, 1 = Solid Display, 2 = Fade Out, 3 = Finished (Hold final slide + Prompt)
    private int transitionState = 0;
    private float textAlpha = 0f;

    // --- JellyByte Logo Animation Assets ---
    private Animation<TextureRegion> logoAnimation;
    private float logoAnimationTime = 0f;
    private final float LOGO_STAY_DURATION = 1.5f; // Seconds logo stays on screen after animation ends

    // --- Audio Assets ---
    private Music logoIntroMusic;
    private boolean musicStarted = false; // --- FIXED: Reliable music tracking flag ---

    // --- Configuration Constants ---
    private final float FADE_DURATION = 0.6f;
    private final float DISPLAY_DURATION = 2.0f;

    // --- Dynamic Acknowledgments Array ---
    private final String[] acknowledgments = {
        "GETTING UNDER YOUR NERVE\n\nThis game features highly reactive traps, intentional design trolls, and abrupt spatial shifts.",
        "GUIDELINES & SAVES\n\nSaves track your coordinates, difficulty parameters, and metrics strictly at active Checkpoint Flags.",
        "AUDIO CRITICAL\n\nEnsure your audio settings are configured for full spatial awareness of enemy radar and movement fields."
    };

    public LoadingScreen(Main game) {
        this.game = game;
        this.viewport = new ExtendViewport(800, 480);
        this.layout = new GlyphLayout();

        loadAssets();
    }

    private void loadAssets() {
        font = loadFont("ui/runescape_uf.ttf", 22, false);
        titleFont = loadFont("ui/runescape_uf.ttf", 28, true);

        // Load Logo Intro Music Track
        logoIntroMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/sounds/UI/logo_intro.mp3"));
        logoIntroMusic.setLooping(false);

        // Match the player's persistent audio options settings volume slider
        float musicVolume = Gdx.app.getPreferences("GettingUnderYourNerve_Settings").getFloat("musicVolume", 0.5f);
        logoIntroMusic.setVolume(musicVolume);

        // Load JellyByte Logo Frame Sequence
        int frameCount = 51;
        TextureRegion[] logoFrames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            String fileName = String.format("ui/logo/ezgif-frame-%03d.png", i + 1);
            Texture texture = new Texture(Gdx.files.internal(fileName));
            logoFrames[i] = new TextureRegion(texture);
        }
        logoAnimation = new Animation<>(0.098f, logoFrames); // Frame duration synchronized to 5s
        logoAnimation.setPlayMode(Animation.PlayMode.NORMAL);
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        // Pitch black canvas for premium contrast aesthetics
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        updateTransitions(delta);

        game.batch.begin();

        // 1. RENDER STAGE CONTENT
        if (currentSlideIndex == -1) {
            // --- JELLYBYTE LOGO SPLASH STAGE ---
            logoAnimationTime += delta;
            TextureRegion currentFrame = logoAnimation.getKeyFrame(logoAnimationTime);

            if (currentFrame != null) {
                game.batch.setColor(1f, 1f, 1f, textAlpha);

                // --- Scale your 920x720 frames to cover the FULL 800x480 virtual screen space ---
                game.batch.draw(currentFrame, 0, 0, 800f, 480f);

                game.batch.setColor(1f, 1f, 1f, 1f);
            }
        } else if (transitionState != 3) {
            // --- STANDARD INNER ACKNOWLEDGMENT TEXT ---
            float maxTextWidth = 620f;
            font.setColor(new Color(1f, 1f, 1f, textAlpha));
            font.draw(game.batch, acknowledgments[currentSlideIndex], (800 - maxTextWidth) / 2f, 270f, maxTextWidth, Align.center, true);
        } else {
            // --- FINAL LOCK ACTION: Hold final array element text visibly ---
            float maxTextWidth = 620f;
            font.setColor(Color.WHITE);
            font.draw(game.batch, acknowledgments[currentSlideIndex], (800 - maxTextWidth) / 2f, 270f, maxTextWidth, Align.center, true);
        }

        // 2. Calculate dynamic pulsing sine modifier
        float glowAlpha = 0.5f + 0.5f * MathUtils.sin(stateTime * 4.0f);

        // 3. RENDER RUNNING PROGRESS BARS OR INTERACTION PROMPTS
        if (transitionState != 3) {
            // --- FIXED: Only show LOADING... if the intro logo (-1) has finished playing ---
            if (currentSlideIndex > -1) {
                titleFont.setColor(new Color(0.7f, 0.7f, 0.7f, glowAlpha));
                String loadingText = "LOADING... ";
                layout.setText(titleFont, loadingText);
                titleFont.draw(game.batch, loadingText, 800f - layout.width - 40f, 50f);
            }
        } else {
            // --- INTERACTION PROMPT (Bottom Center) ---
            titleFont.setColor(new Color(1.0f, 0.85f, 0.0f, glowAlpha));
            String continueText = "PRESS ANY KEY TO CONTINUE";
            layout.setText(titleFont, continueText);
            titleFont.draw(game.batch, continueText, (800 - layout.width) / 2f, 60f);

            // Transition to main menu upon input registration
            if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                game.setScreen(new TitleScreen(game));
                dispose();
            }
        }

        game.batch.end();
    }

    private void updateTransitions(float dt) {
        if (transitionState == 3) return;

        slideTimer += dt;

        switch (transitionState) {
            case 0: // FADING IN ACTIVE ELEMENT
                if (currentSlideIndex == -1 && !musicStarted && logoIntroMusic != null) {
                    logoIntroMusic.play();
                    musicStarted = true;
                }
                textAlpha = Math.min(1f, slideTimer / FADE_DURATION);
                if (slideTimer >= FADE_DURATION) {
                    transitionState = 1;
                    slideTimer = 0f;
                }
                break;

            case 1: // SOLID DISPLAY WINDOW
                textAlpha = 1f;

                if (currentSlideIndex == -1) {
                    if (logoAnimation.isAnimationFinished(logoAnimationTime) && slideTimer >= LOGO_STAY_DURATION) {
                        transitionState = 2; // Fade out logo
                        slideTimer = 0f;
                    }
                } else {
                    if (slideTimer >= DISPLAY_DURATION) {
                        if (currentSlideIndex == acknowledgments.length - 1) {
                            transitionState = 3;
                        } else {
                            transitionState = 2;
                        }
                        slideTimer = 0f;
                    }
                }
                break;

            case 2: // FADING OUT ACTIVE ELEMENT
                textAlpha = Math.max(0f, 1f - (slideTimer / FADE_DURATION));
                if (slideTimer >= FADE_DURATION) {
                    currentSlideIndex++;
                    transitionState = 0;
                    slideTimer = 0f;
                }
                break;
        }
    }

    private BitmapFont loadFont(String filename, int size, boolean hasOutline) {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(filename));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = size;
            p.color = Color.WHITE;
            if (hasOutline) {
                p.borderWidth = 1.5f;
                p.borderColor = new Color(0f, 0f, 0f, 0.7f);
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
    @Override public void dispose() {
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();

        // Stop and dispose the intro music stream cleanly
        if (logoIntroMusic != null) {
            logoIntroMusic.stop();
            logoIntroMusic.dispose();
        }

        // Clean up animation frame native dependencies from GPU VRAM memory
        if (logoAnimation != null) {
            for (TextureRegion frame : logoAnimation.getKeyFrames()) {
                frame.getTexture().dispose();
            }
        }
    }
}
