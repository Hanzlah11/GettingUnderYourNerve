package Game.GettingUnderYourNerve.Trolls;

import com.badlogic.gdx.physics.box2d.Body;

/**
 * TriggerZone
 *
 * An invisible sensor body read from the "Triggers" object layer in Tiled.
 * When the player walks through it, WorldContactListener calls
 * PlayableMap.activateTrigger(id), which deactivates all TrollTiles
 * that share the same trigger ID.
 *
 * In Tiled (object layer named "Triggers"):
 *   Draw a rectangle object wherever you want the trigger
 *   Add custom property → Name: id, Type: int, Value: 1
 *   (value must match the "Troll" property on the tiles you want to drop)
 */
public class TriggerZone {

    public final int     id;      // matches TrollTile.triggerId
    public final Body    body;    // Box2D sensor body
    public boolean       fired = false;

    public TriggerZone(int id, Body body) {
        this.id   = id;
        this.body = body;
    }
}
