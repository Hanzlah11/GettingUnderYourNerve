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
    public static Music rickMusic;
    public static void load()
    {
        shellShoot = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Enemy/shellShoot.wav"));
        projectileBreak = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Enemy/projectileBreak.wav"));
        crabChaseShout = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Enemy/crabChasingShout.wav"));
        crabAttack = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Enemy/crabAttack.wav"));
        crabPatrol = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/Enemy/crabPatrol.wav"));
        rickMusic = Gdx.audio.newMusic(Gdx.files.internal("Audio/Sounds/UI/rickRoll.mp3"));
    }

    public static void dispose()
    {

        shellShoot.dispose();
        projectileBreak.dispose();
        crabChaseShout.dispose();
        crabAttack.dispose();
        crabPatrol.dispose();
        if (rickMusic != null) rickMusic.dispose();
    }

}
