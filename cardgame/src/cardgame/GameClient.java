package cardgame;

import java.io.PrintWriter;
import java.io.IOException; 
import java.net.Socket;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    private GameUnoGUI gui;
    public void setGUI(GameUnoGUI gui){
        this.gui = gui;
    }
    
    //constructor
    public GameClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        new Thread(this::listenToServer).start();
    }
    
    private void listenToServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Receive the message form the server" + message);
                if (message.startsWith("HAND:")) {
                    ArrayList<Card> hand = parseHand(message.substring(5));
                    SwingUtilities.invokeLater(() -> gui.loadCardImages(hand));
                } 
                else if (message.startsWith("STATE:")) {
                    String[] parts = message.substring(6).split("\\|");
                    SwingUtilities.invokeLater(() -> 
                        gui.updateGameState(parts[0], Integer.parseInt(parts[1])));
                }
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(gui, "Disconnected from server"));
        }
    }
    
    private ArrayList<Card> parseHand(String handStr) {
        ArrayList<Card> hand = new ArrayList<>();
        String[] cards = handStr.split(";");
        for (String card : cards) {
            System.out.println("Raw card string: " + card); 
            String[] parts = card.split(",");
            //for testing
            if (parts.length < 2) {
            System.out.println("Parse failed, wrong format: " + card);
            continue;
        }
            hand.add(new Card(parts[1], parts[0])); // value,suit
        }
        return hand;
    }

    public void sendPlayCard(int index, String color) {
        String message = "PLAY_CARD " + index + " " + color;
        out.println(message);
//        System.out.println("[Sent to Server] " + message);
    }

    /*public void sendEndGame() {
        out.println("END_GAME");
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
}
