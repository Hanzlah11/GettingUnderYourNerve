package Game.GettingUnderYourNerve;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class GameCam {

    private OrthographicCamera camera;
    private Vector2 position;

    // Lerp target — camera slides toward this
    private Vector2 target;
    private static final float LERP_SPEED = 3f; // tweak for faster/slower slide

    public GameCam() {
        camera = new OrthographicCamera();
        position = new Vector2();
        target = new Vector2();
    }

    public OrthographicCamera GetCam() {
        return camera;
    }

    /**
     * Normal gameplay update — target follows player, camera lerps to target.
     */
    public void Update(float ww, float wh, float hvw, float hvh, float px, float py) {
        // Clamp the desired target to map bounds
        target.x = MathUtils.clamp(px, hvw, ww - hvw);
        target.y = MathUtils.clamp(py, hvh, wh - hvh);

        // Lerp current position toward target
        position.x = MathUtils.lerp(position.x, target.x, LERP_SPEED * (1f / 60f));
        position.y = MathUtils.lerp(position.y, target.y, LERP_SPEED * (1f / 60f));

        camera.position.set(position.x, position.y, 0);
        camera.update();
    }

    /**
     * Call this on player death — sets the lerp target to spawn,
     * camera will glide there automatically on subsequent Update() calls.
     */
    public void SetDeathTarget(float ww, float wh, float hvw, float hvh, float spawnX, float spawnY) {
        target.x = MathUtils.clamp(spawnX, hvw, ww - hvw);
        target.y = MathUtils.clamp(spawnY, hvh, wh - hvh);
    }

    public float getCamX() {
        return camera.position.x;
    }

    public float getCamY() {
        return camera.position.y;
    }
}
