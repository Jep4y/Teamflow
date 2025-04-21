import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GebruikerTest {

    @Test
    public void testGebruikerAanmaken() {
        Team team = new Team(1, "Algemeen", "Testteam");
        Gebruiker gebruiker = new Gebruiker(1, "Jeppe", "wachtwoord123");
        gebruiker.setTeam(team);
        assertEquals("Jeppe", gebruiker.getNaam());
        assertEquals(team, gebruiker.getTeam());
    }
}
