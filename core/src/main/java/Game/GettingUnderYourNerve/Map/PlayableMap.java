package Game.GettingUnderYourNerve.Map;

import Game.GettingUnderYourNerve.*;
import Game.GettingUnderYourNerve.Collectables.Coin;
import Game.GettingUnderYourNerve.Collectables.Potion;
import Game.GettingUnderYourNerve.Enemies.Crab;
import Game.GettingUnderYourNerve.Enemies.Enemy;
import Game.GettingUnderYourNerve.Enemies.Shell;
import Game.GettingUnderYourNerve.Trap.Spike;
import Game.GettingUnderYourNerve.Trap.SpikedBall;
import Game.GettingUnderYourNerve.Trap.Trap;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
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
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Iterator;

public class PlayableMap {
    private static final float PPM = 32f; // Pixels Per Meter
    public TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    // --- THE VAULT ---
    private GameAssetManager assets;

    // Platform Storage
    private Array<HorizontalPlatform> horizontalPlatforms;
    private Array<VerticalPlatform> verticalPlatforms;

    // Coins & Potions
    private Array<Coin> coins;
    private Array<Potion> potions;

    // BackGround
    BackGround backGround;

    //Water
    private Array<Water> waterPools;
    private Array<Enemy> enemies;

    //Traps
    public Array<Trap> mapTraps;

    public PlayableMap(GameAssetManager assets) {
        this.assets = assets; // Store the vault!

        map = new TmxMapLoader().load("data/tilemaps/untitled.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f / PPM);

        horizontalPlatforms = new Array<HorizontalPlatform>();
        verticalPlatforms = new Array<VerticalPlatform>();

        coins = new Array<Coin>();
        potions = new Array<Potion>();
        waterPools = new Array<Water>();
        enemies = new Array<Enemy>();

        backGround = new BackGround();

        mapTraps = new Array<>();
    }

    public void createEnemiesFromMap(World world) {
        // Loop through EVERY layer in the map[cite: 14]
        for (MapLayer layer : map.getLayers()) {

            // Skip Tile Layers; only look inside Object Layers[cite: 14]
            if (layer instanceof TiledMapTileLayer) continue;

            // Iterate through all objects in THIS specific layer[cite: 14]
            for (MapObject object : layer.getObjects()) {

                // Ensure the object has coordinates[cite: 14]
                if (object.getProperties().containsKey("x")) {
                    float x = object.getProperties().get("x", Float.class);
                    float y = object.getProperties().get("y", Float.class);
                    String name = object.getName();

                    // Spawn based on the Name field in Tiled[cite: 14]
                    if ("Shell".equals(name)) {
                        enemies.add(new Shell(world, x, y, assets));
                    } else if ("Crab".equals(name)) {
                        enemies.add(new Crab(world, x, y, assets));
                    }
                }
            }
        }
    }

    public void createTrapsFromMap(World world){
        MapLayer trapLayer = map.getLayers().get("Traps");

        if (trapLayer != null) {
            for (MapObject object : trapLayer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();

                    // Look for the name you typed in Tiled!
                    if (object.getName() != null && object.getName().equals("spike")) {
                        Spike spike = new Spike(world, object, assets);
                        mapTraps.add(spike);
                    } else if (object.getName().equals("spikedball")) {
                        mapTraps.add(new SpikedBall(world, object, assets)); // Automatically reads your custom properties!
                    }
                    // Later you can add: else if (object.getName().equals("sawblade")) ...
                }
            }
        }
    }

    public void createWaterFromMap(World world) {
        MapLayer layer = map.getLayers().get("Water");
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                // 1. Create the Visual Water
                waterPools.add(new Water(rect, assets));

                // 2. Create the Box2D Sensor Trap
                BodyDef bdef = new BodyDef();
                bdef.type = BodyDef.BodyType.StaticBody;
                bdef.position.set((rect.x + rect.width / 2f) / PPM, (rect.y + rect.height / 2f) / PPM);
                Body body = world.createBody(bdef);

                PolygonShape shape = new PolygonShape();
                shape.setAsBox((rect.width / 2f) / PPM, (rect.height / 2f) / PPM);

                FixtureDef fdef = new FixtureDef();
                fdef.shape = shape;
                fdef.isSensor = true; // Ghost Hitbox!
                fdef.filter.categoryBits = Main.WATER_BIT;
                fdef.filter.maskBits = Main.PLAYER_BIT;

                // Tag it so the Contact Listener recognizes it
                body.createFixture(fdef).setUserData("water_sensor");
                shape.dispose();
            }
        }
    }

    public void createPotionsFromMap(World world) {
        MapLayer layer = map.getLayers().get("Collectables");
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                MapProperties props = object.getProperties();

                String type = props.containsKey("type") ? props.get("type", String.class) : "null";
                if (type.equals("Potion")) {
                    String name = object.getName();
                    potions.add(new Potion(world, rect, name, assets));
                }
            }
        }
    }

    public void createCoinsFromMap(World world) {
        MapLayer layer = map.getLayers().get("Collectables");
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                MapProperties props = object.getProperties();

                String type = props.containsKey("type") ? props.get("type", String.class) : "null";
                if (type.equals("Coin")) {
                    String name = object.getName();
                    coins.add(new Coin(world, rect, name, assets));
                }
            }
        }
    }

    public void updateCoins(float dt, World world) {
        Iterator<Coin> iter = coins.iterator();
        while (iter.hasNext()) {
            Coin coin = iter.next();
            coin.update(dt);

            if (coin.isCollected && !coin.isDestroyed) {
                world.destroyBody(coin.body);
                coin.isDestroyed = true;
                // Memory is safe! AssetManager handles disposal now.
                iter.remove();
            }
        }
    }



    public void updatePotions(float dt, World world) {
        Iterator<Potion> iter = potions.iterator();
        while (iter.hasNext()) {
            Potion potion = iter.next();
            potion.update(dt);

            if (potion.isCollected && !potion.isDestroyed) {
                world.destroyBody(potion.body);
                potion.isDestroyed = true;
                // Memory is safe! AssetManager handles disposal now.
                iter.remove();
            }
        }
    }

    public void drawCoins(SpriteBatch batch) {
        for (Coin coin : coins) {
            coin.draw(batch);
        }
    }

    public void drawPotions(SpriteBatch batch) {
        for (Potion potion : potions) {
            potion.draw(batch);
        }
    }

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
                    float startX = rect.x;
                    float tiledStartX = props.containsKey("startX") ? props.get("startX", Float.class) : rect.x;
                    float tiledEndX = props.containsKey("endX") ? props.get("endX", Float.class) : rect.x;
                    float endX = startX + (tiledEndX - tiledStartX);

                    horizontalPlatforms.add(new HorizontalPlatform(world, rect, startX, endX, speed, assets));
                }
                else if (name.equals("VerticalPlatform")) {
                    float startY = rect.y;
                    float tiledStartY = props.containsKey("startY") ? props.get("startY", Float.class) : rect.y;
                    float tiledEndY = props.containsKey("endY") ? props.get("endY", Float.class) : rect.y;
                    float moveDistance = tiledStartY - tiledEndY;
                    float endY = startY + moveDistance;

                    verticalPlatforms.add(new VerticalPlatform(world, rect, startY, endY, speed, assets));
                }
            }
        }
    }

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
        createPotionsFromMap(world);
        createWaterFromMap(world);
        createEnemiesFromMap(world);
        createTrapsFromMap(world);
    }


    public void updateEnemies(float dt, Player player, World world) {
        Iterator<Enemy> enemyIter = enemies.iterator();
        while (enemyIter.hasNext()) {
            Enemy enemy = enemyIter.next();

            // 1. Update the enemy logic
            enemy.updateEnemy(dt, player);

            // 2. Safely remove them if they died this frame
            if (enemy.setToDestroy && !enemy.destroyed) {

                world.destroyBody(enemy.b2body); // Remove from Box2D Physics
                enemy.destroyed = true;          // Flag as fully handled

                // Optional: enemy.dispose(); if you need to clear anything else!

                enemyIter.remove();              // Safely wipe from the Java Array!
            }
        }
    }

    public void UpdateMap(OrthographicCamera camera, float dt, World world, Player player) {
        mapRenderer.setView(camera);
        mapRenderer.render();

        updatePlatforms(dt);
        updateCoins(dt, world);
        updatePotions(dt, world);
        updatewaters(dt);
        updateEnemies(dt, player, world);
        updateTraps(dt);
    }

    public void updatewaters(float dt){
        for (Water w : waterPools) {
            w.update(dt);
        }
    }

    public void updateTraps(float dt){
        for(Trap t: mapTraps){
            t.update(dt);
        }
    }

    public void drawTraps(SpriteBatch batch, float dt) {
        for (Trap t : mapTraps) {
            t.render(batch, dt);
        }
    }

    public void drawWater(SpriteBatch batch) {
        for (Water w : waterPools)
            w.render(batch);
    }

    public void drawEnemies(SpriteBatch batch, float dt) {
        for (Enemy e : enemies) {
            e.render(dt, batch);
        }
    }

    public void DrawElements(SpriteBatch batch, float dt) {
        drawPlatforms(batch);
        drawCoins(batch);
        drawPotions(batch);
        drawWater(batch);
        drawEnemies(batch, dt);
        drawTraps(batch, dt);
    }

    public void DrawBackGround(SpriteBatch batch, GameCam camera, Viewport viewport, float dt) {
        float camX = camera.GetCam().position.x;
        float camY = camera.GetCam().position.y;

        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        backGround.RenderBg(camX, camY, viewWidth, viewHeight, dt, batch);
    }

    public TiledMap GetMap() {
        return map;
    }

    public void dispose() {
        // 1. Dispose of the Map and its specific Renderer
        map.dispose();
        mapRenderer.dispose();

        // 2. Dispose of the Background system
        backGround.dispose();

        // 3. Dispose of all Enemies
        // We must manually loop through and call their dispose methods
        for (Enemy e : enemies) {
            e.dispose();
        }
        enemies.clear(); // Empty the reference list

        // 4. Clear Collectables and Water
        // These lists don't have separate .dispose() methods usually,
        // but clearing them helps the Garbage Collector.
        coins.clear();
        potions.clear();
        waterPools.clear();

        // 5. Clear Platform arrays
        horizontalPlatforms.clear();
        verticalPlatforms.clear();

        // IMPORTANT: As noted in your code, we do NOT call assets.dispose()
        // here. Main.java owns the GameAssetManager and will kill it at
        // the very end.
    }

    public float getMapWidthInMeters() {
        int mapWidth = map.getProperties().get("width", Integer.class);
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        return (mapWidth * tileWidth) / PPM;
    }

    public float getMapHeightInMeters() {
        int mapHeight = map.getProperties().get("height", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);
        return (mapHeight * tileHeight) / PPM;
    }
}
