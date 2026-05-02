package Game.GettingUnderYourNerve.Utilities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class GameAssetManager {

    public final AssetManager manager = new AssetManager();

    // --- WATER & SKY ---
    public static final String SKY_BASE =
        "Treasure Hunters/Palm Tree Island/Sprites/Background/Additional Sky.png";

    // --- PLAYER ---
    public static final String PLAYER_IDLE_PREFIX =
        "Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/09-Idle Sword/Idle Sword ";
    public static final String PLAYER_RUN_PREFIX =
        "Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/10-Run Sword/Run Sword ";
    public static final String PLAYER_JUMP_PREFIX =
        "Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/11-Jump Sword/Jump Sword ";
    public static final String PLAYER_FALL_PREFIX =
        "Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/12-Fall Sword/Fall Sword ";
    public static final String PLAYER_HIT_PREFIX =
        "Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/14-Hit Sword/Hit Sword ";
    public static final String PLAYER_ATTACK_PREFIX =
        "Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/17-Attack 3/Attack 3 ";

    // --- COLLECTABLES ---
    public static final String COIN_GOLD_PREFIX =
        "Treasure Hunters/Pirate Treasure/Sprites/Gold Coin/";
    public static final String COIN_SILVER_PREFIX =
        "Treasure Hunters/Pirate Treasure/Sprites/Silver Coin/";
    public static final String COIN_DIAMOND_PREFIX =
        "Treasure Hunters/Pirate Treasure/Sprites/Blue Diamond/";

    public static final String POTION_RED_PREFIX =
        "Treasure Hunters/Pirate Treasure/Sprites/Red Potion/";
    public static final String POTION_BLUE_PREFIX =
        "Treasure Hunters/Pirate Treasure/Sprites/Blue Potion/";
    public static final String POTION_GREEN_PREFIX =
        "Treasure Hunters/Pirate Treasure/Sprites/Green Bottle/";

    // --- PLATFORM ---
    public static final String PLATFORM_HELI_PREFIX =
        "Treasure Hunters/Palm Tree Island/Sprites/helicopter/helicopter ";

    // --- SHELL ---
    public static final String SHELL_IDLE_PREFIX =
        "Treasure Hunters/Shooter Traps/Sprites/Seashell/Seashell Idle/";
    public static final String SHELL_FIRE_PREFIX =
        "Treasure Hunters/Shooter Traps/Sprites/Seashell/Seashell Fire/";
    public static final String SHELL_BITE_PREFIX =
        "Treasure Hunters/Shooter Traps/Sprites/Seashell/Seashell Bite/";
    public static final String PEARL_IDLE_PREFIX =
        "Treasure Hunters/Shooter Traps/Sprites/Seashell/Pearl Idle/";
    public static final String PEARL_DESTROYED_PREFIX =
        "Treasure Hunters/Shooter Traps/Sprites/Seashell/Pearl Destroyed/";

    // --- CRAB ---
    public static final String CRAB_IDLE_PREFIX =
        "Treasure Hunters/The Crusty Crew/Sprites/Crabby/01-Idle/Idle ";
    public static final String CRAB_RUN_PREFIX =
        "Treasure Hunters/The Crusty Crew/Sprites/Crabby/02-Run/Run ";
    public static final String CRAB_ATTACK_PREFIX =
        "Treasure Hunters/The Crusty Crew/Sprites/Crabby/07-Attack/Attack ";

    //---WATER---
    public static final String WATER_DEEP =
        "Treasure Hunters/Palm Tree Island/Sprites/Background/Additional Water.png";
    public static final String WATER_SURFACE =
        "Treasure Hunters/Palm Tree Island/Sprites/Background/top/";

    //---TRAPS---
    public static final String SPIKE_ANIM_PREFIX =
        "Treasure Hunters/Palm Tree Island/Sprites/Objects/Spikes/";
    public static final String SPIKED_BALL =
        "Treasure Hunters/Palm Tree Island/Sprites/Objects/Spiked Ball/Spiked Ball.png";
    public static final String CHAIN_LINK =
        "Treasure Hunters/Palm Tree Island/Sprites/Objects/Spiked Ball/spiked_chain.png";

    //---UI---
    public static final String BOARD_TL =
        "Treasure Hunters/Wood and Paper UI/Sprites/Yellow Board/1.png";
    public static final String BOARD_TC =
        "Treasure Hunters/Wood and Paper UI/Sprites/Yellow Board/2.png";
    public static final String BOARD_TR =
        "Treasure Hunters/Wood and Paper UI/Sprites/Yellow Board/3.png";
    public static final String BOARD_CL =
        "Treasure Hunters/Wood and Paper UI/Sprites/Yellow Board/4.png";
    public static final String BOARD_CC =
        "Treasure Hunters/Wood and Paper UI/Sprites/Yellow Board/5.png";
    public static final String BOARD_CR =
        "Treasure Hunters/Wood and Paper UI/Sprites/Yellow Board/6.png";
    public static final String BOARD_BL =
        "Treasure Hunters/Wood and Paper UI/Sprites/Yellow Board/7.png";
    public static final String BOARD_BC =
        "Treasure Hunters/Wood and Paper UI/Sprites/Yellow Board/8.png";
    public static final String BOARD_BR =
        "Treasure Hunters/Wood and Paper UI/Sprites/Yellow Board/9.png";
    public static final String BUTTON_L =
        "Treasure Hunters/Wood and Paper UI/Sprites/Yellow Button/2.png";
    public static final String BUTTON_C =
        "Treasure Hunters/Wood and Paper UI/Sprites/Yellow Button/3.png";
    public static final String BUTTON_R =
        "Treasure Hunters/Wood and Paper UI/Sprites/Yellow Button/4.png";
    public static final String BANNER_TL =
        "Treasure Hunters/Wood and Paper UI/Sprites/Big Banner/1.png";
    public static final String BANNER_TR =
        "Treasure Hunters/Wood and Paper UI/Sprites/Big Banner/2.png";
    public static final String BANNER_CL =
        "Treasure Hunters/Wood and Paper UI/Sprites/Big Banner/3.png";
    public static final String BANNER_CR =
        "Treasure Hunters/Wood and Paper UI/Sprites/Big Banner/4.png";
    public static final String BANNER_BL =
        "Treasure Hunters/Wood and Paper UI/Sprites/Big Banner/5.png";
    public static final String BANNER_BR =
        "Treasure Hunters/Wood and Paper UI/Sprites/Big Banner/6.png";


    public void loadAllAssets() {

        manager.load(SKY_BASE, Texture.class);
        manager.load(WATER_DEEP, Texture.class);

        // PLAYER
        loadFrames(PLAYER_IDLE_PREFIX, 5, "%02d");
        loadFrames(PLAYER_RUN_PREFIX, 6, "%02d");
        loadFrames(PLAYER_JUMP_PREFIX, 3, "%02d");
        loadFrames(PLAYER_FALL_PREFIX, 1, "%02d");
        loadFrames(PLAYER_HIT_PREFIX, 4, "%02d");
        loadFrames(PLAYER_ATTACK_PREFIX, 3,  "%02d");

        // COLLECTABLES
        loadFrames(COIN_GOLD_PREFIX, 4, "%02d");
        loadFrames(COIN_SILVER_PREFIX, 4, "%02d");
        loadFrames(COIN_DIAMOND_PREFIX, 4, "%02d");

        loadFrames(POTION_RED_PREFIX, 4, "%02d");
        loadFrames(POTION_BLUE_PREFIX, 4, "%02d");
        loadFrames(POTION_GREEN_PREFIX, 4, "%02d");

        // PLATFORM
        loadFrames(PLATFORM_HELI_PREFIX, 4, "%02d");

        // SHELL
        loadFrames(SHELL_IDLE_PREFIX, 1, "%d");
        loadFrames(SHELL_FIRE_PREFIX, 6, "%d");
        loadFrames(SHELL_BITE_PREFIX, 6, "%d");
        loadFrames(PEARL_IDLE_PREFIX, 1, "%d");
        loadFrames(PEARL_DESTROYED_PREFIX, 3, "%d");

        // CRAB
        loadFrames(CRAB_IDLE_PREFIX, 9, "%02d");
        loadFrames(CRAB_RUN_PREFIX, 6, "%02d");
        loadFrames(CRAB_ATTACK_PREFIX, 4, "%02d");

        //Water
        loadFrames(WATER_SURFACE, 3, "%d");

        //Traps
        loadFrames(SPIKE_ANIM_PREFIX, 4, "%d");
        manager.load(SPIKED_BALL, Texture.class);
        manager.load(CHAIN_LINK, Texture.class);

        //UI
        manager.load(BOARD_TL,  Texture.class);
        manager.load(BOARD_TC,  Texture.class);
        manager.load(BOARD_TR,  Texture.class);
        manager.load(BOARD_CL,  Texture.class);
        manager.load(BOARD_CC,  Texture.class);
        manager.load(BOARD_CR,  Texture.class);
        manager.load(BOARD_BL,  Texture.class);
        manager.load(BOARD_BC,  Texture.class);
        manager.load(BOARD_BR,  Texture.class);
        manager.load(BUTTON_L,  Texture.class);
        manager.load(BUTTON_C,  Texture.class);
        manager.load(BUTTON_R,  Texture.class);
        manager.load(BANNER_TL,  Texture.class);
        manager.load(BANNER_TR,  Texture.class);
        manager.load(BANNER_CL,  Texture.class);
        manager.load(BANNER_CR,  Texture.class);
        manager.load(BANNER_BL,  Texture.class);
        manager.load(BANNER_BR,  Texture.class);
    }

    private void loadFrames(String prefix, int frameCount, String format) {
        for (int i = 1; i <= frameCount; i++) {
            String path = prefix + String.format(format, i) + ".png";
            manager.load(path, Texture.class);
        }
    }

    public Animation<TextureRegion> getAnimation(
        String prefix,
        int frameCount,
        float frameDuration,
        Animation.PlayMode mode,
        String format
    ) {
        Array<TextureRegion> frames = new Array<>();

        for (int i = 1; i <= frameCount; i++) {
            String path = prefix + String.format(format, i) + ".png";
            Texture tex = manager.get(path, Texture.class);
            frames.add(new TextureRegion(tex));
        }

        Animation<TextureRegion> anim =
            new Animation<>(frameDuration, frames);

        anim.setPlayMode(mode);
        return anim;
    }

    public void dispose() {
        manager.dispose();
    }
}
