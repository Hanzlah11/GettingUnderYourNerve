package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.GameCam;
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

    public void skip() {
        this.finished = true;
    }

    // Helper to find rectangles in your "CutsceneData" layer[cite: 23, 25]
    protected Vector2 getObjectPos(String name) {
        for (MapObject object : screen.getPlayableMap().map.getLayers().get("CutsceneData").getObjects()) {
            if (object instanceof RectangleMapObject && name.equals(object.getName())) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                // Return the center of the rectangle in Meters[cite: 23]
                return new Vector2((rect.x + rect.width / 2f) / 32f, (rect.y + rect.height / 2f) / 32f);
            }
        }
        return new Vector2(0, 0);
    }

    public abstract void update(float dt);
    public boolean isFinished() { return finished; }
}
