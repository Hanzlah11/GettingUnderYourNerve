package Game.GettingUnderYourNerve;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class Player {
    private Body playerBody;
    public static final float PPM = 32f; // Pixels Per Meter
    private final float JumpHeight;

    // --- ANIMATION STATES ---
    public enum State { IDLE, RUNNING, JUMPING, FALLING }
    public State currentState = State.IDLE;
    public State previousState = State.IDLE;

    // --- ANIMATIONS & MEMORY ---
    private Array<Texture> rawTextures; // Holds EVERY texture to prevent memory leaks

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> jumpAnimation;
    private Animation<TextureRegion> fallAnimation;

    private float stateTime = 0f;
    private boolean facingRight = true;

    public float drawWidth;
    public float drawHeight;

    public Player(float jh) {
        JumpHeight = jh;
        rawTextures = new Array<Texture>();

        // LOAD ALL ANIMATIONS HERE
        // Format: (baseFileName, numberOfFrames, durationPerFrame, playMode)
        // Adjust the numbers here based on how many frames your actual animations have!

        idleAnimation = loadAnimation("09-Idle Sword", 5, 0.15f, Animation.PlayMode.LOOP);
        runAnimation = loadAnimation("10-Run Sword", 6, 0.15f, Animation.PlayMode.LOOP);

        // Jump and Fall usually look better as NORMAL (play once and stick on the last frame)
        // rather than looping continuously in mid-air.
        jumpAnimation = loadAnimation("11-Jump Sword", 3, 0.15f, Animation.PlayMode.NORMAL);
        fallAnimation = loadAnimation("12-Fall Sword", 1, 0.15f, Animation.PlayMode.NORMAL);
    }


    // --- NEW: ANIMATION HELPER METHOD ---
    private Animation<TextureRegion> loadAnimation(String folderName, int frameCount, float frameDuration, Animation.PlayMode mode) {
        // If folderName is "09-Idle Sword", parts[1] becomes "Idle Sword"
        String[] parts = folderName.split("-");
        String filePrefix = parts[1];

        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            // String.format("%02d", number) forces the number to have two digits (01, 02, 10, 11)
            String frameNumber = String.format("%02d", i + 1);

            // Build the exact, safe file path
            String filePath = "Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/" +
                folderName + "/" + filePrefix + " " + frameNumber + ".png";

            Texture tex = new Texture(filePath);
            rawTextures.add(tex); // Add to our master list for disposal later
            frames[i] = new TextureRegion(tex);
        }

        Animation<TextureRegion> anim = new Animation<TextureRegion>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    public void SpawnPlayerFromTiled(TiledMap map, World world) {
        MapLayer objectLayer = map.getLayers().get("Objects");

        float startX = 200 / PPM;
        float startY = 200 / PPM;
        drawWidth = 32 / PPM;
        drawHeight = 32 / PPM;

        if (objectLayer != null) {
            MapObject playerObj = objectLayer.getObjects().get("Player");
            if (playerObj instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) playerObj).getRectangle();
                startX = rect.x / PPM;
                startY = rect.y / PPM;
                drawWidth = rect.width / PPM;
                drawHeight = rect.height / PPM;
            }
        }

        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(startX + (drawWidth / 2f), startY + (drawHeight / 2f));
        playerBody = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        float w = drawWidth / 2f;
        float h = drawHeight / 2f;
        float bevel = 2 / PPM;

        Vector2[] vertices = new Vector2[8];
        vertices[0] = new Vector2(-w + bevel, -h);
        vertices[1] = new Vector2(w - bevel, -h);
        vertices[2] = new Vector2(w, -h + bevel);
        vertices[3] = new Vector2(w, h - bevel);
        vertices[4] = new Vector2(w - bevel, h);
        vertices[5] = new Vector2(-w + bevel, h);
        vertices[6] = new Vector2(-w, h - bevel);
        vertices[7] = new Vector2(-w, -h + bevel);

        shape.set(vertices);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 0f;
        fdef.density = 1f;

        playerBody.createFixture(fdef);
        playerBody.setFixedRotation(true);
        shape.dispose();
    }

    public void UpdatePlayer(World world) {
        Vector2 vel = playerBody.getLinearVelocity();
        float desiredVel = 0;

        boolean isPushingRight = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean isPushingLeft = Gdx.input.isKeyPressed(Input.Keys.LEFT);

        if (isPushingLeft) {
            desiredVel = -5f;
        } else if (isPushingRight) {
            desiredVel = 5f;
        }

        playerBody.setLinearVelocity(desiredVel, vel.y);

        // Wall Slide Check
        boolean isTouchingWall = false;
        for (Contact contact : world.getContactList()) {
            if (contact.isTouching()) {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();

                if (fixA.getBody() == playerBody || fixB.getBody() == playerBody) {
                    WorldManifold manifold = contact.getWorldManifold();
                    Vector2 normal = manifold.getNormal();
                    if (Math.abs(normal.x) > 0.8f) {
                        isTouchingWall = true;
                        break;
                    }
                }
            }
        }

        boolean isPushingWall = (desiredVel < 0 && isTouchingWall) || (desiredVel > 0 && isTouchingWall);
        boolean isFalling = vel.y < 0;

        if (isPushingWall && isFalling) {
            float slideSpeed = Math.max(vel.y, -2f);
            playerBody.setLinearVelocity(vel.x, slideSpeed);
        }

        // Jump Logic
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && Math.abs(vel.y) < 0.1f) {
            playerBody.applyLinearImpulse(new Vector2(0, JumpHeight), playerBody.getWorldCenter(), true);
        }
    }

    private State getState() {
        Vector2 vel = playerBody.getLinearVelocity();
        if (vel.y > 0.1f) {
            return State.JUMPING;
        } else if (vel.y < -0.1f) {
            return State.FALLING;
        } else if (Math.abs(vel.x) > 0.1f) {
            return State.RUNNING;
        } else {
            return State.IDLE;
        }
    }

    public TextureRegion GetCurrentFrame(float dt) {
        currentState = getState();
        TextureRegion region;

        // 1. Choose Animation based on State
        switch (currentState) {
            case JUMPING:
                // Notice we ask the animation for the frame, not a static texture!
                region = jumpAnimation.getKeyFrame(stateTime);
                break;
            case FALLING:
                region = fallAnimation.getKeyFrame(stateTime);
                break;
            case RUNNING:
                region = runAnimation.getKeyFrame(stateTime);
                break;
            case IDLE:
            default:
                region = idleAnimation.getKeyFrame(stateTime);
                break;
        }

        // 2. Handle Flipping
        float velX = playerBody.getLinearVelocity().x;
        if (velX < -0.1f) {
            facingRight = false;
        } else if (velX > 0.1f) {
            facingRight = true;
        }

        if (facingRight && region.isFlipX()) {
            region.flip(true, false);
        } else if (!facingRight && !region.isFlipX()) {
            region.flip(true, false);
        }

        // 3. Update Timer (Resets to 0 if the animation state changes)
        stateTime = currentState == previousState ? stateTime + dt : 0;
        previousState = currentState;

        return region;
    }

    public float GetXpos() {
        return playerBody.getPosition().x;
    }

    public float GetYpos() {
        return playerBody.getPosition().y;
    }

    public void dispose() {
        // One clean loop to dispose of every single texture we ever loaded
        for (Texture tex : rawTextures) {
            tex.dispose();
        }
    }

    public Body getPlayerBody() {
        return playerBody;
    }
}
