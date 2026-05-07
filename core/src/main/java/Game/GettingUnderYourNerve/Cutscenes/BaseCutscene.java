package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.Utilities.GameCam;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    public BaseCutscene(PlayScreen screen, Batman batman) {
        this.screen = screen;
        this.player = screen.getPlayer();
        this.batman = batman;
        this.cam = screen.getCam();
    }

    /**
     * Sets the finished flag to true, signaling the PlayScreen to resume normal gameplay.
     */
    public void skip() {
        this.finished = true;
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
        // Default implementation is empty. Subclasses like BossCutscene will override this.
    }
}
