package Game.GettingUnderYourNerve.Decorations;

import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class PalmTree {

    private Animation<TextureRegion> animation;
    private float stateTime;

    private float x, y;
    private float width, height;

    public PalmTree(Rectangle rect) {

        animation = GameAssetManager.getAnimation(
            GameAssetManager.PALM_PLAIN_PREFIX,
            4,                      // number of frames
            0.20f,
            Animation.PlayMode.LOOP,
            "%02d"
        );

        x = rect.x / 32f;
        y = rect.y / 32f;

        width = rect.width / 32f;
        height = rect.height / 32f;
    }

    public void update(float dt) {
        stateTime += dt;
    }

    public void render(SpriteBatch batch) {
        batch.draw(animation.getKeyFrame(stateTime),
            x, y,
            width, height);
    }
}
