package Game.GettingUnderYourNerve.Trolls;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;

public class DevilPlatform {

    public Body body;

    // --- Animation Variables ---
    private Animation<TextureRegion> animation;
    private float stateTime = 0f;

    private float width, height;

    // Normal Patrol Bounds
    private float startX, endX;
    private float normalSpeed;
    private float evadeSpeed;
    private boolean movingRight = true;

    // Troll Mechanics
    private float detectionRadius = 3.5f; // Tiles away before it triggers
    private boolean isEvading = false;

    public DevilPlatform(World world, Rectangle rect, float startX, float endX, float speed, GameAssetManager assets) {
        this.startX = startX / Main.PPM;
        this.endX = endX / Main.PPM;

        // Safety checks to ensure it NEVER stays stationary!
        this.normalSpeed = (speed == 0) ? 2f : speed;
        if (Math.abs(this.startX - this.endX) < 0.1f) {
            this.endX = this.startX + (3f * 32f / Main.PPM); // Force 3-tile movement range if missing
        }

        this.evadeSpeed = this.normalSpeed * 6.5f;

        this.width = rect.width / Main.PPM;
        this.height = rect.height / Main.PPM;

        // --- ANIMATION SETUP ---
        // TODO: Replace this exact line with the animation fetching logic you use in HorizontalPlatform!
        // Example: this.animation = assets.getAnimation(GameAssetManager.PLATFORM_PREFIX, 4, 0.15f, Animation.PlayMode.LOOP, "%02d");
        this.animation = assets.getAnimation(GameAssetManager.PLATFORM_HELI_PREFIX, 4, 0.1f, Animation.PlayMode.LOOP, "%02d");

        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.KinematicBody;
        bdef.position.set((rect.x + rect.width / 2f) / Main.PPM, (rect.y + rect.height / 2f) / Main.PPM);
        body = world.createBody(bdef);

        // Kickstart velocity immediately so it doesn't wait
        body.setLinearVelocity(normalSpeed, 0);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f, height / 2f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 1f;
        fdef.filter.categoryBits = Main.GROUND_BIT;
        fdef.filter.maskBits = Main.PLAYER_BIT | Main.ENEMY_BIT;
        body.createFixture(fdef).setUserData(this);

        shape.dispose();
    }

    public void update(float dt, Player player) {
        stateTime += dt; // Progress the animation frame

        float platX = body.getPosition().x;
        float platY = body.getPosition().y;
        float playerX = player.GetXpos();
        float playerY = player.GetYpos();

        // 1. Check if the player is dangerously close AND above the platform
        boolean playerIsNear = Math.abs(playerX - platX) < detectionRadius && (playerY > platY);

        if (playerIsNear) {
            isEvading = true;
            // Dart away from the player!
            float evadeDirection = (platX >= playerX) ? 1f : -1f;
            body.setLinearVelocity(evadeDirection * evadeSpeed, 0);
        } else {
            isEvading = false;

            // 2. Return to the normal patrol zone if it ran off the tracks
            if (platX < startX) {
                body.setLinearVelocity(normalSpeed, 0);
                movingRight = true;
            } else if (platX > endX) {
                body.setLinearVelocity(-normalSpeed, 0);
                movingRight = false;
            }
            // 3. Do normal patrol routine
            else {
                if (movingRight) {
                    body.setLinearVelocity(normalSpeed, 0);
                    if (platX >= endX) movingRight = false;
                } else {
                    body.setLinearVelocity(-normalSpeed, 0);
                    if (platX <= startX) movingRight = true;
                }
            }
        }
    }

    public void draw(SpriteBatch batch) {
        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
            batch.draw(currentFrame, body.getPosition().x - width / 2f, body.getPosition().y - height / 2f, width, height);
        }
    }
}
