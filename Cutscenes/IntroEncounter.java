package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import com.badlogic.gdx.math.Vector2;

public class IntroEncounter extends BaseCutscene {
    private Vector2 pancamPos, camEndPos, escapePos;
    private boolean playedPlayerVoice = false;
    private boolean playedBatmanVoice = false;

    public IntroEncounter(PlayScreen screen, Batman batman) {
        super(screen, batman);
        pancamPos = getObjectPos("pancam");
        camEndPos = getObjectPos("cam_end");
        escapePos = getObjectPos("escapebatman");
    }

    // Helper to get exact lengths from your screenshot + 0.5s buffer
    private float getWaitTime(boolean isPlayer, int level) {
        if (isPlayer) {
            if (level == 1) return 10.5f;
            if (level == 2) return 11.5f;
            return 11.5f; // Level 3
        } else {
            if (level == 1) return 13.5f;
            if (level == 2) return 13.5f;
            return 12.5f; // Level 3
        }
    }

    private void playDialogue(boolean isPlayer, int level) {
        if (isPlayer) {
            if (level == 1) AudioManager.playSFX(AudioManager.player_lvl1, 1.0f);
            else if (level == 2) AudioManager.playSFX(AudioManager.player_lvl2, 1.0f);
            else AudioManager.playSFX(AudioManager.player_lvl3, 1.0f);
        } else {
            if (level == 1) AudioManager.playSFX(AudioManager.batman_lvl1, 1.0f);
            else if (level == 2) AudioManager.playSFX(AudioManager.batman_lvl2, 1.0f);
            else AudioManager.playSFX(AudioManager.batman_lvl3, 1.0f);
        }
    }

    @Override
    public void update(float dt) {
        stateTimer += dt;
        int level = screen.getPlayableMap().getLevelNumber(); // Detect current level

        switch (state) {
            case 0: // PAN TO PLAYER
                float lerp = 0.05f;
                cam.GetCam().position.x += (player.GetXpos() - cam.GetCam().position.x) * lerp;
                cam.GetCam().position.y += (player.GetYpos() - cam.GetCam().position.y) * lerp;
                if (Math.abs(cam.GetCam().position.x - player.GetXpos()) < 0.1f) {
                    state = 1;
                }
                break;
            case 1: // AUTO-WALK + PLAYER DIALOGUE
                if (player.GetXpos() < pancamPos.x) {
                    player.getPlayerBody().setLinearVelocity(2.5f, 0);
                    cam.GetCam().position.x = player.GetXpos();
                    stateTimer = 0; // Hold timer at 0 while walking
                } else {
                    player.getPlayerBody().setLinearVelocity(0, 0);
                    if (!playedPlayerVoice) {
                        playDialogue(true, level);
                        playedPlayerVoice = true;
                    }

                    if (level == 1)
                    {
                        if (stateTimer < 5.0f)
                        {
                            currentSubtitle = "Hey, you with the ears! My nose is still ringing!";
                        }
                        else
                        {
                            currentSubtitle = "And I am pretty sure justice doesn't involve\npunching innocent pedestrians!";
                        }
                    }
                    // I've chased you this far! Through spike-pits and all those trials. Stop running and I can show you what an innocent left hook looks like.
                    else if(level == 2)
                    {
                        if(stateTimer < 6.5f)
                            currentSubtitle = "I've chased you this far! Through spike-pits\n and all those trails.";
                        else
                            currentSubtitle = "Stop running and I can show you\nwhat an innocent left hook looks like!";
                    }
                    // Wait for Player dialogue to finish before panning to Batman
                    if (stateTimer > getWaitTime(true, level)) {
                        currentSubtitle = "";
                        state = 2;
                        stateTimer = 0;
                    }
                }
                break;

            case 2: // PAN TO BATMAN
                if (batman != null) batman.facingRight = false;
                cam.GetCam().position.x += (camEndPos.x - cam.GetCam().position.x) * 0.05f;
                if (Math.abs(cam.GetCam().position.x - camEndPos.x) < 0.1f) {
                    state = 3;
                    stateTimer = 0;
                }
                break;

            case 3: // BATMAN DIALOGUE
                if (batman != null) {
                    batman.setAction(Batman.State.IDLE);
                    batman.facingRight = false;
                    if (!playedBatmanVoice) {
                        playDialogue(false, level);
                        playedBatmanVoice = true;
                    }
                }

                if (level == 1)
                {
                    if (stateTimer < 7.5f)
                    {
                        currentSubtitle = "Look citizens usually pay for the full vigilante experience.\nConsider that punch a free trail.";
                    }
                    else
                    {
                        currentSubtitle = "Anyways I have a cat to save from a toaster now!\nBye!";
                    }
                }
                // Violence is never the answer, unless I'm the one doing it. Have you tried breathing execercises, I find that they help with the rage. Look theres a distraction over there!

                else if (level == 2)
                {
                    if(stateTimer < 5.5f)
                        currentSubtitle = "Violence is never the answer,\nunless I'm the one doing it.";
                    else if(stateTimer >= 5.5f && stateTimer < 11.0f)
                        currentSubtitle = "Have you tried breathing exercises? I find that\nthey help with the rage.";
                    else
                        currentSubtitle = "Look there's a distraction over there!";
                }

                // Wait for Batman's funny de-escalation to finish
                if (stateTimer > getWaitTime(false, level)) {
                    state = 4;
                    stateTimer = 0;
                    currentSubtitle = "";
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
        if(isFinished())
            screen.increaseLevelAudio(0.5f);
    }

    @Override
    public void skip() {
        super.skip();

        // 1. Identify current level to stop the correct dialogue
        int level = screen.getPlayableMap().getLevelNumber();

        if (level == 1) {
            AudioManager.player_lvl1.stop();
            AudioManager.batman_lvl1.stop();
        } else if (level == 2) {
            AudioManager.player_lvl2.stop();
            AudioManager.batman_lvl2.stop();
        } else if (level == 3) {
            AudioManager.player_lvl3.stop();
            AudioManager.batman_lvl3.stop();
        }

        // 2. Ensure Batman is removed so he doesn't just stand there
        if (batman != null) {
            batman.setToDestroy = true;
        }
    }
}
