package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;

public class PrologueCutscene extends BaseCutscene {

    private Vector2 pStop, bSpawn, bStop, escapeBat, escapePlay;
    private boolean batmanSpawned = false;
    private long footstepId = -1;

    // Audio Flags
    private boolean playedWhoosh = false;
    private boolean playedShout = false;
    private boolean playedSwing = false;
    private boolean playedImpact = false;
    private boolean playedProtest = false;
    private boolean playedSorry = false;
    private boolean playedChaseVoice = false;

    public PrologueCutscene(PlayScreen screen) {
        super(screen, null);
        pStop = getObjectPos("player_stop");
        bSpawn = getObjectPos("batman_spawn");
        bStop = getObjectPos("batman_stop");
        escapeBat = getObjectPos("escapebatman");
        escapePlay = getObjectPos("escapeplayer");
    }

    // HELPER: Calculates where the camera is actually allowed to stop
    private float getClampedX(float targetX) {
        float worldWidth = screen.getPlayableMap().getMapWidthInMeters();
        float halfVW = (cam.GetCam().viewportWidth) / 2f;
        return MathUtils.clamp(targetX, halfVW, worldWidth - halfVW);
    }

    @Override
    public void update(float dt) {
        // --- SKIP CUTSCENE LOGIC ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            player.getPlayerBody().setLinearVelocity(0, 0); // Stop player

            if (batman != null && !batman.destroyed) {
                batman.setToDestroy = true; // Clean up Batman
            }

            // Stop any looping footsteps so they don't persist into the next level!
            if (footstepId != -1) {
                AudioManager.footsteps.stop(footstepId);
                footstepId = -1;
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
                    if (footstepId == -1) footstepId = AudioManager.footsteps.loop(0.4f);
                    cam.GetCam().position.x = getClampedX(player.GetXpos()); // Follow with clamp
                } else {
                    player.getPlayerBody().setLinearVelocity(0, 0);
                    AudioManager.footsteps.stop(footstepId);
                    footstepId = -1;
                    state = 1;
                    stateTimer = 0;
                }
                break;

            case 1: // CAMERA PANS TO SPAWN
                float targetX1 = getClampedX(bSpawn.x); // Pan to clamped target
                cam.GetCam().position.x += (targetX1 - cam.GetCam().position.x) * lerp;

                if (Math.abs(cam.GetCam().position.x - targetX1) < 0.1f) {
                    state = 2;
                    stateTimer = 0;
                }
                break;

            case 2: // SPAWN BATMAN
                if (stateTimer > 0.5f && !batmanSpawned) {
                    if (!playedWhoosh) {
                        AudioManager.batman_spawn_whoosh.play(1.0f); // Max volume
                        playedWhoosh = true;
                    }
                    this.batman = screen.getPlayableMap().spawnBatman(screen.getWorld(), bSpawn.x * 32, bSpawn.y * 32);
                    batmanSpawned = true;
                }
                if (stateTimer > 4.5f) {
                    state = 3;
                    stateTimer = 0;
                }
                break;

            case 3: // BATMAN WALKS IN
                if (batman.GetXpos() < bStop.x) {
                    batman.b2body.setLinearVelocity(3.0f, 0);
                    if (footstepId == -1) footstepId = AudioManager.footsteps.loop(0.4f);
                    batman.setAction(Batman.State.MOVING);
                } else {
                    batman.b2body.setLinearVelocity(0, 0);
                    AudioManager.footsteps.stop(footstepId);
                    footstepId = -1;
                    batman.setAction(Batman.State.IDLE);
                    batman.facingRight = true;

                    if (!playedShout) {
                        AudioManager.batman_shout_stop.play(1.0f);
                        playedShout = true;
                    }
                    if (stateTimer > 5.5f) {
                        state = 4;
                        stateTimer = 0;
                    }
                }
                break;

            case 4: // PAN BACK TO PLAYER
                float targetX4 = getClampedX(player.GetXpos()); // Pan to clamped target
                cam.GetCam().position.x += (targetX4 - cam.GetCam().position.x) * lerp;
                player.facingRight = false;

                if (Math.abs(cam.GetCam().position.x - targetX4) < 0.1f && stateTimer > 2.0f) {
                    state = 5;
                    stateTimer = 0;
                }
                break;

            case 5: // BATMAN PUNCH
                cam.GetCam().position.x += (getClampedX(batman.GetXpos()) - cam.GetCam().position.x) * lerp;

                if (batman.GetXpos() < player.GetXpos() - 1.0f) {
                    batman.b2body.setLinearVelocity(5.0f, 0);
                    if (footstepId == -1) footstepId = AudioManager.footsteps.loop(0.5f);
                    batman.setAction(Batman.State.MOVING);
                    stateTimer = 0;
                } else {
                    batman.b2body.setLinearVelocity(0, 0);
                    AudioManager.footsteps.stop(footstepId);
                    footstepId = -1;
                    batman.setAction(Batman.State.ATTACKING);

                    if (!playedSwing) {
                        AudioManager.batman_swing_fist.play(1.0f);
                        playedSwing = true;
                    }

                    if (stateTimer > 0.7f) {
                        if (!playedImpact) {
                            AudioManager.punch_impact_heavy.play(1.0f);
                            playedImpact = true;
                        }
                        player.hit(0, batman.GetXpos());
                        state = 6;
                        stateTimer = 0;
                    }
                }
                break;

            case 6: // DIALOGUE SEQUENCE + BATMAN ESCAPE
                if (player.isHit) {
                    player.getPlayerBody().setLinearVelocity(0, 0);
                    player.isHit = false;
                }

                // --- Dialogue Audio Logic ---
                if (stateTimer > 1.0f && !playedProtest) {
                    // TODO: Replace with actual player protest sound
                    // AudioManager.player_protest.play(1.0f);
                    playedProtest = true;
                }

                if (stateTimer > 2.5f && !playedSorry) {
                    // TODO: Replace with actual batman sorry sound
                    // AudioManager.batman_sorry.play(1.0f);
                    playedSorry = true;
                }

                // Batman escapes after the dialogue finishes
                if (stateTimer > 4.0f) {
                    if (batman != null && !batman.destroyed) {
                        batman.b2body.setLinearVelocity(8.0f, 0);
                        batman.setAction(Batman.State.MOVING);

                        if (footstepId == -1) footstepId = AudioManager.footsteps.loop(0.5f);

                        if (batman.GetXpos() > escapeBat.x - 1.0f) {
                            batman.setToDestroy = true;
                            AudioManager.footsteps.stop(footstepId);
                            footstepId = -1;
                            state = 7;
                            stateTimer = 0;
                        }
                    } else {
                        state = 7;
                        stateTimer = 0;
                    }
                }
                break;

            case 7: // PLAYER CHASE
                if (!playedChaseVoice) {
                    // TODO: Replace with actual chase shout sound
                    // AudioManager.player_chase_shout.play(1.0f);
                    playedChaseVoice = true;
                }

                player.getPlayerBody().setLinearVelocity(8.0f, 0);

                if (footstepId == -1) footstepId = AudioManager.footsteps.loop(0.4f);

                if (player.GetXpos() > escapePlay.x - 1.0f) {
                    AudioManager.footsteps.stop(footstepId);
                    footstepId = -1;
                    finished = true;
                }
                break;
        }
    }
}
