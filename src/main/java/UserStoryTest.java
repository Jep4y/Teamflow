import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserStoryTest {

    @Test
    public void testUserStoryAanmaken() {
        UserStory userStory = new UserStory(1, "Inloggen als gebruiker");
        assertEquals(1, userStory.getId());
        assertEquals("Inloggen als gebruiker", userStory.getBeschrijving());
    }

    @Test
    public void testUserStoryBeschrijvingWijzigen() {
        UserStory userStory = new UserStory(2, "Oude beschrijving");
        userStory.setBeschrijving("Nieuwe beschrijving");
        assertEquals("Nieuwe beschrijving", userStory.getBeschrijving());
    }
}
