package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public abstract class BaseCutscene {
    protected PlayScreen screen;
    protected Player player;
    protected Batman batman;
    protected GameCam cam;

    protected float stateTimer = 0;
    protected int state = 0;
    protected boolean finished = false;
    protected boolean isSkippable = true; // Cutscenes default to skippable

    // --- Subtitle & Background System Utilities ---
    protected String currentSubtitle = "";
    private BitmapFont subtitleFont;
    private GlyphLayout subtitleLayout;
    private Texture blackBgTexture;

    private Preferences prefs;
    private Rectangle skipBoxRect = new Rectangle();
    private Vector3 touchPoint = new Vector3();

    public BaseCutscene(PlayScreen screen, Batman batman) {
        this.screen = screen;
        this.player = screen.getPlayer();
        this.batman = batman;
        this.cam = screen.getCam();

        this.prefs = Gdx.app.getPreferences("GettingUnderYourNerve_Settings");

        this.subtitleFont = loadFont("ui/runescape_uf.ttf", 26);
        this.subtitleLayout = new GlyphLayout();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.blackBgTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    private BitmapFont loadFont(String filename, int size) {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(filename));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = size;
            p.color = Color.WHITE;
            p.borderWidth = 2.0f;
            p.borderColor = new Color(0f, 0f, 0f, 0.8f);

            BitmapFont f = gen.generateFont(p);
            gen.dispose();
            return f;
        } catch (Exception e) {
            return new BitmapFont();
        }
    }

    public boolean isSkippable() {
        return isSkippable;
    }

    public void skip() {
        if (!isSkippable) return; // Prevent skipping if flagged as unskippable
        AudioManager.stopAllSFXAndDialogue();
        this.finished = true;
        this.currentSubtitle = "";
    }

    protected Vector2 getObjectPos(String name) {
        com.badlogic.gdx.maps.MapLayer layer = screen.getPlayableMap().map.getLayers().get("CutsceneData");
        if (layer == null) {
            System.out.println("ERROR: 'CutsceneData' layer missing in Tiled!");
            return new Vector2(0, 0);
        }

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject && name.equals(object.getName())) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                return new Vector2((rect.x + rect.width / 2f) / 32f, (rect.y + rect.height / 2f) / 32f);
            }
        }
        return new Vector2(0, 0);
    }

    public abstract void update(float dt);

    public boolean isFinished() {
        return finished;
    }

    public void render(SpriteBatch batch) {
        com.badlogic.gdx.math.Matrix4 oldMatrix = batch.getProjectionMatrix().cpy();

        com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
        uiMatrix.setToOrtho2D(0, 0, 800, 480);
        batch.setProjectionMatrix(uiMatrix);

        // TOP-RIGHT CUTSCENE PROMPT BANNER (Cross-platform prompt)
        String skipText = isSkippable ? "Tap / Press R to skip..." : "This cutscene cannot be skipped...";
        com.badlogic.gdx.graphics.g2d.GlyphLayout skipLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        skipLayout.setText(subtitleFont, skipText);

        float skipPaddingX = 15f;
        float skipPaddingY = 8f;
        float skipBoxWidth = skipLayout.width + (skipPaddingX * 2);
        float skipBoxHeight = skipLayout.height + (skipPaddingY * 2);

        float skipBoxX = 800f - skipBoxWidth - 20f;
        float skipBoxY = 480f - skipBoxHeight - 20f;
        skipBoxRect.set(skipBoxX, skipBoxY, skipBoxWidth, skipBoxHeight);

        // Check mobile tap / mouse click on the skip banner
        if (isSkippable && Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            // Translate touch to UI 800x480 coordinate space
            float touchX = (Gdx.input.getX() / (float) Gdx.graphics.getWidth()) * 800f;
            float touchY = (1f - (Gdx.input.getY() / (float) Gdx.graphics.getHeight())) * 480f;

            if (skipBoxRect.contains(touchX, touchY)) {
                skip();
            }
        }

        Color oldColor = batch.getColor().cpy();
        batch.setColor(0f, 0f, 0f, 0.50f);
        batch.draw(blackBgTexture, skipBoxX, skipBoxY, skipBoxWidth, skipBoxHeight);
        batch.setColor(oldColor);

        subtitleFont.draw(batch, skipText, skipBoxX + skipPaddingX, skipBoxY + skipPaddingY + skipLayout.height);

        boolean subtitlesEnabled = prefs.getBoolean("subtitles", true);

        if (currentSubtitle != null && !currentSubtitle.isEmpty() && subtitlesEnabled) {
            float maxTextWidth = 600f;
            subtitleLayout.setText(
                subtitleFont,
                currentSubtitle,
                Color.WHITE,
                maxTextWidth,
                com.badlogic.gdx.utils.Align.center,
                true
            );

            float textWidth = subtitleLayout.width;
            float textHeight = subtitleLayout.height;

            float paddingX = 20f;
            float paddingY = 12f;

            float boxWidth = textWidth + (paddingX * 2);
            float boxHeight = textHeight + (paddingY * 2);
            float boxX = (800 - boxWidth) / 2f;
            float boxY = 45f;

            batch.setColor(0f, 0f, 0f, 0.65f);
            batch.draw(blackBgTexture, boxX, boxY, boxWidth, boxHeight);
            batch.setColor(oldColor);

            float textX = (800 - maxTextWidth) / 2f;
            float textY = boxY + paddingY + textHeight;
            subtitleFont.draw(batch, subtitleLayout, textX, textY);
        }

        batch.setProjectionMatrix(oldMatrix);
    }

    public void dispose() {
        AudioManager.stopAllSFXAndDialogue();
        if (subtitleFont != null) {
            subtitleFont.dispose();
        }
        if (blackBgTexture != null) {
            blackBgTexture.dispose();
        }
    }
}
