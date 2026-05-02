package Game.GettingUnderYourNerve.Trap;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;

public class SpikedBall extends Trap {

    private Texture ballTex;
    private Texture chainTex;
    private Array<Body> chainLinks;

    private float ballRadius;
    private float linkHeight = 16f / Main.PPM; // Adjust based on your chain image height
    private float linkWidth = 16f / Main.PPM;

    public SpikedBall(World world, MapObject object, GameAssetManager assets) {
        super(world, object);

        ballTex = assets.manager.get(GameAssetManager.SPIKED_BALL, Texture.class);
        chainTex = assets.manager.get(GameAssetManager.CHAIN_LINK, Texture.class);
        chainLinks = new Array<>();

        // 1. READ TILED PROPERTIES (Defaults to 5 chains and 16px radius if you forget to set them)
        int chainLength = object.getProperties().get("chainLength", 5, Integer.class);
        float radiusPixels = object.getProperties().get("radius", 16f, Float.class);
        this.ballRadius = radiusPixels / Main.PPM;

        // 2. THE ANCHOR (Static block in the ceiling)
        BodyDef anchorDef = new BodyDef();
        anchorDef.type = BodyDef.BodyType.StaticBody;
        anchorDef.position.set(startX, startY);
        Body anchorBody = world.createBody(anchorDef);
        Body prevBody = anchorBody;

        // 3. THE CHAIN LINKS (Flexible dynamic bodies)
        for (int i = 0; i < chainLength; i++) {
            BodyDef linkDef = new BodyDef();
            linkDef.type = BodyDef.BodyType.DynamicBody;

            // Spawn each link slightly lower than the last
            float linkY = startY - ((i * linkHeight) + (linkHeight / 2f));
            linkDef.position.set(startX, linkY);
            Body linkBody = world.createBody(linkDef);

            PolygonShape linkShape = new PolygonShape();
            linkShape.setAsBox(linkWidth / 4f, linkHeight / 2f); // Thin hitbox

            FixtureDef linkFix = new FixtureDef();
            linkFix.shape = linkShape;
            linkFix.density = 0.5f; // Light chains
            linkFix.isSensor = true; // Chains shouldn't hit the player, only the ball should!
            linkBody.createFixture(linkFix);
            linkShape.dispose();

            chainLinks.add(linkBody);

            // Create a hinge joint connecting to the previous body
            RevoluteJointDef rjd = new RevoluteJointDef();
            Vector2 jointPos = new Vector2(startX, startY - (i * linkHeight));
            rjd.initialize(prevBody, linkBody, jointPos);
            world.createJoint(rjd);

            prevBody = linkBody;
        }

        // 4. THE SPIKED BALL (Heavy dynamic body)
        BodyDef ballDef = new BodyDef();
        ballDef.type = BodyDef.BodyType.DynamicBody;
        float ballY = startY - (chainLength * linkHeight) - ballRadius;
        ballDef.position.set(startX, ballY);

        // Save it to the parent Trap's 'body' variable so WorldContactListener detects it!
        this.body = world.createBody(ballDef);

        CircleShape ballShape = new CircleShape();
        ballShape.setRadius(ballRadius * 0.9f); // Slightly forgiving hitbox

        FixtureDef ballFix = new FixtureDef();
        ballFix.shape = ballShape;
        ballFix.density = 0.25f;     // EXTREMELY HEAVY so it swings wildly
        ballFix.restitution = 0.6f;  // Bouncy so it bounces off the player
        ballFix.friction = 0.5f;
        ballFix.filter.categoryBits = Main.TRAP_BIT;
        ballFix.filter.maskBits = (short) (Main.PLAYER_BIT | Main.GROUND_BIT);

        this.body.createFixture(ballFix).setUserData(this);
        ballShape.dispose();

        // Connect Ball to the final chain link
        RevoluteJointDef ballJoint = new RevoluteJointDef();
        Vector2 finalJointPos = new Vector2(startX, startY - (chainLength * linkHeight));
        ballJoint.initialize(prevBody, this.body, finalJointPos);
        world.createJoint(ballJoint);
    }

    @Override
    public void update(float dt) {
        // Physics engine handles the swinging automatically!
    }

    @Override
    public void render(SpriteBatch batch, float dt) {
        // Draw all the chain links rotated dynamically
        for (Body link : chainLinks) {
            batch.draw(
                chainTex,
                link.getPosition().x - (linkWidth / 2f),
                link.getPosition().y - (linkHeight / 2f),
                linkWidth / 2f, linkHeight / 2f, // Origin for rotation
                linkWidth, linkHeight,
                1f, 1f,
                link.getAngle() * MathUtils.radiansToDegrees, // Rotate based on physics
                0, 0, chainTex.getWidth(), chainTex.getHeight(), false, false
            );
        }

        // Draw the massive ball at the end
        float renderRadius = ballRadius * 2f;
        batch.draw(
            ballTex,
            body.getPosition().x - ballRadius,
            body.getPosition().y - ballRadius,
            ballRadius, ballRadius,
            renderRadius, renderRadius,
            1f, 1f,
            body.getAngle() * MathUtils.radiansToDegrees,
            0, 0, ballTex.getWidth(), ballTex.getHeight(), false, false
        );
    }

    @Override
    public int getDamage() {
        return 25;
    }

    @Override
    public void onHit(Player p) {
        // Figure out which side the player hit us from
        float pushDir = p.GetXpos() < this.body.getPosition().x ? 5f : -5f;

        // Shove OURSELVES backward!
        this.body.applyLinearImpulse(new Vector2(pushDir, 0), this.body.getWorldCenter(), true);
    }
}
