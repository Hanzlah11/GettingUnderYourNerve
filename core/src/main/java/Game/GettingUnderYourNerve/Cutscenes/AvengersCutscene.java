package Game.GettingUnderYourNerve.Cutscenes;

import Game.GettingUnderYourNerve.Enemies.Batman;
import Game.GettingUnderYourNerve.MainGame.PlayScreen;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class AvengersCutscene extends BaseCutscene {

    private Vector2 retreatPos;
    private boolean playedPlayerLine = false;
    private boolean playedBatmanLine = false;

    private Texture blackPixel;
    private float fadeAlpha = 0f;
    private boolean floorDropped = false;

    public AvengersCutscene(PlayScreen screen, Batman batman) {
        super(screen, batman);
        this.isSkippable = false;
        retreatPos = getObjectPos("batman_retreat");

        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(Color.BLACK);
        pix.fill();
        blackPixel = new Texture(pix);
        pix.dispose();
    }

    @Override
    public void update(float dt) {
        stateTimer += dt;

        if (state < 3) {
            cam.GetCam().position.y = player.GetYpos() + 0.5f;
            float targetX = (batman != null) ? (player.GetXpos() + batman.GetXpos()) / 2f : player.GetXpos();
            cam.GetCam().position.x += (targetX - cam.GetCam().position.x) * 0.08f;
        }

        switch (state) {
            case 0:
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

            case 1:
                if (!playedPlayerLine) {
                    AudioManager.playDialogue(AudioManager.ending_player1, 1.0f);
                    playedPlayerLine = true;
                }
                if (stateTimer < 4.5f)
                    currentSubtitle = "It's over batman. Your Charizard is down!";
                else if (stateTimer >= 4.5f && stateTimer <= 8.5f)
                    currentSubtitle = "Now stop this madness and apologize\nfor all the trolls ";
                else
                    currentSubtitle = "and spikes you put me through!";

                if (stateTimer > 10.5f) {
                    state = 2;
                    stateTimer = 0;
                    currentSubtitle = "";
                }
                break;

            case 2:
                if (!playedBatmanLine) {
                    AudioManager.playDialogue(AudioManager.ending_batman1, 1.0f);
                    playedBatmanLine = true;
                }
                if (stateTimer < 3.5f)
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

                if (stateTimer > 21.0f) {
                    state = 3;
                    stateTimer = 0;
                    currentSubtitle = "";
                }
                break;

            case 3:
                if (!floorDropped) {
                    screen.getPlayableMap().dropBossFloor(screen.getWorld());

                    player.getPlayerBody().setAwake(true);
                    player.getPlayerBody().applyLinearImpulse(new Vector2(0, -0.1f), player.getPlayerBody().getWorldCenter(), true);
                    floorDropped = true;
                }

                fadeAlpha = Math.min(1f, fadeAlpha + (dt * 0.8f));

                if (fadeAlpha >= 1f || stateTimer > 2.0f) {
                    finished = true;
                }
                break;
        }

        float halfVW = cam.GetCam().viewportWidth / 2f;
        float mapWidth = screen.getPlayableMap().getMapWidthInMeters();
        cam.GetCam().position.x = Math.max(halfVW, Math.min(cam.GetCam().position.x, mapWidth - halfVW));
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);

        if (fadeAlpha > 0f && blackPixel != null) {
            Color c = batch.getColor().cpy();
            batch.setColor(0f, 0f, 0f, fadeAlpha);

            float vw = cam.GetCam().viewportWidth * cam.GetCam().zoom;
            float vh = cam.GetCam().viewportHeight * cam.GetCam().zoom;
            float drawX = cam.GetCam().position.x - (vw / 2f);
            float drawY = cam.GetCam().position.y - (vh / 2f);

            batch.draw(blackPixel, drawX, drawY, vw, vh);
            batch.setColor(c);
        }
    }

    @Override
    public void skip() {
        if (!isSkippable) return;
        System.out.println("You cannot skip the inevitable.");
    }

    @Override
    public void dispose() {
        super.dispose();
        if (blackPixel != null) {
            blackPixel.dispose();
            blackPixel = null;
        }
    }
}
