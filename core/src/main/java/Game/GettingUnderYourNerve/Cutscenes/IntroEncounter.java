package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

public class IntroEncounter extends BaseCutscene {
    private Vector2 pancamPos, camEndPos, escapePos;

    public IntroEncounter(PlayScreen screen, Batman batman) {
        super(screen, batman);
        pancamPos = getObjectPos("pancam");
        camEndPos = getObjectPos("cam_end");
        escapePos = getObjectPos("escapebatman");
    }

    @Override
    public void update(float dt) {
        // --- NEW: SKIP CUTSCENE LOGIC ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            player.getPlayerBody().setLinearVelocity(0, 0); // Stop player
            if (batman != null && !batman.destroyed) {
                batman.setToDestroy = true; // Clean up Batman
            }
            finished = true; // Give control back to the player
            return;
        }
        // --------------------------------

        stateTimer += dt;

        switch (state) {
            case 0: // PAN TO PLAYER START
                float lerp = 0.05f;
                cam.GetCam().position.x += (player.GetXpos() - cam.GetCam().position.x) * lerp;
                cam.GetCam().position.y += (player.GetYpos() - cam.GetCam().position.y) * lerp;

                if (Math.abs(cam.GetCam().position.x - player.GetXpos()) < 0.1f) {
                    state = 1;
                }
                break;

            case 1: // AUTO-WALK PLAYER
                if (player.GetXpos() < pancamPos.x) {
                    player.getPlayerBody().setLinearVelocity(2.5f, 0);
                    cam.GetCam().position.x = player.GetXpos();
                } else {
                    player.getPlayerBody().setLinearVelocity(0, 0);
                    state = 2;
                }
                break;

            case 2: // PAN TO BATMAN
                if (batman != null) {
                    batman.facingRight = false;
                }

                cam.GetCam().position.x += (camEndPos.x - cam.GetCam().position.x) * 0.05f;
                if (Math.abs(cam.GetCam().position.x - camEndPos.x) < 0.1f) {
                    state = 3;
                    stateTimer = 0;
                }
                break;

            case 3: // DIALOGUE WAIT
                if (batman != null) {
                    batman.setAction(Batman.State.IDLE);
                    batman.facingRight = false;
                }
                if (stateTimer > 3.0f) {
                    state = 4;
                }
                break;

            case 4: // BATMAN ESCAPE
                if (batman != null && !batman.setToDestroy) {
                    batman.b2body.setLinearVelocity(5.0f, 0);
                    batman.setAction(Batman.State.MOVING);

                    if (batman.GetXpos() >= escapePos.x - 1.5f) {
                        batman.setToDestroy = true;
                    }
                } else {
                    state = 5;
                }
                break;

            case 5: // PAN BACK TO PLAYER
                cam.GetCam().position.x += (player.GetXpos() - cam.GetCam().position.x) * 0.05f;
                if (Math.abs(cam.GetCam().position.x - player.GetXpos()) < 0.1f) {
                    finished = true;
                }
                break;
        }
    }
}
