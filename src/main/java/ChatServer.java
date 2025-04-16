import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    // port waarop de server staat
    private static final int PORT = 12345;

    // lijst met alle verbonden clients (synchronized vanwege meerdere threads)
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    // lijst met alle bekende teamnamen
    private static final List<String> teams = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws IOException {
        // start server op de gegeven poort
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            // accepteer innenkomende verbinding
            Socket clientSocket = serverSocket.accept();

            // start een nieuwe thread om met deze client te praten
            new ClientHandler(clientSocket).start();
        }
    }

    //class om individuele clients af te handelen
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String teamNaam;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // setup I/O streams I is input O, output
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                //ontvang de teamnaam van de client
                teamNaam = in.readLine();
                // voeg nieuw team toe aan lijst als die nog niet bestaat
                if (teamNaam != null && !teams.contains(teamNaam)) {
                    teams.add(teamNaam);
                    broadcastTeamList(); // stuur nieuwe lijst door naar alle clients
                }
                // voegt client toe aan de lijst van actieve clients (als nog niet toegevoegd)
                if (!clients.contains(this)) {
                    clients.add(this);
                }

                // luisterd naar berichten van de client
                String message;
                while ((message = in.readLine()) != null) {
                    // stuurt het bericht alleen naar clients van hetzelfde team
                    synchronized (clients) {
                        for (ClientHandler client : clients) {
                            if (client.teamNaam.equals(this.teamNaam)) {
                                client.out.println(message);
                            }
                        }
                    }
                } //IO staat voor Input en Output
            } catch (IOException e) {
                System.out.println(); // (kan vervangen worden met een log)
            } finally {
                // sluit verbinding 
                try {
                    socket.close();
                } catch (IOException ignored) {}
                clients.remove(this);
            }
        }

        // verzendt de lijst van teamnamen naar alle clients
        // het bericht heeft het formaat"TEAMS:team1,team2, etc"

        private void broadcastTeamList() {
            String teamListMsg = "TEAMS:" + String.join(",", teams);

            synchronized (clients) { // alleen clients die hier zijn oftewel allemaal gwn
                for (ClientHandler client : clients) {
                    client.out.println(teamListMsg);
                }
            }
        }
    }
}
