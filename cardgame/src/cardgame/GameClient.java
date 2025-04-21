package cardgame;

import java.io.PrintWriter;
import java.io.IOException; 
import java.net.Socket;

public class GameClient {
    private Socket socket;
    private PrintWriter out;

    public GameClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void sendPlayCard(int index, String color) {
        String message = "PLAY_CARD " + index + " " + color;
        out.println(message);
//        System.out.println("[Sent to Server] " + message);
    }

    public void sendEndGame() {
        out.println("END_GAME");
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}