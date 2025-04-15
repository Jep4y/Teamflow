// Volledige aangepaste code met ArrayList in plaats van List

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ChatClientFX extends Application {

    private PrintWriter output;
    private TextArea chatArea;
    private TextField inputField;
    private String serverAddress = ""; // Server address
    private Gebruiker gebruiker; // De huidige gebruiker
    private Team team; // Het team van de gebruiker
    private Label chatInfoLabel; // Label om huidige chat-informatie weer te geven
    private static final int PORT = 12345;
    private Stage primaryStage; // Hoofdvenster
    private Bericht actiefBericht;

    // Statische lijsten met teams en onderwerpen
    private static final ArrayList<Team> teams = new ArrayList<>();
    private static final ArrayList<String> onderwerpen = new ArrayList<>();

    static {
        Team development = new Team(1, "Development");
        development.voegOnderwerpToe("Bug Fixing");
        development.voegOnderwerpToe("Project Planning");
        development.voegOnderwerpToe("Daily Scrum");

        Team marketing = new Team(2, "Marketing");
        marketing.voegOnderwerpToe("Ad Campaign");
        marketing.voegOnderwerpToe("Daily Scrum");

        Team design = new Team(3, "Design");
        design.voegOnderwerpToe("UX Research");
        design.voegOnderwerpToe("Mockup Design");

        teams.add(development);
        teams.add(marketing);
        teams.add(design);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        if (serverAddress.isEmpty() || gebruiker == null) {
            setupInitialInfo();
        }

        setupChat();
    }

    private void setupInitialInfo() {
        serverAddress = promptServerAddress();
        String gebruikersnaam = promptGebruikersnaam();
        gebruiker = new Gebruiker(1, gebruikersnaam, "");
    }

    private void setupChat() {
        team = promptSelectTeam();
        String chatOnderwerp = promptSelectOnderwerp();

        Chat chat = new Chat(1, chatOnderwerp);
        team.setChat(chat);
        gebruiker.setTeam(team);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        VBox.setVgrow(chatArea, Priority.ALWAYS);
        chatArea.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-font-size: 14px;");

        inputField = new TextField();
        inputField.setPromptText("Typ je bericht hier...");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #cccccc; -fx-font-size: 14px;");

        Button sendButton = new Button("Verstuur");
        sendButton.setPrefWidth(100);
        sendButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        sendButton.setOnMouseEntered(e -> sendButton.setStyle("-fx-background-color: #0056b3; -fx-text-fill: white; -fx-font-weight: bold;"));
        sendButton.setOnMouseExited(e -> sendButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;"));

        Button backButton = new Button("Terug");
        backButton.setPrefWidth(100);
        backButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold;");
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: #565e64; -fx-text-fill: white; -fx-font-weight: bold;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold;"));
        backButton.setOnAction(e -> returnToTeamSelection());

        Button koppelenButton = new Button("Koppel");
        koppelenButton.setPrefWidth(120);
        koppelenButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        koppelenButton.setOnMouseEntered(e -> koppelenButton.setStyle("-fx-background-color: #218838; -fx-text-fill: white; -fx-font-weight: bold;"));
        koppelenButton.setOnMouseExited(e -> koppelenButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;"));
        koppelenButton.setOnAction(e -> koppelBericht());

        chatInfoLabel = new Label("Team: " + team.getNaam() + " | Onderwerp: " + chat.getOnderwerp());
        chatInfoLabel.setStyle("-fx-background-color: #e9ecef; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10px;");
        VBox.setMargin(chatInfoLabel, new Insets(10, 0, 10, 0));

        HBox inputBox = new HBox(10, inputField, sendButton, koppelenButton, backButton);
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-background-color: #f8f9fa;");

        VBox root = new VBox(10, chatInfoLabel, chatArea, inputBox);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #d3d3d3;");

        root.prefWidthProperty().bind(primaryStage.widthProperty());
        root.prefHeightProperty().bind(primaryStage.heightProperty());

        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Chat Team: " + team.getNaam() + " - Onderwerp: " + chat.getOnderwerp());
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
    }

    private void koppelBericht() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Leeg bericht");
            alert.setHeaderText(null);
            alert.setContentText("Voer eerst een bericht in voordat je iets koppelt.");
            alert.showAndWait();
            return;
        }

        actiefBericht = new Bericht(1, message, LocalDateTime.now(), gebruiker);

        ChoiceDialog<String> dialog = new ChoiceDialog<>("UserStory", "UserStory", "Taak", "Epic");
        dialog.setTitle("Koppel bericht");
        dialog.setHeaderText("Koppel dit bericht aan een UserStory, Taak of Epic.");
        dialog.setContentText("Selecteer:");

        String keuze = dialog.showAndWait().orElse(null);
        if (keuze == null) return;

        switch (keuze) {
            case "UserStory" -> actiefBericht.koppelUserStory(new UserStory(1, "Voorbeeld UserStory"));
            case "Taak" -> actiefBericht.koppelTaak(new Taak(1, "Voorbeeld Taak"));
            case "Epic" -> actiefBericht.koppelEpic(new Epic(1, "Voorbeeld Epic"));
        }

        Alert bevestiging = new Alert(Alert.AlertType.INFORMATION);
        bevestiging.setTitle("Gekoppeld");
        bevestiging.setHeaderText(null);
        bevestiging.setContentText("Het bericht is gekoppeld aan: " + actiefBericht.getGekoppeldObject());
        bevestiging.showAndWait();
    }

    private void returnToTeamSelection() {
        output.close();
        setupChat();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                Socket socket = new Socket(serverAddress, PORT);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

                String message;
                while ((message = input.readLine()) != null) {
                    String finalMessage = message;
                    javafx.application.Platform.runLater(() -> chatArea.appendText(finalMessage + "\n"));
                }
            } catch (IOException e) {
                javafx.application.Platform.runLater(() -> chatArea.appendText("\u274C Failed to connect to the server\n"));
            }
        }).start();
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            if (actiefBericht == null) {
                actiefBericht = new Bericht(1, message, LocalDateTime.now(), gebruiker);
            }

            if (actiefBericht.getGekoppeldObject().equals("Geen gekoppelde objecten")) {
                output.println(gebruiker.getNaam() + " (" + team.getNaam() + "): " + actiefBericht.getInhoud());
            } else {
                output.println(gebruiker.getNaam() + " (" + team.getNaam() + "): " + actiefBericht.getInhoud() + " [" + actiefBericht.getGekoppeldObject() + "]");
            }

            actiefBericht = null;
            inputField.clear();
        }
    }

    private String promptServerAddress() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Verbind naar een server / host");
        dialog.setHeaderText("Geef het IP-adres van de server op:");
        dialog.setContentText("Server:");
        return dialog.showAndWait().orElse("");
    }

    private String promptGebruikersnaam() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Gebruikersnaam");
        dialog.setHeaderText("Voer je gebruikersnaam in:");
        dialog.setContentText("Gebruikersnaam:");
        return dialog.showAndWait().orElseThrow();
    }

    private Team promptSelectTeam() {
        ChoiceDialog<Team> dialog = new ChoiceDialog<>(teams.get(0), teams);
        dialog.setTitle("Team Selectie");
        dialog.setHeaderText("Kies een bestaand team:");
        dialog.setContentText("Team:");
        return dialog.showAndWait().orElseThrow();
    }

    private String promptSelectOnderwerp() {
        ArrayList<String> teamOnderwerpen = team.getOnderwerpen();
        ChoiceDialog<String> dialog = new ChoiceDialog<>(teamOnderwerpen.get(0), teamOnderwerpen);
        dialog.setTitle("Chat Onderwerp");
        dialog.setHeaderText("Kies een bestaand onderwerp voor het team: " + team.getNaam());
        dialog.setContentText("Onderwerp:");
        return dialog.showAndWait().orElseThrow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// andere classe

class Team {
    private int id;
    private String naam;
    private ArrayList<String> onderwerpen;
    private Chat chat;

    public Team(int id, String naam) {
        this.id = id;
        this.naam = naam;
        this.onderwerpen = new ArrayList<>();
    }

    public String getNaam() {
        return naam;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Chat getChat() {
        return chat;
    }

    public ArrayList<String> getOnderwerpen() {
        return onderwerpen;
    }

    public void voegOnderwerpToe(String onderwerp) {
        this.onderwerpen.add(onderwerp);
    }

    @Override
    public String toString() {
        return naam;
    }
} // Gebruiker, Chat, UserStory, Epic, Taak en Bericht kun je ongewijzigd laten behalve als je daar ook expliciet ArrayList wilt afdwingen.

class Gebruiker {
    private int id;
    private String naam;
    private String email;
    private Team team;

    public Gebruiker(int id, String naam, String email) {
        this.id = id;
        this.naam = naam;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getEmail() {
        return email;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}

class Chat {
    private int id;
    private String onderwerp;

    public Chat(int id, String onderwerp) {
        this.id = id;
        this.onderwerp = onderwerp;
    }

    public String getOnderwerp() {
        return onderwerp;
    }
}
class UserStory {
    private int id;
    private String naam;

    public UserStory(int id, String naam) {
        this.id = id;
        this.naam = naam;
    }

    public int getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }
}
class Epic {
    private int id;
    private String naam;

    public Epic(int id, String naam) {
        this.id = id;
        this.naam = naam;
    }

    public int getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

}

class Taak {
    private int id;
    private String beschrijving;

    public Taak(int id, String beschrijving) {
        this.id = id;
        this.beschrijving = beschrijving;
    }

    public int getId() {
        return id;
    }

    public String getBeschrijving() {
        return beschrijving;
    }
}


class Bericht {
    private int id;
    private String inhoud;
    private LocalDateTime verzendDatum;
    private Gebruiker verzender;

    // Optioneel gekoppeld: kan één van deze drie bevatten
    private UserStory gekoppeldeUserStory;
    private Taak gekoppeldeTaak;
    private Epic gekoppeldeEpic;

    public Bericht(int id, String inhoud, LocalDateTime verzendDatum, Gebruiker verzender) {
        this.id = id;
        this.inhoud = inhoud;
        this.verzendDatum = verzendDatum;
        this.verzender = verzender;
    }

    // Koppel een UserStory aan het bericht (0..1 relatie)
    public void koppelUserStory(UserStory userStory) {
        verwijderBestaandeKoppelingen();
        this.gekoppeldeUserStory = userStory;
    }

    // Koppel een Taak aan het bericht (0..1 relatie)
    public void koppelTaak(Taak taak) {
        verwijderBestaandeKoppelingen();
        this.gekoppeldeTaak = taak;
    }

    // Koppel een Epic aan het bericht (0..1 relatie)
    public void koppelEpic(Epic epic) {
        verwijderBestaandeKoppelingen();
        this.gekoppeldeEpic = epic;
    }

    // Verwijder gekoppelde objecten zodat er maar 1 actief kan zijn
    private void verwijderBestaandeKoppelingen() {
        this.gekoppeldeUserStory = null;
        this.gekoppeldeTaak = null;
        this.gekoppeldeEpic = null;
    }

    public String getInhoud() {
        return inhoud;
    }

    public Gebruiker getVerzender() {
        return verzender;
    }

    // Hulp-methode om het gekoppelde object te beschrijven
    public String getGekoppeldObject() {
        if (gekoppeldeUserStory != null) {
            return "UserStory: " + gekoppeldeUserStory.getNaam();
        } else if (gekoppeldeTaak != null) {
            return "Taak: " + gekoppeldeTaak.getBeschrijving();
        } else if (gekoppeldeEpic != null) {
            return "Epic: " + gekoppeldeEpic.getNaam();
        } else {
            return "Geen gekoppelde objecten";
        }
    }
}
