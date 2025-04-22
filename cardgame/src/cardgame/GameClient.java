/*
    Cassie Kim
*/

package cardgame;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameUnoGUI gui;

    private final List<Card> hand = new ArrayList<>();
    private boolean handReady = false;

    public GameClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    public void sendStartGameRequest(int numberOfPlayers) {
        out.println("START " + numberOfPlayers);
    }
    
    public void setGUI(GameUnoGUI gui) {
        this.gui = gui;
    }
    
    public List<Card> waitForInitialHand() throws IOException {
        List<Card> hand = new ArrayList<>();
        System.out.println("▶ Waiting for cards...");
        while (true) {
            String line = in.readLine();
            System.out.println("▶ Received: " + line);
            
            if (line == null) break;

            if (line.equals("HAND_DONE")) break;

            if (line.startsWith("CARD")) {
            String[] parts = line.split(" ");
            String value = parts[1];
            String suit = parts[2].equals("null") ? null : parts[2];
            hand.add(new Card(value, suit));
            }
        }
        return hand;
    }

    public void sendPlayCard(int index, String color) {
        String message = "PLAY_CARD " + index + " " + color;
        out.println(message);
//        System.out.println("[Sent to Server] " + message);
    }

    public void sendEndGame() {
        out.println("END_GAME");
    }
    
    public void startListening() {
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("? Received " + line);
                    
                    if (line.startsWith("TOP_CARD")) {
                        String[] parts = line.split(" ");
                        if (parts.length >= 3 && gui != null) {
                            String value = parts[1];
                            String suit = parts[2];
                            gui.setTopCard(value, suit);  // Display Top Cards on GUI
                        }
                    }
                    
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }    

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}