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

public class CR7 extends Enemy {

    private enum State { WALKING, CHASING, SUI, EXPLODING }
    private State currentState;
    private State previousState;

    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> runAnim;
    private Animation<TextureRegion> suiAnim;
    private Animation<TextureRegion> explodeAnim;

    private boolean runningRight = true;
    private boolean hasExploded = false;

    private final float CHASE_RANGE = 5.0f;
    private final float SUI_RANGE = 1.2f;

    private final float WALK_SPEED = 1.0f;
    private final float RUN_SPEED = 2.5f;

    private float turnCooldown = 0f;
    private float minigameCooldown = 0f;

    private final float SPRITE_SCALE = 0.20f;

    public boolean triggerMinigame = false;

    public CR7(World world, float x, float y) {
        super(world, x, y);

        this.drawWidth = 48 / PPM;
        this.drawHeight = 64 / PPM;

        currentState = State.WALKING;
        previousState = State.WALKING;

        walkAnim = GameAssetManager.getAnimation(GameAssetManager.CR7_WALK_PREFIX, 4, 0.15f, Animation.PlayMode.LOOP, "%d");
        runAnim = GameAssetManager.getAnimation(GameAssetManager.CR7_RUN_PREFIX, 4, 0.1f, Animation.PlayMode.LOOP, "%d");
        suiAnim = GameAssetManager.getAnimation(GameAssetManager.CR7_SUI_PREFIX, 7, 0.1f, Animation.PlayMode.NORMAL, "%d");
        explodeAnim = GameAssetManager.getAnimation(GameAssetManager.CR7_EXPLOSION_PREFIX, 6, 0.08f, Animation.PlayMode.NORMAL, "%d");

        defineEnemy();
    }

    @Override
    protected void defineEnemy() {
        BodyDef bdef = new BodyDef();
        bdef.position.set((spawnPosition.x + 32) / PPM, (spawnPosition.y + 32) / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        float w = 11 / PPM;
        float h = 30 / PPM;
        float bevel = 3 / PPM;

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
        fdef.filter.maskBits = Main.GROUND_BIT | Main.PLAYER_BIT | Main.PROJECTILE_BIT | Main.TRAP_BIT | Main.SWORD_BIT;

        fdef.friction = 0f;
        fdef.density = 1000f;

        b2body.createFixture(fdef).setUserData(this);
        b2body.setFixedRotation(true);
        shape.dispose();
    }

    @Override
    public void takeDamage(int damage, float pushDirection) {
    }

    @Override
    public void updateEnemy(float dt, Player player) {
        if (setToDestroy || destroyed) return;

        float distanceToPlayer = Math.abs(player.GetXpos() - b2body.getPosition().x);

        if (turnCooldown > 0) turnCooldown -= dt;
        if (minigameCooldown > 0) minigameCooldown -= dt;

        if (currentState == State.WALKING || currentState == State.CHASING) {
            if (turnCooldown <= 0 && (isEdgeAhead() || isWallAhead())) {
                runningRight = !runningRight;
                turnCooldown = 0.5f;
                if (currentState == State.CHASING) {
                    currentState = State.WALKING;
                }
            }
        }

        switch (currentState) {
            case WALKING:
                if (distanceToPlayer <= CHASE_RANGE) {
                    currentState = State.CHASING;
                } else {
                    b2body.setLinearVelocity(runningRight ? WALK_SPEED : -WALK_SPEED, b2body.getLinearVelocity().y);
                }
                break;

            case CHASING:
                if (distanceToPlayer <= SUI_RANGE) {
                    currentState = State.SUI;
                    b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                } else if (distanceToPlayer > CHASE_RANGE) {
                    currentState = State.WALKING;
                } else {
                    if (turnCooldown <= 0) {
                        runningRight = player.GetXpos() > b2body.getPosition().x;
                    }
                    b2body.setLinearVelocity(runningRight ? RUN_SPEED : -RUN_SPEED, b2body.getLinearVelocity().y);
                }
                break;

            case SUI:
                b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);

                if (distanceToPlayer > SUI_RANGE) {
                    currentState = State.CHASING;
                    if (AudioManager.cr7Sui != null) {
                        AudioManager.cr7Sui.stop();
                    }
                } else if (suiAnim.isAnimationFinished(stateTime)) {
                    if (minigameCooldown <= 0f && Math.random() < 0.30) {
                        triggerMinigame = true;
                    } else {
                        currentState = State.EXPLODING;
                    }
                }
                break;

            case EXPLODING:
                b2body.setLinearVelocity(0, 0);

                if (explodeAnim.isAnimationFinished(stateTime) && !hasExploded) {
                    if (distanceToPlayer < SUI_RANGE + 0.5f) {
                        player.hit(40, b2body.getPosition().x);
                    }
                    hasExploded = true;
                    setToDestroy = true;
                }
                break;
        }

        if (currentState == previousState) {
            stateTime += dt;
        } else {
            stateTime = 0;

            if (currentState == State.SUI) {
                AudioManager.playSFX(AudioManager.cr7Sui, 0.6f);
            }
            if (currentState == State.EXPLODING) {
                if (AudioManager.cr7Sui != null) {
                    AudioManager.cr7Sui.stop();
                }
                AudioManager.playSFX(AudioManager.cr7Explosion, 0.6f);
            }

            previousState = currentState;
        }
    }

    public void resetAfterMinigameLoss() {
        this.currentState = State.WALKING;
        this.previousState = State.WALKING;
        this.stateTime = 0f;
        this.minigameCooldown = 3.0f;
        this.b2body.setLinearVelocity(runningRight ? WALK_SPEED : -WALK_SPEED, 0);
    }

    private boolean isEdgeAhead() {
        if (Math.abs(b2body.getLinearVelocity().y) > 0.01f) return false;

        final boolean[] groundBelow = {false};
        float lookOffset = runningRight ? (16 / PPM) : -(16 / PPM);
        float checkX = b2body.getPosition().x + lookOffset;

        Vector2 rayStart = new Vector2(checkX, b2body.getPosition().y);
        Vector2 rayEnd = new Vector2(checkX, b2body.getPosition().y - (32 / PPM + 6 / PPM));

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
        float checkY = b2body.getPosition().y - (16 / PPM);
        float checkX = b2body.getPosition().x;
        float lookDistance = (16 / PPM);

        Vector2 rayStart = new Vector2(checkX, checkY);
        Vector2 rayEnd = new Vector2(
            runningRight ? checkX + lookDistance : checkX - lookDistance,
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

    @Override
    public TextureRegion GetCurrentFrame(float dt) {
        TextureRegion region;

        switch (currentState) {
            case SUI: region = suiAnim.getKeyFrame(stateTime); break;
            case EXPLODING: region = explodeAnim.getKeyFrame(stateTime); break;
            case CHASING: region = runAnim.getKeyFrame(stateTime, true); break;
            case WALKING:
            default: region = walkAnim.getKeyFrame(stateTime, true); break;
        }

        if (runningRight && region.isFlipX()) {
            region.flip(true, false);
        } else if (!runningRight && !region.isFlipX()) {
            region.flip(true, false);
        }

        return region;
    }

    @Override
    public void render(float dt, SpriteBatch batch) {
        if (!destroyed) {
            applyDamageTint(batch, dt);
            TextureRegion frame = GetCurrentFrame(dt);
            float frameW = (frame.getRegionWidth() * SPRITE_SCALE) / PPM;
            float frameH = (frame.getRegionHeight() * SPRITE_SCALE) / PPM;
            float feetY = b2body.getPosition().y - (30f / PPM);

            batch.draw(frame, b2body.getPosition().x - (frameW / 2f), feetY, frameW, frameH);
            resetTint(batch);
        }
    }

    @Override public void dispose() {}
}
