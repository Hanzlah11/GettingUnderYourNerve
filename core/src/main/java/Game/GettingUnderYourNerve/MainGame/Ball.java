package Game.GettingUnderYourNerve.MainGame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Ball {
    private final Vector2 position;
    private final Vector2 velocity;
    private final Rectangle bounds;
    private final TextureRegion texture;

    private final float FRICTION = 0.985f;

    public Ball(float x, float y, Texture ballTexture) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);

        this.bounds = new Rectangle(x, y, 16, 16);
        this.texture = new TextureRegion(ballTexture);
    }

    public void update(float dt) {
        position.add(velocity.x * dt, velocity.y * dt);
        bounds.setPosition(position);

        velocity.scl(FRICTION);

        if (velocity.len() < 4f) {
            velocity.set(0, 0);
        }
    }

    public void bounceOffWall(Rectangle wall) {
        if (position.x <= wall.x || position.x + bounds.width >= wall.x + wall.width) {
            velocity.x = -velocity.x * 0.75f;

            if (position.x <= wall.x) position.x = wall.x - bounds.width;
            else position.x = wall.x + wall.width;
        }

        if (position.y <= wall.y || position.y + bounds.height >= wall.y + wall.height) {
            velocity.y = -velocity.y * 0.75f;

            if (position.y <= wall.y) position.y = wall.y - bounds.height;
            else position.y = wall.y + wall.height;
        }

        bounds.setPosition(position);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
    }

    public Vector2 getPosition() { return position; }
    public Vector2 getVelocity() { return velocity; }
    public Rectangle getBounds() { return bounds; }
    public void setVelocity(float vx, float vy) { this.velocity.set(vx, vy); }
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.bounds.setPosition(x, y);
    }
}
