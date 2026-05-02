package Game.GettingUnderYourNerve.Trap;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Spike extends Trap {

    private Animation<TextureRegion> spikeAnimation;
    private float stateTime = 0f;

    public Spike(World world, MapObject object, GameAssetManager assets) {
        super(world, object);

        // Load the animation (Make sure to add SPIKE_ANIM_PREFIX to GameAssetManager!)
        // Adjust the frame count and duration based on your actual files
        spikeAnimation = assets.getAnimation(GameAssetManager.SPIKE_ANIM_PREFIX, 4, 0.15f, Animation.PlayMode.LOOP, "%d");

        // --- Box2D Setup ---
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody; // Spikes don't move
        bdef.position.set(startX, startY);
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();

        // We make the hitbox slightly smaller than the drawing size to be fair to the player
        float hitBoxW = (drawWidth / 2f) * 0.8f;
        float hitBoxH = (drawHeight / 2f) * 0.8f;
        shape.setAsBox(hitBoxW, hitBoxH);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.isSensor = true; // Spikes are sensors so you walk "into" them to take damage
        fdef.filter.categoryBits = Main.TRAP_BIT;

        body.createFixture(fdef).setUserData(this);
        shape.dispose();
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
    }

    @Override
    public void render(SpriteBatch batch, float dt) {
        TextureRegion currentFrame = spikeAnimation.getKeyFrame(stateTime);

        batch.draw(
            currentFrame,
            body.getPosition().x - (drawWidth / 2f),
            body.getPosition().y - (drawHeight / 2f),
            drawWidth,
            drawHeight
        );
    }

    @Override
    public int getDamage() {
        return 20; // Touching spikes deals 20 damage!
    }

    @Override
    public void onHit(Player p) {
        // Do nothing! It's a static spike.
        // (Later, you could play a "metal clank" sound effect right here!)
    }
}
