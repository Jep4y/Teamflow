import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    public void testEpicAanmaken() {
        Epic epic = new Epic(1, "Release 1.0");
        assertEquals(1, epic.getId());
        assertEquals("Release 1.0", epic.getBeschrijving());
    }

    @Test
    public void testEpicBeschrijvingAanpassen() {
        Epic epic = new Epic(2, "Oude epic");
        epic.setBeschrijving("Nieuwe epic");
        assertEquals("Nieuwe epic", epic.getBeschrijving());
    }
}
