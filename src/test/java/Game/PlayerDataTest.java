package Game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlayerDataTest {
    PlayerData data;

    @BeforeAll
    static void testMode(){
        PlayerData.setTestMode();
    }

    @BeforeEach
    void init(){
        PlayerData.reset(false);
        data = PlayerData.obtainPlayerData();
    }

    @Test
    void testSetLevel() {
        data.setLevel(4);
        assertEquals(4, data.getLevelUnlocked());
    }

    @Test
    void testSetHighScore() {
        data.setHighScore(3, 750);
        assertEquals(750, data.getScore(3));
    }

    @Test
    void testCompleteUnlocksNext() {
        data.completeLevel(1, 500);
        assertEquals(2, data.getLevelUnlocked());
        assertEquals(500, data.getScore(1));
    }

    @Test
    void testCompleteImprovesHighscore() {
        data.setLevel(2); // already unlocked level 1
        data.setHighScore(1, 200);

        data.completeLevel(1, 300);

        assertEquals(2, data.getLevelUnlocked());
        assertEquals(300, data.getScore(1));
    }

    @Test
    void testCompleteKeepsHighscore() {
        data.setLevel(2); // already unlocked level 1
        data.setHighScore(1, 400);

        data.completeLevel(1, 350);

        assertEquals(2, data.getLevelUnlocked());
        assertEquals(400, data.getScore(1));
    }
}
