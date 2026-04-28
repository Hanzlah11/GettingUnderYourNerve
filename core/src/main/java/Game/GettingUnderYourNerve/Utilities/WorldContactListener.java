package Game.GettingUnderYourNerve.Utilities;

import Game.GettingUnderYourNerve.Enemies.Enemy;
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

        // 1. Identify the collision type using Bitwise OR
        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        switch (cDef) {
            case Main.PLAYER_BIT | Main.ENEMY_BIT:
                handlePlayerEnemyCollision(fixA, fixB);
                break;

            case Main.PLAYER_BIT | Main.GROUND_BIT:
                // Logic for resetting jumps could go here
                break;
        }
    }

    private void handlePlayerEnemyCollision(Fixture a, Fixture b) {
        // Use helper methods to identify who is the player and who is the enemy
        Player player = (Player) (a.getUserData() instanceof Player ? a.getUserData() : b.getUserData());
        Enemy enemy = (Enemy) (a.getUserData() instanceof Enemy ? a.getUserData() : b.getUserData());

        // 2. Determine "Top" Collision
        // We check if the Player's Y position is significantly higher than the Enemy's
        float threshold = enemy.drawHeight / 2.5f;
        if (player.GetYpos() > enemy.GetYpos() + threshold) {
            if (enemy instanceof Shell) {
                ((Shell) enemy).currentState = Shell.State.SHOOTING; // Trigger the trap!
            }
        } else {
            // Player hit the side - Damage logic
            System.out.println("Collions!");
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        // 3. The "Phase Through" Trick
        if (cDef == (Main.PLAYER_BIT | Main.ENEMY_BIT)) {
            Enemy enemy = (Enemy) (fixA.getUserData() instanceof Enemy ? fixA.getUserData() : fixB.getUserData());

            // If the Shell is already open/shooting, disable the collision
            // This makes the player fall INTO the mouth
            if (enemy instanceof Shell && ((Shell) enemy).currentState == Shell.State.SHOOTING) {
                contact.setEnabled(false);
            }
        }
    }

    @Override public void endContact(Contact contact) {}
    @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
}
