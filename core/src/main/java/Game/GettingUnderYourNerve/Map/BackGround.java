package Game.GettingUnderYourNerve.Map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.Random;

import static Game.GettingUnderYourNerve.Player.PPM;

public class BackGround {

    private Texture bgTile;
    private Texture[] smallCloudTextures;

    private static class Cloud {
        float worldX;
        float worldY;
        float windSpeed;
        float scale;
        float parallaxFactor; // Depth: 0.1 = far away, 0.9 = close
        int textureIndex;
    }

    private ArrayList<Cloud> clouds = new ArrayList<>();
    private Random rng = new Random();

    private static final int CLOUD_COUNT = 40;

    // Lock clouds high up in your WORLD coordinates
    private static final float CLOUD_WORLD_Y_MIN = 12f;
    private static final float CLOUD_WORLD_Y_MAX = 25f;

    public BackGround() {
        // Base sky uses the UV optimization because it's a seamless pattern
        bgTile = new Texture("Treasure Hunters/Palm Tree Island/Sprites/Background/Additional Sky.png");
        bgTile.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        smallCloudTextures = new Texture[]{
            new Texture("Treasure Hunters/Palm Tree Island/Sprites/Background/Small Cloud 1.png"),
            new Texture("Treasure Hunters/Palm Tree Island/Sprites/Background/Small Cloud 2.png"),
        };

        spawnInitialClouds();
    }

    private void spawnInitialClouds() {
        for (int i = 0; i < CLOUD_COUNT; i++) {
            Cloud c = new Cloud();
            // Start them randomly across a large world area
            c.worldX = -10f + rng.nextFloat() * 100f;
            randomizeCloud(c);
            clouds.add(c);
        }
    }

    private void randomizeCloud(Cloud c) {
        c.worldY = CLOUD_WORLD_Y_MIN + rng.nextFloat() * (CLOUD_WORLD_Y_MAX - CLOUD_WORLD_Y_MIN);
        c.windSpeed = 0.2f + rng.nextFloat() * 0.8f;
        c.scale = 0.5f + rng.nextFloat() * 1.5f;
        // Assign depth. Smaller number = moves slower relative to camera
        c.parallaxFactor = 0.2f + rng.nextFloat() * 0.5f;
        c.textureIndex = rng.nextInt(smallCloudTextures.length);
    }

    public void RenderBg(float camX, float camY, float viewWidth, float viewHeight,
                         float dt, SpriteBatch batch) {

        float startX = camX - viewWidth / 2f;
        float startY = camY - viewHeight / 2f;

        // --- 1. DRAW INFINITE BASE SKY ---
        // Sky has 0 parallax (follows camera perfectly)
        float tileW = 32f / PPM;
        float tileH = 32f / PPM;

        batch.draw(bgTile, startX, startY, viewWidth, viewHeight,
            startX / tileW, startY / tileH,
            (startX + viewWidth) / tileW, (startY + viewHeight) / tileH);

        // --- 2. DRAW PARALLAX CLOUDS ---
        float leftEdge = camX - viewWidth / 2f;
        float rightEdge = camX + viewWidth / 2f;

        for (Cloud c : clouds) {

            // 1. Apply constant wind speed to the cloud's world position
            c.worldX += c.windSpeed * dt;

            // 2. THE TRUE PARALLAX FORMULA
            // Calculate where it should draw relative to the camera
            float drawX = camX + (c.worldX - camX) * c.parallaxFactor;

            // Lock the Y axis! Never jump with the camera.
            float drawY = c.worldY;

            Texture tex = smallCloudTextures[c.textureIndex];
            float cloudW = (tex.getWidth() / PPM) * c.scale;
            float cloudH = (tex.getHeight() / PPM) * c.scale;

            // 3. INFINITE TREADMILL LOGIC
            // If the DRAWN position drifts off the right side of the screen...
            if (drawX > rightEdge + 2f) {

                // Figure out where to teleport it in the WORLD so it draws off the LEFT side
                float targetDrawX = leftEdge - cloudW - rng.nextFloat() * 5f;
                c.worldX = camX + (targetDrawX - camX) / c.parallaxFactor;

                // Give it a new shape and height
                randomizeCloud(c);
                continue; // Skip drawing this frame to prevent flashing
            }

            // Only draw if it's actually visible on screen (Frustum Culling)
            if (drawX + cloudW > leftEdge && drawX < rightEdge) {
                batch.draw(tex, drawX, drawY, cloudW, cloudH);
            }
        }
    }

    public void dispose() {
        bgTile.dispose();
        for (Texture t : smallCloudTextures) {
            t.dispose();
        }
    }
}
