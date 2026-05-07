package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.Main;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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

        // Generate a simple colored circle (Red for Batman/Charizard, Yellow for Player/Pikachu)
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(isPlayer ? Color.YELLOW : Color.RED);
        pixmap.fillCircle(8, 8, 8);
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float dt) {
        if (finished) return;

        velocityY -= 15.0f * dt; // Gravity
        y += velocityY * dt;

        if (y <= targetY) {
            y = targetY;
            if (bounces < 2) {
                velocityY = -velocityY * 0.4f; // Bounce effect
                bounces++;
            } else {
                finished = true;
            }
        }
    }

    public void render(SpriteBatch batch) {
        float size = 16f / Main.PPM; // Scale to physics world size
        batch.draw(texture, x - size/2f, y - size/2f, size, size);
    }

    public boolean isFinished() { return finished; }

    public void dispose() {
        texture.dispose();
    }
}
