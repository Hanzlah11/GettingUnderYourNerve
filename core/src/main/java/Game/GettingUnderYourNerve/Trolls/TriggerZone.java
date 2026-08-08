package Game.GettingUnderYourNerve.Trolls;

import com.badlogic.gdx.physics.box2d.Body;

public class TriggerZone {

    public final int     id;
    public final Body    body;
    public boolean       fired = false;

    public TriggerZone(int id, Body body) {
        this.id   = id;
        this.body = body;
    }
}
