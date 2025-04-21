import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class BerichtTest {

    @Test
    public void testBerichtZonderKoppeling() {
        Gebruiker gebruiker = new Gebruiker(1, "Jeppe", "");
        Bericht bericht = new Bericht(1, "Hallo wereld", LocalDateTime.now(), gebruiker);

        assertEquals("Geen gekoppelde objecten", bericht.getGekoppeldObject());
    }

    @Test
    public void testBerichtMetUserStory() {
        Gebruiker gebruiker = new Gebruiker(2, "Lisa", "");
        Bericht bericht = new Bericht(2, "Planning af?", LocalDateTime.now(), gebruiker);
        bericht.koppelUserStory(new UserStory(1, "Sprint planning"));

        assertTrue(bericht.getGekoppeldObject().contains("UserStory"));
    }
}
