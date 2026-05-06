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
import com.badlogic.gdx.physics.box2d.*;

/**
 * Box — abstract superclass for all box types.
 *
 * Subclasses:
 *   NormalBox    — solid platform, no behaviour
 *   RotatingBox  — rotates toward the side the player lands from
 *
 * Tiled setup (Object Layer named "Boxes"):
 *   Rectangle object
 *   Name        : "NormalBox" or "RotatingBox"
 *   Properties  : any extra props read by subclass (e.g. Speed)
 */
public abstract class Box {

    public Body      body;
    public float     width;   // Box2D meters
    public float     height;  // Box2D meters

    protected TextureRegion texture;

    // ---------------------------------------------------------------
    // Constructor — builds the Box2D body from a Tiled rectangle.
    // Subclasses call super() first, then add their own setup.
    // ---------------------------------------------------------------
    public Box(World world, MapObject object, GameAssetManager assets,
               BodyDef.BodyType bodyType) {

        Rectangle     rect  = ((RectangleMapObject) object).getRectangle();
        MapProperties props = object.getProperties();

        width  = rect.width  / Main.PPM;
        height = rect.height / Main.PPM;

        float cx = (rect.x + rect.width  / 2f) / Main.PPM;
        float cy = (rect.y + rect.height / 2f) / Main.PPM;

        // Body
        BodyDef bdef = new BodyDef();
        bdef.type = bodyType;
        bdef.position.set(cx, cy);
        body = world.createBody(bdef);

        // Shape
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f, height / 2f);

        // Fixture
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 0.3f;
        fdef.filter.categoryBits = Main.GROUND_BIT;
        fdef.filter.maskBits     = Main.PLAYER_BIT | Main.ENEMY_BIT;
        body.createFixture(fdef).setUserData(this);
        shape.dispose();

        // Texture — load from asset manager
        // Replace BOX_TEXTURE path with your real texture later
        Texture tex = assets.manager.get(GameAssetManager.BOX_TEXTURE, Texture.class);
        texture = new TextureRegion(tex);
    }

    // ---------------------------------------------------------------
    // update() — called every frame from PlayableMap.
    // NormalBox does nothing; RotatingBox drives its state machine.
    // ---------------------------------------------------------------
    public abstract void update(float dt);

    // ---------------------------------------------------------------
    // onPlayerLand() — called by WorldContactListener when the player
    // contacts this box from above.
    // playerX — the player's X position, used by RotatingBox to decide
    //            which direction to rotate toward.
    // ---------------------------------------------------------------
    public abstract void onPlayerLand(float playerX, Player player);

    // ---------------------------------------------------------------
    // render() — draws the box sprite, rotated to match the body angle.
    // ---------------------------------------------------------------
    public void render(SpriteBatch batch) {
        float angleDeg = body.getAngle() * (180f / (float) Math.PI);
        float x        = body.getPosition().x - width  / 2f;
        float y        = body.getPosition().y - height / 2f;

        batch.draw(
            texture,
            x, y,                       // position
            width  / 2f, height / 2f,   // origin (center for rotation)
            width, height,              // size
            1f, 1f,                     // scale
            angleDeg                    // rotation in degrees
        );
    }
}
