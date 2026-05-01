package Game.GettingUnderYourNerve.Utilities;

import Game.GettingUnderYourNerve.Collectables.Coin;
import Game.GettingUnderYourNerve.Collectables.Potion;
import Game.GettingUnderYourNerve.Enemies.Enemy;
import Game.GettingUnderYourNerve.Enemies.Projectile;
import Game.GettingUnderYourNerve.Enemies.Shell;
import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import com.badlogic.gdx.physics.box2d.*;

public class WorldContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        Object objA = fixA.getUserData();
        Object objB = fixB.getUserData();

        // 1. Check for Collectables
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

        // 2. Check for Deadly Water Traps
        if (objA instanceof Player && "water_sensor".equals(objB)) {
            ((Player) objA).addHp(-100);
            return;
        } else if (objB instanceof Player && "water_sensor".equals(objA)) {
            ((Player) objB).addHp(-100);
            return;
        }

        // 3. Handle Bitmask Collisions (Enemies & Projectiles)
        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        switch (cDef) {
            case Main.PLAYER_BIT | Main.ENEMY_BIT:
                handlePlayerEnemyCollision(fixA, fixB);
                break;
            case Main.PLAYER_BIT | Main.PROJECTILE_BIT:
                handlePlayerProjectileCollision(fixA, fixB);
                break;
            case Main.PLAYER_BIT | Main.GROUND_BIT:
                break;
        }
    }

    private void handlePlayerProjectileCollision(Fixture a, Fixture b) {
        Object userDataA = a.getUserData();
        Object userDataB = b.getUserData();

        Projectile projectile;
        Player player;

        // Figure out who is who
        if (userDataA instanceof Projectile) {
            projectile = (Projectile) userDataA;
            player = (Player) userDataB;
        } else {
            projectile = (Projectile) userDataB;
            player = (Player) userDataA;
        }

        // --- NEW: DESTROY PEARL AND DAMAGE PLAYER ---
        projectile.setToDestroy();
        // 10 damage, pass the pearl's X position to calculate knockback direction
        player.hit(10, projectile.GetXpos());
    }

    private void handlePlayerEnemyCollision(Fixture a, Fixture b) {
        Player player = (Player) (a.getUserData() instanceof Player ? a.getUserData() : b.getUserData());
        Enemy enemy = (Enemy) (a.getUserData() instanceof Enemy ? a.getUserData() : b.getUserData());

        float threshold = enemy.drawHeight / 2.5f;

        // Did the player land on top of the enemy?
        if (player.GetYpos() > enemy.GetYpos() + threshold) {
            if (enemy instanceof Shell) {
                ((Shell) enemy).currentState = Shell.State.SHOOTING; // Trigger the trap!
            }
        } else {
            // --- NEW: PLAYER BUMPED THE SIDE OF THE ENEMY ---
            // 20 damage for touching the shell itself
            player.hit(20, enemy.GetXpos());
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        if (cDef == (Main.PLAYER_BIT | Main.ENEMY_BIT)) {
            Enemy enemy = (Enemy) (fixA.getUserData() instanceof Enemy ? fixA.getUserData() : fixB.getUserData());
            if (enemy instanceof Shell && ((Shell) enemy).currentState == Shell.State.SHOOTING) {
                contact.setEnabled(false); // Let the player fall into the open shell
            }
        }
    }

    @Override public void endContact(Contact contact) {}
    @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
}
