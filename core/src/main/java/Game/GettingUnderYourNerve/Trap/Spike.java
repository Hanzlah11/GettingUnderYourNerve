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


        spikeAnimation = assets.getAnimation(GameAssetManager.SPIKE_ANIM_PREFIX, 4, 0.15f, Animation.PlayMode.LOOP, "%d");

        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(startX, startY);
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();


        float hitBoxW = (drawWidth / 2f) * 0.8f;
        float hitBoxH = (drawHeight / 2f) * 0.8f;
        shape.setAsBox(hitBoxW, hitBoxH);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.isSensor = true;
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
        return 20;
    }

    @Override
    public void onHit(Player p) {
    }
}
