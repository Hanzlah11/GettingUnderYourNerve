package Game.GettingUnderYourNerve.Utilities;

import java.io.*;
import java.util.ArrayList;

public class FileHandler {

    // ==========================================
    //          4-SLOT SAVE SYSTEM
    // ==========================================

    // Returns an array [Name, X, Y] or null if the slot is empty
    public static String[] getSlotInfo(int slotIndex) {
        File file = new File("save_slot_" + slotIndex + ".txt");
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    return line.split(",");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Saves the player data into the specified slot
    public static void saveSlot(int slotIndex, String name, float x, float y, int level) {
        try (FileWriter fw = new FileWriter("save_slot_" + slotIndex + ".txt")) {
            fw.write(name + "," + x + "," + y + "," + level);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Deletes the slot file when the user right-clicks
    public static void deleteSlot(int slotIndex) {
        File file = new File("save_slot_" + slotIndex + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    // ==========================================
    //          LEADERBOARD SYSTEM
    // ==========================================

    public static class ScoreEntry {
        public String name;
        public int score;

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    // Retrieves the top 10 scores from the leaderboard file
    public static ArrayList<ScoreEntry> getTopScores() {
        ArrayList<ScoreEntry> scores = new ArrayList<>();
        File file = new File("leaderboard.txt");

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        try {
                            scores.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1])));
                        } catch (NumberFormatException e) {
                            System.out.println("Skipping corrupted score entry: " + line);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Sort descending (highest score first)
        scores.sort((s1, s2) -> Integer.compare(s2.score, s1.score));

        // Pad the list with blank entries (-1) up to 10 so the UI always has 10 lines
        while (scores.size() < 10) {
            scores.add(new ScoreEntry("---", -1));
        }

        // Return strictly the top 10
        return new ArrayList<>(scores.subList(0, 10));
    }

    // Saves a new score and automatically truncates the file to the top 10
    public static void saveScore(String name, int score) {
        ArrayList<ScoreEntry> scores = getTopScores();

        // Remove the blank padding entries before adding the new one
        scores.removeIf(entry -> entry.score == -1);

        scores.add(new ScoreEntry(name, score));

        // Re-sort with the new score included
        scores.sort((s1, s2) -> Integer.compare(s2.score, s1.score));

        try (FileWriter fw = new FileWriter("leaderboard.txt")) {
            // Only write the top 10 back to the file
            for (int i = 0; i < Math.min(10, scores.size()); i++) {
                fw.write(scores.get(i).name + "," + scores.get(i).score + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
