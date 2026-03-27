package Game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import Entities.Player;

import com.badlogic.gdx.files.FileHandle;

/**
 * The PlayerData class is responsible for holding the progression of the player.
 * it is also responsible for the maintaining of the JSON representation of the data, and loading it upon startup.
 * 
 * @author Lane Jacobson
 * @version 1.0
 */
public class PlayerData {
    private int levelUnlocked;
    private ArrayList<Integer> highScores;

    private static PlayerData instance;

    public PlayerData() {
        levelUnlocked = 1;
        highScores = new ArrayList<>();
    }

    /**
     * Resets the player save data. 
     * @param save true if the save should be reflected on file immediately.
     */
    public static void reset(boolean save){
        instance = new PlayerData();
        if(save)
            saveToFile();
    }

    /**
     * sets the current levels unlocked.
     * @param level the level to set to
     */
    public void setLevel(int level){
        levelUnlocked = level;
    }

    /**
     * sets the highscore of a certain level.
     * @param level the level to set
     * @param score the new highscore to set
     */
    public void setHighScore(int level, int score){
        while(highScores.size() < level){
            highScores.add(0);
        }
        highScores.set(level-1, score);
    }

    /**
     * Resets the global save data
     */
    public static void resetData(){
        instance = new PlayerData();
        saveToFile();
    }

    /**
     * singleton method to initialize/obtain shared player save data.
     * @return the save data of the player
     */
    public static PlayerData obtainPlayerData(){
        if(instance == null){
            loadFromFile();
        }
        return instance;
    }

    /**
     * loads the data from file into the player data instance.
     * called internally upon first request of the save data.
     */
    private static void loadFromFile(){
        Json json = new Json();
        String path = "./Save/save.json";

        FileHandle file = Gdx.files.local(path);
        if(!file.exists()){
            instance = new PlayerData();
            saveToFile();
            return;
        }else{
            instance = json.fromJson(PlayerData.class, file.readString());
        }
    }

    /**
     * stores/updates the save file of the player.
     * called internally when save data is updated.
     */
    private static void saveToFile(){
        Json json = new Json();
        String path = "./Save/save.json";

        FileHandle file = Gdx.files.local(path);

        String jsonString = json.prettyPrint(instance);

        file.writeString(jsonString, false);
    }

    /**
     * Update level information when a level is completed
     * @param level the level cleared
     * @param score the score obtained
     */
    public void completeLevel(int level, int score) {
        // newly beat level
		if(level == levelUnlocked){
			if(highScores.size() < level){
				highScores.add(0);
			}
			highScores.set(level-1, score);
			levelUnlocked++;
            saveToFile();
		}

        // beat old level again
		else if(score > highScores.get(level-1)){
            highScores.set(level-1, score);
            saveToFile();
        }
    }

    /**
     * returns the current highest level that is unlocked.
     * @return the highest level unlocked
     */
    public int getLevelUnlocked() {
        return levelUnlocked;
    }

    /**
     * returns the highscore for a level
     * @param level the level of the game
     * @return the highscore as an integer
    */
    public int getScore(int level) {
        return highScores.get(level-1);
    }
}
