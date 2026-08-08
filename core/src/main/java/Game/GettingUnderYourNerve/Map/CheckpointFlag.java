package Game.GettingUnderYourNerve.Map;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;

public class CheckpointFlag {
    private boolean isActivated;
    private Body body;
    private Fixture fixture;

    private Animation<TextureRegion> flagAnimation;
    private float stateTime;
    private float drawX;
    private float drawY;
    private float drawWidth;
    private float drawHeight;

    public CheckpointFlag(World world, Rectangle bounds, GameAssetManager assets) {
        this.isActivated = false;

        this.flagAnimation = assets.getAnimation(GameAssetManager.FLAG_PREFIX, 9, 0.15f, Animation.PlayMode.LOOP, "%d");
        this.stateTime = 0f;

        this.drawWidth = bounds.getWidth() / Main.PPM;
        this.drawHeight = bounds.getHeight() / Main.PPM;
        this.drawX = bounds.getX() / Main.PPM;
        this.drawY = bounds.getY() / Main.PPM;

        BodyDef bdef = new BodyDef();
        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();

        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set((bounds.getX() + bounds.getWidth() / 2) / Main.PPM, (bounds.getY() + bounds.getHeight() / 2) / Main.PPM);

        body = world.createBody(bdef);

        shape.setAsBox((bounds.getWidth() / 2) / Main.PPM, (bounds.getHeight() / 2) / Main.PPM);
        fdef.shape = shape;
        fdef.isSensor = true;
        fdef.filter.categoryBits = Main.CHECKPOINT_BIT;
        fdef.filter.maskBits = Main.PLAYER_BIT;

        fixture = body.createFixture(fdef);
        fixture.setUserData(this);

        shape.dispose();
    }

    public void update(float dt) {
        stateTime += dt;
    }

    public void draw(SpriteBatch batch) {
        if (flagAnimation != null) {
            TextureRegion currentFrame = flagAnimation.getKeyFrame(stateTime, true);

            batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);
        }
    }

    public void onCheckpointHit(Player player) {
        if (!isActivated) {
            isActivated = true;

            player.setCheckpointCoords(player.GetXpos(), player.GetYpos());
            System.out.println("Checkpoint reached! Game auto-saved for: " + player.playerName);
        }
    }
}
