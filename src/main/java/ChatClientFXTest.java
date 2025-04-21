import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

public class ChatClientFXTest extends ApplicationTest {

    private ChatClientFX chatClient;

    @Override
    public void start(Stage stage) throws Exception {
        chatClient = new ChatClientFX();
        chatClient.start(stage);
    }

    @Test
    public void testGUIComponentsLoaded() {
        TextField messageField = lookup("#messageField").query();
        Button sendButton = lookup("#sendButton").query();
        ListView messageList = lookup("#messageList").query();

        assertNotNull(messageField);
        assertNotNull(sendButton);
        assertNotNull(messageList);
    }

    @Test
    public void testBerichtVerzenden() {
        clickOn("#messageField").write("Testbericht");
        clickOn("#sendButton");

        ListView<String> messageList = lookup("#messageList").query();
        assertTrue(messageList.getItems().stream().anyMatch(item -> item.contains("Testbericht")));
    }
}
