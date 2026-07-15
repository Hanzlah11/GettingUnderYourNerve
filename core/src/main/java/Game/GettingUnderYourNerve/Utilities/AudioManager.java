package Game.GettingUnderYourNerve.Utilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
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

    // --- BOSS ---
    public static Sound batman_boss1, batman_boss2, player_boss1;

    // --- Ending ---
    public static Sound ending_player1, ending_batman1;

    // Pokemon
    public static Music pokemonFightMusic;
    public static Sound pokemonPlayerAttack, pokemonPlayerDamage, pokeBallBounce;

    // Game Music
    public static Music elevatorMusic;
    public static Music level1Music;
    public static Music level2Music;
    public static Music bossArenaMusic;

    private static boolean wasRickPlaying = false;
    private static boolean wasPokemonPlaying = false;
    private static boolean wasElevatorPlaying = false;
    private static boolean wasLevel1Playing = false;
    private static boolean wasLevel2Playing = false;
    private static boolean wasBossArenaPlaying = false;

    // --- Persistent Volume Modifiers ---
    private static float musicVolumeModifier = 0.5f;
    private static float sfxVolumeModifier = 0.5f;
    private static Preferences prefs;

    public static void load()
    {
        // Initialize persistent settings file
        prefs = Gdx.app.getPreferences("GettingUnderYourNerve_Settings");
        musicVolumeModifier = prefs.getFloat("musicVolume", 0.5f);
        sfxVolumeModifier = prefs.getFloat("sfxVolume", 0.5f);

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

        // --- Boss
        batman_boss1 = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Boss/batman1.wav")); // 10s
        batman_boss2 = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Boss/batman2.wav")); // 12s
        player_boss1 = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Boss/player1.wav")); // 8s

        // --- Ending
        ending_player1 = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Ending/player1.wav")); // 10s
        ending_batman1 = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Cutscenes/Ending/batman1.wav")); // 20s

        // -- Pokemon
        pokemonFightMusic = Gdx.audio.newMusic(Gdx.files.internal("Audio/Sounds/Pokemon/fightMusic.mp3"));
        pokemonPlayerAttack = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Pokemon/playerAttack.wav"));
        pokemonPlayerDamage = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Pokemon/playerDamage.wav"));
        pokeBallBounce = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Pokemon/pokeball.wav"));

        // Game music
        elevatorMusic  = Gdx.audio.newMusic(Gdx.files.internal("Audio/Sounds/General/elevator.mp3"));
        level1Music    = Gdx.audio.newMusic(Gdx.files.internal("Audio/Sounds/General/level1.mp3"));
        level2Music    = Gdx.audio.newMusic(Gdx.files.internal("Audio/Sounds/General/level2.mp3"));
        bossArenaMusic = Gdx.audio.newMusic(Gdx.files.internal("Audio/Sounds/General/bossLevel.mp3"));

        // Set initial volumes for all music tracks
        syncMusicVolume();
    }

    /**
     * Updates the persistent music volume modifier and propagates it instantly to playing tracks.
     */
    public static void updateMusicVolume(float volume) {
        musicVolumeModifier = volume;
        syncMusicVolume();
    }

    /**
     * Updates the persistent SFX volume modifier.
     */
    public static void updateSFXVolume(float volume) {
        sfxVolumeModifier = volume;
    }

    private static void syncMusicVolume() {
        if (rickMusic != null) rickMusic.setVolume(musicVolumeModifier);
        if (pokemonFightMusic != null) pokemonFightMusic.setVolume(musicVolumeModifier);
        if (elevatorMusic != null) elevatorMusic.setVolume(musicVolumeModifier * 0.5f); // Normalized
        if (level1Music != null) level1Music.setVolume(musicVolumeModifier);
        if (level2Music != null) level2Music.setVolume(musicVolumeModifier);
        if (bossArenaMusic != null) bossArenaMusic.setVolume(musicVolumeModifier);
    }

    /**
     * Plays a sound effect automatically adjusted to the user's current settings slider.
     */
    public static long playSFX(Sound sound) {
        if (sound != null) {
            return sound.play(sfxVolumeModifier);
        }
        return -1;
    }

    /**
     * Plays a sound effect at a relative local volume level scaled against the player's master settings.
     */
    public static long playSFX(Sound sound, float relativeVolume) {
        if (sound != null) {
            return sound.play(sfxVolumeModifier * relativeVolume);
        }
        return -1;
    }

    public static void pauseAll() {
        // 1. Record which music track was actively playing before pausing
        wasRickPlaying = (rickMusic != null && rickMusic.isPlaying());
        wasPokemonPlaying = (pokemonFightMusic != null && pokemonFightMusic.isPlaying());
        wasElevatorPlaying = (elevatorMusic != null && elevatorMusic.isPlaying());
        wasLevel1Playing = (level1Music != null && level1Music.isPlaying());
        wasLevel2Playing = (level2Music != null && level2Music.isPlaying());
        wasBossArenaPlaying = (bossArenaMusic != null && bossArenaMusic.isPlaying());

        // 2. Pause the active music tracks
        if (wasRickPlaying) rickMusic.pause();
        if (wasPokemonPlaying) pokemonFightMusic.pause();
        if (wasElevatorPlaying) elevatorMusic.pause();
        if (wasLevel1Playing) level1Music.pause();
        if (wasLevel2Playing) level2Music.pause();
        if (wasBossArenaPlaying) bossArenaMusic.pause();

        // 3. Pause all active running sound effect instances
        if (footsteps != null) footsteps.pause();
        if (shellShoot != null) shellShoot.pause();
        if (projectileBreak != null) projectileBreak.pause();
        if (crabChaseShout != null) crabChaseShout.pause();
        if (crabAttack != null) crabAttack.pause();
        if (crabPatrol != null) crabPatrol.pause();

        // 4. Pause Dialogue lines
        if (ending_player1 != null) ending_player1.pause();
        if (ending_batman1 != null) ending_batman1.pause();
        if (batman_boss1 != null) batman_boss1.pause();
        if (batman_boss2 != null) batman_boss2.pause();
        if (player_boss1 != null) player_boss1.pause();

        if (player_lvl1 != null) player_lvl1.pause();
        if (player_lvl2 != null) player_lvl2.pause();
        if (player_lvl3 != null) player_lvl3.pause();
        if (batman_lvl1 != null) batman_lvl1.pause();
        if (batman_lvl2 != null) batman_lvl2.pause();
        if (batman_lvl3 != null) batman_lvl3.pause();

        // --- PROLOGUE CUTSCENE AUDIO PAUSES ---
        if (batman_spawn_whoosh != null) batman_spawn_whoosh.pause();
        if (batman_shout_stop != null) batman_shout_stop.pause();
        if (batman_swing_fist != null) batman_swing_fist.pause();
        if (punch_impact_heavy != null) punch_impact_heavy.pause();
        if (player_protest_wrong_guy != null) player_protest_wrong_guy.pause();
        if (batman_apology_sorry != null) batman_apology_sorry.pause();
        if (player_shout_come_back != null) player_shout_come_back.pause();
    }

    /**
     * Resumes only the music track that was active before the pause,
     * and resumes any ongoing sound effects. Call this when exiting the PauseScreen.
     */
    public static void resumeAll() {
        // 1. Only resume the music track that was recorded as playing
        if (wasRickPlaying && rickMusic != null) rickMusic.play();
        if (wasPokemonPlaying && pokemonFightMusic != null) pokemonFightMusic.play();
        if (wasElevatorPlaying && elevatorMusic != null) elevatorMusic.play();
        if (wasLevel1Playing && level1Music != null) level1Music.play();
        if (wasLevel2Playing && level2Music != null) level2Music.play();
        if (wasBossArenaPlaying && bossArenaMusic != null) bossArenaMusic.play();

        // 2. Clear the tracking variables to prevent state pollution
        wasRickPlaying = false;
        wasPokemonPlaying = false;
        wasElevatorPlaying = false;
        wasLevel1Playing = false;
        wasLevel2Playing = false;
        wasBossArenaPlaying = false;

        // 3. Resume all sound effect channels
        if (footsteps != null) footsteps.resume();
        if (shellShoot != null) shellShoot.resume();
        if (projectileBreak != null) projectileBreak.resume();
        if (crabChaseShout != null) crabChaseShout.resume();
        if (crabAttack != null) crabAttack.resume();
        if (crabPatrol != null) crabPatrol.resume();

        // 4. Resume dialogue lines
        if (ending_player1 != null) ending_player1.resume();
        if (ending_batman1 != null) ending_batman1.resume();
        if (batman_boss1 != null) batman_boss1.resume();
        if (batman_boss2 != null) batman_boss2.resume();
        if (player_boss1 != null) player_boss1.resume();

        if (player_lvl1 != null) player_lvl1.resume();
        if (player_lvl2 != null) player_lvl2.resume();
        if (player_lvl3 != null) player_lvl3.resume();
        if (batman_lvl1 != null) batman_lvl1.resume();
        if (batman_lvl2 != null) batman_lvl2.resume();
        if (batman_lvl3 != null) batman_lvl3.resume();

        // --- PROLOGUE CUTSCENE AUDIO RESUMES ---
        if (batman_spawn_whoosh != null) batman_spawn_whoosh.resume();
        if (batman_shout_stop != null) batman_shout_stop.resume();
        if (batman_swing_fist != null) batman_swing_fist.resume();
        if (punch_impact_heavy != null) punch_impact_heavy.resume();
        if (player_protest_wrong_guy != null) player_protest_wrong_guy.resume();
        if (batman_apology_sorry != null) batman_apology_sorry.resume();
        if (player_shout_come_back != null) player_shout_come_back.resume();
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

        if (batman_boss1 != null) batman_boss1.dispose();
        if (batman_boss2 != null) batman_boss2.dispose();
        if (player_boss1 != null) player_boss1.dispose();
        if (ending_player1 != null) ending_player1.dispose();
        if (ending_batman1 != null) ending_batman1.dispose();
        if (pokemonFightMusic != null) pokemonFightMusic.dispose();
        if (pokemonPlayerAttack != null) pokemonPlayerAttack.dispose();
        if (pokemonPlayerDamage != null) pokemonPlayerDamage.dispose();
        if (pokeBallBounce != null) pokeBallBounce.dispose();

        if (elevatorMusic != null) elevatorMusic.dispose();
        if (level1Music != null) level1Music.dispose();
        if (level2Music != null) level2Music.dispose();
        if (bossArenaMusic != null) bossArenaMusic.dispose();
    }
}
