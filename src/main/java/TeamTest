import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TeamTest {

    @Test
    public void testTeamAanmaken() {
        Team team = new Team(1, "Marketing", "Test beschrijving");
        assertEquals(1, team.getId());
        assertEquals("Marketing", team.getNaam());
        assertEquals("Test beschrijving", team.getOnderwerp());
    }

    @Test
    public void testTeamNaamWijzigen() {
        Team team = new Team(2, "Design", "Originele beschrijving");
        team.setNaam("Nieuw Design");
        assertEquals("Nieuw Design", team.getNaam());
    }
}
