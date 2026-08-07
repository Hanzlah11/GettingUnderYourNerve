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

    private boolean playedWhoosh = false;
    private boolean playedShout = false;
    private boolean playedSwing = false;
    private boolean playedImpact = false;
    private boolean playedProtest = false;
    private boolean playedSorry = false;
    private boolean playedChaseVoice = false;

    public PrologueCutscene(PlayScreen screen) {
        super(screen, null);
        this.isSkippable = false;
        pStop = getObjectPos("player_stop");
        bSpawn = getObjectPos("batman_spawn");
        bStop = getObjectPos("batman_stop");
        escapeBat = getObjectPos("escapebatman");
        escapePlay = getObjectPos("escapeplayer");
    }

    private float getClampedX(float targetX) {
        float worldWidth = screen.getPlayableMap().getMapWidthInMeters();
        float halfVW = (cam.GetCam().viewportWidth) / 2f;
        return MathUtils.clamp(targetX, halfVW, worldWidth - halfVW);
    }

    private void updateFootsteps(float relativeVolume) {
        float masterSFX = AudioManager.sfxVolumeModifier;
        if (masterSFX <= 0f) {
            if (footstepId != -1) {
                AudioManager.footsteps.stop(footstepId);
                footstepId = -1;
            }
        } else {
            if (footstepId == -1) {
                footstepId = AudioManager.footsteps.loop(relativeVolume * masterSFX);
            } else {
                AudioManager.footsteps.setVolume(footstepId, relativeVolume * masterSFX);
            }
        }
    }

    private void stopFootsteps() {
        if (footstepId != -1) {
            AudioManager.footsteps.stop(footstepId);
            footstepId = -1;
        }
    }

    @Override
    public void update(float dt) {
        stateTimer += dt;
        float lerp = 0.05f;

        switch (state) {
            case 0:
                if (player.GetXpos() < pStop.x) {
                    player.getPlayerBody().setLinearVelocity(2.5f, 0);
                    updateFootsteps(0.4f);
                    cam.GetCam().position.x = getClampedX(player.GetXpos());
                } else {
                    player.getPlayerBody().setLinearVelocity(0, 0);
                    stopFootsteps();
                    state = 1;
                    stateTimer = 0;
                }
                break;

            case 1:
                float targetX1 = getClampedX(bSpawn.x);
                cam.GetCam().position.x += (targetX1 - cam.GetCam().position.x) * lerp;

                if (Math.abs(cam.GetCam().position.x - targetX1) < 0.1f) {
                    state = 2;
                    stateTimer = 0;
                }
                break;

            case 2:
                if (!batmanSpawned) {
                    if (!playedWhoosh) {
                        AudioManager.playSFX(AudioManager.batman_spawn_whoosh, 1.0f);
                        playedWhoosh = true;
                    }
                    this.batman = screen.getPlayableMap().spawnBatman(screen.getWorld(), bSpawn.x * 32, bSpawn.y * 32);
                    batmanSpawned = true;
                }
                if (stateTimer > 3.0f) {
                    state = 3;
                    stateTimer = 0;
                }
                break;

            case 3:
                if (batman.GetXpos() < bStop.x) {
                    batman.b2body.setLinearVelocity(3.0f, 0);
                    updateFootsteps(0.4f);
                    batman.setAction(Batman.State.MOVING);
                } else {
                    batman.b2body.setLinearVelocity(0, 0);
                    stopFootsteps();
                    batman.setAction(Batman.State.IDLE);
                    batman.facingRight = true;

                    if (!playedShout) {
                        AudioManager.playDialogue(AudioManager.batman_shout_stop, 1.0f);
                        playedShout = true;
                    }
                    currentSubtitle = "Halt evildoer! Your reign of minor\ninconveniences ends here!";

                    if (stateTimer > 5.5f) {
                        state = 4;
                        stateTimer = 0;
                    }
                }
                break;

            case 4:
                float targetX4 = getClampedX(player.GetXpos());
                cam.GetCam().position.x += (targetX4 - cam.GetCam().position.x) * lerp;
                player.facingRight = false;

                if (Math.abs(cam.GetCam().position.x - targetX4) < 0.1f && stateTimer > 2.0f) {
                    state = 5;
                    stateTimer = 0;
                }
                break;

            case 5:
                cam.GetCam().position.x += (getClampedX(batman.GetXpos()) - cam.GetCam().position.x) * lerp;

                if (batman.GetXpos() < player.GetXpos() - 1.0f) {
                    batman.b2body.setLinearVelocity(5.0f, 0);
                    updateFootsteps(0.5f);
                    batman.setAction(Batman.State.MOVING);
                    stateTimer = 0;
                } else {
                    currentSubtitle = "";
                    batman.b2body.setLinearVelocity(0, 0);
                    stopFootsteps();
                    batman.setAction(Batman.State.ATTACKING);
                    if (!playedSwing) {
                        AudioManager.playSFX(AudioManager.batman_swing_fist, 1.0f);
                        playedSwing = true;
                    }

                    if (stateTimer > 0.7f) {
                        if (!playedImpact) {
                            AudioManager.playSFX(AudioManager.punch_impact_heavy, 1.0f);
                            playedImpact = true;
                        }
                        player.hit(0, batman.GetXpos());
                        state = 6;
                        stateTimer = 0;
                    }
                }
                break;

            case 6:
                if (player.isHit) {
                    player.getPlayerBody().setLinearVelocity(0, 0);
                    player.isHit = false;
                }

                if (stateTimer < 0.5f) {
                    batman.setAction(Batman.State.IDLE);
                    batman.b2body.setLinearVelocity(0, 0);
                    batman.facingRight = true;
                }
                else if (stateTimer < 6.0f) {
                    if (!playedProtest) {
                        AudioManager.playDialogue(AudioManager.player_protest_wrong_guy, 1.0f);
                        playedProtest = true;
                    }
                    currentSubtitle = "Ouch! What the hell man!\nYou got the wrong guy!\nI was just going to the store!";
                    batman.setAction(Batman.State.IDLE);
                    batman.b2body.setLinearVelocity(0, 0);
                }
                else if (stateTimer < 12.3f) {
                    if (!playedSorry) {
                        AudioManager.playDialogue(AudioManager.batman_apology_sorry, 1.0f);
                        playedSorry = true;
                    }
                    currentSubtitle = "Wait your not the potato chip man?!\nOh shit sorry, I got the wrong guy!";
                    batman.b2body.setLinearVelocity(0, 0);
                    batman.setAction(Batman.State.IDLE);
                }
                else if (stateTimer < 14.3f) {
                    currentSubtitle = "My bad! Forget you saw me!";
                    batman.b2body.setLinearVelocity(4.0f, 0);
                    updateFootsteps(0.3f);
                    batman.setAction(Batman.State.MOVING);

                    if (batman.GetXpos() > player.GetXpos() && !player.facingRight)
                        player.facingRight = true;
                }
                else {
                    if (batman != null && !batman.destroyed) {
                        batman.b2body.setLinearVelocity(10.0f, 0);
                        if (batman.GetXpos() > escapeBat.x - 1.0f) {
                            stopFootsteps();
                            batman.setToDestroy = true;
                            state = 7;
                            stateTimer = 0;
                        }
                    } else {
                        state = 7;
                    }
                }
                break;

            case 7:
                if (!playedChaseVoice) {
                    AudioManager.playDialogue(AudioManager.player_shout_come_back, 1.0f);
                    playedChaseVoice = true;
                }

                if (stateTimer < 4.0f) {
                    currentSubtitle = "Hey where the hell do you think your going!\nGet your ass back here!";
                    player.getPlayerBody().setLinearVelocity(0, 0);
                } else {
                    player.getPlayerBody().setLinearVelocity(8.0f, 0);
                    currentSubtitle = "My nose is bleeding!\nGet me to the doctor immediately!";
                    updateFootsteps(0.6f);
                }

                if (player.GetXpos() > escapePlay.x - 1.0f && stateTimer > 8.0f) {
                    stopFootsteps();
                    finished = true;
                    currentSubtitle = "";
                }
                break;
        }
    }

    @Override
    public void skip() {
        stopFootsteps();
        super.skip();
    }
}
