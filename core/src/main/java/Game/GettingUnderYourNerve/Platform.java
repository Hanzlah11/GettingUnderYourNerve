package Game.GettingUnderYourNerve;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;

public abstract class Platform {
    protected Body body;
    protected float speed;
    public float drawWidth;
    public float drawHeight;

    // --- ANIMATION VARIABLES ---
    protected Animation<TextureRegion> animation;
    protected float stateTime = 0f;

    public Platform(float width, float height, float speed) {
        this.drawWidth = width / Player.PPM;
        this.drawHeight = height / Player.PPM;
        this.speed = speed;
    }

    // Every child class MUST implement this method to handle its unique movement
    public abstract void update(float dt);

    // --- UNIVERSAL DRAW METHOD ---
    public void draw(SpriteBatch batch) {
        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime);
            batch.draw(currentFrame,
                body.getPosition().x - (drawWidth / 2f),
                body.getPosition().y - (drawHeight / 2f),
                drawWidth, drawHeight);
        }
    }
}
