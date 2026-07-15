package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class BossCutscene extends BaseCutscene {

    private Vector2 bossStop;
    private PokeBall batmanBall;
    private PokeBall playerBall;

    private boolean playedThreat = false;
    private boolean playedBatmanChoose = false;
    private boolean playedPlayerChoose = false;

    private Texture whitePixel;
    private float flashAlpha = 0f;

    // Inside BossCutscene.java
    private boolean playedB2 = false;
    private boolean playedP1 = false;

    public BossCutscene(PlayScreen screen, Batman batman) {
        super(screen, batman);
        bossStop = getObjectPos("boss_stop");

        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fill();
        whitePixel = new Texture(pix);
        pix.dispose();
    }

    @Override
    public void update(float dt) {
        stateTimer += dt;

        if (batmanBall != null) batmanBall.update(dt);
        if (playerBall != null) playerBall.update(dt);

        // ==========================================
        // --- CAMERA FIX & CINEMATIC PANNING ---
        // ==========================================
        // 1. Keep camera at the correct vertical height
        cam.GetCam().position.y = player.GetYpos() + 0.5f;

        // 2. Determine where the camera should look
        float targetX;
        if (state == 0) {
            targetX = player.GetXpos() + 1.5f; // Look slightly ahead of walking player
        } else {
            // Cinematic shot: Pan to the middle of Player and Batman
            targetX = (batman != null) ? (player.GetXpos() + batman.GetXpos()) / 2f : player.GetXpos();
        }

        // 3. Smoothly pan the camera X
        cam.GetCam().position.x += (targetX - cam.GetCam().position.x) * 0.05f;

        // 4. Clamp camera so we don't see the black void outside the map
        float halfVW = cam.GetCam().viewportWidth / 2f;
        float mapWidth = screen.getPlayableMap().getMapWidthInMeters();
        cam.GetCam().position.x = Math.max(halfVW, Math.min(cam.GetCam().position.x, mapWidth - halfVW));
        // ==========================================

        switch (state) {
            case 0: // PLAYER AUTO-WALKS IN
                if (player.GetXpos() < bossStop.x) {
                    player.getPlayerBody().setLinearVelocity(2.5f, 0);
                } else {
                    player.getPlayerBody().setLinearVelocity(0, 0);
                    state = 1;
                    stateTimer = 0;
                }
                break;

            case 1: // BATMAN INTRO: batman1.wav (10 seconds)
                if (batman != null) {
                    batman.facingRight = false;
                    batman.setAction(Batman.State.IDLE);
                }
                if (!playedThreat) {
                    AudioManager.playSFX(AudioManager.batman_boss1, 1.0f); // --- UPDATED ---
                    playedThreat = true;
                }
                // You really thought you could walk in here? I've been watching you since the prologue. I have analyzed your patterns, your jumps and even your sword swings!

                if(stateTimer < 3.5f)
                    currentSubtitle = "You really thought you could walk in here?";
                else if (stateTimer >= 3.5f && stateTimer < 8.0f)
                    currentSubtitle = "I've been watching you since the prologue.\n I have analyzed your patterns,";
                else
                    currentSubtitle = "your jumps and even your sword swings.";
                // Wait for audio to finish before moving to next dialogue
                if (stateTimer > 10.0f) { state = 2; stateTimer = 0; }
                break;

            // But lets see how you handle a different kind of combat. I have prepared a specific simulation for someone like you. CHARIZARD I CHOOSE YOU!
            case 2: // BATMAN "CHARIZARD": batman2.wav (12 seconds)
                if (!playedB2) {
                    AudioManager.playSFX(AudioManager.batman_boss2, 1.0f); // --- UPDATED ---
                    playedB2 = true;
                }

                if(stateTimer < 5.0f)
                    currentSubtitle = "But lets see how you handle a different kind of combat!";
                else if (stateTimer >= 5.0f && stateTimer <= 9.5f)
                    currentSubtitle = "I have prepared a specific simulation for someone like you!";
                else
                    currentSubtitle = "CHARIZARD I CHOOSE YOU!";

                // RED BALL DROPS: At the end of the 12s dialogue
                if (stateTimer > 12.0f && !playedBatmanChoose) {
                    playedBatmanChoose = true;
                    if (batman != null) {
                        float startX = batman.GetXpos() - 0.5f;
                        float startY = batman.GetYpos() + 0.5f;
                        float targetY = batman.GetYpos() - 0.4f;
                        batmanBall = new PokeBall(startX, startY, targetY, false);
                    }
                }

                // Buffer: Wait 2 extra seconds to see the ball hit the ground
                if (stateTimer > 14.0f) { state = 3; stateTimer = 0; currentSubtitle = "";}
                break;
            // A pokemon battle, seriosuly? Fine, if this is how we do it. Go PIKACHU!
            case 3: // PLAYER "PIKACHU": player1.wav (8 seconds)
                if (!playedP1) {
                    AudioManager.playSFX(AudioManager.player_boss1, 1.0f); // --- UPDATED ---
                    playedP1 = true;
                }

                if(stateTimer < 6.0f)
                    currentSubtitle = "A Pokemon battle, seriously?\nFine, if this is how we do it...";
                else
                    currentSubtitle = "GO PIKACHU!";
                // YELLOW BALL DROPS: At the end of the 8s dialogue
                if (stateTimer > 8.0f && !playedPlayerChoose) {
                    playedPlayerChoose = true;
                    float startX = player.GetXpos() + 0.5f;
                    float startY = player.GetYpos() + 0.5f;
                    float targetY = player.GetYpos() - 0.4f;
                    playerBall = new PokeBall(startX, startY, targetY, true);
                }

                // Buffer: Wait 2.5 extra seconds before the final flash
                if (stateTimer > 8.0f) { state = 4; stateTimer = 0; currentSubtitle = "";}
                break;

            case 4: // FLASHBANG & TRANSITION
                flashAlpha = Math.min(1f, flashAlpha + (dt * 1.5f));
                if (flashAlpha >= 1f) {
                    dispose(); // Disposes both BaseCutscene components and local whitePixel cleanly
                    screen.startPokemonBattle();
                    finished = true;
                }
                break;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
        if (batmanBall != null) batmanBall.render(batch);
        if (playerBall != null) playerBall.render(batch);

        if (flashAlpha > 0 && whitePixel != null) {
            Color c = batch.getColor();
            batch.setColor(1, 1, 1, flashAlpha);
            // Draw massive white box covering the camera
            batch.draw(whitePixel, cam.GetCam().position.x - 400, cam.GetCam().position.y - 240, 800, 480);
            batch.setColor(c);
        }
    }

    @Override
    public void skip() {
        super.skip();

        dispose(); // Clean up textures/fonts cleanly upon skip

        // --- NEW: STOP ALL BOSS DIALOGUE AUDIO ---
        // This prevents the dialogue from continuing over the Pokemon battle music
        AudioManager.batman_boss1.stop();
        AudioManager.batman_boss2.stop();
        AudioManager.player_boss1.stop();

        // Transition to the battle screen
        screen.startPokemonBattle();
    }

    @Override
    public void dispose() {
        super.dispose(); // Cleans up subtitleFont and blackBgTexture from BaseCutscene
        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
    }
}
