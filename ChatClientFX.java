import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ChatClientFX extends Application {

    private PrintWriter output;
    private TextArea chatArea;
    private TextField inputField;

    private static final String SERVER = "localhost";
    private static final int PORT = 12345;

    @Override
    public void start(Stage primaryStage) {
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        inputField = new TextField();
        inputField.setPromptText("Typ je bericht hier...");

        Button sendButton = new Button("Verstuur");

        HBox inputBox = new HBox(10, inputField, sendButton);
        inputBox.setPadding(new Insets(10));
        inputBox.setHgrow(inputField, Priority.ALWAYS);

        VBox root = new VBox(10, chatArea, inputBox);
        root.setPadding(new Insets(10));

        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setTitle("Teamflow Chat");
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                Socket socket = new Socket(SERVER, PORT);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

                String message;
                while ((message = input.readLine()) != null) {
                    String finalMessage = message;
                    javafx.application.Platform.runLater(() -> chatArea.appendText(finalMessage + "\n"));
                }
            } catch (IOException e) {
                javafx.application.Platform.runLater(() -> chatArea.appendText("❌ Verbinding met server mislukt\n"));
            }
        }).start();
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            output.println(message);
            inputField.clear();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
