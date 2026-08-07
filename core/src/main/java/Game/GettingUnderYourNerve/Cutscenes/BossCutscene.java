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

    private boolean playedB2 = false;
    private boolean playedP1 = false;

    public BossCutscene(PlayScreen screen, Batman batman) {
        super(screen, batman);
        this.isSkippable = false;
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

        cam.GetCam().position.y = player.GetYpos() + 0.5f;

        float targetX;
        if (state == 0) {
            targetX = player.GetXpos() + 1.5f;
        } else {
            targetX = (batman != null) ? (player.GetXpos() + batman.GetXpos()) / 2f : player.GetXpos();
        }

        cam.GetCam().position.x += (targetX - cam.GetCam().position.x) * 0.05f;

        float halfVW = cam.GetCam().viewportWidth / 2f;
        float mapWidth = screen.getPlayableMap().getMapWidthInMeters();
        cam.GetCam().position.x = Math.max(halfVW, Math.min(cam.GetCam().position.x, mapWidth - halfVW));

        switch (state) {
            case 0:
                if (player.GetXpos() < bossStop.x) {
                    player.getPlayerBody().setLinearVelocity(2.5f, 0);
                } else {
                    player.getPlayerBody().setLinearVelocity(0, 0);
                    state = 1;
                    stateTimer = 0;
                }
                break;

            case 1:
                if (batman != null) {
                    batman.facingRight = false;
                    batman.setAction(Batman.State.IDLE);
                }
                if (!playedThreat) {
                    AudioManager.playDialogue(AudioManager.batman_boss1, 1.0f);
                    playedThreat = true;
                }

                if (stateTimer < 3.5f)
                    currentSubtitle = "You really thought you could walk in here?";
                else if (stateTimer >= 3.5f && stateTimer < 8.0f)
                    currentSubtitle = "I've been watching you since the prologue.\n I have analyzed your patterns,";
                else
                    currentSubtitle = "your jumps and even your sword swings.";

                if (stateTimer > 10.0f) {
                    state = 2;
                    stateTimer = 0;
                }
                break;

            case 2:
                if (!playedB2) {
                    AudioManager.playDialogue(AudioManager.batman_boss2, 1.0f);
                    playedB2 = true;
                }

                if (stateTimer < 5.0f)
                    currentSubtitle = "But lets see how you handle a different kind of combat!";
                else if (stateTimer >= 5.0f && stateTimer <= 9.5f)
                    currentSubtitle = "I have prepared a specific simulation for someone like you!";
                else
                    currentSubtitle = "CHARIZARD I CHOOSE YOU!";

                if (stateTimer > 12.0f && !playedBatmanChoose) {
                    playedBatmanChoose = true;
                    if (batman != null) {
                        float startX = batman.GetXpos() - 0.5f;
                        float startY = batman.GetYpos() + 0.5f;
                        float targetY = batman.GetYpos() - 0.4f;
                        batmanBall = new PokeBall(startX, startY, targetY, false);
                    }
                }

                if (stateTimer > 14.0f) {
                    state = 3;
                    stateTimer = 0;
                    currentSubtitle = "";
                }
                break;

            case 3:
                if (!playedP1) {
                    AudioManager.playDialogue(AudioManager.player_boss1, 1.0f);
                    playedP1 = true;
                }

                if (stateTimer < 6.0f)
                    currentSubtitle = "A Pokemon battle, seriously?\nFine, if this is how we do it...";
                else
                    currentSubtitle = "GO PIKACHU!";

                if (stateTimer > 8.0f && !playedPlayerChoose) {
                    playedPlayerChoose = true;
                    float startX = player.GetXpos() + 0.5f;
                    float startY = player.GetYpos() + 0.5f;
                    float targetY = player.GetYpos() - 0.4f;
                    playerBall = new PokeBall(startX, startY, targetY, true);
                }

                if (stateTimer > 8.0f) {
                    state = 4;
                    stateTimer = 0;
                    currentSubtitle = "";
                }
                break;

            case 4:
                flashAlpha = Math.min(1f, flashAlpha + (dt * 1.5f));
                if (flashAlpha >= 1f) {
                    dispose();
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
            batch.draw(whitePixel, cam.GetCam().position.x - 400, cam.GetCam().position.y - 240, 800, 480);
            batch.setColor(c);
        }
    }

    @Override
    public void skip() {
        if (!isSkippable) return;
        super.skip();
        dispose();
        screen.startPokemonBattle();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
    }
}
