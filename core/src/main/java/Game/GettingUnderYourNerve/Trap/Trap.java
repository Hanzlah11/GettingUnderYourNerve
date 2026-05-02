package Game.GettingUnderYourNerve.Trap;

import Game.GettingUnderYourNerve.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public abstract class Trap {

    protected World world;
    public Body body;
    protected Rectangle bounds;

    public float startX;
    public float startY;
    public float drawWidth;
    public float drawHeight;

    // CHANGED: Now takes MapObject to read Tiled Properties!
    public Trap(World world, MapObject object) {
        this.world = world;

        // Extract the rectangle from the object
        this.bounds = ((RectangleMapObject) object).getRectangle();

        this.startX = (bounds.x + bounds.width / 2f) / 32f;
        this.startY = (bounds.y + bounds.height / 2f) / 32f;

        this.drawWidth = bounds.width / 32f;
        this.drawHeight = bounds.height / 32f;
    }

    public abstract void update(float dt);
    public abstract void render(SpriteBatch batch, float dt);
    public abstract int getDamage();
    public abstract void onHit(Player p);
}
