package Game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Graphite Gambit");
        config.setWindowedMode(1920, 1080);
        config.useVsync(true);

        new Lwjgl3Application(new GdxGame(), config);
    }
}