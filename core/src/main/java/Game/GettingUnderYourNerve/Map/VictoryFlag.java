package Game.GettingUnderYourNerve.Map;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;

public class VictoryFlag {

    public Body body;
    private Animation<TextureRegion> flagAnim;
    private float stateTime;
    private Rectangle bounds;

    // Prevent "Victory" from printing 60 times a second
    public boolean isReached = false;

    public VictoryFlag(World world, Rectangle rect, GameAssetManager assets) {
        this.bounds = rect;
        this.stateTime = 0f;

        // Load the animation (Update the frame count if your flag has more/less than 4 frames!)
        flagAnim = assets.getAnimation(GameAssetManager.FLAG_PREFIX, 9, 0.15f, Animation.PlayMode.LOOP, "%d");

        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set((rect.x + rect.width / 2f) / Main.PPM, (rect.y + rect.height / 2f) / Main.PPM);

        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((rect.width / 2f) / Main.PPM, (rect.height / 2f) / Main.PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.isSensor = true; // CRITICAL: This makes it non-blocking!
        fdef.filter.categoryBits = Main.TRIGGER_BIT;
        fdef.filter.maskBits = Main.PLAYER_BIT;

        body.createFixture(fdef).setUserData(this);
        shape.dispose();
    }

    public void update(float dt) {
        stateTime += dt;
    }

    public void render(SpriteBatch batch) {
        if (flagAnim != null) {
            TextureRegion currentFrame = flagAnim.getKeyFrame(stateTime);
            // Draw it perfectly inside the rectangle you drew in Tiled
            batch.draw(currentFrame, bounds.x / Main.PPM, bounds.y / Main.PPM, bounds.width / Main.PPM, bounds.height / Main.PPM);
        }
    }

    public void onPlayerReach() {
        if (!isReached) {
            isReached = true;
            System.out.println("Victory");
            // You can trigger your level transition or victory screen here later!
        }
    }
}
