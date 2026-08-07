package Game.GettingUnderYourNerve.Trolls;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import static Game.GettingUnderYourNerve.Main.PPM;

public abstract class Box {

    public Body      body;
    public float     width;
    public float     height;

    protected TextureRegion texture;

    public Box(World world, MapObject object, GameAssetManager assets,
               BodyDef.BodyType bodyType) {

        Rectangle     rect  = ((RectangleMapObject) object).getRectangle();
        MapProperties props = object.getProperties();

        width  = rect.width  / PPM;
        height = rect.height / PPM;

        float cx = (rect.x + rect.width  / 2f) / PPM;
        float cy = (rect.y + rect.height  / 2f) / PPM;

        BodyDef bdef = new BodyDef();
        bdef.type = bodyType;
        bdef.position.set(cx, cy);
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        float fixtureWidth  = width / 2f;
        float fixtureHeight = (height / 2f) * 0.80f;
        float fixtureOffsetY = -4f / PPM;
        shape.setAsBox(fixtureWidth, fixtureHeight, new Vector2(0, fixtureOffsetY), 0f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 0.3f;
        fdef.filter.categoryBits = Main.GROUND_BIT;
        fdef.filter.maskBits     = Main.PLAYER_BIT | Main.ENEMY_BIT;
        body.createFixture(fdef).setUserData(this);
        shape.dispose();

        Texture tex = assets.manager.get(GameAssetManager.BOX_TEXTURE, Texture.class);
        texture = new TextureRegion(tex);
    }

    public abstract void update(float dt);

    public abstract void onPlayerLand(float playerX, Player player);

    public void render(SpriteBatch batch) {
        float angleDeg = body.getAngle() * (180f / (float) Math.PI);
        float x        = body.getPosition().x - width  / 2f;
        float y        = body.getPosition().y - height / 2f;

        batch.draw(
            texture,
            x, y - 5.0f / PPM,
            width  / 2f, height / 2f,
            width, height,
            1f, 1f,
            angleDeg
        );
    }
}
