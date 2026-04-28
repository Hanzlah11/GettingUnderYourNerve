package Game.GettingUnderYourNerve.Collectables;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

abstract class Collectable {
    public Body body;
    public boolean isCollected = false;
    public boolean isDestroyed = false; // Crucial for safe deletion

    protected float drawWidth, drawHeight;
    protected Animation<TextureRegion> animation;
    protected float stateTime = 0f;
    protected Array<Texture> rawTextures;


    protected void loadAnimation(String folderPath, int frameCount, float frameDuration) {
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
}
