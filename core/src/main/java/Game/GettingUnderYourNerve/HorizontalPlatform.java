package Game.GettingUnderYourNerve;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class HorizontalPlatform extends Platform {
    private float minX;
    private float maxX;
    private float currentTargetX;

    public HorizontalPlatform(World world, Rectangle rect, float startX, float endX, float speed) {
        // 1. Pass the basics up to the parent Platform class
        super(rect.width, rect.height, speed);

        // 2. Setup Horizontal bounds
        float centerOffset = this.drawWidth / 2f;
        this.minX = Math.min(startX, endX) / Player.PPM + centerOffset;
        this.maxX = Math.max(startX, endX) / Player.PPM + centerOffset;
        this.currentTargetX = (startX < endX) ? maxX : minX;

        float lockedY = (rect.y / Player.PPM) + (this.drawHeight / 2f);

        // 3. Setup Box2D Body
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

        // 4. Load the Animation!
        // IMPORTANT: Update these strings to match your actual folder and file names
        loadAnimation("Treasure Hunters/Palm Tree Island/Sprites/helicopter", "helicopter", 4, 0.05f);
    }

    @Override
    public void update(float dt) {
        stateTime += dt; // Tick the animation clock

        Vector2 pos = body.getPosition();
        if (Math.abs(pos.x - currentTargetX) < 0.05f) {
            currentTargetX = (currentTargetX == minX) ? maxX : minX;
        }

        float direction = (currentTargetX > pos.x) ? 1 : -1;
        body.setLinearVelocity(direction * speed, 0);
    }
}
