package Game;

import com.badlogic.gdx.utils.Json;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks the bundled level files, since WorldLoader silently skips entity types it doesn't know.
 */
class LevelDataTest {

    private static final Set<String> KNOWN_TYPES = Set.of("Player", "Eraser", "Node", "PencilSharpener");

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void testLevelHasOnePlayerAndOnlyKnownEntityTypes(int level) throws IOException {
        String text = Files.readString(Path.of("src/main/java/Worlds/level" + level + ".json"));
        LevelData data = new Json().fromJson(LevelData.class, text);

        int players = 0;
        for (EntityData entity : data.entities) {
            assertTrue(KNOWN_TYPES.contains(entity.type), "unknown entity type " + entity.type + " in level " + level);
            if (entity.type.equals("Player")) {
                players++;
            }
        }
        assertEquals(1, players, "level " + level + " should spawn exactly one player");
    }
}
