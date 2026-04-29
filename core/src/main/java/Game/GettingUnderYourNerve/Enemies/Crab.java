package Game.GettingUnderYourNerve.Enemies;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static Game.GettingUnderYourNerve.Main.PPM;
import static java.lang.Math.abs;

public class Crab extends Enemy {
    public enum State { PATROL, CHASE, ATTACK }
    public State currentState = State.PATROL;
    public State previousState = State.PATROL;

    private Animation<TextureRegion> idleAnim, runAnim, attackAnim;
    private boolean facingRight = false;

    private final float WALK_SPEED = 0.7f;
    private final float CHASE_SPEED = 4.0f;

    // Timers
    private float patrolTimer = 0f;
    private float stateTime = 0f;
    private float shoutCooldown = 0f; // To prevent "Come here boy" spam

    // Audio - Patrol loop ID
    private long patrolSoundId = -1;

    public Crab(World world, float x, float y) {
        super(world, x, y);
        drawWidth = 72 / PPM;
        drawHeight = 32 / PPM;

        defineEnemy();

        idleAnim = loadAnimation("01-Idle", 9, 0.1f, Animation.PlayMode.LOOP);
        runAnim = loadAnimation("02-Run", 6, 0.1f, Animation.PlayMode.LOOP);
        attackAnim = loadAnimation("07-Attack", 4, 0.1f, Animation.PlayMode.NORMAL);

        patrolSoundId = AudioManager.crabPatrol.loop(0.0f);
    }

    @Override
    protected void defineEnemy() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(spawnPosition.x / PPM, (spawnPosition.y / PPM));
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        float w = drawWidth / 2f;
        float h = drawHeight / 2f;
        float bevel = 2 / PPM;

        // Beveled vertices to prevent tile sticking
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

        if (shoutCooldown > 0) shoutCooldown -= dt; // Tick down cooldown

        // 1. State Logic
        if (currentState != State.ATTACK) {
            if (distance <= 6.0f && abs(dy) < 1.5f) {
                changeState(State.CHASE);
            } else {
                changeState(State.PATROL);
            }
        }

        // 2. Movement Logic
        switch (currentState) {
            case ATTACK:
                b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                if (attackAnim.isAnimationFinished(stateTime)) {
                    changeState(State.PATROL);
                }
                break;

            case CHASE:
                facingRight = (dx > 0);
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
        b2body.setLinearVelocity(new Vector2(facingRight ? speed : -speed, b2body.getLinearVelocity().y));
    }

    public void changeState(State newState) {
        if (currentState == newState) return;

        if (newState == State.ATTACK) {
            stateTime = 0f;
            AudioManager.crabAttack.play(0.5f);
            // Set cooldown so he doesn't shout right after an attack finishes
            shoutCooldown = 3.0f;
        } else if (newState == State.CHASE) {
            // Shout only once when spotted and cooldown is over
            if (shoutCooldown <= 0) {
                AudioManager.crabChaseShout.play(0.25f);
                shoutCooldown = 3.0f;
            }
        }

        currentState = newState;
    }

    private void handleAudio(float distance) {
        float vol = Math.max(0, 0.4f * (1.0f - (distance / 20.0f)));
        AudioManager.crabPatrol.setVolume(patrolSoundId, vol);
    }

    @Override
    public TextureRegion GetCurrentFrame(float dt) {
        TextureRegion region;
        if (currentState == State.ATTACK) region = attackAnim.getKeyFrame(stateTime);
        else if (abs(b2body.getLinearVelocity().x) > 0.1f) region = runAnim.getKeyFrame(stateTime);
        else region = idleAnim.getKeyFrame(stateTime);

        if (facingRight && !region.isFlipX()) region.flip(true, false);
        else if (!facingRight && region.isFlipX()) region.flip(true, false);

        stateTime = (currentState == previousState) ? stateTime + dt : 0;
        previousState = currentState;
        return region;
    }

    @Override
    public void render(float dt, SpriteBatch batch) {
        batch.draw(GetCurrentFrame(dt),
            GetXpos() - (drawWidth / 2f),
            GetYpos() - (drawHeight / 2f) - 5f / PPM,
            drawWidth,
            drawHeight);
    }

    @Override
    protected Animation<TextureRegion> loadAnimation(String folderName, int frameCount, float frameDuration, Animation.PlayMode mode) {
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            String frameNumber = String.format("%02d", i + 1);
            String filePath = "Treasure Hunters/The Crusty Crew/Sprites/Crabby/" + folderName + "/" + frameNumber + ".png";
            Texture tex = new Texture(filePath);
            textures.add(tex);
            frames[i] = new TextureRegion(tex);
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    @Override
    public void dispose() {
        super.dispose();
        AudioManager.crabPatrol.stop(patrolSoundId);
    }
}
