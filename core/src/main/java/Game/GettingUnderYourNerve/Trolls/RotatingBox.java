package Game.GettingUnderYourNerve.Trolls;

import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class RotatingBox extends Box {

    private static final float TILT_ANGLE_DEG = 90f;
    private static final float RESET_DELAY    = 0.5f;

    public enum State { FLAT, ROTATING_DOWN, TILTED, ROTATING_BACK }
    private State state = State.FLAT;

    private float rotateSpeed  = 120f;
    private float currentAngle = 0f;
    private float targetAngle  = 0f;
    private float resetTimer   = 0f;

    public RotatingBox(World world, MapObject object, GameAssetManager assets) {

        super(world, object, assets, BodyDef.BodyType.KinematicBody);

        MapProperties props = object.getProperties();
        if (props.containsKey("Speed")) {
            rotateSpeed = props.get("Speed", Float.class);
        }
    }

    public void onPlayerLand(float playerX, Player player) {
        if (state != State.FLAT) return;

        float boxCenterX = body.getPosition().x;

        if (playerX < boxCenterX) {
            targetAngle = TILT_ANGLE_DEG;
            player.getPlayerBody().applyLinearImpulse(
                new Vector2(-60f, 0f),
                player.getPlayerBody().getWorldCenter(),
                true
            );
        } else {
            targetAngle = -TILT_ANGLE_DEG;
            player.getPlayerBody().applyLinearImpulse(
                new Vector2(60f, 0f),
                player.getPlayerBody().getWorldCenter(),
                true
            );
        }

        state = State.ROTATING_DOWN;
    }

    @Override
    public void update(float dt) {
        switch (state) {

            case FLAT:
                break;

            case ROTATING_DOWN:
                if (targetAngle > 0) {
                    currentAngle += rotateSpeed * dt;
                    if (currentAngle >= targetAngle) {
                        currentAngle = targetAngle;
                        state        = State.TILTED;
                        resetTimer   = 0f;
                    }
                } else {
                    currentAngle -= rotateSpeed * dt;
                    if (currentAngle <= targetAngle) {
                        currentAngle = targetAngle;
                        state        = State.TILTED;
                        resetTimer   = 0f;
                    }
                }
                applyAngle();
                break;

            case TILTED:
                resetTimer += dt;
                if (resetTimer >= RESET_DELAY) {
                    state = State.ROTATING_BACK;
                }
                break;

            case ROTATING_BACK:
                if (currentAngle > 0) {
                    currentAngle -= rotateSpeed * dt;
                    if (currentAngle <= 0f) {
                        currentAngle = 0f;
                        state        = State.FLAT;
                    }
                } else {
                    currentAngle += rotateSpeed * dt;
                    if (currentAngle >= 0f) {
                        currentAngle = 0f;
                        state        = State.FLAT;
                    }
                }
                applyAngle();
                break;
        }
    }
    private void applyAngle() {
        body.setTransform(
            body.getPosition(),
            currentAngle * MathUtils.degreesToRadians
        );
    }
}
