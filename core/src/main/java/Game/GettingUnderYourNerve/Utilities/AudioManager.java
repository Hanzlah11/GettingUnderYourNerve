//AudioManager.java
package Game.GettingUnderYourNerve.Utilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager
{
    public static Sound shellShoot;
    public static Sound projectileBreak;
    public static Sound crabChaseShout;
    public static Sound crabAttack;
    public static Sound crabPatrol;
    public static Sound buttonSound;
    public static Music rickMusic;

    public static Sound footsteps;

    // DIALOGUE
    // --- Prologue ---
    public static Sound batman_spawn_whoosh;
    public static Sound batman_shout_stop;
    public static Sound batman_swing_fist;
    public static Sound punch_impact_heavy;
    public static Sound player_protest_wrong_guy;
    public static Sound batman_apology_sorry;
    public static Sound player_shout_come_back;

    // --- Level Dialogues ---
    public static Sound player_lvl1, player_lvl2, player_lvl3;
    public static Sound batman_lvl1, batman_lvl2, batman_lvl3;


    public static void load()
    {

        footsteps = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/General/footsteps.wav"));

        shellShoot = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Enemy/shellShoot.wav"));
        projectileBreak = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Enemy/projectileBreak.wav"));
        crabChaseShout = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Enemy/crabChasingShout.wav"));
        crabAttack = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Enemy/crabAttack.wav"));
        crabPatrol = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Enemy/crabPatrol.wav"));
        rickMusic = Gdx.audio.newMusic(Gdx.files.internal("Audio/Sounds/UI/rickRoll.mp3"));
        buttonSound = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/UI/button_press.mp3"));

        // Cutscenes
        // --- Prologue
        batman_spawn_whoosh      = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Prologue/batman_spawn_whoosh.wav"));
        batman_shout_stop        = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Prologue/batman_shout_stop.wav"));
        batman_swing_fist        = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Prologue/batman_swing_fist.wav"));
        punch_impact_heavy       = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Prologue/punch_impact_heavy.wav"));
        player_protest_wrong_guy  = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Prologue/player_protest_wrong_guy.wav"));
        batman_apology_sorry     = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Prologue/batman_apology_sorry.wav"));
        player_shout_come_back   = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Prologue/player_shout_come_back.wav"));

        // --- Levels
        player_lvl1 = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Levels/player_lvl1.wav"));
        player_lvl2 = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Levels/player_lvl2.wav"));
        player_lvl3 = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Levels/player_lvl3.wav"));

        batman_lvl1 = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Levels/batman_lvl1.wav"));
        batman_lvl2 = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Levels/batman_lvl2.wav"));
        batman_lvl3 = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Levels/batman_lvl3.wav"));

    }

    public static void dispose()
    {

        shellShoot.dispose();
        projectileBreak.dispose();
        crabChaseShout.dispose();
        crabAttack.dispose();
        crabPatrol.dispose();
        if (rickMusic != null) rickMusic.dispose();
        if (buttonSound != null) buttonSound.dispose();

        if(buttonSound != null) buttonSound.dispose();
        footsteps.dispose();
        batman_spawn_whoosh.dispose();
        batman_shout_stop.dispose();
        batman_swing_fist.dispose();
        punch_impact_heavy.dispose();
        player_protest_wrong_guy.dispose();
        batman_apology_sorry.dispose();
        player_shout_come_back.dispose();

        player_lvl1.dispose(); player_lvl2.dispose(); player_lvl3.dispose();
        batman_lvl1.dispose(); batman_lvl2.dispose(); batman_lvl3.dispose();
    }

}
