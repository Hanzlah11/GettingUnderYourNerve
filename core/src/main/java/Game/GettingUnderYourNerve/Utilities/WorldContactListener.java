package Game.GettingUnderYourNerve.Utilities;

import Game.GettingUnderYourNerve.Trolls.LauncherBox;
import Game.GettingUnderYourNerve.Trolls.RotatingBox;
import Game.GettingUnderYourNerve.Collectables.Coin;
import Game.GettingUnderYourNerve.Collectables.Potion;
import Game.GettingUnderYourNerve.Enemies.Crab;
import Game.GettingUnderYourNerve.Enemies.Enemy;
import Game.GettingUnderYourNerve.Enemies.Projectile;
import Game.GettingUnderYourNerve.Enemies.Shell;
import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Map.PlayableMap;
import Game.GettingUnderYourNerve.Trolls.TriggerZone;
import Game.GettingUnderYourNerve.Player;
import Game.GettingUnderYourNerve.Trap.Trap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class WorldContactListener implements ContactListener {

    // ---------------------------------------------------------------
    // PlayableMap reference — set once from Main after map is created.
    // ---------------------------------------------------------------
    private PlayableMap playableMap;

    public void setPlayableMap(PlayableMap map) {
        this.playableMap = map;
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        Object objA = fixA.getUserData();
        Object objB = fixB.getUserData();

        // ---- 1. Trigger zones ----
        if (objA instanceof Player && objB instanceof TriggerZone) {
            TriggerZone zone = (TriggerZone) objB;
            if (!zone.fired && playableMap != null)
                playableMap.activateTrigger(zone.id);
            return;
        }
        if (objB instanceof Player && objA instanceof TriggerZone) {
            TriggerZone zone = (TriggerZone) objA;
            if (!zone.fired && playableMap != null)
                playableMap.activateTrigger(zone.id);
            return;
        }

        // ---- 2. Collectables ----
        if (objA instanceof Player && objB instanceof Coin) {
            ((Coin) objB).onCollect((Player) objA);
            return;
        } else if (objB instanceof Player && objA instanceof Coin) {
            ((Coin) objA).onCollect((Player) objB);
            return;
        }

        if (objA instanceof Player && objB instanceof Potion) {
            ((Potion) objB).onCollect((Player) objA);
            return;
        } else if (objB instanceof Player && objA instanceof Potion) {
            ((Potion) objA).onCollect((Player) objB);
            return;
        }

        // ---- 3. Deadly water ----
        if (objA instanceof Player && "water_sensor".equals(objB)) {
            ((Player) objA).addHp(-100);
            return;
        } else if (objB instanceof Player && "water_sensor".equals(objA)) {
            ((Player) objB).addHp(-100);
            return;
        }

        // ---- 4. Bitmask collisions ----
        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        switch (cDef) {
            case Main.PLAYER_BIT | Main.ENEMY_BIT:
                handlePlayerEnemyCollision(fixA, fixB);
                break;
            case Main.PLAYER_BIT | Main.PROJECTILE_BIT:
                handlePlayerProjectileCollision(fixA, fixB);
                break;
            case Main.PROJECTILE_BIT | Main.GROUND_BIT:
                handleProjectileGroundCollision(fixA, fixB);
                break;

            // ---- Box interactions ----
            case Main.PLAYER_BIT | Main.GROUND_BIT: {
                Fixture groundFix = fixA.getFilterData().categoryBits == Main.GROUND_BIT ? fixA : fixB;
                Fixture playerFix = fixA.getFilterData().categoryBits == Main.PLAYER_BIT ? fixA : fixB;

                Object groundData = groundFix.getUserData();
                Object playerData = playerFix.getUserData();

                if (playerData instanceof Player) {
                    Player p = (Player) playerData;

                    // Only react when player is landing from above,
                    // not when walking into the side of the box
                    if (p.getPlayerBody().getLinearVelocity().y <= 0) {

                        if (groundData instanceof RotatingBox) {
                            // Pass player X so box knows which side they landed on
                            ((RotatingBox) groundData).onPlayerLand(p.GetXpos());
                        }
                        else if (groundData instanceof LauncherBox) {
                            // Pass full player so launcher can read velocity and call launch()
                            ((LauncherBox) groundData).onPlayerLand(p);
                        }
                    }
                }
                break;
            }

            case Main.PLAYER_BIT | Main.TRAP_BIT: {
                Fixture playerFix = fixA.getFilterData().categoryBits == Main.PLAYER_BIT ? fixA : fixB;
                Fixture trapFix   = fixA.getFilterData().categoryBits == Main.TRAP_BIT   ? fixA : fixB;
                Player p = (Player) playerFix.getUserData();
                Trap   t = (Trap)   trapFix  .getUserData();
                p.hit(t.getDamage(), t.body.getPosition().x);
                t.onHit(p);
                break;
            }
            case Main.SWORD_BIT | Main.ENEMY_BIT: {
                Fixture swordFix = fixA.getFilterData().categoryBits == Main.SWORD_BIT ? fixA : fixB;
                Fixture enemyFix = fixA.getFilterData().categoryBits == Main.ENEMY_BIT ? fixA : fixB;
                Player attackingPlayer = (Player) swordFix.getUserData();
                Enemy  targetEnemy     = (Enemy)  enemyFix.getUserData();
                float  pushDirection   = attackingPlayer.GetXpos() < targetEnemy.GetXpos() ? 5f : -5f;
                if (!targetEnemy.isDead) targetEnemy.takeDamage(1, pushDirection);
                break;
            }
        }
    }

    private void handleProjectileGroundCollision(Fixture a, Fixture b) {
        Object userDataA = a.getUserData();
        Object userDataB = b.getUserData();
        Projectile projectile = (userDataA instanceof Projectile)
            ? (Projectile) userDataA
            : (Projectile) userDataB;
        if (projectile != null) projectile.setToDestroy();
    }

    private void handlePlayerProjectileCollision(Fixture a, Fixture b) {
        Object userDataA = a.getUserData();
        Object userDataB = b.getUserData();
        Projectile projectile;
        Player     player;
        if (userDataA instanceof Projectile) {
            projectile = (Projectile) userDataA;
            player     = (Player)     userDataB;
        } else {
            projectile = (Projectile) userDataB;
            player     = (Player)     userDataA;
        }
        projectile.setToDestroy();
        player.hit(10, projectile.GetXpos());
    }

    private void handlePlayerEnemyCollision(Fixture a, Fixture b) {
        Object objA = a.getUserData();
        Object objB = b.getUserData();

        Player player = (Player) (objA instanceof Player ? objA : objB);
        Enemy  enemy  = (Enemy)  (objA instanceof Enemy  ? objA : objB);
        if (player == null || enemy == null) return;

        float   threshold = enemy.drawHeight / 2.0f;
        boolean isFalling = player.getPlayerBody().getLinearVelocity().y < -0.1f;

        if (player.GetYpos() > enemy.GetYpos() + threshold && isFalling) {
            if (enemy instanceof Shell) {
                ((Shell) enemy).bite();
                player.hit(40, enemy.GetXpos());
            }
            if (enemy instanceof Crab) {
                player.getPlayerBody().setLinearVelocity(
                    player.getPlayerBody().getLinearVelocity().x, 0);
                player.getPlayerBody().applyLinearImpulse(
                    new Vector2(0, 15.0f),
                    player.getPlayerBody().getWorldCenter(), true);
                ((Crab) enemy).attack();
                player.hit(10, enemy.GetXpos());
            }
        } else {
            if (enemy instanceof Crab) {
                ((Crab) enemy).attack();
                player.hit(20, enemy.GetXpos());
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        if (cDef == (Main.PLAYER_BIT | Main.ENEMY_BIT)) {
            handlePlayerEnemyCollision(fixA, fixB);

            Player player = (Player) (fixA.getUserData() instanceof Player
                ? fixA.getUserData() : fixB.getUserData());
            Enemy  enemy  = (Enemy)  (fixA.getUserData() instanceof Enemy
                ? fixA.getUserData() : fixB.getUserData());

            if (enemy instanceof Shell) {
                Shell   shell         = (Shell) enemy;
                float   sinkThreshold = enemy.drawHeight / 4.0f;
                boolean isAbove       = player.GetYpos() > enemy.GetYpos() + sinkThreshold;
                if (isAbove && (shell.isBiting() ||
                    shell.currentState == Shell.State.SHOOTING)) {
                    contact.setEnabled(false);
                }
            }
        }
    }

    @Override public void endContact(Contact contact) {}
    @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
}
