package Game.GettingUnderYourNerve;

import Game.GettingUnderYourNerve.MainGame.TitleScreen;
//import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends Game {

    // --- Global Shared Utilities ---
    public SpriteBatch batch;
    public GameAssetManager assets;

    // --- Box2D & Scaling (Kept here so other classes don't break) ---
    public static final float PPM = 32f;

    // --- Collision Bits (Kept here so other classes don't break) ---
    public static final short BIT_NONE       = 0;
    public static final short GROUND_BIT     = 1;
    public static final short PLAYER_BIT     = 2;
    public static final short ENEMY_BIT      = 4;
    public static final short PROJECTILE_BIT = 8;
    public static final short WATER_BIT      = 16;
    public static final short COIN_BIT       = 32;
    public static final short POTION_BIT     = 64;
    public static final short TRAP_BIT       = 128;
    public static final short SWORD_BIT      = 256;
    public static final short TRIGGER_BIT    = 512;

    @Override
    public void create() {
        // Initialize Global Graphics
        batch = new SpriteBatch();

        // Initialize Global Audio
        AudioManager.load();

        // Initialize Global Assets
        assets = new GameAssetManager();
        assets.loadAllAssets();
        assets.manager.finishLoading();

        // ---------------------------------------------------------
        // Transfer control to your gameplay screen!
        // ---------------------------------------------------------
        this.setScreen(new TitleScreen(this));
    }

    @Override
    public void render() {
        // CRITICAL: This passes the render loop to whatever screen is currently active
        super.render();
    }

    @Override
    public void dispose() {
        // Only dispose of global things here
        batch.dispose();
        assets.dispose();
        AudioManager.dispose();
    }
}
