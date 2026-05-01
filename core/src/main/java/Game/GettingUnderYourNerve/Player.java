package Game.GettingUnderYourNerve;

import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {

    private Body playerBody;

    public static final float PPM = 32f;

    private final float JumpHeight;

    private int score;
    private int Hp;

    public float spawnX;
    public float spawnY;
    public boolean isDead = false;

    // ---------------- STATES ----------------
    public enum State {
        IDLE,
        RUNNING,
        JUMPING,
        FALLING
    }

    // --- KNOCKBACK & STUN VARIABLES ---
    public boolean isHit = false;
    private float hitTimer = 0f;

    public State currentState = State.IDLE;
    public State previousState = State.IDLE;

    // ---------------- ANIMATIONS ----------------
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> jumpAnimation;
    private Animation<TextureRegion> fallAnimation;
    private Animation<TextureRegion> hitAnimation; // NEW: The damage animation!

    private float stateTime = 0f;
    private boolean facingRight = true;

    // ---------------- DRAW SIZE ----------------
    public float drawWidth;
    public float drawHeight;

    // ---------------- COLLISION FLAGS ----------------
    public boolean isGrounded = false;
    private boolean isTouchingWall = false;

    public Player(float jh, GameAssetManager assets) {

        JumpHeight = jh;

        score = 0;
        Hp = 100;
        isGrounded = false;

        idleAnimation = assets.getAnimation(GameAssetManager.PLAYER_IDLE_PREFIX, 5, 0.15f, Animation.PlayMode.LOOP, "%02d");
        runAnimation = assets.getAnimation(GameAssetManager.PLAYER_RUN_PREFIX, 6, 0.15f, Animation.PlayMode.LOOP, "%02d");
        jumpAnimation = assets.getAnimation(GameAssetManager.PLAYER_JUMP_PREFIX, 3, 0.15f, Animation.PlayMode.NORMAL, "%02d");
        fallAnimation = assets.getAnimation(GameAssetManager.PLAYER_FALL_PREFIX, 1, 0.15f, Animation.PlayMode.NORMAL, "%02d");

        // NEW: Load the Hit Animation! (Adjust the frame count and format string to match your files!)
        hitAnimation = assets.getAnimation(GameAssetManager.PLAYER_HIT_PREFIX, 4, 0.1f, Animation.PlayMode.LOOP, "%02d");
    }

    // ---------------------------------------------------
    // SPAWN PLAYER FROM TILED
    // ---------------------------------------------------
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

        spawnX = startX + (drawWidth / 2f);
        spawnY = startY + (drawHeight / 2f);

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

        fdef.filter.categoryBits = Main.PLAYER_BIT;
        fdef.filter.maskBits = (short) (Main.GROUND_BIT | Main.ENEMY_BIT | Main.PROJECTILE_BIT | Main.COIN_BIT | Main.POTION_BIT | Main.WATER_BIT);

        playerBody.createFixture(fdef).setUserData(this);
        playerBody.setFixedRotation(true);
        shape.dispose();
    }

    // ---------------------------------------------------
    // UPDATE PLAYER
    // ---------------------------------------------------
    public void UpdatePlayer(float dt, World world) {

        if (isDead) {
            Respawn();
            return;
        }

        // --- STUN LOGIC ---
        if (isHit) {
            hitTimer += dt;
            if (hitTimer >= 0.5f) {
                isHit = false;
            } else {
                return; // Return early! Player cannot move left/right while stunned.
            }
        }

        isGrounded = false;
        isTouchingWall = false;

        for (Contact contact : world.getContactList()) {
            if (!contact.isTouching()) continue;

            Fixture fixA = contact.getFixtureA();
            Fixture fixB = contact.getFixtureB();

            if (fixA.getBody() == playerBody || fixB.getBody() == playerBody) {
                if (fixA.isSensor() || fixB.isSensor()) continue;

                WorldManifold manifold = contact.getWorldManifold();
                Vector2 normal = manifold.getNormal();

                if (Math.abs(normal.x) > 0.8f) isTouchingWall = true;

                if (fixA.getBody() == playerBody && normal.y <= -0.8f) isGrounded = true;
                else if (fixB.getBody() == playerBody && normal.y >= 0.8f) isGrounded = true;
            }
        }

        Vector2 vel = playerBody.getLinearVelocity();
        float desiredVel = 0;

        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean moveLeft = Gdx.input.isKeyPressed(Input.Keys.LEFT);

        if (moveLeft) desiredVel = -10f;
        else if (moveRight) desiredVel = 10f;

        playerBody.setLinearVelocity(desiredVel, vel.y);

        boolean pushingWall = (desiredVel < 0 && isTouchingWall) || (desiredVel > 0 && isTouchingWall);
        boolean falling = vel.y < 0;

        if (pushingWall && falling && !isGrounded) {
            playerBody.setLinearVelocity(vel.x, Math.max(vel.y, -2f));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && isGrounded) {
            ApplyJump();
        }
    }

    // ---------------------------------------------------
    // STATE MACHINE
    // ---------------------------------------------------
    private State getState() {
        Vector2 vel = playerBody.getLinearVelocity();

        if (!isGrounded) {
            if (vel.y > 0) return State.JUMPING;
            else return State.FALLING;
        }

        if (Math.abs(vel.x) > 0.1f) return State.RUNNING;

        return State.IDLE;
    }

    // ---------------------------------------------------
    // RENDER
    // ---------------------------------------------------
    public void Render(SpriteBatch batch, float dt) {

        if (isDead) return;

        float spriteDrawWidth = drawWidth * 2;
        float spriteDrawHeight = drawHeight * 2;

        TextureRegion frame = GetCurrentFrame(dt);

        batch.draw(
            frame,
            GetXpos() - spriteDrawWidth / 2f,
            GetYpos() - spriteDrawHeight / 2f,
            spriteDrawWidth,
            spriteDrawHeight
        );

    }

    public TextureRegion GetCurrentFrame(float dt) {
        TextureRegion region;

        // --- NEW: OVERRIDE NORMAL ANIMATION IF HIT ---
        if (isHit) {
            region = hitAnimation.getKeyFrame(hitTimer);
        } else {
            // Normal State Machine
            currentState = getState();
            switch (currentState) {
                case JUMPING: region = jumpAnimation.getKeyFrame(stateTime); break;
                case FALLING: region = fallAnimation.getKeyFrame(stateTime); break;
                case RUNNING: region = runAnimation.getKeyFrame(stateTime); break;
                default: region = idleAnimation.getKeyFrame(stateTime); break;
            }
        }

        // --- NEW: FIX MOONWALKING ---
        // We ONLY update which way we are facing if we are NOT being knocked back.
        // This ensures the player stares at the enemy while sliding backward!
        if (!isHit) {
            float velX = playerBody.getLinearVelocity().x;
            if (velX < -0.1f) facingRight = false;
            else if (velX > 0.1f) facingRight = true;
        }

        if (facingRight && region.isFlipX()) {
            region.flip(true, false);
        } else if (!facingRight && !region.isFlipX()) {
            region.flip(true, false);
        }

        // Only progress normal animation time if we are not hit
        if (!isHit) {
            stateTime = currentState == previousState ? stateTime + dt : 0;
            previousState = currentState;
        }

        return region;
    }

    // ---------------------------------------------------
    // GETTERS & GAMEPLAY MECHANICS
    // ---------------------------------------------------
    public float GetXpos() { return playerBody.getPosition().x; }
    public float GetYpos() { return playerBody.getPosition().y; }
    public Body getPlayerBody() { return playerBody; }

    public void addScore(int points) {
        score += points;
        System.out.println("Score is now: " + score);
    }

    public void addHp(int hp) {
        Hp = Math.min(Hp + hp, 100);
        System.out.println("HP is now: " + Hp);

        if(this.Hp <= 0 && !isDead) {
            System.out.println("CAPTAIN CLOWN NOSE HAS DIED!");
            isDead = true;
        }
    }

    public void hit(int damage, float sourceX) {
        if (isDead || isHit) return;

        addHp(-damage);

        isHit = true;
        hitTimer = 0f;
        isGrounded = false;

        float pushDirection = GetXpos() < sourceX ? -1f : 1f;

        playerBody.setLinearVelocity(0, 0);
        playerBody.applyLinearImpulse(new Vector2(pushDirection * 10f, 10f), playerBody.getWorldCenter(), true);

        System.out.println("OUCH! Player was knocked back!");
    }

    public int getScore(){ return score; }
    public int getHealth(){ return Hp; }
    public void OverridePos(float x, float y){ playerBody.setTransform(x, y, playerBody.getAngle()); playerBody.setLinearVelocity(0, 0); }
    public void OverrideScore(int score){ this.score = score; }
    public void OverrideHealth(int health){ this.Hp = health; }

    public void ApplyJump(){
        playerBody.applyLinearImpulse(new Vector2(0, JumpHeight), playerBody.getWorldCenter(), true);
    }

    public void Respawn() {
        this.Hp = 100;
        this.isDead = false;
        this.isHit = false;

        playerBody.setTransform(spawnX, spawnY, 0);
        playerBody.setLinearVelocity(0, 0);

        System.out.println("RESPAWNED!");
    }

    public void dispose() {}
}
