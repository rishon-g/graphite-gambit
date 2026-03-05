package Game;

import com.badlogic.gdx.Screen;

import java.util.Stack;

/**
 * ScreenManager manages the stack of overlayed screens
 * @author Luke McRae
 * @version 1.0
 */
public class ScreenManager {
    private final Stack<Screen> screenStack = new Stack<>();

    /**
     * Pushes a new screen to stack, overlaying it over the current screen. (for pause menu, etc)
     * @param screen the screen to push, most recent push will be most visible
     */
    public void push(Screen screen) {
        screenStack.push(screen);
        screen.show();
    }

    /**
     * Removes the topmost screen from the stack and disposes it, such as unpausing
     */
    public void pop() {
        if (!screenStack.empty()) {
            screenStack.peek().hide();
            screenStack.pop().dispose();
        }
    }

    /**
     * Clears entire screen stack and replaces with new screen
     * @param screen
     */
    public void switchScreen(Screen screen) {
        while (!screenStack.empty()) {
            pop();
        }
        push(screen);
    }

    public void clear() {
        while (!screenStack.empty()) {
            pop();
        }
    }

    // util info methods
    public Screen getCurrentScreen() {
        return screenStack.peek();
    }

    public boolean isEmpty() {
        return screenStack.empty();
    }

    public int getSize() {
        return screenStack.size();
    }

    public void render(float delta) {
        for (Screen screen : new java.util.ArrayList<>(screenStack)) {
            screen.render(delta);
        }
    }

    public void resize(int width, int height) {
        for (Screen screen : new java.util.ArrayList<>(screenStack)) {
            screen.resize(width, height);
        }
    }

    public void dispose() {
        for (Screen screen : new java.util.ArrayList<>(screenStack)) {
            screen.dispose();
        }
    }
}