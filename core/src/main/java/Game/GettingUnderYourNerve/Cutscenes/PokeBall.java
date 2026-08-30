package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import static Game.GettingUnderYourNerve.Utilities.GameAssetManager.manager;

public class PokeBall {
    private float x, y;
    private float velocityY = 0;
    private float targetY;
    private Texture texture;
    private boolean finished = false;
    private int bounces = 0;

    public PokeBall(float startX, float startY, float targetY, boolean isPlayer) {
        this.x = startX;
        this.y = startY;
        this.targetY = targetY;

        this.texture = manager.get(GameAssetManager.POKEBALL_SPRITE, Texture.class);
    }

    public void update(float dt) {
        if (finished) return;

        velocityY -= 15.0f * dt;
        y += velocityY * dt;

        if (y <= targetY) {
            y = targetY;
            if (bounces < 2) {
                velocityY = -velocityY * 0.4f;

                if (AudioManager.pokeBallBounce != null) {
                    AudioManager.playSFX(AudioManager.pokeBallBounce);
                }

                bounces++;
            } else {
                finished = true;
            }
        }
    }

    public void render(SpriteBatch batch) {
        float size = 30f / Main.PPM;
        batch.draw(texture, x - size / 2f, y - size / 2f, size, size);
    }

    public boolean isFinished() { return finished; }

    public void dispose() { }
}
