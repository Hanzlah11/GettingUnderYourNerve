package Game.GettingUnderYourNerve.Collectables;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;

public class Coin extends Collectable {

    private int pointValue;

    public Coin(World world, Rectangle rect, String type, GameAssetManager assets) {
        this.drawWidth = rect.width / Player.PPM;
        this.drawHeight = rect.height / Player.PPM;

        // 1. Assign values and fetch animations from the vault using format specifier "%02d"
        if (type.equals("gold")) {
            pointValue = 100;
            animation = assets.getAnimation(GameAssetManager.COIN_GOLD_PREFIX, 4, 0.1f, Animation.PlayMode.LOOP, "%02d");
        } else if (type.equals("silver")) {
            pointValue = 50;
            animation = assets.getAnimation(GameAssetManager.COIN_SILVER_PREFIX, 4, 0.1f, Animation.PlayMode.LOOP, "%02d");
        } else {
            pointValue = 500;
            animation = assets.getAnimation(GameAssetManager.COIN_DIAMOND_PREFIX, 4, 0.1f, Animation.PlayMode.LOOP, "%02d");
        }

        // 2. Create the Sensor Body
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set((rect.x + rect.width / 2f) / Player.PPM, (rect.y + rect.height / 2f) / Player.PPM);
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(this.drawWidth / 2f, this.drawHeight / 2f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.isSensor = true;

        // 3. Setup Collision Filters
        fdef.filter.categoryBits = Main.COIN_BIT;
        fdef.filter.maskBits = Main.PLAYER_BIT;

        // Pass THIS specific coin object to the collision detector
        body.createFixture(fdef).setUserData(this);
        shape.dispose();
    }

    public void update(float dt) {
        stateTime += dt;
    }

    public void draw(SpriteBatch batch) {
        if (!isDestroyed && animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime);
            batch.draw(currentFrame,
                body.getPosition().x - (drawWidth / 2f),
                body.getPosition().y - (drawHeight / 2f),
                drawWidth, drawHeight);
        }
    }

    public void onCollect(Player player) {
        if (!isCollected) {
            isCollected = true;
            player.addScore(pointValue);
        }
    }
}
