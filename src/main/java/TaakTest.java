import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TaakTest {

    @Test
    public void testTaakAanmaken() {
        Taak taak = new Taak(1, "Database ontwerpen");
        assertEquals(1, taak.getId());
        assertEquals("Database ontwerpen", taak.getBeschrijving());
    }

    @Test
    public void testTaakBeschrijvingAanpassen() {
        Taak taak = new Taak(2, "Oude taak");
        taak.setBeschrijving("Nieuwe taak");
        assertEquals("Nieuwe taak", taak.getBeschrijving());
    }
}
