package Game.GettingUnderYourNerve.Map;

import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Water {
    private Texture deepWaterTexture;
    private Animation<TextureRegion> surfaceAnimation;
    private Rectangle bounds;
    private float stateTime = 0f;

    public Water(Rectangle rect, GameAssetManager assets) {
        this.bounds = rect;

        // Fetch Deep Water
        deepWaterTexture = assets.manager.get(GameAssetManager.WATER_DEEP, Texture.class);
        deepWaterTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        // Fetch Surface Animation directly from the vault using the 3 individual frames
        // NOTE: Adjust the "%d" to "%02d" if your files are named 01.png, 02.png instead of 1.png, 2.png
        surfaceAnimation = assets.getAnimation(GameAssetManager.WATER_SURFACE, 3, 0.15f, Animation.PlayMode.LOOP, "%d");
    }

    public void update(float dt) {
        stateTime += dt;
    }

    public void render(SpriteBatch batch) {
        float tileSize = 32f / Player.PPM;

        float startX = bounds.x / Player.PPM;
        float startY = bounds.y / Player.PPM;
        float totalWidth = bounds.width / Player.PPM;
        float totalHeight = bounds.height / Player.PPM;

        // --- 1. DRAW DEEP WATER (Everything except the top row) ---
        float deepHeight = totalHeight - tileSize;

        if (deepHeight > 0) {
            float u2 = totalWidth / tileSize;
            float v2 = deepHeight / tileSize;
            batch.draw(deepWaterTexture, startX, startY, totalWidth, deepHeight, 0, 0, u2, v2);
        }

        // --- 2. DRAW ANIMATED SURFACE (Exactly 1 tile high) ---
        float surfaceY = startY + deepHeight;
        TextureRegion currentFrame = surfaceAnimation.getKeyFrame(stateTime, true);

        for (float x = startX; x < startX + totalWidth; x += tileSize) {
            batch.draw(currentFrame, x, surfaceY, tileSize, tileSize);
        }
    }
}
