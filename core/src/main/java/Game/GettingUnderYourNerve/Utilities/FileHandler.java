package Game.GettingUnderYourNerve.Utilities;

import Game.GettingUnderYourNerve.Map.PlayableMap;
import Game.GettingUnderYourNerve.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Collections;

public class FileHandler {
    private Json json;

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
            return Integer.compare(o.score, this.score); // Descending order
        }
    }

    public FileHandler() {
        json = new Json();
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
        file.writeString(saveString, false); // false = overwrite existing file

        System.out.println("--- GAME SAVED SUCCESSFULLY TO: " + fileName + " ---");
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
            System.out.println("--- GAME LOADED SUCCESSFULLY FROM: " + fileName + " ---");
        } else {
            System.out.println("--- NO SAVE FILE FOUND FOR: " + fileName + " ---");
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
}
