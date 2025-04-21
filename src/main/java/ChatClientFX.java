//javaFX lib voor de UI
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
// lib voor connectie en overige dingen
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;
// application is een class van javaFX die je extend zodat je methoden kan gebruken van javaFX
// zoals launch, start en andree
public class ChatClientFX extends Application {

    private PrintWriter output;
    private TextArea chatArea;
    private TextField inputField;
    private final String serverAddress = "localhost";
    private Gebruiker gebruiker;
    private Team team;
    private Label chatInfo;
    private static final int PORT = 12345;
    private Stage scherm;
    private Bericht actiefBericht;
    private VBox root;
    // lijst met alle teams
    private static final ArrayList<Team> teams = new ArrayList<>();

    static {
        Team algemeen = new Team("Algemeen", "De chat voor algemeen gesprek.");
        Team marketing = new Team("Marketing", "De chat voor het marketing van de applicatie.");
        Team design = new Team("Design", "De chat voor het design van de applicatie.");
        Team development = new Team("Development", "De chat voor de ontwikkeling van de applicatie.");
        teams.add(algemeen);
        teams.add(development);
        teams.add(marketing);
        teams.add(design);
    }


    public void start(Stage stage) {
        this.scherm = stage;
        setupChat();
    }

    private void setupChat() {
        String gebruikersnaam = promptGebruikersnaam();
        gebruiker = new Gebruiker(1, gebruikersnaam, "");
        // Begin met het instellen van het eerste team
        team = teams.get(0);
        gebruiker.setTeam(team);
        connectToServer();
        // setOnMouseEntered is de kleur die het vakje is als je je muis er over heen houdt.
        // exited doet het terug naar wat het hoort te zijn.
        // listen luister naar de gebruiker voor een actie zoals een click

        // tekstvak voor de chat
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        VBox.setVgrow(chatArea, Priority.ALWAYS);
        chatArea.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-font-size: 14px;");
        // Maakt het invoerveld voor een bericht
        inputField = new TextField();
        inputField.setPromptText("Type je bericht hier...");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #cccccc; -fx-font-size: 14px;");
        //knop om een nieuw team aan te maken
        Button maakNieuwTeam = new Button("Maak nieuw team");
        maakNieuwTeam.setPrefWidth(150);
        maakNieuwTeam.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold;");
        maakNieuwTeam.setOnMouseEntered(click -> maakNieuwTeam.setStyle("-fx-background-color: #e0a800; -fx-text-fill: black; -fx-font-weight: bold;"));
        maakNieuwTeam.setOnMouseExited(click -> maakNieuwTeam.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold;"));
        maakNieuwTeam.setOnAction(click -> NieuwteamBericht());
        // Maak een knop om het bericht te sturen
        Button verstuurKnop = new Button("Verstuur");
        verstuurKnop.setPrefWidth(100);
        verstuurKnop.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        verstuurKnop.setOnMouseEntered(click -> verstuurKnop.setStyle("-fx-background-color: #0056b3; -fx-text-fill: white; -fx-font-weight: bold;"));
        verstuurKnop.setOnMouseExited(click -> verstuurKnop.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;"));
        // Maak een knop om berichten te koppelen
        Button kopelKnop = new Button("Koppel");
        kopelKnop.setPrefWidth(120);
        kopelKnop.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        kopelKnop.setOnMouseEntered(click -> kopelKnop.setStyle("-fx-background-color: #218838; -fx-text-fill: white; -fx-font-weight: bold;"));
        kopelKnop.setOnMouseExited(click -> kopelKnop.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;"));
        kopelKnop.setOnAction(click -> koppelBericht());
        //  chatinformatie van je team
        chatInfo = new Label("Team: " + team.getNaam() + " (" + team.getOnderwerp() + ")");
        chatInfo.setStyle("-fx-background-color: #e9ecef; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10px;");
        VBox.setMargin(chatInfo, new Insets(10, 0, 10, 0));
        //ComboBox(box met meerde opties) voor het wisselen van team
        ComboBox<String> teamComboBox = new ComboBox<>();
        teamComboBox.getItems().addAll(
                teams.stream().map(Team::getNaam).toList());
        teamComboBox.setValue(team.getNaam());
        teamComboBox.setStyle("-fx-font-size: 14px;");
        teamComboBox.setOnAction(click -> {
            String selectedTeam = teamComboBox.getValue();
            team = teams.stream().filter(t -> t.getNaam().equals(selectedTeam)).findFirst().orElse(teams.get(0));
            updateChatvoorTeam();
        });
        // Voeg de ComboBox toe aan de bovenkant van de layout (dashboard)
        HBox dashboard = new HBox(10, new Label("Selecteer team:"), teamComboBox);
        dashboard.setPadding(new Insets(10));
        dashboard.setStyle("-fx-background-color: transparent; -fx-border-radius: 15px; -fx-border-color: #007bff; -fx-border-width: 2px;");
        Label label = (Label) dashboard.getChildren().get(0); // Haal de label op uit de HBox
        label.setStyle("-fx-font-size: 20px;");
        // layout chat
        HBox inputBox = new HBox(10, inputField, verstuurKnop, kopelKnop, maakNieuwTeam);
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-background-color: #f8f9fa;");
        root = new VBox(10, dashboard, chatInfo, chatArea, inputBox, maakNieuwTeam);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #d3d3d3;");
        root.prefWidthProperty().bind(scherm.widthProperty());
        root.prefHeightProperty().bind(scherm.heightProperty());
        verstuurKnop.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());
        Scene scene = new Scene(root, 800, 600);
        scherm.setTitle("Chat Team: " + team.getNaam());
        scherm.setScene(scene);
        scherm.show();
    }

    // Methode om de chat bij te werken omdat je switched van team
    private void updateChatvoorTeam() {
        chatInfo.setText("Team: " + team.getNaam() + " (" + team.getOnderwerp() + ")");
        // Leegt de chat omdat je in een nieuwe team gaat
        chatArea.clear();
        // verbinf opniew met server omdat we nu in een ander team gaan
        connectToServer();
    }

    private void koppelBericht() {
        // leest het bericht in die in het type hier veld staat
        String message = inputField.getText().trim();
        actiefBericht = new Bericht(1, message, LocalDateTime.now(), gebruiker);
        //popup die je vraagt aan wat je het wil kopelen
        ChoiceDialog<String> dialog = new ChoiceDialog<>("UserStory", "UserStory", "Taak", "Epic");
        dialog.setTitle("Koppel bericht");
        dialog.setHeaderText("Koppel dit bericht aan een UserStory, Taak of Epic.");
        dialog.setContentText("Selecteer:");
        String keuze = dialog.showAndWait().orElse(null);
        if (keuze == null) return; // als je niks kiest om te koppelen
        switch (keuze) { // kopelt bericht aan taak, epic, userstory die dan wordt gemaakt
            case "UserStory" -> actiefBericht.koppelUserStory(new UserStory(1, "Voorbeeld UserStory"));
            case "Taak" -> actiefBericht.koppelTaak(new Taak(1, "Voorbeeld Taak"));
            case "Epic" -> actiefBericht.koppelEpic(new Epic(1, "Voorbeeld Epic"));
        }

    }

    private void updateTeamLijst(String teamData) {
        String[] serverTeams = teamData.split(","); // split de string van teamnamen af en doet het in een array
        // hieronder haal je de team box op
        ComboBox<String> teamComboBox = (ComboBox<String>) ((HBox) root.getChildren().get(0)).getChildren().get(1);
        for (String teamName : serverTeams) {
            // hier check je of het team in in de lijst van teams staat
            boolean bestaatAl = teams.stream().anyMatch(t -> t.getNaam().equals(teamName));
            if (!bestaatAl) { // als bestaalAl false is voegen we nieuwe team toe
                // Voeg nieuw team toe aan lijst
                int newId = teams.size() + 1;
                Team nieuwTeam = new Team(teamName, "Team toegevoegd vanaf server.");
                teams.add(nieuwTeam);
                // voegen we het toe aan de box waar je kan kiezen welk team je wil
                teamComboBox.getItems().add(teamName);
            }
        }
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                // Sluit de oude verbinding als die nog open is
                if (output != null) {
                    output.close();
                }
                // Maak een nieuwe verbinding
                Socket socket = new Socket(serverAddress, PORT);
                // het ontvangen van berichten (inlezen van berichten)
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // het het maken / schrijven van nieuwe berichten naar de server
                output = new PrintWriter(socket.getOutputStream(), true);
                //stuur naam van het team dat je nu in zit naar ChatServer oftewel server
                output.println(team.getNaam());

                String teamNamen = teams.stream()
                        .map(Team::getNaam)
                        .collect(Collectors.joining(",")); // Verzamel alle teamnamen in een komma-gescheiden string
                output.println("TEAMLIST:" + teamNamen); // Stuur de lijst van lokale teams naar de server
                String message;
                while ((message = input.readLine()) != null) {
                    String finalMessage = message;
                    // als je een bericht krijgt van de server die begint met TEAMS: begint de volgende code
                    if (message.startsWith("TEAMS:")) {
                        // haalt TEAMS: weg en stopt wat over blijft in teamData
                        String teamData = message.substring("TEAMS:".length());
                        // hier gaan we de teams updaten met mogelijke nieuwe teams die er zijn
                        javafx.application.Platform.runLater(() -> updateTeamLijst(teamData));
                    } else {
                        // als het bericht niet begint met TEAMS: stuur je dit bericht je kan bijvoorbeeld zeggen connectie succesvol heb ik weg gelaten
                        javafx.application.Platform.runLater(() -> chatArea.appendText(finalMessage + "\n"));
                    }
                }
            } catch (IOException e) {// in het geval dat er een connectie fout is wordt dit bericht getoont
                javafx.application.Platform.runLater(() -> chatArea.appendText(""));
            }// Start de thread om de verbinding met de server in de achtergrond te laten werken
        }).start();
    }

    // Methode om een dialoogvenster weer te geven voor het maken van een nieuw team
    private void NieuwteamBericht() {
        // Maak de dialoogvensters voor naam en beschrijving
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Nieuw team");
        nameDialog.setHeaderText("Geef de naam van het team in:");
        nameDialog.setContentText("Teamnaam:");
        TextInputDialog descriptionDialog = new TextInputDialog();
        descriptionDialog.setTitle("Nieuw team");
        descriptionDialog.setHeaderText("Geef een beschrijving voor het team in:");
        descriptionDialog.setContentText("Teamomschrijving:");
        // Wacht op de input van de gebruiker
        String teamName = nameDialog.showAndWait().orElse("");
        String teamDescription = descriptionDialog.showAndWait().orElse("");
        if (!teamName.isEmpty() && !teamDescription.isEmpty()) {
            // Maak een nieuw team en voeg het toe aan de lijst van teams
            int newTeamId = teams.size() + 1;
            Team newTeam = new Team(teamName, teamDescription);
            teams.add(newTeam);
            // Voeg het nieuwe team toe aan de ComboBox voor teamselectie
            ComboBox<String> teamComboBox = (ComboBox<String>) ((HBox) root.getChildren().get(0)).getChildren().get(1);
            teamComboBox.getItems().add(newTeam.getNaam());
            // Selecteer het nieuwe team
            teamComboBox.setValue(newTeam.getNaam());
            updateChatvoorTeam();
        } else {
            // Toon een waarschuwing als de naam of beschrijving van het nieuwe team leeg is
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Fout");
            alert.setHeaderText(null);
            alert.setContentText("Zowel de naam als de beschrijving moeten ingevuld worden.");
            alert.showAndWait();
        }
    }

    private void sendMessage() {
        // Haalt de tekst uit het inputveld en verwijderd spaties voor en achter  het bericht
        String message = inputField.getText().trim();
        // Controleer of het bericht niet leeg is
        if (!message.isEmpty()) {
            // Controleer of er nog geen actief bericht is (voor koppelen)
            if (actiefBericht == null) {
                // Maak een nieuw bericht object met de huidige tijd en gebruiker we gebruiken niet de tijd en datum zo ver komen we niet
                actiefBericht = new Bericht(1, message, LocalDateTime.now(), gebruiker);
            }
            // controleert of het actieve bericht niet gekoppeld is aan een object
            if (actiefBericht.getGekoppeldObject().equals("geen")) {
                // stuurt het bericht naar de server met de gebruikersnaam en teamnaam
                output.println(gebruiker.getNaam() + " (" + team.getNaam() + "): " + actiefBericht.getInhoud());
            } else {
                // als het actieve bericht wel gekoppeld is zelfde als de vorige maar dan met het gekoppelde object
                output.println(gebruiker.getNaam() + " (" + team.getNaam() + "): " + actiefBericht.getInhoud() + " [" + actiefBericht.getGekoppeldObject() + "]"); // Stuur het bericht met de koppeling informatie naar de server
            }
            // reset het actieve bericht na het verzenden
            actiefBericht = null;
            // leeg het inputveld voor het volgende bericht
            inputField.clear();
        }
    }

    private String promptGebruikersnaam() {
        //popup die vraagt voor gebruikersnaam
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Gebruikersnaam");
        dialog.setHeaderText("Voer je gebruikersnaam in:");
        dialog.setContentText("Gebruikersnaam:");
        //throw betekent gwn sluit de hele aplicatie
        return dialog.showAndWait().orElseThrow();
    }

    public static void main(String[] args) { // dit is het eerste dat runt
        // start de JavaFX applicatie
        launch(args);
}
}
// andere classen

class Team {
    private String naam;
    private String onderwerp;
    


    public Team(String naam, String onderwerp) {

        this.naam = naam;
        this.onderwerp = onderwerp;
    }
    public String getOnderwerp() {
        return onderwerp;
    }

    public String getNaam() {
        return naam;
    }
    public void setOnderwerp(String onderwerp) {
        this.onderwerp = onderwerp;
    }

    

    @Override
    public String toString() {
        return naam;
    }
}

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



    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getEmail() { // gebruiken we nog niet
        return email;
    }

    public Team getTeam() {// gebruiken we nog niet
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
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
    private String beschrijvingVanTaak;

    public Taak(int id, String beschrijvingVanTaak) {
        this.id = id;
        this.beschrijvingVanTaak = beschrijvingVanTaak;
    }

    public int getId() {
        return id;
    }

    public String getBeschrijvingVanTaak() {
        return beschrijvingVanTaak;
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
            return "Taak: " + gekoppeldeTaak.getBeschrijvingVanTaak();
        } else if (gekoppeldeEpic != null) {
            return "Epic: " + gekoppeldeEpic.getNaam();
        } else {
            return "geen";
        }
    }
}
