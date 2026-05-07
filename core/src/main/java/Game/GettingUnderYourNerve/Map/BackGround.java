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
            c.worldX = -10f + rng.nextFloat() * 100f;
            randomizeCloud(c);
            clouds.add(c);
        }
    }

    private void randomizeCloud(Cloud c) {
        c.worldY = CLOUD_WORLD_Y_MIN + rng.nextFloat() * (CLOUD_WORLD_Y_MAX - CLOUD_WORLD_Y_MIN);
        c.windSpeed = 0.2f + rng.nextFloat() * 0.8f;
        c.scale = 0.5f + rng.nextFloat() * 1.5f;
        c.parallaxFactor = 0.2f + rng.nextFloat() * 0.5f;
        c.textureIndex = rng.nextInt(smallCloudTextures.length);
    }

    public void RenderBg(float camX, float camY, float viewWidth, float viewHeight,
                         float dt, SpriteBatch batch) {

        float startX = camX - viewWidth / 2f;
        float startY = camY - viewHeight / 2f;

        float tileW = 32f / PPM;
        float tileH = 32f / PPM;

        batch.draw(bgTile, startX, startY, viewWidth, viewHeight,
            startX / tileW, startY / tileH,
            (startX + viewWidth) / tileW, (startY + viewHeight) / tileH);

        float leftEdge = camX - viewWidth / 2f;
        float rightEdge = camX + viewWidth / 2f;

        for (Cloud c : clouds) {

            c.worldX += c.windSpeed * dt;

            float drawX = camX + (c.worldX - camX) * c.parallaxFactor;

            float drawY = c.worldY;

            Texture tex = smallCloudTextures[c.textureIndex];
            float cloudW = (tex.getWidth() / PPM) * c.scale;
            float cloudH = (tex.getHeight() / PPM) * c.scale;

            if (drawX > rightEdge + 2f) {

                float targetDrawX = leftEdge - cloudW - rng.nextFloat() * 5f;
                c.worldX = camX + (targetDrawX - camX) / c.parallaxFactor;

                randomizeCloud(c);
                continue;
            }

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
