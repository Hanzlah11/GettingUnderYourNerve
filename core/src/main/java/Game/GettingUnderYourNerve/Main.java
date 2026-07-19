package Game.GettingUnderYourNerve;

import Game.GettingUnderYourNerve.MainGame.LoadingScreen;
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

    public static final float PPM = 32f;

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
    public static final short CHECKPOINT_BIT = 1024;

    @Override
    public void create() {
        batch = new SpriteBatch();

        AudioManager.load();
        GameAssetManager.loadAllAssets();
        GameAssetManager.manager.finishLoading();

        this.setScreen(new LoadingScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        // Only dispose of global things here
        batch.dispose();
        assets.dispose();
        AudioManager.dispose();
    }

    public SpriteBatch getBatch() {
        return batch;
    }
}
