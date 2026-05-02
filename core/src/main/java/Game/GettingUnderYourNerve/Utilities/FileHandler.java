package Game.GettingUnderYourNerve.Utilities;

import Game.GettingUnderYourNerve.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class FileHandler extends InputAdapter {
    private Json json;

    // Typing mode variables
    private boolean isSavingMode = false;
    private boolean isLoadingMode = false;
    private StringBuilder typedName;
    private Player currentPlayerRef; // Caches the player so we can use it when Enter is pressed

    public static class PlayerSaveData {
        public float x;
        public float y;
        public int score;
        public int health;
        public boolean isJumping;
    }

    public FileHandler() {
        json = new Json();
        typedName = new StringBuilder();

        // IMPORTANT: We set this class as the InputProcessor.
        // This allows it to capture raw keystrokes (typing) without any UI!
        Gdx.input.setInputProcessor(this);
    }

    public void savePlayerState(Player player, String fileName) {
        PlayerSaveData data = new PlayerSaveData();
        data.x = player.GetXpos();
        data.y = player.GetYpos();
        data.score = player.getScore();
        data.health = player.getHealth();
        data.isJumping = !player.isGrounded && player.getPlayerBody().getLinearVelocity().y > 0;

        String saveString = json.toJson(data);
        FileHandle file = Gdx.files.local(fileName);

        // Passing 'false' means overwrite if the file already exists
        file.writeString(saveString, false);

        System.out.println("Game Saved Successfully to " + fileName + "!");
    }

    public void loadPlayerState(Player player, String fileName) {
        FileHandle file = Gdx.files.local(fileName);

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

            System.out.println("Game Loaded Successfully from " + fileName + "!");
        } else {
            System.out.println("No save file found with name: " + fileName);
        }
    }

    public void GetFilInput(Player player) {
        this.currentPlayerRef = player; // keep reference updated

        // If we are currently typing a name, ignore new trigger commands
        if (isSavingMode || isLoadingMode) {
            return;
        }

        boolean isCtrlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

        // --- CTRL + N: Enter SAVE MODE ---
        if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            isSavingMode = true;
            isLoadingMode = false;
            typedName.setLength(0);
            System.out.println("\n--- SAVE MODE ACTIVATED ---");
            System.out.println("Type your save name and press ENTER. (Check IDE console)");
        }

        // --- CTRL + L: Enter LOAD MODE ---
        if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            isLoadingMode = true;
            isSavingMode = false;
            typedName.setLength(0);
            System.out.println("\n--- LOAD MODE ACTIVATED ---");
            System.out.println("Type your save name to load and press ENTER. (Check IDE console)");
        }
    }

    // -------------------------------------------------------------------
    // InputAdapter Override: This captures actual keyboard characters!
    // -------------------------------------------------------------------
    @Override
    public boolean keyTyped(char character) {
        if (isSavingMode || isLoadingMode) {

            // 1. Handle Backspace (Delete character)
            if (character == '\b' && typedName.length() > 0) {
                typedName.setLength(typedName.length() - 1);
                System.out.println("Current Name: " + typedName.toString());
            }

            // 2. Handle Enter (Finalize and Save/Load)
            else if (character == '\r' || character == '\n') {
                if (typedName.length() > 0) {
                    String fileName = typedName.toString().trim() + ".json";

                    if (isSavingMode) {
                        savePlayerState(currentPlayerRef, fileName);
                    } else if (isLoadingMode) {
                        loadPlayerState(currentPlayerRef, fileName);
                    }
                } else {
                    System.out.println("Action Cancelled: No name entered.");
                }

                // Exit typing mode regardless of outcome
                isSavingMode = false;
                isLoadingMode = false;
                typedName.setLength(0);
            }

            // 3. Handle Letters, Numbers, Spaces, and Underscores
            else if (Character.isLetterOrDigit(character) || character == ' ' || character == '_') {
                typedName.append(character);
                System.out.println("Current Name: " + typedName.toString());
            }

            return true; // Tell LibGDX this input was handled here
        }

        return false;
    }
}
