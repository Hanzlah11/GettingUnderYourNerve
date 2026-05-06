package Game.GettingUnderYourNerve.Map;

import Game.GettingUnderYourNerve.*;
import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.Trolls.*;
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

    private static final float PPM = 32f;

    public TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    private GameAssetManager assets;

    // Platforms
    private Array<HorizontalPlatform> horizontalPlatforms;
    private Array<VerticalPlatform>   verticalPlatforms;
    private Array<DevilPlatform> devilPlatforms;

    // Collectables
    private Array<Coin>   coins;
    private Array<Potion> potions;

    // Background
    BackGround backGround;

    // Water & Enemies
    private Array<Water> waterPools;
    private Array<Enemy> enemies;

    // Traps
    public Array<Trap> mapTraps;

    // Boxes & Trolls
    private Array<Box> boxes;
    private Array<EvilCoin> evilCoins;
    private Array<GhostBlock> ghostBlocks; // --- NEW: GHOST BLOCKS ---

    // ---------------------------------------------------------------
    // Trigger system
    // ---------------------------------------------------------------
    private Array<TrollTile>   trollTiles;
    private Array<TrollTile>   deactivatedTrollTiles;
    private Array<TriggerZone> triggerZones;

    // Pending trigger activations — collected during contact callback,
    // applied safely outside the Box2D step in updateTriggers()
    private Array<Integer> pendingTriggers;

    public java.util.HashMap<Coin, String> coinIds = new java.util.HashMap<>();
    public java.util.ArrayList<String> collectedCoinIds = new java.util.ArrayList<>();
    public java.util.ArrayList<String> pendingDestroyCoinIds = new java.util.ArrayList<>();

    public java.util.HashMap<Potion, String> potionIds = new java.util.HashMap<>();
    public java.util.ArrayList<String> collectedPotionIds = new java.util.ArrayList<>();
    public java.util.ArrayList<String> pendingDestroyPotionIds = new java.util.ArrayList<>();

    private int currentLevel;

    public PlayableMap(GameAssetManager assets, int level) {
        this.assets = assets;

        String mapPath = (level == 0) ? "data/tilemaps/prologue.tmx" : "data/tilemaps/untitled.tmx";
        this.currentLevel = level;

        map = new TmxMapLoader().load(mapPath);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f / PPM);

        horizontalPlatforms   = new Array<>();
        verticalPlatforms     = new Array<>();
        devilPlatforms        = new Array<>();
        coins                 = new Array<>();
        potions               = new Array<>();
        waterPools            = new Array<>();
        enemies               = new Array<>();
        mapTraps              = new Array<>();
        boxes                 = new Array<>();
        evilCoins             = new Array<>();
        ghostBlocks           = new Array<>(); // --- NEW ---

        trollTiles            = new Array<>();
        deactivatedTrollTiles = new Array<>();
        triggerZones          = new Array<>();
        pendingTriggers       = new Array<>();

        backGround = new BackGround();
    }

    // Safely queues collected items for destruction so Box2D doesn't crash
    public void applyLoadedCollectables(java.util.ArrayList<String> loadedCoins,
                                        java.util.ArrayList<String> loadedPotions) {
        if (loadedCoins != null) {
            this.collectedCoinIds = loadedCoins;
            this.pendingDestroyCoinIds.addAll(loadedCoins);
        }
        if (loadedPotions != null) {
            this.collectedPotionIds = loadedPotions;
            this.pendingDestroyPotionIds.addAll(loadedPotions);
        }
    }

    // ===============================================================
    // createPhysicsFromMap
    // ===============================================================
    public void createPhysicsFromMap(World world) {
        TiledMapTileLayer layer =
            (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");
        if (layer == null) return;

        BodyDef      bdef  = new BodyDef();
        FixtureDef   fdef  = new FixtureDef();
        PolygonShape shape = new PolygonShape();

        for (int row = 0; row < layer.getHeight(); row++) {
            for (int col = 0; col < layer.getWidth(); col++) {
                TiledMapTileLayer.Cell cell = layer.getCell(col, row);
                if (cell == null || cell.getTile() == null) continue;

                MapProperties tileProps = cell.getTile().getProperties();

                if (tileProps.containsKey("Solid")) {

                    // Skip tiles covered by a TrollZone — they get their
                    // own dedicated body created in createTrollTilesFromMap()
                    if (isCoveredByTrollZone(col, row)) continue;

                    bdef.type = BodyDef.BodyType.StaticBody;
                    bdef.position.set(
                        (col + 0.5f) * 16 / PPM,
                        (row + 0.5f) * 16 / PPM
                    );

                    Body body = world.createBody(bdef);
                    shape.setAsBox(8 / PPM, 8 / PPM);
                    fdef.shape = shape;
                    fdef.filter.categoryBits = Main.GROUND_BIT;
                    fdef.filter.maskBits     = Main.PLAYER_BIT | Main.ENEMY_BIT |
                        Main.PROJECTILE_BIT;
                    body.createFixture(fdef);
                }
            }
        }
        shape.dispose();

        createTrollTilesFromMap(world);
        createTriggersFromMap(world);
        createPlatformsFromMap(world);
        createCoinsFromMap(world);
        createPotionsFromMap(world);
        createWaterFromMap(world);
        createEnemiesFromMap(world, currentLevel);
        createTrapsFromMap(world);
        createBoxesFromMap(world);
        createEvilCoinsFromMap(world);
        createGhostBlocksFromMap(world); // --- NEW ---
    }

    // ===============================================================
    // isCoveredByTrollZone
    // ===============================================================
    private boolean isCoveredByTrollZone(int col, int row) {
        MapLayer trollLayer = map.getLayers().get("TrollZones");
        if (trollLayer == null) return false;

        int tileWidth  = map.getProperties().get("tilewidth",  Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);

        float tileCenterX = (col + 0.5f) * tileWidth;
        float tileCenterY = (row + 0.5f) * tileHeight;

        for (MapObject object : trollLayer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            if (rect.contains(tileCenterX, tileCenterY)) return true;
        }
        return false;
    }

    // ===============================================================
    // createTrollTilesFromMap
    // ===============================================================
    private void createTrollTilesFromMap(World world) {
        TiledMapTileLayer tileLayer =
            (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");
        MapLayer trollLayer = map.getLayers().get("TrollZones");
        if (tileLayer == null || trollLayer == null) return;

        int tileWidth  = map.getProperties().get("tilewidth",  Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);

        for (MapObject object : trollLayer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;

            MapProperties props = object.getProperties();
            if (!props.containsKey("id")) continue;

            int       triggerId = props.get("id", Integer.class);
            Rectangle rect      = ((RectangleMapObject) object).getRectangle();

            int colStart = (int) (rect.x / tileWidth);
            int colEnd   = (int) ((rect.x + rect.width)  / tileWidth);
            int rowStart = (int) (rect.y / tileHeight);
            int rowEnd   = (int) ((rect.y + rect.height) / tileHeight);

            for (int row = rowStart; row < rowEnd; row++) {
                for (int col = colStart; col < colEnd; col++) {
                    TiledMapTileLayer.Cell cell = tileLayer.getCell(col, row);
                    if (cell == null || cell.getTile() == null) continue;

                    BodyDef bdef = new BodyDef();
                    bdef.type = BodyDef.BodyType.StaticBody;
                    bdef.position.set(
                        (col + 0.5f) * tileWidth  / PPM,
                        (row + 0.5f) * tileHeight / PPM
                    );
                    Body body = world.createBody(bdef);

                    PolygonShape shape = new PolygonShape();
                    shape.setAsBox(
                        (tileWidth  / 2f) / PPM,
                        (tileHeight / 2f) / PPM
                    );
                    FixtureDef fdef = new FixtureDef();
                    fdef.shape = shape;
                    fdef.filter.categoryBits = Main.GROUND_BIT;
                    fdef.filter.maskBits     = Main.PLAYER_BIT | Main.ENEMY_BIT |
                        Main.PROJECTILE_BIT;
                    body.createFixture(fdef);
                    shape.dispose();

                    trollTiles.add(new TrollTile(triggerId, body, tileLayer, col, row));
                }
            }
        }
    }

    // ===============================================================
    // createTriggersFromMap
    // ===============================================================
    private void createTriggersFromMap(World world) {
        MapLayer triggerLayer = map.getLayers().get("Triggers");
        if (triggerLayer == null) return;

        for (MapObject object : triggerLayer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;

            MapProperties props = object.getProperties();
            if (!props.containsKey("id")) continue;

            int       triggerId = props.get("id", Integer.class);
            Rectangle rect      = ((RectangleMapObject) object).getRectangle();

            BodyDef bdef = new BodyDef();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set(
                (rect.x + rect.width  / 2f) / PPM,
                (rect.y + rect.height / 2f) / PPM
            );

            Body body = world.createBody(bdef);

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(
                (rect.width  / 2f) / PPM,
                (rect.height / 2f) / PPM
            );

            FixtureDef fdef = new FixtureDef();
            fdef.shape       = shape;
            fdef.isSensor    = true;
            fdef.filter.categoryBits = Main.TRIGGER_BIT;
            fdef.filter.maskBits     = Main.PLAYER_BIT;

            TriggerZone zone = new TriggerZone(triggerId, body);
            body.createFixture(fdef).setUserData(zone);

            triggerZones.add(zone);
            shape.dispose();
        }
    }

    // ===============================================================
    // createGhostBlocksFromMap (NEW)
    // ===============================================================
    private void createGhostBlocksFromMap(World world) {
        MapLayer layer = map.getLayers().get("GhostBlocks");
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;
            ghostBlocks.add(new GhostBlock(world, object, assets));
        }
    }

    // ===============================================================
    // createBoxesFromMap
    // ===============================================================
    private void createBoxesFromMap(World world) {
        MapLayer layer = map.getLayers().get("Boxes");
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;
            if (object.getName() == null) continue;

            switch (object.getName()) {
                case "NormalBox":
                    boxes.add(new NormalBox(world, object, assets));
                    break;
                case "RotatingBox":
                    boxes.add(new RotatingBox(world, object, assets));
                    break;
                case "LauncherBox":
                    boxes.add(new LauncherBox(world, object, assets));
                    break;
            }
        }
    }

    // ===============================================================
    // createEvilCoinsFromMap
    // ===============================================================
    private void createEvilCoinsFromMap(World world) {
        MapLayer layer = map.getLayers().get("EvilCoins");
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;
            evilCoins.add(new EvilCoin(world, object, assets));
        }
    }

    // ===============================================================
    // activateTrigger — called by WorldContactListener
    // ===============================================================
    public void activateTrigger(int triggerId) {
        for (TriggerZone zone : triggerZones) {
            if (zone.id == triggerId && zone.fired) return;
        }
        if (!pendingTriggers.contains(triggerId, false)) {
            pendingTriggers.add(triggerId);
        }
    }

    // ===============================================================
    // updateTriggers — called every frame from UpdateMap()
    // ===============================================================
    private void updateTriggers(World world) {
        if (pendingTriggers.size == 0) return;

        for (int triggerId : pendingTriggers) {

            for (TriggerZone zone : triggerZones) {
                if (zone.id == triggerId) zone.fired = true;
            }

            Iterator<TrollTile> iter = trollTiles.iterator();
            while (iter.hasNext()) {
                TrollTile troll = iter.next();
                if (troll.triggerId == triggerId && !troll.activated) {
                    troll.activated = true;

                    world.destroyBody(troll.body);
                    troll.body = null;

                    troll.layer.setCell(troll.col, troll.row, null);

                    deactivatedTrollTiles.add(troll);
                    iter.remove();
                }
            }
        }

        pendingTriggers.clear();
    }

    // ===============================================================
    // resetTriggers — called on player respawn
    // ===============================================================
    public void resetTriggers(World world) {

        for (TriggerZone zone : triggerZones) {
            zone.fired = false;
        }

        // --- NEW: Reset all ghost blocks so they become invisible again ---
        for (GhostBlock gb : ghostBlocks) {
            gb.reset();
        }

        Iterator<TrollTile> iter = deactivatedTrollTiles.iterator();
        while (iter.hasNext()) {
            TrollTile troll = iter.next();

            troll.layer.setCell(troll.col, troll.row, troll.originalCell);

            BodyDef bdef = new BodyDef();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set(troll.bodyX, troll.bodyY);
            Body newBody = world.createBody(bdef);

            int tileWidth  = map.getProperties().get("tilewidth",  Integer.class);
            int tileHeight = map.getProperties().get("tileheight", Integer.class);

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(
                (tileWidth  / 2f) / PPM,
                (tileHeight / 2f) / PPM
            );
            FixtureDef fdef = new FixtureDef();
            fdef.shape = shape;
            fdef.filter.categoryBits = Main.GROUND_BIT;
            fdef.filter.maskBits     = Main.PLAYER_BIT | Main.ENEMY_BIT | Main.PROJECTILE_BIT;
            newBody.createFixture(fdef);
            shape.dispose();

            troll.body      = newBody;
            troll.activated = false;

            trollTiles.add(troll);
            iter.remove();
        }

        pendingTriggers.clear();
    }

    // ===============================================================
    // UpdateMap — called every frame from PlayScreen (when not paused)
    // ===============================================================
    public void UpdateMap(OrthographicCamera camera, float dt,
                          World world, Player player) {
        updateTriggers(world);
        updatePlatforms(dt, player);
        updateCoins(dt, world);
        updatePotions(dt, world);
        updatewaters(dt);
        updateEnemies(dt, player, world);
        updateTraps(dt);
        updateBoxes(dt);
        updateEvilCoins(dt, player, world);
    }

    // ===============================================================
    // Render
    // ===============================================================
    public void RenderTileMap(OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    // ===============================================================
    // Creation methods
    // ===============================================================

    public void createEnemiesFromMap(World world, int level) {
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof TiledMapTileLayer) continue;
            for (MapObject object : layer.getObjects()) {
                if (object.getProperties().containsKey("x")) {
                    float  x    = object.getProperties().get("x", Float.class);
                    float  y    = object.getProperties().get("y", Float.class);
                    String name = object.getName();

                    if ("Batman".equals(name) && level == 0) continue;
                    if ("Shell".equals(name)) enemies.add(new Shell(world, x, y, assets));
                    else if ("Crab".equals(name)) enemies.add(new Crab(world, x, y, assets));
                    else if ("Batman".equals(name)) enemies.add(new Batman(world, x, y, assets));
                }
            }
        }
    }

    public Batman spawnBatman(World world, float pixelX, float pixelY) {
        Batman b = new Batman(world, pixelX, pixelY, assets);
        enemies.add(b);
        return b;
    }

    public void createTrapsFromMap(World world) {
        MapLayer trapLayer = map.getLayers().get("Traps");
        if (trapLayer == null) return;
        for (MapObject object : trapLayer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;
            if (object.getName() == null) continue;
            if (object.getName().equals("spike"))
                mapTraps.add(new Spike(world, object, assets));
            else if (object.getName().equals("spikedball"))
                mapTraps.add(new SpikedBall(world, object, assets));
        }
    }

    public void createWaterFromMap(World world) {
        MapLayer layer = map.getLayers().get("Water");
        if (layer == null) return;
        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            waterPools.add(new Water(rect, assets));

            BodyDef bdef = new BodyDef();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set(
                (rect.x + rect.width  / 2f) / PPM,
                (rect.y + rect.height / 2f) / PPM
            );
            Body body = world.createBody(bdef);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox((rect.width / 2f) / PPM, (rect.height / 2f) / PPM);
            FixtureDef fdef = new FixtureDef();
            fdef.shape = shape;
            fdef.isSensor = true;
            fdef.filter.categoryBits = Main.WATER_BIT;
            fdef.filter.maskBits     = Main.PLAYER_BIT;
            body.createFixture(fdef).setUserData("water_sensor");
            shape.dispose();
        }
    }

    public void createPotionsFromMap(World world) {
        MapLayer layer = map.getLayers().get("Collectables");
        if (layer == null) return;
        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;
            Rectangle     rect  = ((RectangleMapObject) object).getRectangle();
            MapProperties props = object.getProperties();
            String type = props.containsKey("type") ? props.get("type", String.class) : "null";

            if (type.equals("Potion")) {
                Potion newPotion = new Potion(world, rect, object.getName(), assets);
                potions.add(newPotion);
                potionIds.put(newPotion, "potion_" + rect.x + "_" + rect.y);
            }
        }
    }

    public void createCoinsFromMap(World world) {
        MapLayer layer = map.getLayers().get("Collectables");
        if (layer == null) return;
        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;
            Rectangle     rect  = ((RectangleMapObject) object).getRectangle();
            MapProperties props = object.getProperties();
            String type = props.containsKey("type") ? props.get("type", String.class) : "null";

            if (type.equals("Coin")) {
                Coin newCoin = new Coin(world, rect, object.getName(), assets);
                coins.add(newCoin);
                coinIds.put(newCoin, "coin_" + rect.x + "_" + rect.y);
            }
        }
    }

    public void createPlatformsFromMap(World world) {
        MapLayer platformLayer = map.getLayers().get("Platforms");
        if (platformLayer == null) return;
        for (MapObject object : platformLayer.getObjects()) {
            if (!(object instanceof RectangleMapObject) || object.getName() == null) continue;
            Rectangle     rect  = ((RectangleMapObject) object).getRectangle();
            MapProperties props = object.getProperties();
            String name  = object.getName();
            float  speed = props.containsKey("Speed") ? props.get("Speed", Float.class) : 1f;

            if (name.equals("HorizontalPlatform")) {
                float startX      = rect.x;
                float tiledStartX = props.containsKey("startX") ? props.get("startX", Float.class) : rect.x;
                float tiledEndX   = props.containsKey("endX")   ? props.get("endX",   Float.class) : rect.x;
                float endX        = startX + (tiledEndX - tiledStartX);
                horizontalPlatforms.add(new HorizontalPlatform(world, rect, startX, endX, speed, assets));
            } else if (name.equals("VerticalPlatform")) {
                float startY      = rect.y;
                float tiledStartY = props.containsKey("startY") ? props.get("startY", Float.class) : rect.y;
                float tiledEndY   = props.containsKey("endY")   ? props.get("endY",   Float.class) : rect.y;
                float moveDistance = tiledStartY - tiledEndY;
                float endY        = startY + moveDistance;
                verticalPlatforms.add(new VerticalPlatform(world, rect, startY, endY, speed, assets));
            } else if (name.equals("DevilPlatform")) {
                float startX      = rect.x;
                float tiledStartX = props.containsKey("startX") ? props.get("startX", Float.class) : rect.x;
                float tiledEndX   = props.containsKey("endX")   ? props.get("endX",   Float.class) : rect.x;
                float endX        = startX + (tiledEndX - tiledStartX);
                devilPlatforms.add(new DevilPlatform(world, rect, startX, endX, speed, assets));
            }
        }
    }

    // ===============================================================
    // Update methods
    // ===============================================================

    public void updateCoins(float dt, World world) {
        Iterator<Coin> iter = coins.iterator();
        while (iter.hasNext()) {
            Coin coin = iter.next();

            String id = coinIds.get(coin);
            if (pendingDestroyCoinIds.contains(id)) {
                coin.isCollected = true;
                pendingDestroyCoinIds.remove(id);
            }

            coin.update(dt);

            if (coin.isCollected && !coin.isDestroyed) {
                world.destroyBody(coin.body);
                coin.isDestroyed = true;

                if (!collectedCoinIds.contains(id)) {
                    collectedCoinIds.add(id);
                }

                iter.remove();
            }
        }
    }

    public void updatePotions(float dt, World world) {
        Iterator<Potion> iter = potions.iterator();
        while (iter.hasNext()) {
            Potion potion = iter.next();

            String id = potionIds.get(potion);
            if (pendingDestroyPotionIds.contains(id)) {
                potion.isCollected = true;
                pendingDestroyPotionIds.remove(id);
            }

            potion.update(dt);

            if (potion.isCollected && !potion.isDestroyed) {
                world.destroyBody(potion.body);
                potion.isDestroyed = true;

                if (!collectedPotionIds.contains(id)) {
                    collectedPotionIds.add(id);
                }

                iter.remove();
            }
        }
    }

    public void updatePlatforms(float dt, Player player) {
        for (HorizontalPlatform hp : horizontalPlatforms) hp.update(dt);
        for (VerticalPlatform   vp : verticalPlatforms)   vp.update(dt);
        for (DevilPlatform      dp : devilPlatforms)      dp.update(dt, player);
    }

    public void updateEnemies(float dt, Player player, World world) {
        Iterator<Enemy> iter = enemies.iterator();
        while (iter.hasNext()) {
            Enemy enemy = iter.next();
            enemy.updateEnemy(dt, player);
            if (enemy.setToDestroy && !enemy.destroyed) {
                world.destroyBody(enemy.b2body);
                enemy.destroyed = true;
                iter.remove();
            }
        }
    }

    public void updatewaters(float dt) {
        for (Water w : waterPools) w.update(dt);
    }

    public void updateTraps(float dt) {
        for (Trap t : mapTraps) t.update(dt);
    }

    public void updateBoxes(float dt) {
        for (Box b : boxes) b.update(dt);
    }

    public void updateEvilCoins(float dt, Player player, World world) {
        Iterator<EvilCoin> iter = evilCoins.iterator();
        while (iter.hasNext()) {
            EvilCoin coin = iter.next();
            coin.update(dt, player);
            if (coin.setToDestroy && !coin.destroyed) {
                world.destroyBody(coin.body);
                coin.destroyed = true;
                iter.remove();
            }
        }
    }

    // ===============================================================
    // Draw methods
    // ===============================================================

    public void drawCoins(SpriteBatch batch)      { for (Coin c : coins)      c.draw(batch); }
    public void drawPotions(SpriteBatch batch)    { for (Potion p : potions)  p.draw(batch); }
    public void drawPlatforms(SpriteBatch batch)  {
        for (HorizontalPlatform hp : horizontalPlatforms) hp.draw(batch);
        for (VerticalPlatform   vp : verticalPlatforms)   vp.draw(batch);
        for (DevilPlatform      dp : devilPlatforms)      dp.draw(batch);
    }
    public void drawWater(SpriteBatch batch)             { for (Water w : waterPools) w.render(batch); }
    public void drawEnemies(SpriteBatch batch, float dt) { for (Enemy e : enemies)    e.render(dt, batch); }
    public void drawTraps(SpriteBatch batch, float dt)   { for (Trap t : mapTraps)    t.render(batch, dt); }
    public void drawBoxes(SpriteBatch batch)             { for (Box b : boxes)        b.render(batch); }
    public void drawEvilCoins(SpriteBatch batch, float dt) { for (EvilCoin ec : evilCoins) ec.render(batch, dt); }
    public void drawGhostBlocks(SpriteBatch batch)       { for (GhostBlock gb : ghostBlocks) gb.render(batch); }

    public void DrawElements(SpriteBatch batch, float dt) {
        drawPlatforms(batch);
        drawBoxes(batch);
        drawGhostBlocks(batch);
        drawCoins(batch);
        drawPotions(batch);
        drawWater(batch);
        drawEnemies(batch, dt);
        drawTraps(batch, dt);
        drawEvilCoins(batch, dt);
    }

    public void DrawBackGround(SpriteBatch batch, GameCam camera,
                               Viewport viewport, float dt) {
        float camX       = camera.GetCam().position.x;
        float camY       = camera.GetCam().position.y;
        float viewWidth  = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        backGround.RenderBg(camX, camY, viewWidth, viewHeight, dt, batch);
    }

    public TiledMap GetMap() { return map; }

    public float getMapWidthInMeters() {
        int mapWidth  = map.getProperties().get("width",     Integer.class);
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        return (mapWidth * tileWidth) / PPM;
    }

    public float getMapHeightInMeters() {
        int mapHeight  = map.getProperties().get("height",     Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);
        return (mapHeight * tileHeight) / PPM;
    }

    // Inside PlayableMap.java
    public Batman getBatman() {
        for (Enemy e : enemies) {
            if (e instanceof Batman) return (Batman) e;
        }
        return null;
    }

    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        backGround.dispose();
        for (Enemy e : enemies) e.dispose();
        enemies.clear();
        coins.clear();
        potions.clear();
        waterPools.clear();
        horizontalPlatforms.clear();
        verticalPlatforms.clear();
        trollTiles.clear();
        deactivatedTrollTiles.clear();
        triggerZones.clear();
        boxes.clear();
        evilCoins.clear();
        ghostBlocks.clear();
    }

    // --- RE-ADDED ---
    public int getLevelNumber() {
        return currentLevel;
    }
}
