package Game.GettingUnderYourNerve.Utilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.BufferedReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class FileHandler {

    public static String[] getSlotInfo(int slotIndex) {
        FileHandle file = Gdx.files.local("save_slot_" + slotIndex + ".txt");
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(file.reader())) {
                String line = br.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    return line.split(",");
                }
            } catch (Exception e) {
                Gdx.app.error("FileHandler", "Error reading slot " + slotIndex, e);
            }
        }
        return null;
    }

    public static void saveSlot(int slotIndex, String name, float x, float y, int level) {
        FileHandle file = Gdx.files.local("save_slot_" + slotIndex + ".txt");
        try (Writer writer = file.writer(false)) {
            writer.write(name + "," + x + "," + y + "," + level);
        } catch (Exception e) {
            Gdx.app.error("FileHandler", "Error saving slot " + slotIndex, e);
        }
    }

    public static void deleteSlot(int slotIndex) {
        FileHandle file = Gdx.files.local("save_slot_" + slotIndex + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    public static class ScoreEntry {
        public String name;
        public int score;

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    public static ArrayList<ScoreEntry> getTopScores() {
        ArrayList<ScoreEntry> scores = new ArrayList<>();
        FileHandle file = Gdx.files.local("leaderboard.txt");

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(file.reader())) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        try {
                            scores.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1])));
                        } catch (NumberFormatException e) {
                            Gdx.app.log("FileHandler", "Corrupted score line: " + line);
                        }
                    }
                }
            } catch (Exception e) {
                Gdx.app.error("FileHandler", "Error loading leaderboard", e);
            }
        }

        Collections.sort(scores, new Comparator<ScoreEntry>() {
            @Override
            public int compare(ScoreEntry s1, ScoreEntry s2) {
                return Integer.compare(s2.score, s1.score);
            }
        });

        while (scores.size() < 10) {
            scores.add(new ScoreEntry("---", -1));
        }

        return new ArrayList<>(scores.subList(0, 10));
    }

    public static void saveScore(String name, int score) {
        ArrayList<ScoreEntry> scores = getTopScores();

        Iterator<ScoreEntry> iterator = scores.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().score == -1) {
                iterator.remove();
            }
        }

        scores.add(new ScoreEntry(name, score));

        Collections.sort(scores, new Comparator<ScoreEntry>() {
            @Override
            public int compare(ScoreEntry s1, ScoreEntry s2) {
                return Integer.compare(s2.score, s1.score);
            }
        });

        FileHandle file = Gdx.files.local("leaderboard.txt");
        try (Writer writer = file.writer(false)) {
            for (int i = 0; i < Math.min(10, scores.size()); i++) {
                writer.write(scores.get(i).name + "," + scores.get(i).score + "\n");
            }
        } catch (Exception e) {
            Gdx.app.error("FileHandler", "Error saving leaderboard", e);
        }
    }

    public static void resetTopScores() {
        FileHandle file = Gdx.files.local("leaderboard.txt");
        if (file.exists()) {
            file.delete();
        }
    }
}
