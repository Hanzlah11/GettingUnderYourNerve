package Game.GettingUnderYourNerve;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

public abstract class Platform {
    protected Body body;
    protected float speed;
    public float drawWidth;
    public float drawHeight;

    // --- ANIMATION VARIABLES ---
    protected Animation<TextureRegion> animation;
    protected float stateTime = 0f;
    private Array<Texture> rawTextures;

    public Platform(float width, float height, float speed) {
        this.drawWidth = width / Player.PPM;
        this.drawHeight = height / Player.PPM;
        this.speed = speed;
        this.rawTextures = new Array<Texture>();
    }

    // Every child class MUST implement this method to handle its unique movement
    public abstract void update(float dt);

    // --- ANIMATION HELPER (Similar to Player) ---
    // Make sure your platform sprites are named cleanly, like "platform_1.png", "platform_2.png"
    protected void loadAnimation(String folderPath, String filePrefix, int frameCount, float frameDuration) {
        TextureRegion[] frames = new TextureRegion[frameCount];

        for (int i = 0; i < frameCount; i++) {
            // Adjust this string format if your numbers don't have leading zeros!
            String frameNumber = String.format("%02d", i + 1);
            String filePath = folderPath + "/" + filePrefix + " " + frameNumber + ".png";

            Texture tex = new Texture(filePath);
            rawTextures.add(tex);
            frames[i] = new TextureRegion(tex);
        }

        animation = new Animation<TextureRegion>(frameDuration, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
    }

    // --- UNIVERSAL DRAW METHOD ---
    public void draw(SpriteBatch batch) {
        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime);
            batch.draw(currentFrame,
                body.getPosition().x - (drawWidth / 2f),
                body.getPosition().y - (drawHeight / 2f),
                drawWidth, drawHeight);
        }
    }

    // Prevent memory leaks
    public void dispose() {
        for (Texture tex : rawTextures) {
            tex.dispose();
        }
    }
}
