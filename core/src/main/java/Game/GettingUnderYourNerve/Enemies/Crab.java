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
        fdef.filter.categoryBits = Main.ENEMY_BIT;
        fdef.filter.maskBits = Main.GROUND_BIT | Main.PLAYER_BIT;
        fdef.friction = 0f;
        fdef.density = 1000f;

        b2body.createFixture(fdef).setUserData(this);
        b2body.setFixedRotation(true);

        shape.dispose();
    }

    @Override
    public void updateEnemy(float dt, Player player) {

        float dx = player.GetXpos() - GetXpos();
        float dy = player.GetYpos() - GetYpos();
        float distance = abs(dx);

        if (shoutCooldown > 0)
            shoutCooldown -= dt;

        if (currentState != State.ATTACK) {

            if (distance <= 6f && abs(dy) < 1.5f)
                changeState(State.CHASE);
            else
                changeState(State.PATROL);
        }

        switch (currentState) {

            case ATTACK:
                b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);

                if (attackAnim.isAnimationFinished(stateTime))
                    changeState(State.PATROL);
                break;

            case CHASE:
                facingRight = dx > 0;
                move(CHASE_SPEED);
                break;

            case PATROL:
                patrolTimer += dt;

                if (patrolTimer >= 5f) {
                    facingRight = !facingRight;
                    patrolTimer = 0f;
                }

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
            GetYpos() - drawHeight / 2f - 5f / PPM,
            drawWidth,
            drawHeight
        );
    }

}
