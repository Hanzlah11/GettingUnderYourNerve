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
    private float linkHeight = 16f / Main.PPM;
    private float linkWidth = 16f / Main.PPM;

    public SpikedBall(World world, MapObject object, GameAssetManager assets) {
        super(world, object);

        ballTex = assets.manager.get(GameAssetManager.SPIKED_BALL, Texture.class);
        chainTex = assets.manager.get(GameAssetManager.CHAIN_LINK, Texture.class);
        chainLinks = new Array<>();

        int chainLength = object.getProperties().get("chainLength", 5, Integer.class);
        float radiusPixels = object.getProperties().get("radius", 16f, Float.class);
        this.ballRadius = radiusPixels / Main.PPM;

        BodyDef anchorDef = new BodyDef();
        anchorDef.type = BodyDef.BodyType.StaticBody;
        anchorDef.position.set(startX, startY);
        Body anchorBody = world.createBody(anchorDef);
        Body prevBody = anchorBody;

        for (int i = 0; i < chainLength; i++) {
            BodyDef linkDef = new BodyDef();
            linkDef.type = BodyDef.BodyType.DynamicBody;

            float linkY = startY - ((i * linkHeight) + (linkHeight / 2f));
            linkDef.position.set(startX, linkY);
            Body linkBody = world.createBody(linkDef);

            PolygonShape linkShape = new PolygonShape();
            linkShape.setAsBox(linkWidth / 4f, linkHeight / 2f);

            FixtureDef linkFix = new FixtureDef();
            linkFix.shape = linkShape;
            linkFix.density = 0.5f;
            linkFix.isSensor = true;
            linkBody.createFixture(linkFix);
            linkShape.dispose();

            chainLinks.add(linkBody);

            RevoluteJointDef rjd = new RevoluteJointDef();
            Vector2 jointPos = new Vector2(startX, startY - (i * linkHeight));
            rjd.initialize(prevBody, linkBody, jointPos);
            world.createJoint(rjd);

            prevBody = linkBody;
        }

        BodyDef ballDef = new BodyDef();
        ballDef.type = BodyDef.BodyType.DynamicBody;
        float ballY = startY - (chainLength * linkHeight) - ballRadius;
        ballDef.position.set(startX, ballY);

        this.body = world.createBody(ballDef);

        CircleShape ballShape = new CircleShape();
        ballShape.setRadius(ballRadius * 0.9f);

        FixtureDef ballFix = new FixtureDef();
        ballFix.shape = ballShape;
        ballFix.density = 0.25f;
        ballFix.restitution = 0.6f;
        ballFix.friction = 0.5f;
        ballFix.filter.categoryBits = Main.TRAP_BIT;
        ballFix.filter.maskBits = (short) (Main.PLAYER_BIT | Main.GROUND_BIT);

        this.body.createFixture(ballFix).setUserData(this);
        ballShape.dispose();

        RevoluteJointDef ballJoint = new RevoluteJointDef();
        Vector2 finalJointPos = new Vector2(startX, startY - (chainLength * linkHeight));
        ballJoint.initialize(prevBody, this.body, finalJointPos);
        world.createJoint(ballJoint);
    }

    @Override
    public void update(float dt) {
    }

    @Override
    public void render(SpriteBatch batch, float dt) {
        for (Body link : chainLinks) {
            batch.draw(
                chainTex,
                link.getPosition().x - (linkWidth / 2f),
                link.getPosition().y - (linkHeight / 2f),
                linkWidth / 2f, linkHeight / 2f,
                linkWidth, linkHeight,
                1f, 1f,
                link.getAngle() * MathUtils.radiansToDegrees,
                0, 0, chainTex.getWidth(), chainTex.getHeight(), false, false
            );
        }

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
        float pushDir = p.GetXpos() < this.body.getPosition().x ? 5f : -5f;

        this.body.applyLinearImpulse(new Vector2(pushDir, 0), this.body.getWorldCenter(), true);
    }
}
