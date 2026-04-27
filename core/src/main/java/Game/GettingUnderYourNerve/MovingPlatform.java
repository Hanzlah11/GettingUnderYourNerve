package Game.GettingUnderYourNerve;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class MovingPlatform {
    private Body body;
    private Vector2 startPos;
    private float distance;
    private float speed;
    private boolean isHorizontal;
    private TextureRegion textureRegion; // Changed from Texture
    private float drawWidth;
    private float drawHeight;

    public MovingPlatform(World world, TextureRegion textureRegion, float x, float y, float width, float height, float distance, float speed, boolean isHorizontal) {
        this.distance = distance;
        this.speed = speed;
        this.isHorizontal = isHorizontal;
        this.startPos = new Vector2(x, y);
        this.textureRegion = textureRegion;
        this.drawWidth = width;
        this.drawHeight = height;
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.KinematicBody;
        bdef.position.set(x, y);
        body = world.createBody(bdef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f, height / 2f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 1f;

        body.createFixture(fdef);
        shape.dispose();
        if (isHorizontal) {
            body.setLinearVelocity(speed, 0);
        } else {
            body.setLinearVelocity(0, speed);
        }
    }
    public void update() {              //updates velocity
        Vector2 pos = body.getPosition();

        if (isHorizontal) {
            if (pos.x >= startPos.x + distance) {
                body.setLinearVelocity(-speed, 0);
            } else if (pos.x <= startPos.x) {
                body.setLinearVelocity(speed, 0);
            }
        } else {
            if (pos.y >= startPos.y + distance) {
                body.setLinearVelocity(0, -speed);
            } else if (pos.y <= startPos.y) {
                body.setLinearVelocity(0, speed);
            }
        }
    }
    public void render(SpriteBatch batch) {
        batch.draw(textureRegion,body.getPosition().x - (drawWidth / 2f), body.getPosition().y - (drawHeight / 2f),drawWidth, drawHeight);
    }
}
