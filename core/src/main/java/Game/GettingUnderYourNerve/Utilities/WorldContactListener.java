package Game.GettingUnderYourNerve.Utilities;

import Game.GettingUnderYourNerve.Collectables.Coin;
import Game.GettingUnderYourNerve.Enemies.Crab;
import Game.GettingUnderYourNerve.Enemies.Enemy;
import Game.GettingUnderYourNerve.Enemies.Projectile;
import Game.GettingUnderYourNerve.Enemies.Shell;
import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Player;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class WorldContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        // 1. Check for Coins First (Using UserData)
        Object objA = fixA.getUserData();
        Object objB = fixB.getUserData();

        // CRITICAL DEBUG PRINT: See if the collision is even firing!
        // System.out.println("Contact! A: " + objA + " | B: " + objB);

        if (objA instanceof Player && objB instanceof Coin) {
            ((Coin) objB).onCollect((Player) objA);
            return; // Stop evaluating this specific contact; we handled it.
        } else if (objB instanceof Player && objA instanceof Coin) {
            ((Coin) objA).onCollect((Player) objB);
            return; // Stop evaluating.
        }

        // 2. If it wasn't a coin, handle enemies/ground using Bitmasks
        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        switch (cDef) {
            case Main.PLAYER_BIT | Main.ENEMY_BIT:
                handlePlayerEnemyCollision(fixA, fixB);
                break;
            case Main.PLAYER_BIT | Main.PROJECTILE_BIT:
                handlePlayerProjectileCollision(fixA, fixB);
                break;
            case Main.PLAYER_BIT | Main.GROUND_BIT:
                // Logic for resetting jumps could go here
                break;
        }
    }

    private void handlePlayerProjectileCollision(Fixture a, Fixture b)
    {
        System.out.println("Projectile hit Player!"); // DEBUG PRINT
        Object userDataA = a.getUserData();
        Object userDataB = b.getUserData();

        Projectile projectile = (userDataA instanceof Projectile) ? (Projectile) userDataA : (Projectile) userDataB;
        projectile.setToDestroy();
    }

    private void handlePlayerEnemyCollision(Fixture a, Fixture b) {
        // 1. Identify participants
        Object userDataA = a.getUserData();
        Object userDataB = b.getUserData();

        Player player = (Player) (userDataA instanceof Player ? userDataA : userDataB);
        Enemy enemy = (Enemy) (userDataA instanceof Enemy ? userDataA : userDataB);

        if (player == null || enemy == null) return;

        // 2. Determine Collision Type (Top vs Side)
        float threshold = enemy.drawHeight / 2.0f;

        if (player.GetYpos() > enemy.GetYpos() + threshold) {
            // TOP COLLISION: Player is jumping on the enemy
            if (enemy instanceof Shell) {
                ((Shell) enemy).currentState = Shell.State.SHOOTING; // Trigger the trap!
            }
            // You can add Crab "death" or "stun" logic here later if needed
        }
        else {
            // SIDE COLLISION
            if (enemy instanceof Crab) {
                ((Crab) enemy).attack();
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        // Handle Player and Enemy continuous contact
        if (cDef == (Main.PLAYER_BIT | Main.ENEMY_BIT)) {

            // 1. Continuous Attack Logic
            // By calling this in preSolve, the Crab checks its timer every frame
            // you are touching it, snapping as soon as the cooldown is over.
            handlePlayerEnemyCollision(fixA, fixB);

            // 2. The "Phase Through" Shell logic
            Object userDataA = fixA.getUserData();
            Object userDataB = fixB.getUserData();
            Enemy enemy = (Enemy) (userDataA instanceof Enemy ? userDataA : userDataB);

            // If the Shell is already open/shooting, disable the collision
            if (enemy instanceof Shell && ((Shell) enemy).currentState == Shell.State.SHOOTING) {
                contact.setEnabled(false);
            }
        }
    }

    @Override public void endContact(Contact contact) {}
    @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
}
