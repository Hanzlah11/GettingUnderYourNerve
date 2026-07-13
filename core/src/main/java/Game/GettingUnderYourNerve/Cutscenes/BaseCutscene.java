package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.Utilities.GameCam;
import com.badlogic.gdx.Gdx;
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

public abstract class BaseCutscene {
    protected PlayScreen screen;
    protected Player player;
    protected Batman batman;
    protected GameCam cam;

    protected float stateTimer = 0;
    protected int state = 0;
    protected boolean finished = false;

    // --- Subtitle & Background System Utilities ---
    protected String currentSubtitle = "";
    private BitmapFont subtitleFont;
    private GlyphLayout subtitleLayout;
    private Texture blackBgTexture; // Used to draw the background box
    private boolean subtitle = true;

    public BaseCutscene(PlayScreen screen, Batman batman) {
        this.screen = screen;
        this.player = screen.getPlayer();
        this.batman = batman;
        this.cam = screen.getCam();

        // Load your custom TTF font with high contrast outlines
        this.subtitleFont = loadFont("ui/runescape_uf.ttf", 26);
        this.subtitleLayout = new GlyphLayout();

        // Generate a 1x1 white pixel texture programmatically to use as a tinted background box
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.blackBgTexture = new Texture(pixmap);
        pixmap.dispose(); // Always clean up native pixmaps immediately!
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

    public void skip() {
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

    /**
     * Renders overlays over the cutscene. Swaps projection matrices
     * to ensure subtitles render pixel-perfect at screen-space positions.
     */
    public void render(SpriteBatch batch) {
        // --- 1. Save the world-camera matrix so we don't disrupt map coordinates ---
        com.badlogic.gdx.math.Matrix4 oldMatrix = batch.getProjectionMatrix().cpy();

        // --- 2. Temporarily switch batch to screen-space pixel coordinates (800x480) ---
        com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
        uiMatrix.setToOrtho2D(0, 0, 800, 480);
        batch.setProjectionMatrix(uiMatrix);

        // =========================================================================
        // PERMANENT "PRESS R TO SKIP" PROMPT (TOP-RIGHT CORNER)
        // =========================================================================
        String skipText = "Press R to skip the cutscene...";
        com.badlogic.gdx.graphics.g2d.GlyphLayout skipLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        // We reuse your custom subtitleFont so it matches the Runescape text design style
        skipLayout.setText(subtitleFont, skipText);

        float skipPaddingX = 15f;
        float skipPaddingY = 8f;
        float skipBoxWidth = skipLayout.width + (skipPaddingX * 2);
        float skipBoxHeight = skipLayout.height + (skipPaddingY * 2);

        // Position it flush against the top-right margins of our 800x480 frame container
        float skipBoxX = 800f - skipBoxWidth - 20f; // 20px padding from right edge
        float skipBoxY = 480f - skipBoxHeight - 20f; // 20px padding from top edge

        // Save and color-tint the batch for the skip background box
        Color oldColor = batch.getColor().cpy();
        batch.setColor(0f, 0f, 0f, 0.50f); // Slightly softer 50% opacity dark bar box
        batch.draw(blackBgTexture, skipBoxX, skipBoxY, skipBoxWidth, skipBoxHeight);
        batch.setColor(oldColor); // Instantly restore standard color mask

        // Draw prompt message text
        subtitleFont.draw(batch, skipText, skipBoxX + skipPaddingX, skipBoxY + skipPaddingY + skipLayout.height);
        // =========================================================================

        // --- 3. Render Dialogue Subtitles (Only if one exists and subtitles are enabled) ---
        if (currentSubtitle != null && !currentSubtitle.isEmpty() && subtitle) {
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

            // Draw Subtitle Box
            batch.setColor(0f, 0f, 0f, 0.65f);
            batch.draw(blackBgTexture, boxX, boxY, boxWidth, boxHeight);
            batch.setColor(oldColor);

            // Draw Subtitle Text
            float textX = (800 - maxTextWidth) / 2f;
            float textY = boxY + paddingY + textHeight;
            subtitleFont.draw(batch, subtitleLayout, textX, textY);
        }

        // --- 6. Restore original world camera matrix back to standard rendering ---
        batch.setProjectionMatrix(oldMatrix);
    }
}
