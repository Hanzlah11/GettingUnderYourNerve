package Game.GettingUnderYourNerve.Utilities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuBackground {

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private Viewport viewport;

    private float autoPanX = 0f;
    private static final float PAN_SPEED = 15f; // Pixels per second

    public MenuBackground() {
        // Load your specifically designed background map
        map = new TmxMapLoader().load("data/tilemaps/menu_bg.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1f); // 1:1 pixel scale

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(800, 480, camera);
        camera.position.set(400, 240, 0); // Center the camera initially
    }

    public void updateAndRender(float delta, SpriteBatch batch) {
        // Slow horizontal pan to make the background feel alive
        autoPanX += PAN_SPEED * delta;
        camera.position.x = 400 + autoPanX;

        // Loop the camera if it reaches the edge of your background map
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        if (camera.position.x > mapWidth - 400) {
            autoPanX = 0;
            camera.position.x = 400;
        }

        camera.update();

        // Render the tilemap first
        renderer.setView(camera);
        renderer.render();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void dispose() {
        if (map != null) map.dispose();
        if (renderer != null) renderer.dispose();
    }
}
