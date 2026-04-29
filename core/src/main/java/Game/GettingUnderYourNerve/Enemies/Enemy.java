package Game.GettingUnderYourNerve.Enemies;

import Game.GettingUnderYourNerve.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public abstract class Enemy
{
    protected World world;
    protected Body b2body;

    protected float stateTime = 0f;

    public float drawWidth;
    public float drawHeight;

    protected Vector2 spawnPosition;

    public Enemy(World world, float x, float y)
    {
        this.world = world;
        spawnPosition = new Vector2(x, y);
    }

    protected abstract void defineEnemy();
    public abstract void updateEnemy(float dt, Player player);
    public abstract TextureRegion GetCurrentFrame(float dt);
    public abstract void render(float dt, SpriteBatch batch);

    public float GetXpos() {
        return b2body.getPosition().x;
    }

    public float GetYpos() {
        return b2body.getPosition().y;
    }
}
