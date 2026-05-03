package Game.GettingUnderYourNerve.Trolls;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * TrollTile
 *
 * A tile that has the "Troll" (int) custom property set in Tiled.
 * When its trigger ID is activated:
 *   - The Box2D body is destroyed  → no more collision
 *   - The tile cell is cleared     → no more rendering
 *
 * On player respawn, resetTriggers() in PlayableMap will:
 *   - Restore the originalCell     → tile visible again
 *   - Recreate the Box2D body      → collision restored
 *
 * In Tiled (tileset editor):
 *   Select the tile → Add custom property → Name: Troll, Type: int, Value: 1
 *   (use the same integer on every tile that should fall for trigger #1)
 */
public class TrollTile {

    public final int                    triggerId;    // matches TriggerZone.id
    public       Body                   body;         // Box2D static body — NOT final, recreated on respawn
    public final TiledMapTileLayer      layer;        // tile layer the cell lives in
    public final int                    col;          // column in tile layer
    public final int                    row;          // row in tile layer
    public boolean                      activated = false;

    // --- Respawn data ---
    public final TiledMapTileLayer.Cell originalCell; // saved cell so we can restore the tile visually
    public final float                  bodyX;        // Box2D body center X (meters) — to recreate body
    public final float                  bodyY;        // Box2D body center Y (meters) — to recreate body

    public TrollTile(int triggerId, Body body,
                     TiledMapTileLayer layer, int col, int row) {
        this.triggerId    = triggerId;
        this.body         = body;
        this.layer        = layer;
        this.col          = col;
        this.row          = row;

        // Save the cell reference BEFORE it is ever cleared
        this.originalCell = layer.getCell(col, row);

        // Save body position so we can recreate it after world.destroyBody()
        this.bodyX        = body.getPosition().x;
        this.bodyY        = body.getPosition().y;
    }
}
