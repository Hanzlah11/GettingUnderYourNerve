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

    private boolean facingRight = true;
    private GameAssetManager assets;

    public Batman(World world, float x, float y, GameAssetManager assets) {
        super(world, x, y);
        this.assets = assets;

        // 1. MATCH VISUALS TO TILED: Set both dimensions to 32 pixels
        this.drawWidth = 48 / PPM;
        this.drawHeight = 48 / PPM;

        this.maxHealth = 10;
        this.currentHealth = 10;

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
        // 2. MATCH HITBOX TO TILED: Half-extents of 32 are 16
        // This creates a physical body that is exactly 32x32 pixels
        float hitBoxHalfWidth = 24 / PPM;
        float hitBoxHalfHeight = 24 / PPM;
        shape.setAsBox(hitBoxHalfWidth, hitBoxHalfHeight);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = Main.ENEMY_BIT;
        fdef.filter.maskBits = Main.GROUND_BIT | Main.PLAYER_BIT | Main.SWORD_BIT;

        b2body.createFixture(fdef).setUserData(this);
        b2body.setFixedRotation(true);

        shape.dispose();
    }

    @Override
    public void updateEnemy(float dt, Player player) {
        if (isDead) return;
        stateTime += dt;
    }

    @Override
    public TextureRegion GetCurrentFrame(float dt) {
        TextureRegion region;
        switch (currentState) {
            case ATTACKING: region = attackAnim.getKeyFrame(stateTime); break;
            case MOVING: region = moveAnim.getKeyFrame(stateTime); break;
            default: region = idleAnim.getKeyFrame(stateTime); break;
        }

        if (b2body.getLinearVelocity().x > 0.1f && !region.isFlipX()) {
            region.flip(true, false);
            facingRight = true;
        } else if (b2body.getLinearVelocity().x < -0.1f && region.isFlipX()) {
            region.flip(true, false);
            facingRight = false;
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

        // 3. FIX RENDERING OFFSET: Lower the sprite so feet stay on ground
        // Since the hitbox height is 32, we subtract 16 (half-height) to find the floor
        batch.draw(
            frame,
            GetXpos() - drawWidth / 2f,
            GetYpos() - (24 / PPM),
            drawWidth,
            drawHeight
        );

        resetTint(batch);
    }

    public void setAction(State newState) {
        // Only reset the timer if we are actually switching to a different state
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
