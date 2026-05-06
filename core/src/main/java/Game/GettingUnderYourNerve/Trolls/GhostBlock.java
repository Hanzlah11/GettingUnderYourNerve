package Game.GettingUnderYourNerve.Trolls;

import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import Game.GettingUnderYourNerve.Main;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;

public class GhostBlock {
    public Body body;
    private Rectangle bounds;
    private Texture texture;
    private boolean isVisible = false;

    public GhostBlock(World world, MapObject object, GameAssetManager assets) {
        this.bounds = ((RectangleMapObject) object).getRectangle();

        // Placeholder texture until you assign your real one
        this.texture = assets.manager.get(GameAssetManager.BOARD_TC, Texture.class);

        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(
            (bounds.x + bounds.width / 2f) / Main.PPM,
            (bounds.y + bounds.height / 2f) / Main.PPM
        );
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((bounds.width / 2f) / Main.PPM, (bounds.height / 2f) / Main.PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = Main.GROUND_BIT; // Behaves exactly like normal ground
        fdef.filter.maskBits = Main.PLAYER_BIT | Main.ENEMY_BIT | Main.PROJECTILE_BIT;

        body.createFixture(fdef).setUserData(this);
        shape.dispose();
    }

    public void reveal() {
        isVisible = true;
    }

    public void reset() {
        isVisible = false;
    }

    public void render(SpriteBatch batch) {
        if (isVisible && texture != null) {
            batch.draw(texture, bounds.x / Main.PPM, bounds.y / Main.PPM, bounds.width / Main.PPM, bounds.height / Main.PPM);
        }
    }
}
