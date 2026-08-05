package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import com.badlogic.gdx.math.Vector2;

public class AvengersCutscene extends BaseCutscene {

    private Vector2 retreatPos;
    private boolean playedPlayerLine = false;
    private boolean playedBatmanLine = false;

    public AvengersCutscene(PlayScreen screen, Batman batman) {
        super(screen, batman);
        retreatPos = getObjectPos("batman_retreat");
    }

    @Override
    public void update(float dt) {
        stateTimer += dt;

        // FIXED: Only lock camera Y during speech; freeze Y when floor drops (state 3)
        if (state < 3) {
            cam.GetCam().position.y = player.GetYpos() + 0.5f;
        }

        // Pan camera between Batman and the Player
        float targetX = (batman != null) ? (player.GetXpos() + batman.GetXpos()) / 2f : player.GetXpos();
        cam.GetCam().position.x += (targetX - cam.GetCam().position.x) * 0.08f;

        switch (state) {
            case 0: // BATMAN WALKS TO THE RIGHT
                if (batman != null && retreatPos.x > 0 && batman.GetXpos() < retreatPos.x) {
                    batman.b2body.setLinearVelocity(4.0f, 0);
                    batman.setAction(Batman.State.MOVING);
                    batman.facingRight = true;
                } else if (batman != null) {
                    batman.b2body.setLinearVelocity(0, 0);
                    batman.setAction(Batman.State.IDLE);
                    batman.facingRight = false;
                    state = 1; stateTimer = 0;
                }
                break;

            case 1: // PLAYER SPEAKS FIRST
                if (!playedPlayerLine) {
                    AudioManager.playSFX(AudioManager.ending_player1, 1.0f);
                    playedPlayerLine = true;
                }
                if(stateTimer < 4.5f)
                    currentSubtitle = "It's over batman. Your Charizard is down!";
                else if (stateTimer >= 4.5f && stateTimer <= 8.5f)
                    currentSubtitle = "Now stop this madness and apologize\nfor all the trolls ";
                else
                    currentSubtitle = "and spikes you put me through!";

                if (stateTimer > 10.5f) { state = 2; stateTimer = 0; currentSubtitle = "";}
                break;

            case 2: // BATMAN SPEAKS SECOND
                if (!playedBatmanLine) {
                    AudioManager.playSFX(AudioManager.ending_batman1, 1.0f);
                    playedBatmanLine = true;
                }
                if(stateTimer < 3.5f)
                    currentSubtitle = "Apologize? You still don't understand, do you?";
                else if (stateTimer >= 3.5f && stateTimer < 9.5f)
                    currentSubtitle = "I don't lose! I simply adjust the parameters of the fight!";
                else if (stateTimer >= 9.5f && stateTimer < 11.5f)
                    currentSubtitle = "I am Vengeance!";
                else if (stateTimer >= 11.5f && stateTimer < 13.5f)
                    currentSubtitle = "I am the Night!";
                else if (stateTimer >= 13.5f && stateTimer < 16.5f)
                    currentSubtitle = "I am the BATMAN!";
                else
                    currentSubtitle = "And I have contingencies for everything!";

                if (stateTimer > 21.0f) { state = 3; stateTimer = 0; currentSubtitle = "";}
                break;

            case 3: // THE FLOOR DISAPPEARS
                screen.getPlayableMap().dropBossFloor(screen.getWorld());

                // Wake up physics engine
                player.getPlayerBody().setAwake(true);
                player.getPlayerBody().applyLinearImpulse(new Vector2(0, -0.1f), player.getPlayerBody().getWorldCenter(), true);

                finished = true; // End cutscene
                break;
        }

        // Keep camera in bounds
        float halfVW = cam.GetCam().viewportWidth / 2f;
        float mapWidth = screen.getPlayableMap().getMapWidthInMeters();
        cam.GetCam().position.x = Math.max(halfVW, Math.min(cam.GetCam().position.x, mapWidth - halfVW));
    }

    @Override
    public void skip() {
        System.out.println("You cannot skip the inevitable.");
    }
}
