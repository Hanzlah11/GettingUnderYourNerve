package Game.GettingUnderYourNerve.Utilities;

import Game.GettingUnderYourNerve.Collectables.Coin;
import Game.GettingUnderYourNerve.Collectables.Potion;
import Game.GettingUnderYourNerve.Enemies.Crab;
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

        // -------------------------------------------------
        // PLAYER + COIN
        // -------------------------------------------------
        if (objA instanceof Player && objB instanceof Coin) {
            ((Coin) objB).onCollect((Player) objA);
            return;
        }
        else if (objB instanceof Player && objA instanceof Coin) {
            ((Coin) objA).onCollect((Player) objB);
            return;
        }

        // -------------------------------------------------
        // PLAYER + POTION
        // -------------------------------------------------
        if (objA instanceof Player && objB instanceof Potion) {
            ((Potion) objB).onCollect((Player) objA);
            return;
        }
        else if (objB instanceof Player && objA instanceof Potion) {
            ((Potion) objA).onCollect((Player) objB);
            return;
        }

        // -------------------------------------------------
        // BITMASK COLLISIONS
        // -------------------------------------------------
        int cDef =
            fixA.getFilterData().categoryBits |
                fixB.getFilterData().categoryBits;

        switch (cDef) {

            case Main.PLAYER_BIT | Main.ENEMY_BIT:
                handlePlayerEnemyCollision(fixA, fixB);
                break;

            case Main.PLAYER_BIT | Main.PROJECTILE_BIT:
                handlePlayerProjectileCollision(fixA, fixB);
                break;

            case Main.PLAYER_BIT | Main.GROUND_BIT:
                // Jump reset logic if needed later
                break;
        }
    }

    private void handlePlayerProjectileCollision(Fixture a, Fixture b) {

        Object objA = a.getUserData();
        Object objB = b.getUserData();

        Projectile projectile =
            (objA instanceof Projectile)
                ? (Projectile) objA
                : (Projectile) objB;

        if (projectile != null) {
            projectile.setToDestroy();
        }
    }

    private void handlePlayerEnemyCollision(Fixture a, Fixture b) {

        Object objA = a.getUserData();
        Object objB = b.getUserData();

        Player player =
            (Player) (objA instanceof Player ? objA : objB);

        Enemy enemy =
            (Enemy) (objA instanceof Enemy ? objA : objB);

        if (player == null || enemy == null)
            return;

        // -----------------------------------------
        // TOP COLLISION CHECK
        // -----------------------------------------
        float threshold = enemy.drawHeight / 2.0f;

        if (player.GetYpos() > enemy.GetYpos() + threshold) {

            // Player landed on enemy

            if (enemy instanceof Shell) {
                ((Shell) enemy).currentState =
                    Shell.State.SHOOTING;
            }

            // Add crab stomp logic later if needed
        }
        else {

            // Side collision

            if (enemy instanceof Crab) {
                ((Crab) enemy).attack();
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        int cDef =
            fixA.getFilterData().categoryBits |
                fixB.getFilterData().categoryBits;

        if (cDef == (Main.PLAYER_BIT | Main.ENEMY_BIT)) {

            // Continuous collision logic
            handlePlayerEnemyCollision(fixA, fixB);

            Object objA = fixA.getUserData();
            Object objB = fixB.getUserData();

            Enemy enemy =
                (Enemy) (objA instanceof Enemy ? objA : objB);

            // Shell becomes pass-through while open
            if (enemy instanceof Shell &&
                ((Shell) enemy).currentState ==
                    Shell.State.SHOOTING) {

                contact.setEnabled(false);
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
    }

    @Override
    public void postSolve(Contact contact,
                          ContactImpulse impulse) {
    }
}
