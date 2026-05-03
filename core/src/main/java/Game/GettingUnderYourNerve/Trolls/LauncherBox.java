package Game.GettingUnderYourNerve.Trolls;

import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 * LauncherBox — launches the player in the opposite direction they came from
 * the moment they land on it.
 *
 * Direction logic:
 *   Player was moving LEFT  (velX < 0) → launched RIGHT (positive X)
 *   Player was moving RIGHT (velX > 0) → launched LEFT  (negative X)
 *   Player was stationary              → launched straight UP only
 *
 * Tiled setup:
 *   Object Layer : "Boxes"
 *   Name         : "LauncherBox"
 *   Rectangle    : any size
 *   Properties:
 *     Force (float) — launch impulse strength (default 25)
 */
public class LauncherBox extends Box {

    private static final float DEFAULT_FORCE     = 25f;
    private static final float VERTICAL_FORCE    = 20f; // upward component always applied
    private static final float COOLDOWN_DURATION = 0.5f; // seconds before it can launch again

    private float launchForce;
    private float cooldownTimer = 0f;
    private boolean onCooldown  = false;

    public LauncherBox(World world, MapObject object, GameAssetManager assets) {
        // Static body — the box itself doesn't move
        super(world, object, assets, BodyDef.BodyType.StaticBody);

        MapProperties props = object.getProperties();
        launchForce = props.containsKey("Force")
            ? props.get("Force", Float.class)
            : DEFAULT_FORCE;
    }

    // ---------------------------------------------------------------
    // onPlayerLand — reads the player's current velocity to determine
    // which direction they came from, then launches them opposite.
    // ---------------------------------------------------------------
    @Override
    public void onPlayerLand(float playerX) {
        // Direction is handled in the overloaded version below
        // This signature is required by Box but unused for LauncherBox
    }

    /**
     * Called by WorldContactListener, passing the full Player reference
     * so we can read velocity and apply impulse directly.
     */
    public void onPlayerLand(Player player) {
        if (onCooldown) return;

        float playerX  = player.GetXpos();
        float boxCenterX = body.getPosition().x;

        // Determine direction based on which side of the box the player is on
        float horizontalImpulse;
        if (playerX < boxCenterX) {
            // Player is on the LEFT side → came from left → launch RIGHT
            horizontalImpulse = launchForce;
        } else if (playerX > boxCenterX) {
            // Player is on the RIGHT side → came from right → launch LEFT
            horizontalImpulse = -launchForce;
        } else {
            // Exactly centered — no horizontal launch
            horizontalImpulse = 0f;
        }

        player.launch(horizontalImpulse, VERTICAL_FORCE);

        onCooldown    = true;
        cooldownTimer = 0f;
    }

    // ---------------------------------------------------------------
    // update — ticks the cooldown so the box can launch again
    // ---------------------------------------------------------------
    @Override
    public void update(float dt) {
        if (onCooldown) {
            cooldownTimer += dt;
            if (cooldownTimer >= COOLDOWN_DURATION) {
                onCooldown    = false;
                cooldownTimer = 0f;
            }
        }
    }
}
