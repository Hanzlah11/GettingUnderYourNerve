package Game.GettingUnderYourNerve.Utilities;

import Game.GettingUnderYourNerve.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class TouchController {

    private Viewport viewport;
    private SpriteBatch batch;

    private Texture texLeft;
    private Texture texRight;
    private Texture texJump;
    private Texture texAttack;
    private Texture texPause;
    private Texture[] texCooldown = new Texture[12];

    private Rectangle leftBtnRect;
    private Rectangle rightBtnRect;
    private Rectangle jumpBtnRect;
    private Rectangle attackBtnRect;
    private Rectangle pauseBtnRect;

    public boolean jumpPressed = false;
    public boolean attackPressed = false;

    // Renamed to backOrPausePressed so it catches back button/swipes as well
    public boolean pausePressed = false;

    private static final float COOLDOWN_DURATION = 10.0f;
    private static final float FRAME_DURATION = 10.0f / 12.0f;

    private Vector3 touchPoint = new Vector3();

    public TouchController(SpriteBatch batch) {
        this.batch = batch;
        this.viewport = new FitViewport(800, 480);

        texLeft   = loadTexture(GameAssetManager.BTN_LEFT);
        texRight  = loadTexture(GameAssetManager.BTN_RIGHT);
        texJump   = loadTexture(GameAssetManager.BTN_JUMP);
        texAttack = loadTexture(GameAssetManager.BTN_ATTACK);
        texPause  = loadTexture(GameAssetManager.BTN_PAUSE);

        for (int i = 0; i < 12; i++) {
            String framePath = GameAssetManager.BTN_ATTACK_COOLDOWN_PREFIX + (i + 1) + ".png";
            texCooldown[i] = loadTexture(framePath);
        }

        leftBtnRect   = new Rectangle(10, 20, 100, 100);
        rightBtnRect  = new Rectangle(130, 20, 100, 100);

        attackBtnRect = new Rectangle(610, 10, 100, 100);
        jumpBtnRect   = new Rectangle(700, 105, 100 , 100);

        pauseBtnRect  = new Rectangle(725, 405, 55, 55);
    }

    private Texture loadTexture(String path) {
        if (GameAssetManager.manager != null && GameAssetManager.manager.isLoaded(path)) {
            return GameAssetManager.manager.get(path, Texture.class);
        }
        return null;
    }

    public boolean isLeftPressed() {
        return isTouchedInRect(leftBtnRect);
    }

    public boolean isRightPressed() {
        return isTouchedInRect(rightBtnRect);
    }

    private boolean isTouchedInRect(Rectangle rect) {
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                // Ignore touches right at screen edges where system back swipe occurs
                float rawX = Gdx.input.getX(i);
                float edgeThreshold = Gdx.graphics.getWidth() * 0.05f;
                if (rawX < edgeThreshold || rawX > (Gdx.graphics.getWidth() - edgeThreshold)) {
                    continue;
                }

                touchPoint.set(rawX, Gdx.input.getY(i), 0);
                viewport.unproject(touchPoint);
                if (rect.contains(touchPoint.x, touchPoint.y)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void draw() {
        draw(null);
    }

    public void draw(Player player) {
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                float rawX = Gdx.input.getX(i);
                float edgeThreshold = Gdx.graphics.getWidth() * 0.04f;

                // Skip touches that are part of an Android system edge swipe gesture
                if (rawX < edgeThreshold || rawX > (Gdx.graphics.getWidth() - edgeThreshold)) {
                    continue;
                }

                touchPoint.set(rawX, Gdx.input.getY(i), 0);
                viewport.unproject(touchPoint);

                if (Gdx.input.justTouched()) {
                    if (jumpBtnRect.contains(touchPoint.x, touchPoint.y)) {
                        jumpPressed = true;
                    }

                    if (attackBtnRect.contains(touchPoint.x, touchPoint.y)) {
                        if (player == null || player.attackCooldown <= 0f) {
                            attackPressed = true;
                        }
                    }

                    if (pauseBtnRect.contains(touchPoint.x, touchPoint.y)) {
                        pausePressed = true;
                    }
                }
            }
        }

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        drawTextureButton(leftBtnRect, texLeft);
        drawTextureButton(rightBtnRect, texRight);
        drawTextureButton(jumpBtnRect, texJump);
        drawTextureButton(pauseBtnRect, texPause);

        float activeCooldown = (player != null) ? player.attackCooldown : 0f;

        if (activeCooldown > 0f) {
            float elapsedTime = COOLDOWN_DURATION - activeCooldown;
            int frameIndex = Math.min(11, Math.max(0, (int) (elapsedTime / FRAME_DURATION)));
            drawTextureButton(attackBtnRect, texCooldown[frameIndex]);
        } else {
            drawTextureButton(attackBtnRect, texAttack);
        }

        batch.end();
    }

    private void drawTextureButton(Rectangle rect, Texture texture) {
        if (texture != null) {
            batch.setColor(1.0f, 1.0f, 1.0f, 0.85f);
            batch.draw(texture, rect.x, rect.y, rect.width, rect.height);
            batch.setColor(Color.WHITE);
        }
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void dispose() {}
}
