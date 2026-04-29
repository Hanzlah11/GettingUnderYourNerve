package Game.GettingUnderYourNerve.Utilities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class GameAssetManager {

    public final AssetManager manager = new AssetManager();

    // --- WATER & SKY ---
    //public static final String WATER_DEEP = "Treasure Hunters/Merchant Ship/Sprites/Water/Water/Bottom.png";
    //public static final String WATER_SURFACE = "Treasure Hunters/Merchant Ship/Sprites/Water/Water/Top.png";
    public static final String SKY_BASE = "Treasure Hunters/Palm Tree Island/Sprites/Background/Additional Sky.png";

    // --- PLAYER ANIMATIONS ---
    public static final String PLAYER_IDLE_PREFIX = "Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/09-Idle Sword/Idle Sword ";
    public static final String PLAYER_RUN_PREFIX = "Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/10-Run Sword/Run Sword ";
    public static final String PLAYER_JUMP_PREFIX = "Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/11-Jump Sword/Jump Sword ";
    public static final String PLAYER_FALL_PREFIX = "Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/12-Fall Sword/Fall Sword ";

    // --- COLLECTABLES ---
    public static final String COIN_GOLD_PREFIX = "Treasure Hunters/Pirate Treasure/Sprites/Gold Coin/";
    public static final String COIN_SILVER_PREFIX = "Treasure Hunters/Pirate Treasure/Sprites/Silver Coin/";
    public static final String COIN_DIAMOND_PREFIX = "Treasure Hunters/Pirate Treasure/Sprites/Blue Diamond/";
    public static final String POTION_RED_PREFIX = "Treasure Hunters/Pirate Treasure/Sprites/Red Potion/";
    public static final String POTION_BLUE_PREFIX = "Treasure Hunters/Pirate Treasure/Sprites/Blue Potion/";
    public static final String POTION_GREEN_PREFIX = "Treasure Hunters/Pirate Treasure/Sprites/Green Bottle/";

    // --- PLATFORMS ---
    public static final String PLATFORM_HELI_PREFIX = "Treasure Hunters/Palm Tree Island/Sprites/helicopter/helicopter ";

    // --- ENEMIES ---
    public static final String SHELL_IDLE_PREFIX = "Treasure Hunters/Shooter Traps/Sprites/Seashell/Seashell Idle/";
    public static final String SHELL_FIRE_PREFIX = "Treasure Hunters/Shooter Traps/Sprites/Seashell/Seashell Fire/";
    public static final String PEARL_IDLE_PREFIX = "Treasure Hunters/Shooter Traps/Sprites/Seashell/Pearl Idle/";
    public static final String PEARL_DESTROYED_PREFIX = "Treasure Hunters/Shooter Traps/Sprites/Seashell/Pearl Destroyed/";

    public void loadAllAssets() {
        //manager.load(WATER_DEEP, Texture.class);
        //manager.load(WATER_SURFACE, Texture.class);
        manager.load(SKY_BASE, Texture.class);

        // Uses "%02d" for 01, 02, 03 formatting
        loadFrames(PLAYER_IDLE_PREFIX, 5, "%02d");
        loadFrames(PLAYER_RUN_PREFIX, 6, "%02d");
        loadFrames(PLAYER_JUMP_PREFIX, 3, "%02d");
        loadFrames(PLAYER_FALL_PREFIX, 1, "%02d");

        loadFrames(COIN_GOLD_PREFIX, 4, "%02d");
        loadFrames(COIN_SILVER_PREFIX, 4, "%02d");
        loadFrames(COIN_DIAMOND_PREFIX, 4, "%02d");

        loadFrames(POTION_RED_PREFIX, 4, "%02d");
        loadFrames(POTION_BLUE_PREFIX, 4, "%02d");
        loadFrames(POTION_GREEN_PREFIX, 4, "%02d");

        loadFrames(PLATFORM_HELI_PREFIX, 4, "%02d");

        // Uses "%d" for 1, 2, 3 formatting
        loadFrames(SHELL_IDLE_PREFIX, 1, "%d");
        loadFrames(SHELL_FIRE_PREFIX, 6, "%d");
        loadFrames(PEARL_IDLE_PREFIX, 1, "%d");
        loadFrames(PEARL_DESTROYED_PREFIX, 3, "%d");
    }

    private void loadFrames(String prefix, int frameCount, String format) {
        for (int i = 1; i <= frameCount; i++) {
            String path = prefix + String.format(format, i) + ".png";
            manager.load(path, Texture.class);
        }
    }

    public Animation<TextureRegion> getAnimation(String prefix, int frameCount, float frameDuration, Animation.PlayMode mode, String format) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 1; i <= frameCount; i++) {
            String path = prefix + String.format(format, i) + ".png";
            Texture tex = manager.get(path, Texture.class);
            frames.add(new TextureRegion(tex));
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    public void dispose() {
        manager.dispose();
    }
}
