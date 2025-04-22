package cardgame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import cardgame.GameClient;
import cardgame.Card;

public class GameUnoGUI extends JFrame {
    private GameClient client;
    private JPanel handPanel;
    private JComboBox<String> colorSelector;
    private JLabel topCardLabel;
    private List<Card> hand;

private final Map<String, String> valueToNumberMap = Map.ofEntries(
    Map.entry("1", "1"),
    Map.entry("2", "2"),
    Map.entry("3", "3"),
    Map.entry("4", "4"),
    Map.entry("5", "5"),
    Map.entry("6", "6"),
    Map.entry("7", "7"),
    Map.entry("8", "8"),
    Map.entry("9", "9"),
    Map.entry("Skip", "10"),
    Map.entry("Draw 2", "11"),
    Map.entry("Reverse", "12"),
    Map.entry("Wild", "13"),
    Map.entry("Draw 4", "14")
);

    public GameUnoGUI(GameClient client, List<Card> hand) {
        this.client = client;
        this.hand = hand;
        setTitle("UNO Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        topCardLabel = new JLabel("Top Card", SwingConstants.CENTER);
        topCardLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(topCardLabel, BorderLayout.NORTH);

        handPanel = new JPanel();
        handPanel.setLayout(new FlowLayout());
        add(handPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        colorSelector = new JComboBox<>(new String[]{"Red", "Blue", "Green", "Yellow"});
        bottomPanel.add(new JLabel("Choose color (for Wild):"));
        bottomPanel.add(colorSelector);
        add(bottomPanel, BorderLayout.SOUTH);

        loadCardImages(hand);

        setVisible(true);
    }

    private void loadCardImages(List<Card> hand) {
          int index = 0;
          
          for (Card card : hand) {
              String value = card.getValue();
              String suit = card.getSuit();
              String number = valueToNumberMap.get(value);
              if (number == null) continue;
              String filename = suit + "-" + number + ".png";
              addCardButton(filename, value, suit, index++);
          }
//        String[] colorSuits = {"Red", "Blue", "Green", "Yellow"};
//        String[] colorValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "Skip", "Reverse", "Draw 2", "Wild", "Draw 4"};
//        String[] blackValues = {"Wild", "Draw 4"};
//        int index = 0;
//
//        for (String suit : colorSuits) {
//            for (String value : colorValues) {
//                
//                if ((value.equals("Wild") || value.equals("Draw 4")) && !suit.equals("Black")) {
//                    continue;
//                }
//                
//                if ((suit.equals("Black")) && !(value.equals("Wild") || value.equals("Draw 4"))) {
//                    continue; 
//                }
//                
//                String number = valueToNumberMap.get(value);
//                if (number == null) continue;
//                
//                String filename = suit + "-" + number + ".png"; 
//                addCardButton(filename, value, suit, index++);
//            }
//        }
//        
//        for (int i = 0; i < blackValues.length; i++) {
//            String value = blackValues[i];
//            int blackNum = 13 + i;
//            String filename = "Black-" + blackNum + ".png";
//            
//            addCardButton(filename, value, "Black", index++);
//        }
    }
    
    public void setTopCard(String value, String suit) {
        String number = valueToNumberMap.get(value);
        if (number == null) return;
        String filename = suit + "-" + number + ".png";
        URL location = getClass().getResource("/cardgame/image/" + filename);
        if (location != null) {
            topCardLabel.setIcon(new ImageIcon(location));
        }
    }

    public void addCardButton(String filename, String value, String suit, int index) {
        String path = "/cardgame/image/" + filename;
        URL location = getClass().getResource(path);

        if (location == null) {
            System.out.println("Image not found: " + path);
            return;
        }

        ImageIcon icon = new ImageIcon(location);
//        JButton cardButton = new JButton(icon);
        
        Image scaledImage = icon.getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH);
        JButton cardButton = new JButton(new ImageIcon(scaledImage));
        
        cardButton.setPreferredSize(new Dimension(80, 120));
        cardButton.addActionListener(e -> {
            String selectedColor = (String) colorSelector.getSelectedItem();
            client.sendPlayCard(index, selectedColor); 
        });

        handPanel.add(cardButton);
    }

    public static void main(String[] args) {
        try {
            GameClient client = new GameClient("localhost", 12345);
            
//            if (args.length > 0 && args[0].equals("first")) {
//                String[] options = {"1", "2", "3", "4"};
//                String selected = (String) JOptionPane.showInputDialog(null, "Number of Players?", "UNO Setup",
//                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
//                if (selected != null) {
//                    client.sendStartGameRequest(Integer.parseInt(selected));
//                }
//            }

            String[] options = {"1", "2", "3", "4"};
            String selected = (String) JOptionPane.showInputDialog(null, "Number of Players?", "UNO Setup",
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (selected != null) {
            client.sendStartGameRequest(Integer.parseInt(selected));
            }
        
            List<Card> hand = client.waitForInitialHand();
            GameUnoGUI gui = new GameUnoGUI(client, hand);
            client.setGUI(gui);
            client.startListening();
        } catch (IOException ex) {
            Logger.getLogger(GameUnoGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}