package Game.GettingUnderYourNerve;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

public class PlayableMap {
    private static final float PPM = 32f; // Pixels Per Meter
    public TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    // --- NEW: Platform Storage ---
    private Array<HorizontalPlatform> horizontalPlatforms;
    private Array<VerticalPlatform> verticalPlatforms;

    // Coins
    private Array<Coin> coins;

    public PlayableMap() {
        map = new TmxMapLoader().load("data/tilemaps/untitled.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f / PPM);

        // --- NEW: Initialize the arrays ---
        horizontalPlatforms = new Array<HorizontalPlatform>();
        verticalPlatforms = new Array<VerticalPlatform>();
        coins = new Array<Coin>();
    }

    public void createCoinsFromMap(World world) {
        MapLayer layer = map.getLayers().get("Collectables"); // Match your Tiled layer name!
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                MapProperties props = object.getProperties();

                // Get the type property, default to gold if you forgot to set it
                String type = props.containsKey("type") ? props.get("type", String.class) : "null";
                if (type.equals("Coin")) {
                    String name = object.getName();
                    coins.add(new Coin(world, rect, name));
                }
            }
        }
    }

    public void updateCoins(float dt, World world) {
        Iterator<Coin> iter = coins.iterator();
        while (iter.hasNext()) {
            Coin coin = iter.next();
            coin.update(dt);

            // Safely destroy Box2D body and remove from rendering list
            if (coin.isCollected && !coin.isDestroyed) {
                world.destroyBody(coin.body);
                coin.isDestroyed = true;
                coin.dispose(); // Free up memory
                iter.remove();  // Take it out of the array
            }
        }
    }

    public void drawCoins(SpriteBatch batch) {
        for (Coin coin : coins) {
            coin.draw(batch);
        }
    }

    // --- NEW: Platform Spawner ---
    public void createPlatformsFromMap(World world) {
        MapLayer platformLayer = map.getLayers().get("Platforms");
        if (platformLayer == null) return;

        for (MapObject object : platformLayer.getObjects()) {
            if (object instanceof RectangleMapObject && object.getName() != null) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                MapProperties props = object.getProperties();
                String name = object.getName();

                float speed = props.containsKey("Speed") ? props.get("Speed", Float.class) : 1f;

                if (name.equals("HorizontalPlatform")) {
                    // 1. Anchor to the libGDX corrected position
                    float startX = rect.x;

                    // 2. Read the Tiled properties
                    float tiledStartX = props.containsKey("startX") ? props.get("startX", Float.class) : rect.x;
                    float tiledEndX = props.containsKey("endX") ? props.get("endX", Float.class) : rect.x;

                    // 3. X axes match in both systems, so just add the difference
                    float endX = startX + (tiledEndX - tiledStartX);

                    horizontalPlatforms.add(new HorizontalPlatform(world, rect, startX, endX, speed));
                }
                else if (name.equals("VerticalPlatform")) {
                    // 1. Anchor to the libGDX corrected position
                    float startY = rect.y;

                    // 2. Read the Tiled properties
                    float tiledStartY = props.containsKey("startY") ? props.get("startY", Float.class) : rect.y;
                    float tiledEndY = props.containsKey("endY") ? props.get("endY", Float.class) : rect.y;

                    // 3. THE FLIP: Tiled Y goes down, LibGDX goes up. We reverse the math here!
                    float moveDistance = tiledStartY - tiledEndY;
                    float endY = startY + moveDistance;

                    verticalPlatforms.add(new VerticalPlatform(world, rect, startY, endY, speed));
                }
            }
        }
    }

    // --- NEW: Platform Updater ---
    public void updatePlatforms(float dt) {
        for (HorizontalPlatform hp : horizontalPlatforms) {
            hp.update(dt);
        }
        for (VerticalPlatform vp : verticalPlatforms) {
            vp.update(dt);
        }
    }

    public void drawPlatforms(SpriteBatch batch) {
        for (HorizontalPlatform hp : horizontalPlatforms) {
            hp.draw(batch);
        }
        for (VerticalPlatform vp : verticalPlatforms) {
            vp.draw(batch);
        }
    }

    public void createPhysicsFromMap(World world) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");
        if (layer == null) return;

        BodyDef bdef = new BodyDef();
        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();

        for (int row = 0; row < layer.getHeight(); row++) {
            for (int col = 0; col < layer.getWidth(); col++) {
                TiledMapTileLayer.Cell cell = layer.getCell(col, row);

                if (cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey("Solid")) {
                    bdef.type = BodyDef.BodyType.StaticBody;
                    bdef.position.set((col + 0.5f) * 16 / PPM, (row + 0.5f) * 16 / PPM);

                    Body body = world.createBody(bdef);
                    shape.setAsBox(8 / PPM, 8 / PPM);
                    fdef.shape = shape;
                    body.createFixture(fdef);
                }
            }
        }
        shape.dispose();

        createPlatformsFromMap(world);
        createCoinsFromMap(world);
    }

    public void UpdateMap(OrthographicCamera camera, float dt, World world) {
        mapRenderer.setView(camera);
        mapRenderer.render();
        updatePlatforms(dt);
        updateCoins(dt,  world);
    }

    public void DrawElements(SpriteBatch batch) {
        drawPlatforms(batch);
        drawCoins(batch);
    }

    public TiledMap GetMap() {
        return map;
    }

    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
    }

    public float getMapWidthInMeters() {
        int mapWidth = map.getProperties().get("width", Integer.class);
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        return (mapWidth * tileWidth) / PPM; // Fixed to use local PPM instead of Main.PPM to prevent errors
    }

    public float getMapHeightInMeters() {
        int mapHeight = map.getProperties().get("height", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);
        return (mapHeight * tileHeight) / PPM; // Fixed to use local PPM instead of Main.PPM
    }
}
