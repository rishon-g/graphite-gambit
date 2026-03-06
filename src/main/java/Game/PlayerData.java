package Game;

import java.util.ArrayList;

public class PlayerData {
    private final ArrayList<Integer> highScores;
    public PlayerData() {
        highScores = new ArrayList<>();
    }

    /**
     * Update level information when a level is completed
     * @param id
     * @param score
     */
    public void completeLevel(int id, int score) {
        if (highScores.get(id) == null || highScores.get(id) < score) {
            highScores.set(id, score);
        }
    }

    public int getScore(int id) {
        return highScores.get(id);
    }

    // count number of completed levels based on number of high scores
    public int numCompletedLevels() {
        int count = 0;
        for (Integer highScore : highScores) {
            // technically, it should be size but might as well be safe
            if (highScore != null) {
                count++;
            }
        }
        return count;
    }

    public int numAvailableLevels() {
        return numCompletedLevels() + 1;
    }
}
