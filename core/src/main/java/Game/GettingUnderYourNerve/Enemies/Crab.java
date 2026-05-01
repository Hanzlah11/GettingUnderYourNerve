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

    private long patrolSoundId = -1;

    private GameAssetManager assets;

    public Crab(World world, float x, float y, GameAssetManager assets) {
        super(world, x, y);

        this.assets = assets;

        drawWidth = 72 / PPM;
        drawHeight = 32 / PPM;

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
        // Keep spawn position but ensure the body is dynamic
        bdef.position.set(spawnPosition.x / PPM, spawnPosition.y / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;

        b2body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();

        // REDUCED HITBOX SIZE:
        // Original was drawWidth/2 (36px). Now it's ~22px wide (half-width of 11px).
        // Original was drawHeight/2 (16px). Now it's ~20px high (half-height of 10px).
        float w = (drawWidth / 3.2f);
        float h = (drawHeight / 3.2f);
        float bevel = 2 / PPM;

        // Build the beveled octagon so it slides over tile gaps
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

        // Collision filtering for Ground and Player[cite: 11]
        fdef.filter.categoryBits = Main.ENEMY_BIT;
        fdef.filter.maskBits = Main.GROUND_BIT | Main.PLAYER_BIT;

        fdef.friction = 0f;    // Keeps movement snappy[cite: 11]
        fdef.density = 1000f;  // Heavy mass[cite: 11]

        b2body.createFixture(fdef).setUserData(this);
        b2body.setFixedRotation(true); // Prevent the crab from rolling[cite: 11]

        shape.dispose();
    }

    /**
     * Checks if there is a gap in the floor between the Crab and the Player.
     */

    @Override
    public void updateEnemy(float dt, Player player) {
        float dx = player.GetXpos() - GetXpos();
        float dy = player.GetYpos() - GetYpos();
        float distance = abs(dx);

        if (shoutCooldown > 0) shoutCooldown -= dt;

        // 1. State Logic: Detect Player + Check for Gaps
        if (currentState != State.ATTACK) {
            // Only Chase if: Close enough AND on same height AND ground is continuous
            if (distance <= 6f && abs(dy) < 1.5f && isFloorContinuous(player)) {
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

        // 3. Safety Edge Detection (Prevents falling during patrol or chase)
        if (currentState != State.ATTACK && isEdgeAhead()) {
            facingRight = !facingRight;
            patrolTimer = 0f;
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

    // Inside Crab.java
    private boolean isEdgeAhead() {
        // Don't check for edges if we are currently jumping or falling
        if (Math.abs(b2body.getLinearVelocity().y) > 0.01f) return false;

        final boolean[] groundBelow = {false};

        // Determine where to look: slightly in front of the Crab's side
        float lookOffset = facingRight ? (drawWidth / 2.1f) : -(drawWidth / 2.1f);
        float checkX = b2body.getPosition().x + lookOffset;

        // Start at center Y, end just below the feet
        Vector2 rayStart = new Vector2(checkX, b2body.getPosition().y);
        Vector2 rayEnd = new Vector2(checkX, b2body.getPosition().y - (drawHeight / 2f + 4 / PPM));

        world.rayCast((fixture, point, normal, fraction) -> {
            if (fixture.getFilterData().categoryBits == Main.GROUND_BIT) {
                groundBelow[0] = true;
                return 0; // Found ground, stop the ray
            }
            return -1; // Ignore other fixtures (like player or sensors)
        }, rayStart, rayEnd);

        return !groundBelow[0]; // If no ground was found, there is an edge
    }

    private boolean isFloorContinuous(Player player) {
        float startX = b2body.getPosition().x;
        float endX = player.getPlayerBody().getPosition().x;

        // Check a point halfway between them
        float midX = (startX + endX) / 2f;

        // Check a point 3/4 of the way toward the player
        float farX = startX + (endX - startX) * 0.75f;

        // If either the midpoint or the far point is over a hole, don't chase
        return !isHoleAt(midX) && !isHoleAt(farX);
    }

    /**
     * Optimized hole check that looks specifically for GROUND_BIT
     */
    private boolean isHoleAt(float x) {
        final boolean[] groundFound = {false};

        // Start ray slightly above the crab's feet level
        float rayStartY = b2body.getPosition().y - (drawHeight / 4f);
        // End ray well below the floor level
        float rayEndY = b2body.getPosition().y - (drawHeight);

        world.rayCast((fixture, point, normal, fraction) -> {
            if (fixture.getFilterData().categoryBits == Main.GROUND_BIT) {
                groundFound[0] = true;
                return 0; // Stop ray, ground exists
            }
            return -1; // Ignore everything else
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

        batch.draw(
            GetCurrentFrame(dt),
            GetXpos() - drawWidth / 2f,
            GetYpos() - drawHeight / 2f - 2f / PPM,
            drawWidth,
            drawHeight
        );
    }

    @Override
    public void dispose() {
        // 1. Stop looping audio immediately
        // If you don't stop this, the crab's patrol sound will play forever
        // even after the crab is deleted from the world.
        AudioManager.crabPatrol.stop(patrolSoundId);

        // 2. Clear local animation references
        // While the AssetManager holds the textures, clearing these helps
        // the Garbage Collector reclaim this specific Crab instance.
        idleAnim = null;
        runAnim = null;
        attackAnim = null;

        // 3. AssetManager safety
        // We do NOT dispose of 'assets' here because Main owns the vault.
    }

}
