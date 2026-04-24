package Game.GettingUnderYourNerve;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.*;

public class PlayableMap {
    private static final float PPM = 32f; // Pixels Per Meter
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    public PlayableMap(){
        // Load Map and scale the renderer to meters
        map = new TmxMapLoader().load("data/tilemaps/untitled.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f / PPM);
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

                // Checks for your custom "solid" property in Tiled
                if (cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey("Solid")) {
                    bdef.type = BodyDef.BodyType.StaticBody;

                    // Since your XML shows tilewidth="16", we center the rigid body at 8px (0.5 of a tile)
                    bdef.position.set((col + 0.5f) * 16 / PPM, (row + 0.5f) * 16 / PPM);

                    Body body = world.createBody(bdef);
                    shape.setAsBox(8 / PPM, 8 / PPM); // Half-width and half-height of 16
                    fdef.shape = shape;
                    body.createFixture(fdef);
                }
            }
        }
        shape.dispose();
    }


    public void UpdateMap(OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.render();
    }


    public TiledMap GetMap(){
        return map;
    }


    public void dispose(){
        map.dispose();
        mapRenderer.dispose();
    }


    public float getMapWidthInMeters() {
        int mapWidth = map.getProperties().get("width", Integer.class);
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        return (mapWidth * tileWidth) / Main.PPM;
    }


    public float getMapHeightInMeters() {
        int mapHeight = map.getProperties().get("height", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);
        return (mapHeight * tileHeight) / Main.PPM;
    }

}
