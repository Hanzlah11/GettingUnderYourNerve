package Game.GettingUnderYourNerve.Collectables;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;

public abstract class Collectable {
    public Body body;
    public boolean isCollected = false;
    public boolean isDestroyed = false; // Crucial for safe Box2D deletion

    protected float drawWidth;
    protected float drawHeight;
    protected Animation<TextureRegion> animation;
    protected float stateTime = 0f;

    // Notice: rawTextures and loadAnimation() are completely gone.
}
