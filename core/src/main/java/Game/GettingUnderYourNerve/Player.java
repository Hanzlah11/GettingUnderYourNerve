package Game.GettingUnderYourNerve;

import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
        FALLING,
        ATTACKING,
        SLIDING
    }

    // --- KNOCKBACK & STUN VARIABLES ---
    public boolean isHit = false;
    private float hitTimer = 0f;
    private float attackTimer;

    public State currentState = State.IDLE;
    public State previousState = State.IDLE;

    // ---------------- ANIMATIONS ----------------
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> jumpAnimation;
    private Animation<TextureRegion> fallAnimation;
    private Animation<TextureRegion> hitAnimation;
    private Animation<TextureRegion> attackAnimation;
    private TextureRegion SlideTexture;

    private float stateTime = 0f;
    private boolean facingRight = true;

    // ---------------- DRAW SIZE ----------------
    public float drawWidth;
    public float drawHeight;

    // ---------------- COLLISION FLAGS ----------------
    public boolean isGrounded = false;
    private boolean isTouchingWall = false;
    private boolean isSliding = false;

    //----------------- ATTACK --------------------
    public int swordUses = 10;
    public float attackCooldown = 0f;
    public boolean isAttacking = false;
    private Fixture swordFixture;

    public Player(float jh, GameAssetManager assets) {

        JumpHeight = jh;
        attackTimer = 10f;

        score = 0;
        Hp = 100;
        isGrounded = false;

        idleAnimation = assets.getAnimation(GameAssetManager.PLAYER_IDLE_PREFIX, 5, 0.15f, Animation.PlayMode.LOOP, "%02d");
        runAnimation = assets.getAnimation(GameAssetManager.PLAYER_RUN_PREFIX, 6, 0.15f, Animation.PlayMode.LOOP, "%02d");
        jumpAnimation = assets.getAnimation(GameAssetManager.PLAYER_JUMP_PREFIX, 3, 0.15f, Animation.PlayMode.NORMAL, "%02d");
        fallAnimation = assets.getAnimation(GameAssetManager.PLAYER_FALL_PREFIX, 1, 0.15f, Animation.PlayMode.NORMAL, "%02d");
        hitAnimation = assets.getAnimation(GameAssetManager.PLAYER_HIT_PREFIX, 4, 0.1f, Animation.PlayMode.LOOP, "%02d");
        attackAnimation = assets.getAnimation(GameAssetManager.PLAYER_ATTACK_PREFIX, 3, 0.1f, Animation.PlayMode.NORMAL, "%02d");
        Texture slideTex = assets.manager.get(GameAssetManager.PLAYER_WALL_SLIDE, Texture.class);
        SlideTexture = new TextureRegion(slideTex);
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
        fdef.filter.maskBits = (short) (Main.GROUND_BIT | Main.ENEMY_BIT | Main.PROJECTILE_BIT | Main.COIN_BIT | Main.POTION_BIT | Main.WATER_BIT | Main.TRAP_BIT | Main.TRIGGER_BIT);

        playerBody.createFixture(fdef).setUserData(this);
        playerBody.setFixedRotation(true);
        shape.dispose();

        // --- NEW: THE SWORD ATTACHMENT ---
        PolygonShape swordShape = new PolygonShape();
        swordShape.setAsBox(20 / Main.PPM, 20 / Main.PPM, new Vector2(25 / Main.PPM, 0), 0);

        FixtureDef sdef = new FixtureDef();
        sdef.shape = swordShape;
        sdef.isSensor = true;
        sdef.filter.categoryBits = Main.SWORD_BIT;
        sdef.filter.maskBits = 0; // Starts disabled!

        swordFixture = playerBody.createFixture(sdef);
        swordFixture.setUserData(this);
        swordShape.dispose();
    }

    // ---------------------------------------------------
    // UPDATE PLAYER
    // ---------------------------------------------------
    public void UpdatePlayer(float dt, World world) {

        if (isDead) {
            Respawn();
            return;
        }

        if(GetYpos() < 0){
            isDead = true;
            Hp = 0;
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

        // --- SWORD COOLDOWN ---
        if (attackCooldown > 0) {
            attackCooldown -= dt;
        }

        // --- SWORD ATTACK INPUT ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && swordUses > 0 && attackCooldown <= 0 && !isAttacking) {
            isAttacking = true;
            swordUses--;
            attackCooldown = attackTimer;
            stateTime = 0;

            // Turn on the sword hitbox
            Filter filter = swordFixture.getFilterData();
            filter.maskBits = Main.ENEMY_BIT;
            swordFixture.setFilterData(filter);

            System.out.println("Swung sword! Uses left: " + swordUses);
        }

        // --- SWORD ATTACK FINISH ---
        if (isAttacking && attackAnimation.isAnimationFinished(stateTime)) {
            isAttacking = false;

            // Turn off the sword hitbox
            Filter filter = swordFixture.getFilterData();
            filter.maskBits = 0;
            swordFixture.setFilterData(filter);
        }

        // --- DYNAMIC SWORD HITBOX PLACEMENT ---
        PolygonShape shape = (PolygonShape) swordFixture.getShape();
        if (facingRight) {
            shape.setAsBox(20 / Main.PPM, 20 / Main.PPM, new Vector2(25 / Main.PPM, 0), 0);
        } else {
            shape.setAsBox(20 / Main.PPM, 20 / Main.PPM, new Vector2(-25 / Main.PPM, 0), 0);
        }

        isGrounded = false;
        isTouchingWall = false;
        isSliding = false;

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

        // Lock movement if currently swinging sword
        if (moveLeft && !isAttacking) desiredVel = -10f;
        else if (moveRight && !isAttacking) desiredVel = 10f;

        playerBody.setLinearVelocity(desiredVel, vel.y);

        boolean pushingWall = (desiredVel < 0 && isTouchingWall) || (desiredVel > 0 && isTouchingWall);
        boolean falling = vel.y < 0;

        if (pushingWall && falling && !isGrounded) {
            isSliding = true;
            playerBody.setLinearVelocity(vel.x, Math.max(vel.y, -2f));
        }

        // Lock jump if currently swinging sword
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && isGrounded && !isAttacking) {
            ApplyJump();
        }
    }

    // ---------------------------------------------------
    // STATE MACHINE
    // ---------------------------------------------------
    private State getState() {
        if (isAttacking) return State.ATTACKING;
        if(isSliding) return State.SLIDING;

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

        if (isHit) {
            region = hitAnimation.getKeyFrame(hitTimer);
        } else {
            currentState = getState();
            switch (currentState) {
                case ATTACKING: region = attackAnimation.getKeyFrame(stateTime); break;
                case JUMPING: region = jumpAnimation.getKeyFrame(stateTime); break;
                case FALLING: region = fallAnimation.getKeyFrame(stateTime); break;
                case RUNNING: region = runAnimation.getKeyFrame(stateTime); break;
                case SLIDING: region = SlideTexture ; break;
                default: region = idleAnimation.getKeyFrame(stateTime); break;
            }
        }

        if (!isHit && !isAttacking) {
            float velX = playerBody.getLinearVelocity().x;
            if (velX < -0.1f) facingRight = false;
            else if (velX > 0.1f) facingRight = true;
        }

        if (facingRight && region.isFlipX()) {
            region.flip(true, false);
        } else if (!facingRight && !region.isFlipX()) {
            region.flip(true, false);
        }

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
        this.swordUses = 10;
        this.attackCooldown = 0f;

        playerBody.setTransform(spawnX, spawnY, 0);
        playerBody.setLinearVelocity(0, 0);

        System.out.println("RESPAWNED!");
    }

    public void dispose() {}
}
