package Game.GettingUnderYourNerve;

import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class VerticalPlatform extends Platform {
    private float minY;
    private float maxY;
    private float currentTargetY;

    public VerticalPlatform(World world, Rectangle rect, float startY, float endY, float speed, GameAssetManager assets) {
        super(rect.width, rect.height, speed);

        float centerOffset = this.drawHeight / 2f;
        this.minY = Math.min(startY, endY) / Player.PPM + centerOffset;
        this.maxY = Math.max(startY, endY) / Player.PPM + centerOffset;
        this.currentTargetY = (startY < endY) ? maxY : minY;

        float lockedX = (rect.x / Player.PPM) + (this.drawWidth / 2f);

        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.KinematicBody;
        bdef.position.set(lockedX, (startY / Player.PPM) + centerOffset);
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(this.drawWidth / 2f, this.drawHeight / 2f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 1f;

        body.createFixture(fdef).setUserData(this);
        shape.dispose();

        // Fetch the Animation from the Vault (Using the "%02d" string format)
        animation = assets.getAnimation(GameAssetManager.PLATFORM_HELI_PREFIX, 4, 0.05f, Animation.PlayMode.LOOP, "%02d");
    }

    @Override
    public void update(float dt) {
        stateTime += dt; // Tick the animation clock

        Vector2 pos = body.getPosition();
        if (Math.abs(pos.y - currentTargetY) < 0.05f) {
            currentTargetY = (currentTargetY == minY) ? maxY : minY;
        }

        float direction = (currentTargetY > pos.y) ? 1 : -1;
        body.setLinearVelocity(0, direction * speed);
    }
}
