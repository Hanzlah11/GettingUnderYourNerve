package Game.GettingUnderYourNerve.Utilities;

import Game.GettingUnderYourNerve.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class FileHandler {
    private static final String SAVE_FILE = "savegame.json";
    private Json json;
    public static class PlayerSaveData {
        public float x;
        public float y;
        public int score;
        public int health;
        public boolean isJumping;
    }
    public FileHandler() {
        json = new Json();
    }
    public void savePlayerState(Player player) {
        PlayerSaveData data = new PlayerSaveData();
        data.x = player.GetXpos();
        data.y = player.GetYpos();
        data.score = player.getScore();
        data.health = player.getHealth();
        data.isJumping = !player.isGrounded && player.getPlayerBody().getLinearVelocity().y > 0;

        String saveString = json.toJson(data);
        FileHandle file = Gdx.files.local(SAVE_FILE);
        file.writeString(saveString, false);

        System.out.println("Game Saved Successfully!");
        System.out.println("Data: " + saveString);
    }

    public void loadPlayerState(Player player) {
        FileHandle file = Gdx.files.local(SAVE_FILE);

        if (file.exists()) {
            String saveString = file.readString();

            // Read JSON back into our SaveData format
            PlayerSaveData data = json.fromJson(PlayerSaveData.class, saveString);

            // 1. Restore Position
            player.OverridePos(data.x, data.y);

            // 2. Restore Points & Health
            player.OverrideScore(data.score);
            player.OverrideHealth(data.health);

            // 3. Restore Jump State
            if (data.isJumping) {
                player.ApplyJump();
            }

            System.out.println("Game Loaded Successfully!");
            System.out.println("Loaded - Score: " + data.score + ", Health: " + data.health + ", Was Jumping: " + data.isJumping);
        } else {
            System.out.println("No save file found! Creating a new save next time you press Ctrl+S.");
        }
    }

    public void GetFilInput(Player player){
        boolean isCtrlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

        if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            savePlayerState(player);
        }
        if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.L)) { // Changed to L
            loadPlayerState(player);
        }
    }
}
