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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LoadingScreen implements Screen {

    private Main game;
    private Viewport viewport;
    private GlyphLayout layout;
    private ShapeRenderer shapeRenderer;

    // --- Fonts ---
    private BitmapFont font;
    private BitmapFont titleFont;

    // --- State Machine & Timers ---
    private float stateTime = 0f;
    private float slideTimer = 0f;

    private int currentSlideIndex = -1;

    private int transitionState = 0;
    private float textAlpha = 0f;

    // --- Retro Custom Transition States ---
    private boolean isTransitioningToTitle = false;
    private float transitionTimer = 0f;
    private float promptScale = 1.0f;
    private float screenFadeAlpha = 0f;
    private final float RETRO_DURATION = 1.0f;

    private boolean screenFinished = false;

    // --- JellyByte Logo Animation Assets ---
    private Animation<TextureRegion> logoAnimation;
    private float logoAnimationTime = 0f;
    private final float LOGO_STAY_DURATION = 1.5f;

    // --- Audio Assets ---
    private Music logoIntroMusic;
    private boolean musicStarted = false;

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
        // FIXED: Switched from ExtendViewport to FitViewport to preserve resolution and prevent UI stretching[cite: 23]
        this.viewport = new FitViewport(800, 480);
        this.layout = new GlyphLayout();
        this.shapeRenderer = new ShapeRenderer();

        loadAssets();
    }

    private void loadAssets() {
        font = loadFont("ui/runescape_uf.ttf", 22, false);
        titleFont = loadFont("ui/runescape_uf.ttf", 28, true);

        logoIntroMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/sounds/UI/logo_intro.mp3"));
        logoIntroMusic.setLooping(false);

        float musicVolume = Gdx.app.getPreferences("GettingUnderYourNerve_Settings").getFloat("musicVolume", 0.5f);
        logoIntroMusic.setVolume(musicVolume);

        int frameCount = 52;
        TextureRegion[] logoFrames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            String fileName = String.format("ui/logo/ezgif-frame-%03d.png", i + 1);
            Texture texture = new Texture(Gdx.files.internal(fileName));
            logoFrames[i] = new TextureRegion(texture);
        }
        logoAnimation = new Animation<>(0.04f, logoFrames);
        logoAnimation.setPlayMode(Animation.PlayMode.NORMAL);
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        Gdx.gl.glClearColor(0.02f, 0.02f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        updateTransitions(delta);

        if (screenFinished) {
            return;
        }

        // FIXED: Retrieve active viewport width and height dynamically[cite: 23]
        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();

        game.batch.begin();

        // 1. RENDER STAGE CONTENT
        if (currentSlideIndex == -1) {
            logoAnimationTime += delta;
            TextureRegion currentFrame = logoAnimation.getKeyFrame(logoAnimationTime);

            if (currentFrame != null) {
                float logoW = 800f;
                float logoH = 480f;
                float logoX = (worldW - logoW) / 2f;
                float logoY = (worldH - logoH) / 2f;

                game.batch.setColor(1f, 1f, 1f, textAlpha);
                game.batch.draw(currentFrame, logoX, logoY, logoW, logoH);
                game.batch.setColor(1f, 1f, 1f, 1f);
            }
        } else if (transitionState != 3) {
            float maxTextWidth = 620f;
            float textX = (worldW - maxTextWidth) / 2f;
            float textY = worldH * 0.5625f; // Maintains vertical proportions[cite: 23]

            font.setColor(new Color(1f, 1f, 1f, textAlpha));
            font.draw(game.batch, acknowledgments[currentSlideIndex], textX, textY, maxTextWidth, Align.center, true);
        } else {
            float maxTextWidth = 620f;
            float textX = (worldW - maxTextWidth) / 2f;
            float textY = worldH * 0.5625f;

            float currentTextAlpha = isTransitioningToTitle ? Math.max(0f, 1f - (transitionTimer * 2f)) : 1f;
            font.setColor(new Color(1f, 1f, 1f, currentTextAlpha));
            font.draw(game.batch, acknowledgments[currentSlideIndex], textX, textY, maxTextWidth, Align.center, true);
        }

        // 2. Calculate dynamic pulsing sine modifier
        float glowAlpha = 0.5f + 0.5f * MathUtils.sin(stateTime * 4.0f);

        // 3. RENDER RUNNING PROGRESS BARS OR INTERACTION PROMPTS
        if (transitionState != 3) {
            if (currentSlideIndex > -1) {
                titleFont.setColor(new Color(0.7f, 0.7f, 0.7f, glowAlpha));
                String loadingText = "LOADING... ";
                layout.setText(titleFont, loadingText);
                titleFont.draw(game.batch, loadingText, worldW - layout.width - 40f, 50f);
            }
        } else {
            // --- RETRO "PRESS ANY KEY" SCALE POP RENDERING ---
            String continueText = "PRESS ANY KEY TO CONTINUE";

            titleFont.getData().setScale(promptScale);
            layout.setText(titleFont, continueText);

            float targetX = (worldW - layout.width) / 2f;
            float targetY = 60f + (layout.height / 2f);

            if (!isTransitioningToTitle) {
                titleFont.setColor(new Color(1.0f, 0.85f, 0.0f, glowAlpha));
            } else {
                float flashGlow = (int)(transitionTimer * 20f) % 2 == 0 ? 1f : 0.4f;
                float promptAlpha = Math.max(0f, 1f - (transitionTimer / RETRO_DURATION));
                titleFont.setColor(new Color(1.0f, 0.95f, 0.4f * flashGlow, promptAlpha));
            }

            titleFont.draw(game.batch, continueText, targetX, targetY);
            titleFont.getData().setScale(1.0f);

            if (!isTransitioningToTitle && (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))) {
                isTransitioningToTitle = true;
                transitionTimer = 0f;
            }
        }

        game.batch.end();

        // 4. DRAW CINEMATIC FADE OVERLAY
        if (isTransitioningToTitle) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(new Color(0.02f, 0.02f, 0.02f, screenFadeAlpha));
            shapeRenderer.rect(0, 0, worldW, worldH);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    private void updateTransitions(float dt) {
        if (transitionState == 3) {
            if (isTransitioningToTitle) {
                transitionTimer += dt;
                float progress = Math.min(1f, transitionTimer / RETRO_DURATION);

                promptScale = 1.0f + (MathUtils.sin(progress * MathUtils.PI) * 0.4f);
                screenFadeAlpha = Math.min(1f, progress * 1.2f);

                if (progress >= 1f) {
                    screenFinished = true;
                    game.setScreen(new TitleScreen(game));
                    dispose();
                }
            }
            return;
        }

        slideTimer += dt;

        switch (transitionState) {
            case 0:
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

            case 1:
                textAlpha = 1f;

                if (currentSlideIndex == -1) {
                    if (logoAnimation.isAnimationFinished(logoAnimationTime) && slideTimer >= LOGO_STAY_DURATION) {
                        transitionState = 2;
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

            case 2:
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
        if (shapeRenderer != null) shapeRenderer.dispose();

        if (logoIntroMusic != null) {
            logoIntroMusic.stop();
            logoIntroMusic.dispose();
        }

        if (logoAnimation != null) {
            for (TextureRegion frame : logoAnimation.getKeyFrames()) {
                frame.getTexture().dispose();
            }
        }
    }
}
