package Game.GettingUnderYourNerve.Trolls;

import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class LauncherBox extends Box {

    private static final float DEFAULT_FORCE     = 25f;
    private static final float VERTICAL_FORCE    = 20f;
    private static final float COOLDOWN_DURATION = 0.5f;

    private float launchForce;
    private float cooldownTimer = 0f;
    private boolean onCooldown  = false;

    public LauncherBox(World world, MapObject object, GameAssetManager assets) {
        super(world, object, assets, BodyDef.BodyType.StaticBody);

        MapProperties props = object.getProperties();
        launchForce = props.containsKey("Force")
            ? props.get("Force", Float.class)
            : DEFAULT_FORCE;
    }

    @Override
    public void onPlayerLand(float playerX, Player player) {
        onPlayerLand(player);
    }

    public void onPlayerLand(Player player) {
        if (onCooldown) return;

        float playerX  = player.GetXpos();
        float boxCenterX = body.getPosition().x;

        float horizontalImpulse;
        if (playerX < boxCenterX) {
            horizontalImpulse = launchForce;
        } else if (playerX > boxCenterX) {
            horizontalImpulse = -launchForce;
        } else {
            horizontalImpulse = 0f;
        }

        player.launch(horizontalImpulse, VERTICAL_FORCE);

        onCooldown    = true;
        cooldownTimer = 0f;
    }

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
