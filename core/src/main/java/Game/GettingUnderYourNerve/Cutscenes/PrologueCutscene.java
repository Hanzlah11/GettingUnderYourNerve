package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
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
                    if (stateTimer > 5.5f) { state = 4; stateTimer = 0; }
                }
                break;

            case 4: // PAN BACK TO PLAYER
                float targetX4 = getClampedX(player.GetXpos()); // Pan to clamped target[cite: 19]
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
                    if (!playedSwing) { AudioManager.batman_swing_fist.play(1.0f); playedSwing = true; }

                    if (stateTimer > 0.7f) {
                        if (!playedImpact) { AudioManager.punch_impact_heavy.play(1.0f); playedImpact = true; }
                        player.hit(0, batman.GetXpos());
                        state = 6;
                        stateTimer = 0;
                    }
                }
                break;

            case 6: // DIALOGUE SEQUENCE
                if (player.isHit) {
                    player.getPlayerBody().setLinearVelocity(0, 0);
                    player.isHit = false;
                }

                // PHASE 0: Dramatic Pause (Finish Whoosh + 1s Silence)
                if (stateTimer < 1.3f) {
                    batman.setAction(Batman.State.IDLE);
                    batman.b2body.setLinearVelocity(0, 0);
                    batman.facingRight = true;
                }
                // PHASE A: Player Protests (Starts at 1.3s, lasts 5s)
                else if (stateTimer < 6.3f) {
                    if (!playedProtest) {
                        AudioManager.player_protest_wrong_guy.play(1.0f); // "I'm innocent! Wrong guy!"
                        playedProtest = true;
                    }
                    batman.setAction(Batman.State.IDLE);
                    batman.b2body.setLinearVelocity(0, 0);
                }
                // PHASE B: Batman Apologizes (Starts at 6.3s, 6s Stillness)
                else if (stateTimer < 12.3f) {
                    if (!playedSorry) {
                        AudioManager.batman_apology_sorry.play(1.0f);
                        playedSorry = true;
                    }
                    batman.b2body.setLinearVelocity(0, 0); // Remain still for 75% of apology
                    batman.setAction(Batman.State.IDLE);
                }
                // PHASE C: Walk Away (Starts at 12.3s, lasts 2s)[cite: 19]
                else if (stateTimer < 14.3f) {
                    batman.b2body.setLinearVelocity(4.0f, 0); // Awkward walk
                    if (footstepId == -1) footstepId = AudioManager.footsteps.loop(0.3f);
                    batman.setAction(Batman.State.MOVING);
                }
                // PHASE D: Escape[cite: 19]
                else {
                    if (batman != null && !batman.destroyed) {
                        batman.b2body.setLinearVelocity(10.0f, 0);
                        if (batman.GetXpos() > escapeBat.x - 1.0f) {
                            AudioManager.footsteps.stop(footstepId);
                            footstepId = -1;
                            batman.setToDestroy = true;
                            state = 7;
                            stateTimer = 0; // Reset for the final chase
                        }
                    } else {
                        state = 7;
                    }
                }
                break;

            case 7: // FINAL CHASE
                if (!playedChaseVoice) {
                    AudioManager.player_shout_come_back.play(1.0f);
                    playedChaseVoice = true;
                }

                // Hold player for 4 seconds (half of 8s shout) before running
                if (stateTimer < 4.0f) {
                    player.getPlayerBody().setLinearVelocity(0, 0);
                } else {
                    player.getPlayerBody().setLinearVelocity(8.0f, 0);
                    if (footstepId == -1) footstepId = AudioManager.footsteps.loop(0.6f);
                }

                if (player.GetXpos() > escapePlay.x - 1.0f && stateTimer > 8.0f) {
                    AudioManager.footsteps.stop(footstepId);
                    finished = true;
                }
                break;
        }
    }

    @Override
    public void skip() {
        super.skip(); // Sets finished = true[cite: 16]

        // Stop all possible cutscene sounds immediately
        if (footstepId != -1) {
            AudioManager.footsteps.stop(footstepId);
            footstepId = -1;
        }

        // Optionally stop dialogue sounds to prevent overlap if skipped mid-sentence
        AudioManager.batman_shout_stop.stop();
        AudioManager.batman_apology_sorry.stop();
        AudioManager.player_protest_wrong_guy.stop();
        AudioManager.player_shout_come_back.stop();
    }
}
