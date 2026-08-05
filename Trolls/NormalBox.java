package Game.GettingUnderYourNerve.Trolls;

import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class NormalBox extends Box {

    public NormalBox(World world, MapObject object, GameAssetManager assets) {

        super(world, object, assets, BodyDef.BodyType.StaticBody);
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void onPlayerLand(float playerX, Player player) { }
}
