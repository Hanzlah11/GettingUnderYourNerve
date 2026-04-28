package Game.GettingUnderYourNerve;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class Coin {
    public Body body;
    public boolean isCollected = false;
    public boolean isDestroyed = false; // Crucial for safe deletion

    private int pointValue;
    private float drawWidth, drawHeight;
    private Animation<TextureRegion> animation;
    private float stateTime = 0f;
    private Array<Texture> rawTextures;

    public Coin(World world, Rectangle rect, String type) {
        this.rawTextures = new Array<Texture>();
        this.drawWidth = rect.width / Player.PPM;
        this.drawHeight = rect.height / Player.PPM;

        // 1. Assign values based on Tiled properties
        if (type.equals("gold")) {
            pointValue = 100;
            loadAnimation("Treasure Hunters/Pirate Treasure/Sprites/Gold Coin/", 4, 0.1f); // Adjust paths!
        } else {
            pointValue = 50;
            loadAnimation("Treasure Hunters/Pirate Treasure/Sprites/Silver Coin", 4, 0.1f);
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
        fdef.isSensor = true; // CRITICAL: Makes it a ghost hitbox

        // Pass THIS specific coin object to the collision detector
        body.createFixture(fdef).setUserData(this);
        shape.dispose();
    }

    private void loadAnimation(String folderPath, int frameCount, float frameDuration) {
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            String frameNumber = String.format("%02d", i + 1);
            String filePath = folderPath + "/" + frameNumber + ".png";
            Texture tex = new Texture(filePath);
            rawTextures.add(tex);
            frames[i] = new TextureRegion(tex);
        }
        animation = new Animation<TextureRegion>(frameDuration, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
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

    // Called by the Contact Listener
    public void onCollect(Player player) {
        if (!isCollected) {
            isCollected = true;
            player.addScore(pointValue);
        }
    }

    public void dispose() {
        for (Texture tex : rawTextures) tex.dispose();
    }
}
