package Game.GettingUnderYourNerve.Enemies;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import com.badlogic.gdx.graphics.Texture;
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
    public enum State {IDLE, SHOOTING};
    public State currentState = State.IDLE;
    public State previousState = State.SHOOTING;

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> shootingAnimation;

    private boolean facingRight = false;
    private boolean shooting = false;

    private float attackTimer = 0f;
    private boolean hasPlayedSound = false;
    private int ammo = 5;

    private Array<Projectile> activeProjectiles;
    public Shell(World world, float x, float y)
    {
        super(world, x, y);
        drawWidth = 48 / PPM;
        drawHeight = 38 / PPM;

        defineEnemy();
        activeProjectiles = new Array<Projectile>();
        idleAnimation = loadAnimation("Seashell Idle", 1, 0.15f, Animation.PlayMode.LOOP);
        shootingAnimation = loadAnimation("Seashell Fire", 6, 0.12f, Animation.PlayMode.NORMAL);
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


            if (shooting) {
                if(shootingAnimation.getKeyFrameIndex(stateTime) == 4 && !hasPlayedSound) {
                    AudioManager.shellShoot.play(0.5f);
                    hasPlayedSound = true;
                    if(facingRight)
                        activeProjectiles.add(new Projectile(world, GetXpos() + drawWidth / 2f, GetYpos() - (6f / PPM), facingRight));
                    else
                        activeProjectiles.add(new Projectile(world, GetXpos() - drawWidth / 2f, GetYpos() - (6f / PPM), facingRight));
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
                    i--; // Important to adjust the index after removal
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
            case SHOOTING:
                region = shootingAnimation.getKeyFrame(stateTime);
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

    private State getState() {
        if(shooting)
            return State.SHOOTING;
        else
            return State.IDLE;
    }

    @Override
    protected Animation<TextureRegion> loadAnimation(String folderName, int frameCount, float frameDuration, Animation.PlayMode mode)
    {
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            // String.format("%02d", number) forces the number to have two digits (01, 02, 10, 11)
            String frameNumber = String.format("%01d", i + 1);

            // Build the exact, safe file path
            String filePath = "Treasure Hunters/Shooter Traps/Sprites/Seashell/" +
                folderName + "/" + frameNumber + ".png";

            Texture tex = new Texture(filePath);
            textures.add(tex); // Add to our master list for disposal later
            frames[i] = new TextureRegion(tex);
        }

        Animation<TextureRegion> anim = new Animation<TextureRegion>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }


}
