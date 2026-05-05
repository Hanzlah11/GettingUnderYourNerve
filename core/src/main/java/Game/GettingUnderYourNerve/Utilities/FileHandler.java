//FileHandler.java
package Game.GettingUnderYourNerve.Utilities;

import Game.GettingUnderYourNerve.Map.PlayableMap;
import Game.GettingUnderYourNerve.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Collections;

public class FileHandler extends InputAdapter {
    private Json json;

    private boolean isSavingMode = false;
    private boolean isLoadingMode = false;
    private StringBuilder typedName;

    private Player currentPlayerRef;
    private PlayableMap currentMapRef;

    public static class GameSaveData {
        public float x;
        public float y;
        public int score;
        public int health;
        public boolean isJumping;
        public ArrayList<String> collectedCoins = new ArrayList<>();
        public ArrayList<String> collectedPotions = new ArrayList<>();
    }

    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public String name;
        public int score;

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public int compareTo(ScoreEntry o) {
            return Integer.compare(o.score, this.score);
        }
    }

    public FileHandler() {
        json = new Json();
        typedName = new StringBuilder();
        Gdx.input.setInputProcessor(this);
    }

    public void saveGameState(Player player, PlayableMap map, String fileName) {
        GameSaveData data = new GameSaveData();
        data.x = player.GetXpos();
        data.y = player.GetYpos();
        data.score = player.getScore();
        data.health = player.getHealth();
        data.isJumping = !player.isGrounded && player.getPlayerBody().getLinearVelocity().y > 0;
        data.collectedCoins = map.collectedCoinIds;
        data.collectedPotions = map.collectedPotionIds;

        String saveString = json.toJson(data);
        FileHandle file = Gdx.files.local(fileName);
        file.writeString(saveString, false);
        System.out.println("Game Saved Successfully to " + fileName + "!");
    }

    public void loadGameState(Player player, PlayableMap map, String fileName) {
        FileHandle file = Gdx.files.local(fileName);

        if (file.exists()) {
            String saveString = file.readString();
            GameSaveData data = json.fromJson(GameSaveData.class, saveString);

            player.OverridePos(data.x, data.y);
            player.OverrideScore(data.score);
            player.OverrideHealth(data.health);

            if (data.isJumping) {
                player.ApplyJump();
            }

            map.applyLoadedCollectables(data.collectedCoins, data.collectedPotions);
            System.out.println("Game Loaded Successfully from " + fileName + "!");
        } else {
            System.out.println("No save file found with name: " + fileName);
        }
    }

    public void GetFilInput(Player player, PlayableMap map) {
        this.currentPlayerRef = player;
        this.currentMapRef = map;

        if (isSavingMode || isLoadingMode) return;

        boolean isCtrlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

        if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            isSavingMode = true;
            isLoadingMode = false;
            typedName.setLength(0);
            System.out.println("\n--- SAVE MODE ACTIVATED ---");
        }

        if (isCtrlPressed && Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            isLoadingMode = true;
            isSavingMode = false;
            typedName.setLength(0);
            System.out.println("\n--- LOAD MODE ACTIVATED ---");
        }
    }

    public static ArrayList<ScoreEntry> getTopScores() {
        ArrayList<ScoreEntry> allScores = new ArrayList<>();
        FileHandle dir = Gdx.files.local("SavedFiles");
        Json jsonParser = new Json();

        if (dir.exists() && dir.isDirectory()) {
            for (FileHandle file : dir.list(".json")) {
                try {
                    GameSaveData data = jsonParser.fromJson(GameSaveData.class, file.readString());
                    allScores.add(new ScoreEntry(file.nameWithoutExtension(), data.score));
                } catch (Exception ignored) {}
            }
        }

        Collections.sort(allScores);
        ArrayList<ScoreEntry> top10 = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            if (i < allScores.size()) {
                top10.add(allScores.get(i));
            } else {
                top10.add(new ScoreEntry("--", -1));
            }
        }
        return top10;
    }

    @Override
    public boolean keyTyped(char character) {
        if (isSavingMode || isLoadingMode) {
            if (character == '\b' && typedName.length() > 0) {
                typedName.setLength(typedName.length() - 1);
                System.out.println("Current Name: " + typedName.toString());
            } else if (character == '\r' || character == '\n') {
                if (typedName.length() > 0) {
                    String fileName = "SavedFiles/" + typedName.toString().trim() + ".json";
                    if (isSavingMode) {
                        saveGameState(currentPlayerRef, currentMapRef, fileName);
                    } else if (isLoadingMode) {
                        loadGameState(currentPlayerRef, currentMapRef, fileName);
                    }
                }
                isSavingMode = false;
                isLoadingMode = false;
                typedName.setLength(0);
            } else if (Character.isLetterOrDigit(character) || character == ' ' || character == '_') {
                typedName.append(character);
                System.out.println("Current Name: " + typedName.toString());
            }
            return true;
        }
        return false;
    }
}
