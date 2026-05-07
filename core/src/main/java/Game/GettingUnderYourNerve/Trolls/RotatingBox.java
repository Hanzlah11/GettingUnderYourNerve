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

    private static final float TILT_ANGLE_DEG = 90f;   // how far it tilts
    private static final float RESET_DELAY    = 0.5f;    // seconds before rotating back

    // --- State machine ---
    public enum State { FLAT, ROTATING_DOWN, TILTED, ROTATING_BACK }
    private State state = State.FLAT;

    private float rotateSpeed  = 120f;  // degrees per second
    private float currentAngle = 0f;    // degrees (positive = CCW, negative = CW)
    private float targetAngle  = 0f;    // degrees — set when player lands
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
            // Nudge player LEFT (same direction as tilt)
            player.getPlayerBody().applyLinearImpulse(
                new Vector2(-60f, 0f),
                player.getPlayerBody().getWorldCenter(),
                true
            );
        } else {
            targetAngle = -TILT_ANGLE_DEG;
            // Nudge player RIGHT (same direction as tilt)
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
                // Nothing to do — waiting for player to land
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
                // Wait before resetting
                resetTimer += dt;
                if (resetTimer >= RESET_DELAY) {
                    state = State.ROTATING_BACK;
                }
                break;

            case ROTATING_BACK:
                // Rotate back toward 0 degrees
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
