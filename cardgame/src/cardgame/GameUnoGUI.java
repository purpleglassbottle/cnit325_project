package cardgame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import cardgame.GameClient;
import java.util.ArrayList;

public class GameUnoGUI extends JFrame {
    private GameClient client;
    private JPanel handPanel;
    private JPanel opponentPanel;
    private JComboBox<String> colorSelector;
    private JLabel topCardLabel;
    private JLabel opponentLabel;

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

    private final Map<JButton, Integer> buttonIndexMap = new HashMap<>();
    private ArrayList<JButton> currentHandButtons = new ArrayList<JButton>();

    //constructor
    public GameUnoGUI() {
        try{
            this.client = new GameClient("localhost",12345);
            this.client.setGUI(this);
            setupGUI();
            setVisible(true);
            System.out.println("Connected to the server");
                               
        }catch(IOException e){            
            System.exit(1);
        }
    }
    
    public void setupGUI()
    {
        setTitle("UNO Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLayout(new BorderLayout());

        topCardLabel = new JLabel("Waiting for the game to start", SwingConstants.CENTER);
        topCardLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(topCardLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        
        //opponent player
        opponentPanel = new JPanel();
        opponentLabel = new JLabel("opponent's current cards: 0");
        opponentPanel.add(opponentLabel);
        centerPanel.add(opponentPanel, BorderLayout.NORTH);
        //player
        handPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        centerPanel.add(new JScrollPane(handPanel), BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        colorSelector = new JComboBox<>(new String[]{"Red", "Blue", "Green", "Yellow"});
        colorSelector.setEnabled(false); 
        bottomPanel.add(new JLabel("Choose color:"));
        bottomPanel.add(colorSelector);
        add(bottomPanel, BorderLayout.SOUTH);        
    }
    
    //display player's handCards on GUI
    public void loadCardImages(ArrayList<Card> hand) {
        clearHandPanel();
        
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            String filename = getCardFilename(card);
            addCardButton(filename, card.getValue(), card.getSuit(), i);           
        }
    }
    
    private void clearHandPanel() {
        handPanel.removeAll();
        buttonIndexMap.clear();
        currentHandButtons.clear();
        handPanel.revalidate();
        handPanel.repaint();
    }

    //add those images to buttons
    private void addCardButton(String filename, String value, String suit, int index) {
        //attach the image to the button
        ImageIcon icon = loadScaledCardImage(filename);
        if (icon == null) return;

        JButton cardButton = createCardButton(icon, value, suit, index);
        handPanel.add(cardButton);
        buttonIndexMap.put(cardButton, index);
        currentHandButtons.add(cardButton);
    }
    
    private ImageIcon loadScaledCardImage(String filename) {
        String path = "/cardgame/image/" + filename;
        URL location = getClass().getResource(path);
        if (location == null) {
            System.out.println("Image not found: " + path);
            return null;
        }
        ImageIcon icon = new ImageIcon(location);
        Image scaledImage = icon.getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private JButton createCardButton(ImageIcon icon, String value, String suit, int index) {
        JButton button = new JButton(icon);
        button.setPreferredSize(new Dimension(80, 120));
        
        button.addActionListener(e -> {
            if (value.equals("Wild") || value.equals("Draw 4")) {
                colorSelector.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Please select a color first");
            } else {
                client.sendPlayCard(index, null);
            }
        });
        
        return button;
    }
    
    public void updateGameState(String topCardInfo, int opponentCardCount) {
        topCardLabel.setText("Top Card: " + topCardInfo);
        opponentLabel.setText("Opponent's cards: " + opponentCardCount);
    }
    
    private String getCardFilename(Card card) {
        // check null 
    if (card == null || card.getSuit() == null) {
        System.err.println("INVALID CARD DATA");
        return "Black-13.png"; // default
    }
        String number = valueToNumberMap.get(card.getValue());
        String suit = card.getSuit().equals("Wild") ? "Black" : card.getSuit();
        return suit + "-" + number + ".png";
    }
    
    public static void main(String[] args) {
        try{
            new GameUnoGUI();
        }catch(Exception e){
            System.err.println("Failed to start");
            e.printStackTrace();
        }
    }
}
