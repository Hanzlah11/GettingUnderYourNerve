package Game.GettingUnderYourNerve.Enemies;

import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import static Game.GettingUnderYourNerve.Main.*;

public class Projectile
{
    private World world;
    private Body b2body;

    private float stateTime = 0f;

    public float drawWidth;
    public float drawHeight;

    private Boolean facingRight;
    private Vector2 spawnPosition;

    public enum State {IDLE, DESTROYED};
    public State currentState = State.IDLE;
    public State previousState = State.IDLE;

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> destroyingAnimation;

    private boolean setToDestoy = false;
    private boolean destroyed = false;
    private boolean hasPlayedCollisionEffects = false;

    public Projectile(World world, float x, float y, boolean facingRight, GameAssetManager assets)
    {
        this.world = world;
        this.facingRight = facingRight;
        drawWidth = drawHeight = 16 / PPM;
        spawnPosition = new Vector2(x, y);

        defineProjectile();

        idleAnimation = assets.getAnimation(GameAssetManager.PEARL_IDLE_PREFIX, 1, 0.15f, Animation.PlayMode.LOOP, "%d");
        destroyingAnimation = assets.getAnimation(GameAssetManager.PEARL_DESTROYED_PREFIX, 3, 0.35f, Animation.PlayMode.LOOP, "%d");
    }

    private void defineProjectile()
    {
        BodyDef bdef = new BodyDef();
        bdef.position.set(spawnPosition.x, spawnPosition.y);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        CircleShape shape = new CircleShape();
        shape.setRadius(4f / PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;

        fdef.filter.categoryBits = PROJECTILE_BIT;
        fdef.filter.maskBits = GROUND_BIT | PLAYER_BIT;

        fdef.isSensor = true;
        b2body.createFixture(fdef).setUserData(this);
        b2body.setGravityScale(0f);

        float speed = 5f;
        b2body.setLinearVelocity(new Vector2(facingRight ? speed : -speed, 0));

        shape.dispose();
    }

    public void setToDestroy()
    {
        setToDestoy = true;
        b2body.setLinearVelocity(new Vector2(0f, 0f));
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void updateProjectile(float dt)
    {
        stateTime += dt;

        if (!setToDestoy && stateTime >= 2f) {
            setToDestroy();
        }

        if(setToDestoy)
        {
            if (!hasPlayedCollisionEffects) {
                AudioManager.shellShoot.stop();
                AudioManager.projectileBreak.play(0.5f);
                hasPlayedCollisionEffects = true;
            }
            if (destroyingAnimation.isAnimationFinished(stateTime))
            {
                world.destroyBody(b2body);
                destroyed = true;
            }
        }
    }

    public void render(float dt, SpriteBatch batch)
    {
        batch.draw(GetCurrentFrame(dt),
            GetXpos() - (drawWidth / 2f),
            GetYpos() - (drawHeight / 2f),
            drawWidth,
            drawHeight);
    }

    public TextureRegion GetCurrentFrame(float dt) {
        currentState = getState();
        TextureRegion region;

        switch (currentState) {
            case DESTROYED:
                region = destroyingAnimation.getKeyFrame(stateTime);
                break;
            case IDLE:
            default:
                region = idleAnimation.getKeyFrame(stateTime);
                break;
        }

        if (facingRight && !region.isFlipX()) {
            region.flip(true, false);
        } else if (!facingRight && region.isFlipX()) {
            region.flip(true, false);
        }

        stateTime = currentState == previousState ? stateTime + dt : 0;
        previousState = currentState;

        return region;
    }

    private State getState()
    {
        if(setToDestoy)
            return State.DESTROYED;
        else
            return State.IDLE;
    }

    public float GetXpos() { return b2body.getPosition().x; }
    public float GetYpos() { return b2body.getPosition().y; }
}
