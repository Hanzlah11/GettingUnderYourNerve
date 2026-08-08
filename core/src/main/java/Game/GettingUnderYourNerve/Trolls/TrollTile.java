package Game.GettingUnderYourNerve.Trolls;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.physics.box2d.Body;


public class TrollTile {

    public final int                    triggerId;
    public       Body                   body;
    public final TiledMapTileLayer      layer;
    public final int                    col;
    public final int                    row;
    public boolean                      activated = false;

    public final TiledMapTileLayer.Cell originalCell;
    public final float                  bodyX;
    public final float                  bodyY;

    public TrollTile(int triggerId, Body body,
                     TiledMapTileLayer layer, int col, int row) {
        this.triggerId    = triggerId;
        this.body         = body;
        this.layer        = layer;
        this.col          = col;
        this.row          = row;

        this.originalCell = layer.getCell(col, row);

        this.bodyX        = body.getPosition().x;
        this.bodyY        = body.getPosition().y;
    }
}
