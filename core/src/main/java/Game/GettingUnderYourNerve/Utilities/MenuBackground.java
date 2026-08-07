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
    private static final float PAN_SPEED = 15f;

    public MenuBackground() {
        map = new TmxMapLoader().load("data/tilemaps/menu_bg.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1f);

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(800, 480, camera);
    }

    public void updateAndRender(float delta, SpriteBatch batch) {
        viewport.apply();

        float halfVW = viewport.getWorldWidth() / 2f;
        float halfVH = viewport.getWorldHeight() / 2f;

        autoPanX += PAN_SPEED * delta;

        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);

        if (halfVW + autoPanX > mapWidth - halfVW) {
            autoPanX = 0f;
        }

        camera.position.set(halfVW + autoPanX, halfVH, 0);
        camera.update();

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
