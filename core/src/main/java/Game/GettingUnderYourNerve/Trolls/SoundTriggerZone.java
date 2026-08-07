package Game.GettingUnderYourNerve.Trolls;

import com.badlogic.gdx.math.Rectangle;

public class SoundTriggerZone {

    public int id;
    public Rectangle bounds;
    public boolean active = false;

    public SoundTriggerZone(int id, Rectangle bounds) {
        this.id = id;
        this.bounds = bounds;
    }

    public void reset() {
        this.active = false;
    }
}
