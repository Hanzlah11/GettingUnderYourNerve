package Game.GettingUnderYourNerve.Enemies;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import static Game.GettingUnderYourNerve.Main.PPM;
import static java.lang.Math.abs;

public class Crab extends Enemy {

    public enum State { PATROL, CHASE, ATTACK }

    public State currentState = State.PATROL;
    public State previousState = State.PATROL;

    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> runAnim;
    private Animation<TextureRegion> attackAnim;

    private boolean facingRight = false;

    private final float WALK_SPEED = 0.7f;
    private final float CHASE_SPEED = 4.0f;

    private float patrolTimer = 0f;
    private float stateTime = 0f;
    private float shoutCooldown = 0f;

    // --- NEW: Cooldown to prevent hyper-fast flipping glitches ---
    private float turnCooldown = 0f;

    private long patrolSoundId = -1;

    private GameAssetManager assets;

    public Crab(World world, float x, float y, GameAssetManager assets) {
        super(world, x, y);

        this.assets = assets;

        drawWidth = 72 / PPM;
        drawHeight = 32 / PPM;

        this.maxHealth = 2;
        this.currentHealth = 2;

        defineEnemy();

        idleAnim = assets.getAnimation(
            GameAssetManager.CRAB_IDLE_PREFIX,
            9, 0.1f,
            Animation.PlayMode.LOOP,
            "%02d"
        );

        runAnim = assets.getAnimation(
            GameAssetManager.CRAB_RUN_PREFIX,
            6, 0.1f,
            Animation.PlayMode.LOOP,
            "%02d"
        );

        attackAnim = assets.getAnimation(
            GameAssetManager.CRAB_ATTACK_PREFIX,
            4, 0.1f,
            Animation.PlayMode.NORMAL,
            "%02d"
        );

        patrolSoundId = AudioManager.crabPatrol.loop(0f);
    }

    @Override
    protected void defineEnemy() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(spawnPosition.x / PPM, spawnPosition.y / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;

        b2body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();

        float w = (drawWidth / 3.2f);
        float h = (drawHeight / 3.2f);
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

        fdef.filter.categoryBits = Main.ENEMY_BIT;
        fdef.filter.maskBits = Main.GROUND_BIT | Main.PLAYER_BIT | Main.SWORD_BIT;

        fdef.friction = 0f;
        fdef.density = 1000f;

        b2body.createFixture(fdef).setUserData(this);
        b2body.setFixedRotation(true);

        shape.dispose();
    }

    @Override
    public void updateEnemy(float dt, Player player) {
        if (isDead) return;
        if (hitTimer > 0) return;

        float dx = player.GetXpos() - GetXpos();
        float dy = player.GetYpos() - GetYpos();
        float distance = abs(dx);

        if (shoutCooldown > 0) shoutCooldown -= dt;
        if (turnCooldown > 0) turnCooldown -= dt; // Tick down the anti-glitch cooldown

        // 1. State Logic
        if (currentState != State.ATTACK) {
            // ONLY allow chase if we aren't recovering from hitting an edge/wall
            if (turnCooldown <= 0 && distance <= 6f && abs(dy) < 1.5f && isFloorContinuous(player)) {
                changeState(State.CHASE);
            } else {
                changeState(State.PATROL);
            }
        }

        // 2. Directional Logic
        if (currentState == State.CHASE) {
            facingRight = dx > 0;
        } else if (currentState == State.PATROL) {
            patrolTimer += dt;
            if (patrolTimer >= 5f) {
                facingRight = !facingRight;
                patrolTimer = 0f;
            }
        }

        // 3. Safety Edge & Wall Detection
        // Prevent checking for edges if we just turned, avoids getting stuck in corners
        if (currentState != State.ATTACK && turnCooldown <= 0 && (isEdgeAhead() || isWallAhead())) {
            facingRight = !facingRight;
            patrolTimer = 0f;
            turnCooldown = 0.5f; // FORCES crab to walk away for 0.5s before thinking about the player again!

            if (currentState == State.CHASE) {
                changeState(State.PATROL);
            }
        }

        // 4. Movement Execution
        switch (currentState) {
            case ATTACK:
                b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                if (attackAnim.isAnimationFinished(stateTime)) changeState(State.PATROL);
                break;
            case CHASE:
                move(CHASE_SPEED);
                break;
            case PATROL:
                move(WALK_SPEED);
                break;
        }

        handleAudio(distance);
    }

    public void attack() {
        changeState(State.ATTACK);
    }

    private void move(float speed) {
        b2body.setLinearVelocity(
            facingRight ? speed : -speed,
            b2body.getLinearVelocity().y
        );
    }

    private boolean isEdgeAhead() {
        if (Math.abs(b2body.getLinearVelocity().y) > 0.01f) return false;

        final boolean[] groundBelow = {false};

        float lookOffset = facingRight ? (drawWidth / 2.1f) : -(drawWidth / 2.1f);
        float checkX = b2body.getPosition().x + lookOffset;

        Vector2 rayStart = new Vector2(checkX, b2body.getPosition().y);
        Vector2 rayEnd = new Vector2(checkX, b2body.getPosition().y - (drawHeight / 2f + 4 / PPM));

        world.rayCast((fixture, point, normal, fraction) -> {
            if (fixture.getFilterData().categoryBits == Main.GROUND_BIT) {
                groundBelow[0] = true;
                return 0;
            }
            return -1;
        }, rayStart, rayEnd);

        return !groundBelow[0];
    }

    private boolean isWallAhead() {
        final boolean[] wallInFront = {false};

        float checkY = b2body.getPosition().y + (2 / PPM);
        float checkX = b2body.getPosition().x;
        float lookDistance = (drawWidth / 2.5f);

        Vector2 rayStart = new Vector2(checkX, checkY);
        Vector2 rayEnd = new Vector2(
            facingRight ? checkX + lookDistance : checkX - lookDistance,
            checkY
        );

        world.rayCast((fixture, point, normal, fraction) -> {
            if (fixture.getFilterData().categoryBits == Main.GROUND_BIT) {
                wallInFront[0] = true;
                return 0;
            }
            return -1;
        }, rayStart, rayEnd);

        return wallInFront[0];
    }

    private boolean isFloorContinuous(Player player) {
        float startX = b2body.getPosition().x;
        float endX = player.getPlayerBody().getPosition().x;

        // --- NEW: Better gap detection checks 3 points instead of 2
        float qX = startX + (endX - startX) * 0.25f;
        float midX = startX + (endX - startX) * 0.50f;
        float farX = startX + (endX - startX) * 0.75f;

        return !isHoleAt(qX) && !isHoleAt(midX) && !isHoleAt(farX);
    }

    private boolean isHoleAt(float x) {
        final boolean[] groundFound = {false};

        float rayStartY = b2body.getPosition().y - (drawHeight / 4f);
        float rayEndY = b2body.getPosition().y - (drawHeight);

        world.rayCast((fixture, point, normal, fraction) -> {
            if (fixture.getFilterData().categoryBits == Main.GROUND_BIT) {
                groundFound[0] = true;
                return 0;
            }
            return -1;
        }, new Vector2(x, rayStartY), new Vector2(x, rayEndY));

        return !groundFound[0];
    }

    public void changeState(State newState) {

        if (currentState == newState)
            return;

        if (newState == State.ATTACK) {

            stateTime = 0f;
            AudioManager.crabAttack.play(0.5f);
            shoutCooldown = 3f;

        } else if (newState == State.CHASE) {

            if (shoutCooldown <= 0f) {
                AudioManager.crabChaseShout.play(0.25f);
                shoutCooldown = 3f;
            }
        }

        currentState = newState;
    }

    private void handleAudio(float distance) {

        float vol =
            Math.max(0, 0.4f * (1f - (distance / 20f)));

        AudioManager.crabPatrol.setVolume(
            patrolSoundId,
            vol
        );
    }

    @Override
    public TextureRegion GetCurrentFrame(float dt) {

        TextureRegion region;

        if (currentState == State.ATTACK)
            region = attackAnim.getKeyFrame(stateTime);

        else if (abs(b2body.getLinearVelocity().x) > 0.1f)
            region = runAnim.getKeyFrame(stateTime);

        else
            region = idleAnim.getKeyFrame(stateTime);

        if (facingRight && !region.isFlipX())
            region.flip(true, false);
        else if (!facingRight && region.isFlipX())
            region.flip(true, false);

        stateTime =
            (currentState == previousState)
                ? stateTime + dt
                : 0;

        previousState = currentState;

        return region;
    }

    @Override
    public void render(float dt, SpriteBatch batch) {
        applyDamageTint(batch, dt);
        batch.draw(
            GetCurrentFrame(dt),
            GetXpos() - drawWidth / 2f,
            GetYpos() - drawHeight / 2f - 2f / PPM,
            drawWidth,
            drawHeight
        );
        resetTint(batch);
    }

    @Override
    public void dispose() {
        AudioManager.crabPatrol.stop(patrolSoundId);

        idleAnim = null;
        runAnim = null;
        attackAnim = null;
    }
}
