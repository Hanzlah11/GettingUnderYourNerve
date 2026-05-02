package Game.GettingUnderYourNerve.Enemies;

import Game.GettingUnderYourNerve.Player;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public abstract class Enemy {
    protected World world;
    public Body b2body;

    public int maxHealth;
    public int currentHealth;
    public boolean isDead;
    public boolean destroyed;
    public boolean setToDestroy;
    public float hitTimer;

    protected float stateTime = 0f;
    public float drawWidth;
    public float drawHeight;
    protected Vector2 spawnPosition;

    public Enemy(World world, float x, float y) {
        this.world = world;
        this.spawnPosition = new Vector2(x, y);
        this.isDead = false;
        this.destroyed = false;
        this.setToDestroy = false;
        this.hitTimer = 0f;
    }

    protected abstract void defineEnemy();
    public abstract void updateEnemy(float dt, Player player);
    public abstract TextureRegion GetCurrentFrame(float dt);
    public abstract void render(float dt, SpriteBatch batch);

    public void takeDamage(int damage, float knockbackDir) {
        if (isDead) return; // Don't beat a dead enemy!

        currentHealth -= damage;
        hitTimer = 0.2f;

        b2body.setLinearVelocity(0, 0);

        // Multiply the push by the enemy's mass so it ALWAYS works!
        float mass = b2body.getMass();
        b2body.applyLinearImpulse(new Vector2(knockbackDir * mass * 2f, mass * 6f), b2body.getWorldCenter(), true);

        if (currentHealth <= 0) {
            setToDestroy = true;
            isDead = true;
        }
    }

    protected void applyDamageTint(SpriteBatch batch, float dt) {
        if (hitTimer > 0) {
            hitTimer -= dt;
            batch.setColor(Color.RED);
        } else {
            batch.setColor(Color.WHITE);
        }
    }

    protected void resetTint(SpriteBatch batch) {
        batch.setColor(Color.WHITE);
    }

    public float GetXpos() {
        return b2body.getPosition().x;
    }

    public float GetYpos() {
        return b2body.getPosition().y;
    }

    public abstract void dispose();
}
