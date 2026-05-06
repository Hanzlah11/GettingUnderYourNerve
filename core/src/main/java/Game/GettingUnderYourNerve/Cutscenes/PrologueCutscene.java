package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

public class PrologueCutscene extends BaseCutscene {

    private Vector2 pStop, bSpawn, bStop, escapeBat, escapePlay;
    private boolean batmanSpawned = false;

    public PrologueCutscene(PlayScreen screen) {
        super(screen, null);
        pStop = getObjectPos("player_stop");
        bSpawn = getObjectPos("batman_spawn");
        bStop = getObjectPos("batman_stop");
        escapeBat = getObjectPos("escapebatman");
        escapePlay = getObjectPos("escapeplayer");
    }

    @Override
    public void update(float dt) {
        // --- NEW: SKIP CUTSCENE LOGIC ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            player.getPlayerBody().setLinearVelocity(0, 0); // Stop player
            if (batman != null && !batman.destroyed) {
                batman.setToDestroy = true; // Clean up Batman
            }
            finished = true; // Instantly jump to Level 1
            return;
        }
        // --------------------------------

        stateTimer += dt;
        float lerp = 0.05f;

        switch (state) {
            case 0: // PLAYER AUTO-WALKS IN
                if (player.GetXpos() < pStop.x) {
                    player.getPlayerBody().setLinearVelocity(2.5f, 0);
                    cam.GetCam().position.x = player.GetXpos();
                } else {
                    player.getPlayerBody().setLinearVelocity(0, 0);
                    state = 1;
                }
                break;

            case 1: // CAMERA PANS BACK TO SPAWN
                cam.GetCam().position.x += (bSpawn.x - cam.GetCam().position.x) * lerp;
                float worldWidth = screen.getPlayableMap().getMapWidthInMeters();
                float halfVW = (cam.GetCam().viewportWidth) / 2f;
                float clampedTargetX = com.badlogic.gdx.math.MathUtils.clamp(bSpawn.x, halfVW, worldWidth - halfVW);

                if (Math.abs(cam.GetCam().position.x - clampedTargetX) < 0.1f) {
                    state = 2;
                    stateTimer = 0;
                }
                break;

            case 2: // WAIT A MOMENT, THEN SPAWN BATMAN
                if (stateTimer > 0.5f && !batmanSpawned) {
                    this.batman = screen.getPlayableMap().spawnBatman(screen.getWorld(), bSpawn.x * 32, bSpawn.y * 32);
                    batmanSpawned = true;
                    state = 3;
                }
                break;

            case 3: // BATMAN SHOUTS
                if (batman.GetXpos() < bStop.x) {
                    batman.b2body.setLinearVelocity(3.0f, 0);
                    batman.setAction(Batman.State.MOVING);
                } else {
                    batman.b2body.setLinearVelocity(0, 0);
                    batman.setAction(Batman.State.IDLE);
                    batman.facingRight = true;
                    if (stateTimer > 2.0f) state = 4;
                }
                break;

            case 4: // PLAYER WAITS
                cam.GetCam().position.x += (player.GetXpos() - cam.GetCam().position.x) * lerp;
                player.facingRight = false;
                if (Math.abs(cam.GetCam().position.x - player.GetXpos()) < 0.1f) {
                    if (stateTimer > 4.0f) state = 5;
                }
                break;

            case 5: // BATMAN PUNCHES
                cam.GetCam().position.x += (batman.GetXpos() - cam.GetCam().position.x) * lerp;
                if (batman.GetXpos() < player.GetXpos() - 1.0f) {
                    batman.b2body.setLinearVelocity(5.0f, 0);
                    batman.setAction(Batman.State.MOVING);
                } else {
                    batman.b2body.setLinearVelocity(0, 0);
                    batman.setAction(Batman.State.ATTACKING);
                    batman.facingRight = true;

                    if (stateTimer > 6.0f) {
                        player.hit(0, batman.GetXpos());
                        state = 6;
                        stateTimer = 0;
                    }
                }
                break;

            case 6: // APOLOGY + BATMAN ESCAPE
                if (player.isHit) {
                    player.getPlayerBody().setLinearVelocity(0, 0);
                    player.isHit = false;
                }

                if (batman != null && !batman.destroyed) {
                    batman.b2body.setLinearVelocity(8.0f, 0);
                    batman.setAction(Batman.State.MOVING);

                    if (batman.GetXpos() > escapeBat.x - 1.0f) {
                        batman.setToDestroy = true;
                        state = 7;
                        stateTimer = 0;
                    }
                } else {
                    state = 7;
                }
                break;

            case 7: // PLAYER CHASE
                player.getPlayerBody().setLinearVelocity(8.0f, 0);
                if (player.GetXpos() > escapePlay.x - 1.0f) {
                    finished = true;
                }
                break;
        }
    }
}
