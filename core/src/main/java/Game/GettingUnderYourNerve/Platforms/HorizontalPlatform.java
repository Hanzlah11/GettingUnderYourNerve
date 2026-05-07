package Game.GettingUnderYourNerve.Platforms;

import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class HorizontalPlatform extends Platform {
    private float minX;
    private float maxX;
    private float currentTargetX;

    public HorizontalPlatform(World world, Rectangle rect, float startX, float endX, float speed, GameAssetManager assets) {

        super(rect.width, rect.height, speed);


        float centerOffset = this.drawWidth / 2f;
        this.minX = Math.min(startX, endX) / Player.PPM + centerOffset;
        this.maxX = Math.max(startX, endX) / Player.PPM + centerOffset;
        this.currentTargetX = (startX < endX) ? maxX : minX;

        float lockedY = (rect.y / Player.PPM) + (this.drawHeight / 2f);


        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.KinematicBody;
        bdef.position.set((startX / Player.PPM) + centerOffset, lockedY);
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(this.drawWidth / 2f, this.drawHeight / 2f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 1f;

        body.createFixture(fdef).setUserData(this);
        shape.dispose();


        animation = assets.getAnimation(GameAssetManager.PLATFORM_HELI_PREFIX, 4, 0.05f, Animation.PlayMode.LOOP, "%02d");
    }

    @Override
    public void update(float dt) {
        stateTime += dt;

        Vector2 pos = body.getPosition();
        if (Math.abs(pos.x - currentTargetX) < 0.05f) {
            currentTargetX = (currentTargetX == minX) ? maxX : minX;
        }

        float direction = (currentTargetX > pos.x) ? 1 : -1;
        body.setLinearVelocity(direction * speed, 0);
    }
}
