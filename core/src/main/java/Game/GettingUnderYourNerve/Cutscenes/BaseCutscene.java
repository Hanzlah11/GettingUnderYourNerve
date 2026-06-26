package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.Utilities.GameCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

    protected String currentSubtitle = ""; // Holds the active line of dialogue
    private com.badlogic.gdx.graphics.g2d.BitmapFont subtitleFont;
    private com.badlogic.gdx.graphics.g2d.GlyphLayout subtitleLayout;

    public BaseCutscene(PlayScreen screen, Batman batman) {
        this.screen = screen;
        this.player = screen.getPlayer();
        this.batman = batman;
        this.cam = screen.getCam();

        this.subtitleFont = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        this.subtitleFont.getData().setScale(1.5f); // Make text readable
        this.subtitleFont.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        this.subtitleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
    }

    /**
     * Sets the finished flag to true, signaling the PlayScreen to resume normal gameplay.
     */
    public void skip() {
        this.finished = true;
        screen.increaseLevelAudio(0.5f);
    }

    protected BitmapFont loadFont(String filename, int size) {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(filename));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = size; p.color = Color.WHITE; p.borderWidth = 1.5f; p.borderColor = new Color(0f, 0f, 0f, 0.6f);
            BitmapFont f = gen.generateFont(p); gen.dispose(); return f;
        } catch (Exception e) { return new BitmapFont(); }
    }

    /**
     * Helper to find rectangles in the "CutsceneData" Tiled layer and return their center in meters.
     * Includes a safety check to prevent crashes if the layer is missing.
     */
    protected Vector2 getObjectPos(String name) {
        com.badlogic.gdx.maps.MapLayer layer = screen.getPlayableMap().map.getLayers().get("CutsceneData");

        // Safety check to prevent NullPointerException if the layer isn't in the TMX file
        if (layer == null) {
            System.out.println("ERROR: 'CutsceneData' layer missing in Tiled!");
            return new Vector2(0, 0);
        }

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject && name.equals(object.getName())) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                // Return the center of the rectangle converted to Box2D meters (divided by 32)
                return new Vector2((rect.x + rect.width / 2f) / 32f, (rect.y + rect.height / 2f) / 32f);
            }
        }
        return new Vector2(0, 0);
    }

    /**
     * Must be implemented by subclasses to handle cutscene logic and state transitions.
     */
    public abstract void update(float dt);

    public boolean isFinished() {
        return finished;
    }

    /**
     * Optional method for subclasses to draw extra elements (like Pokéballs or text) during a cutscene.
     */
    public void render(SpriteBatch batch) {
        if (currentSubtitle == null || currentSubtitle.isEmpty()) return;

        // 1. Save the world-camera matrix so we don't disrupt map rendering
        com.badlogic.gdx.math.Matrix4 oldMatrix = batch.getProjectionMatrix().cpy();

        // 2. Temporarily switch batch to screen-space pixel coordinates (800x480)
        com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
        uiMatrix.setToOrtho2D(0, 0, 800, 480);
        batch.setProjectionMatrix(uiMatrix);

        // 3. Calculate text width to perfectly center it
        subtitleLayout.setText(subtitleFont, currentSubtitle);
        float x = (800 - subtitleLayout.width) / 2f;
        float y = 60f; // 60 pixels up from the bottom of the screen

        // 4. Draw subtitle text
        subtitleFont.draw(batch, currentSubtitle, x, y);

        // 5. Restore original world matrix for subsequent drawing calls
        batch.setProjectionMatrix(oldMatrix);
    }
}
