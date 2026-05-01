package Game.GettingUnderYourNerve.Enemies;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import static Game.GettingUnderYourNerve.Main.PPM;
import static java.lang.Math.abs;

public class Shell extends Enemy
{
    public enum State { IDLE, SHOOTING, BITING };
    public State currentState = State.IDLE;
    public State previousState = State.SHOOTING;

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> shootingAnimation;
    private Animation<TextureRegion> bitingAnimation;

    private boolean facingRight = false;
    private boolean shooting = false;

    private float attackTimer = 0f;
    private boolean hasPlayedSound = false;
    private int ammo;

    private Array<Projectile> activeProjectiles;

    // We hold onto the vault here so we can spawn projectiles mid-game
    private GameAssetManager assets;

    public Shell(World world, float x, float y, GameAssetManager assets)
    {
        super(world, x, y);
        this.assets = assets;

        drawWidth = 48 / PPM;
        drawHeight = 38 / PPM;

        ammo = com.badlogic.gdx.math.MathUtils.random(3, 8);
        defineEnemy();
        activeProjectiles = new Array<Projectile>();

        idleAnimation = assets.getAnimation(GameAssetManager.SHELL_IDLE_PREFIX, 1, 0.15f, Animation.PlayMode.LOOP, "%d");
        shootingAnimation = assets.getAnimation(GameAssetManager.SHELL_FIRE_PREFIX, 6, 0.12f, Animation.PlayMode.NORMAL, "%d");
        bitingAnimation = assets.getAnimation(GameAssetManager.SHELL_BITE_PREFIX, 6, 0.05f, Animation.PlayMode.NORMAL, "%d");
    }

    @Override
    protected void defineEnemy()
    {
        BodyDef bdef = new BodyDef();
        bdef.position.set(spawnPosition.x / PPM + (drawWidth / 2f), spawnPosition.y / PPM + (drawHeight / 2f));
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(drawWidth / 2, drawHeight / 2);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;

        fdef.filter.categoryBits = Main.ENEMY_BIT;
        fdef.filter.maskBits = Main.GROUND_BIT | Main.PLAYER_BIT;

        fdef.friction = 1.0f;
        fdef.density = 10000f;

        b2body.createFixture(fdef).setUserData(this);
        b2body.setFixedRotation(true);
        shape.dispose();
    }

    @Override
    public void updateEnemy(float dt, Player player)
    {
        float dx = player.GetXpos() - GetXpos();
        float dy = player.GetYpos() - GetYpos();

        if(dx < 0f)
            facingRight = false;
        else
            facingRight = true;

        if (currentState == State.BITING) {
            if (bitingAnimation.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
            }
            return; // Don't do other logic while biting[cite: 5]
        }
        else if (shooting) {
            if(shootingAnimation.getKeyFrameIndex(stateTime) == 4 && !hasPlayedSound) {
                AudioManager.shellShoot.play(0.5f);
                hasPlayedSound = true;

                // Pass the assets vault into the new Projectile!
                if(facingRight)
                    activeProjectiles.add(new Projectile(world, GetXpos() + drawWidth / 2f, GetYpos() - (6f / PPM), facingRight, assets));
                else
                    activeProjectiles.add(new Projectile(world, GetXpos() - drawWidth / 2f, GetYpos() - (6f / PPM), facingRight, assets));
                ammo--;
            }
            if (shootingAnimation.isAnimationFinished(stateTime)) {
                shooting = false;
                attackTimer = 0;
                hasPlayedSound = false;
            }
        }
        else {
            if(abs(dx) < 10.0f && abs(dy) < 1.0f && ammo > 0)
            {
                attackTimer += dt;
                if (attackTimer >= 1.5f) {
                    shooting = true;
                }
            }
            else
            {
                shooting = false;
                attackTimer = 0;
            }
        }

        for(int i = 0; i < activeProjectiles.size; i++)
        {
            Projectile p = activeProjectiles.get(i);
            p.updateProjectile(dt);

            if (p.isDestroyed()) {
                activeProjectiles.removeIndex(i);
                i--;
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

        for(Projectile p : activeProjectiles)
            p.render(dt, batch);
    }

    @Override
    public TextureRegion GetCurrentFrame(float dt) {
        currentState = getState();
        TextureRegion region;

        switch (currentState) {
            case BITING:
                region = bitingAnimation.getKeyFrame(stateTime); // Use bite texture[cite: 5]
                break;
            case SHOOTING:
                region = shootingAnimation.getKeyFrame(stateTime);
                break;
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

    public void bite() {
        if (currentState != State.BITING) {
            currentState = State.BITING;
            stateTime = 0; // Start animation from frame 0[cite: 5]
            shooting = false; // Stop shooting if we start biting[cite: 2]
            AudioManager.shellShoot.play(0.8f);
        }
    }

    // Helper to check if the player should phase through
    public boolean isBiting() {
        return currentState == State.BITING;
    }

    private State getState() {
        if (currentState == State.BITING) return State.BITING; // Priority 1[cite: 5]
        if (shooting) return State.SHOOTING; // Priority 2[cite: 2]
        return State.IDLE;
    }

    @Override
    public void dispose() {
        // 1. Dispose of all flying projectiles managed by this shell
        for (Projectile p : activeProjectiles) {
            p.dispose();
        }
        activeProjectiles.clear();

        // 2. Clear animation references
        idleAnimation = null;
        shootingAnimation = null;
        bitingAnimation = null;

        // 3. Stop any associated audio if necessary
        // (shellShoot is a one-shot play() usually, so no stop needed)
    }
}
