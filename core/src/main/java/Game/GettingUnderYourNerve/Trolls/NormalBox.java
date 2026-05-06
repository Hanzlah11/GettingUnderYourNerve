package Game.GettingUnderYourNerve.Trolls;

import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 * NormalBox — a plain solid box the player can stand on.
 * No special behaviour; just a static body with a texture.
 *
 * Tiled setup:
 *   Object Layer : "Boxes"
 *   Name         : "NormalBox"
 *   Rectangle    : any size
 */
public class NormalBox extends Box {

    public NormalBox(World world, MapObject object, GameAssetManager assets) {
        // Static body — never moves
        super(world, object, assets, BodyDef.BodyType.StaticBody);
    }

    @Override
    public void update(float dt) {
        // Nothing to update for a normal box
    }

    @Override
    public void onPlayerLand(float playerX, Player player) { }
}
