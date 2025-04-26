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
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("READY_ACK");

            new Thread(this::listenToServer).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
                        gui.enableCardClicks(true);
                        gui.showTurnMessage(true);
                    });
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
