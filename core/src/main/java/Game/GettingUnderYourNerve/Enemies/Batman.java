package Game.GettingUnderYourNerve.Enemies;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import static Game.GettingUnderYourNerve.Main.PPM;

public class Batman extends Enemy {

    public enum State { IDLE, MOVING, ATTACKING }
    public State currentState = State.IDLE;
    public State previousState = State.IDLE;

    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> moveAnim;
    private Animation<TextureRegion> attackAnim;

    public boolean facingRight = true;
    private GameAssetManager assets;

    public boolean activateAI = false;
    private float attackTimer = 0f;
    public boolean isHit = false;
    private float hitTimer = 0f;

    public Batman(World world, float x, float y, GameAssetManager assets) {
        super(world, x, y);
        this.assets = assets;

        this.drawWidth = 48 / PPM;
        this.drawHeight = 48 / PPM;

        this.maxHealth = 100;
        this.currentHealth = 100;

        idleAnim = assets.getAnimation(GameAssetManager.BATMAN_IDLE_PREFIX, 3, 0.2f, Animation.PlayMode.LOOP, "%d");
        moveAnim = assets.getAnimation(GameAssetManager.BATMAN_MOVE_PREFIX, 3, 0.15f, Animation.PlayMode.LOOP, "%d");
        attackAnim = assets.getAnimation(GameAssetManager.BATMAN_ATTACK_PREFIX, 8, 0.3f, Animation.PlayMode.NORMAL, "%d");

        defineEnemy();
    }

    @Override
    protected void defineEnemy() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(spawnPosition.x / PPM, spawnPosition.y / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

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
        fdef.friction = 1.0f;
        fdef.density = 5.0f;

        fdef.filter.categoryBits = Main.ENEMY_BIT;
        fdef.filter.maskBits = Main.GROUND_BIT;

        b2body.createFixture(fdef).setUserData(this);
        b2body.setFixedRotation(true);

        shape.dispose();
    }

    public void hit(int damage, float sourceX) {
        if (isDead || isHit) return;

        this.currentHealth -= damage;
        this.isHit = true;
        this.hitTimer = 0f;
        this.stateTime = 0;

        // Minimal knockback for Batman, he is a boss!
        float pushDir = GetXpos() < sourceX ? -1f : 1f;
        b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
        b2body.applyLinearImpulse(new Vector2(pushDir * 2f, 2f), b2body.getWorldCenter(), true);

        if (currentHealth <= 0) {
            isDead = true;
            setToDestroy = true;
        }
    }

    @Override
    public void updateEnemy(float dt, Player player) {
        if (isDead) return;
        stateTime += dt;

        // Update hit stun timer
        if (isHit) {
            hitTimer += dt;
            if (hitTimer >= 0.4f) isHit = false;
        }

        if (!activateAI) return;

        float distanceX = player.GetXpos() - GetXpos();
        float absDistX = Math.abs(distanceX);

        if (attackTimer > 0) attackTimer -= dt;

        if (Math.abs(b2body.getLinearVelocity().y) > 0.1f) {
            setAction(State.MOVING);
            return;
        }

        if (absDistX < 2.0f && Math.abs(player.GetYpos() - GetYpos()) < 2.0f) {
            b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);

            if (attackTimer <= 0) {
                setAction(State.ATTACKING);
                attackTimer = 1.5f;
                player.hit(15, GetXpos());
            } else if (currentState != State.ATTACKING || attackAnim.isAnimationFinished(stateTime)) {
                setAction(State.IDLE);
            }
        } else {
            if (currentState != State.ATTACKING || attackAnim.isAnimationFinished(stateTime)) {
                setAction(State.MOVING);
                float speed = 3.5f;
                b2body.setLinearVelocity(distanceX > 0 ? speed : -speed, b2body.getLinearVelocity().y);
            }
        }
    }

    @Override
    public TextureRegion GetCurrentFrame(float dt) {
        TextureRegion region;
        switch (currentState) {
            case ATTACKING: region = attackAnim.getKeyFrame(stateTime); break;
            case MOVING: region = moveAnim.getKeyFrame(stateTime); break;
            default: region = idleAnim.getKeyFrame(stateTime); break;
        }

        float velX = b2body.getLinearVelocity().x;
        if (velX > 0.1f)      facingRight = true;
        else if (velX < -0.1f) facingRight = false;

        boolean visuallyFacingRight = facingRight;
        if (visuallyFacingRight && !region.isFlipX()) {
            region.flip(true, false);
        } else if (!visuallyFacingRight && region.isFlipX()) {
            region.flip(true, false);
        }

        if (currentState != previousState) {
            stateTime = 0;
        }
        previousState = currentState;
        return region;
    }

    @Override
    public void render(float dt, SpriteBatch batch) {
        TextureRegion frame = GetCurrentFrame(dt);
        applyDamageTint(batch, dt);

        batch.draw(
            frame,
            GetXpos() - drawWidth / 2f,
            GetYpos() - (24 / PPM) - 3.5f / PPM,
            drawWidth,
            drawHeight
        );

        resetTint(batch);
    }

    public void setAction(State newState) {
        if (this.currentState != newState) {
            this.currentState = newState;
            this.stateTime = 0;
        }
    }

    @Override
    public void dispose() {
        idleAnim = null;
        moveAnim = null;
        attackAnim = null;
    }
}
