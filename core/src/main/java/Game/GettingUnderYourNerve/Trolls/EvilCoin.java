package Game.GettingUnderYourNerve.Trolls;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class EvilCoin {

    private static final float PPM         = 32f;
    private static final float SPEED       = 4.5f;
    private static final float AGGRO_RANGE = 3f;

    public  Body    body;
    public  boolean setToDestroy = false;
    public  boolean destroyed    = false;

    private boolean isChasing = false;

    private float lifetime;
    private float timer = 0f;

    private float drawWidth;
    private float drawHeight;

    private Animation<TextureRegion> animation;
    private float stateTime = 0f;

    public EvilCoin(World world, MapObject object, GameAssetManager assets) {

        Rectangle     rect  = ((RectangleMapObject) object).getRectangle();
        MapProperties props = object.getProperties();

        lifetime = props.containsKey("lifetime")
            ? props.get("lifetime", Float.class)
            : 5f;

        drawWidth  = rect.width  / PPM;
        drawHeight = rect.height / PPM;

        BodyDef bdef = new BodyDef();
        bdef.type         = BodyDef.BodyType.DynamicBody;
        bdef.gravityScale = 0f;
        bdef.position.set(
            (rect.x + rect.width  / 2f) / PPM,
            (rect.y + rect.height / 2f) / PPM
        );
        body = world.createBody(bdef);
        body.setFixedRotation(true);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(drawWidth / 2f, drawHeight / 2f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape    = shape;
        fdef.isSensor = true;
        fdef.filter.categoryBits = Main.ENEMY_BIT;
        fdef.filter.maskBits     = 0;

        body.createFixture(fdef).setUserData(this);
        shape.dispose();

        animation = assets.getAnimation(
            GameAssetManager.COIN_GOLD_PREFIX,
            4, 0.10f,
            Animation.PlayMode.LOOP,
            "%02d"
        );
    }

    public void update(float dt, Player player) {
        if (setToDestroy) return;

        float px = player.GetXpos();
        float py = player.GetYpos();
        float cx = body.getPosition().x;
        float cy = body.getPosition().y;

        float dist = Vector2.dst(cx, cy, px, py);

        if (!isChasing) {
            body.setLinearVelocity(0, 0);

            if (dist <= AGGRO_RANGE) {
                isChasing = true;

                Filter filter = body.getFixtureList().get(0).getFilterData();
                filter.maskBits = Main.PLAYER_BIT;
                body.getFixtureList().get(0).setFilterData(filter);

                System.out.println("EvilCoin activated! Chasing player.");
            }
            return;
        }

        // --- Chasing ---
        timer += dt;
        if (timer >= lifetime) {
            setToDestroy = true;
            return;
        }

        Vector2 dir = new Vector2(px - cx, py - cy).nor();
        body.setLinearVelocity(dir.x * SPEED, dir.y * SPEED);
    }

    public void onHitPlayer(Player player) {
        if (setToDestroy) return;
        player.hit(20, body.getPosition().x);
        setToDestroy = true;
    }

    // dt added so stateTime always advances — animation plays whether dormant or chasing
    public void render(SpriteBatch batch, float dt) {
        if (destroyed) return;

        stateTime += dt;

        TextureRegion frame = animation.getKeyFrame(stateTime);
        float x = body.getPosition().x - drawWidth  / 2f;
        float y = body.getPosition().y - drawHeight / 2f;
        batch.draw(frame, x, y, drawWidth, drawHeight);
    }

    public void dispose() {}
}
