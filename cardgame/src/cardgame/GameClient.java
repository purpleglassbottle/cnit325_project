package cardgame;

import java.io.PrintWriter;
import java.io.IOException; 
import java.net.Socket;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    private GameUnoGUI gui;
    public void setGUI(GameUnoGUI gui){
        this.gui = gui;
    }
    
    //constructor
    // temp 
//    public GameClient(String host, int port) throws IOException {
//        try {
//            socket = new Socket(host, port);
//            out = new PrintWriter(socket.getOutputStream(), true);
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//            out.println("READY_ACK");
//
//            new Thread(this::listenToServer).start();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    
    private final Map<String, String> numberToValueMap = Map.ofEntries(
        Map.entry("1", "1"),
        Map.entry("2", "2"),
        Map.entry("3", "3"),
        Map.entry("4", "4"),
        Map.entry("5", "5"),
        Map.entry("6", "6"),
        Map.entry("7", "7"),
        Map.entry("8", "8"),
        Map.entry("9", "9"),
        Map.entry("10", "Skip"),
        Map.entry("11", "Reverse"),
        Map.entry("12", "Draw 2"),
        Map.entry("13", "Wild"),
        Map.entry("14", "Draw 4")
    );
    
    // Legacy Connections
    public GameClient(String h, int p) throws IOException {
        this(h, p, false, "new", 0);
    }

    // New dynmaic
    public GameClient(String host, int port, boolean load, String gameId, int playerId) throws IOException {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
       
//            out.println("INIT_GAME " + (load ? "LOAD " : "NEW ") + gameId);
//            out.println("HELLO " + playerId);
//            out.println("READY_ACK");

//            new Thread(this::listenToServer).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    public void startListening() {
        new Thread(this::listenToServer).start();
    }    
    
    private void listenToServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Receive the message from server:" + " " + message);
                
                if (message.startsWith("HAND:")) {
                    ArrayList<Card> hand = parseHand(message.substring(5));
                    SwingUtilities.invokeLater(() -> gui.loadCardImages(hand));
                } 
                else if (message.startsWith("STATE:")) {
                    String[] parts = message.substring(6).split("\\|");
//                    SwingUtilities.invokeLater(() -> 
//                    gui.updateGameState(parts[0], Integer.parseInt(parts[1])));
                    if (parts.length == 2) {
                        String topCardStr = parts[0];
                        int oppCardCount = Integer.parseInt(parts[1]);
                        boolean isMyTurn = topCardStr.contains("*");
        
                        String cleanedTopCardStr = topCardStr.replace("*", "");

                        SwingUtilities.invokeLater(() -> {
                            gui.updateGameState(cleanedTopCardStr, oppCardCount, isMyTurn);
                            gui.showTurnMessage(false); 
                        });
                    } else {
                        System.out.println("Invalid STATE format received: " + message);
                    }
                }
                else if (message.equals("Your turn")) {
                    SwingUtilities.invokeLater(() -> {
                        gui.setMyTurn(true);
                        gui.enableCardClicks(true);
                        gui.showTurnMessage(true);
                    });
                }
                else if (message.startsWith("TIMER_START:")) {
                    int seconds = Integer.parseInt(message.substring(12));
                    SwingUtilities.invokeLater(() -> {
                        if (gui.isMyTurn()) { 
                            gui.startCountdown(seconds);
                        }
                    });
//                    gui.setMyTurn(true);
//                    SwingUtilities.invokeLater(() -> {
//                        gui.enableCardClicks(true);
//                        gui.showTurnMessage(true);
//                    });
                }                
                else if (message.startsWith("play rejected: Not your turn")) {
                        SwingUtilities.invokeLater(() -> {
                            gui.enableCardClicks(false);
                            JOptionPane.showMessageDialog(gui, "It's not your turn!", "Warning", JOptionPane.WARNING_MESSAGE);
                        });
                }
                else if (message.startsWith("Game already started")) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(gui, "The game has already started. You cannot join now.", "Game Full", JOptionPane.ERROR_MESSAGE);
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }        
                        System.exit(0); 
                    });
                }
                else if (message.startsWith("END_GAME")) {
                    String reason = message.substring(9); 
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(gui, reason, "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        System.exit(0);
                    });
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
            String suit = parts[0];
            String valueCode = parts[1];

            String value = numberToValueMap.getOrDefault(valueCode, valueCode);
            
            hand.add(new Card(value, suit)); // value,suit
        }
        return hand;
    }

    public void sendPlayCard(int index, String color) {
        String message = "PLAY_CARD " + index + " " + color;
        out.println(message);
        System.out.println("[DEBUG] Sending card index: " + index + ", color: " + color);
//        System.out.println("[Sent to Server] " + message);
    }
    
    public void sendMessage(String message) {
        try {
            out.println(message);
        } catch (Exception e) {
            System.err.println("Failed to send message: " + message);
            e.printStackTrace();
        }
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
