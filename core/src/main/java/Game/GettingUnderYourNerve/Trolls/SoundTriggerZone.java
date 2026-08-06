package Game.GettingUnderYourNerve.Trolls;

import com.badlogic.gdx.math.Rectangle;

/**
 * Represents an invisible ambient sound zone loaded from Tiled map layers.
 * Plays a proximity-based humming sound to give hints for hidden triggers.
 */
public class SoundTriggerZone {

    public int id;
    public Rectangle bounds;
    public boolean active = false;

    public SoundTriggerZone(int id, Rectangle bounds) {
        this.id = id;
        this.bounds = bounds;
    }

    /**
     * Resets the sound zone state when the player dies and respawns.
     */
    public void reset() {
        this.active = false;
    }
}
