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
        if (currentSubtitle == null || currentSubtitle.isEmpty() || !subtitle) return;

        // 1. Save the world-camera matrix so we don't disrupt map coordinates
        com.badlogic.gdx.math.Matrix4 oldMatrix = batch.getProjectionMatrix().cpy();

        // 2. Temporarily switch batch to screen-space pixel coordinates (800x480)
        com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
        uiMatrix.setToOrtho2D(0, 0, 800, 480);
        batch.setProjectionMatrix(uiMatrix);

        // 3. Measure text bounds AND apply center-alignment for text inside the layout
        float maxTextWidth = 600f; // Gives room for your text to align itself within
        subtitleLayout.setText(
            subtitleFont,
            currentSubtitle,
            Color.WHITE,
            maxTextWidth,
            com.badlogic.gdx.utils.Align.center, // <-- Forces internal lines to center
            true
        );

        // GlyphLayout width reflects the longest line, height reflects total wrapped lines
        float textWidth = subtitleLayout.width;
        float textHeight = subtitleLayout.height;

        float paddingX = 20f;
        float paddingY = 12f;

        // Calculate box constraints to wrap the text center-bottom perfectly
        float boxWidth = textWidth + (paddingX * 2);
        float boxHeight = textHeight + (paddingY * 2);
        float boxX = (800 - boxWidth) / 2f;
        float boxY = 45f; // Position from screen floor

        // 4. Draw the semi-transparent black background box first
        Color oldColor = batch.getColor().cpy();
        batch.setColor(0f, 0f, 0f, 0.65f); // 65% opacity black background box
        batch.draw(blackBgTexture, boxX, boxY, boxWidth, boxHeight);
        batch.setColor(oldColor); // Instantly restore default batch rendering tint

        // 5. FIXED: Draw the layout object directly instead of the raw string!
        // Since Align.center relies on a target width context, we align it over the actual
        // width of the layout text box bounding container.
        float textX = (800 - maxTextWidth) / 2f;
        float textY = boxY + paddingY + textHeight; // Font drawing works from the baseline up

        subtitleFont.draw(batch, subtitleLayout, textX, textY);

        // 6. Restore original world camera matrix back to standard rendering
        batch.setProjectionMatrix(oldMatrix);
    }

    // --- NEW: Clean up disposable assets to prevent memory leaks ---
    public void dispose() {
        if (subtitleFont != null) {
            subtitleFont.dispose();
        }
        if (blackBgTexture != null) {
            blackBgTexture.dispose();
        }
    }
}
