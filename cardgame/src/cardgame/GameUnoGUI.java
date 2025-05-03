package cardgame;

/*
* Cassie Kim
* Xiaotong Luo
* Sean Maloney
* Emily Zhang
*/

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import cardgame.GameClient;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.util.UUID;


public class GameUnoGUI extends JFrame {
    private GameClient client;
    private JPanel handPanel;
    private JPanel opponentPanel;
    private JComboBox<String> colorSelector;
    private JLabel topCardLabel;
    private JLabel opponentLabel;
    private JLabel turnLabel;
    private JButton settingsButton;
    private Locale selectedLocale = Locale.ENGLISH;
    private String topCardInfo = "Waiting..."; 
    private int opponentCardCount = 0; 
    private boolean isMyTurn = false;
    private Timer countdownTimer;
    private JLabel timerLabel;    

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
    Map.entry("Reverse", "11"),
    Map.entry("Draw 2", "12"),
    Map.entry("Wild", "13"),
    Map.entry("Draw 4", "14")
);

    private final Map<JButton, Integer> buttonIndexMap = new HashMap<>();
    private ArrayList<JButton> currentHandButtons = new ArrayList<JButton>();
    private ArrayList<Card> lastReceivedHand = new ArrayList<>();

    //constructor
    public GameUnoGUI() {
        
        String[] languages = {"English", "한국어", "中文"};
        String inputLanguage = (String) JOptionPane.showInputDialog(
                null,
                "Select Language / 언어 선택 / 选择语言",
                "Language",
                JOptionPane.QUESTION_MESSAGE,
                null,
                languages,
                languages[0]
        );

        if (inputLanguage == null) {
            System.exit(0);
        }

        if (inputLanguage.equals("한국어")) {
            selectedLocale = Locale.KOREAN;
        } else if (inputLanguage.equals("中文")) {
            selectedLocale = Locale.CHINESE;
        } else {
            selectedLocale = Locale.ENGLISH;
        }
        
        try{
            Object[] opts = { getLocalizedText("New Game"),
                  getLocalizedText("Continue") };

            int choice = JOptionPane.showOptionDialog(null, getLocalizedText("Start a new game or continue?"), "UNO"
                    , JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);

            boolean load = (choice == 1);

            String gameId;
            int playerId;

            if (load) {
                gameId = JOptionPane.showInputDialog(null, getLocalizedText("Enter game ID:"));
                if (gameId == null || gameId.isBlank()){
                    System.exit(0);
                }

                String s = JOptionPane.showInputDialog(null, getLocalizedText("Which player number are you? (0, 1, ...)"));
                if (s == null) {
                    System.exit(0);
                }
                playerId = Integer.parseInt(s.trim());
            } else {
                gameId = UUID.randomUUID().toString().substring(0, 6);
                JOptionPane.showMessageDialog(null, getLocalizedText("Your game ID is: ") + gameId);
//                playerId = 0;                         // first joiner

                String s = JOptionPane.showInputDialog(null, getLocalizedText("Which player number are you? (0, 1, ...)"));
                if (s == null || s.isBlank()) {
                    System.exit(0);
                }
                playerId = Integer.parseInt(s.trim());
            }

            this.client = new GameClient("localhost", 12345, load, gameId, playerId);
//            this.client = new GameClient("localhost",12345);
            this.client.setGUI(this);
            this.client.startListening();
            this.client.sendMessage("INIT_GAME " + (load ? "LOAD " : "NEW ") + gameId);
            this.client.sendMessage("HELLO " + playerId);
            this.client.sendMessage("READY");
            
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

        topCardLabel = new JLabel(getLocalizedText("Waiting for the game to start"), SwingConstants.CENTER);
        topCardLabel.setFont(new Font("Noto Sans CJK", Font.BOLD, 24));
        
        turnLabel = new JLabel(getLocalizedText("Waiting for turn info"), SwingConstants.CENTER);
        turnLabel.setFont(new Font("Noto Sans CJK", Font.BOLD, 18));

        timerLabel = new JLabel(getLocalizedText("Time left: "), SwingConstants.CENTER);
        timerLabel.setFont(new Font("Noto Sans CJK", Font.BOLD, 16));
        
        JPanel titlePanel = new JPanel(new GridLayout(3, 1));
        
        titlePanel.add(topCardLabel);
        titlePanel.add(turnLabel);
        titlePanel.add(timerLabel);
        add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
//        JLabel turnLabel = new JLabel(getLocalizedText("Waiting for turn..."));
        
        //opponent player
        opponentPanel = new JPanel();
        opponentLabel = new JLabel(getLocalizedText("opponent's current cards: 0"));
        opponentPanel.add(opponentLabel);
        centerPanel.add(opponentPanel, BorderLayout.NORTH);
        //player
        handPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        centerPanel.add(new JScrollPane(handPanel), BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        colorSelector = new JComboBox<>(new String[]{
            getLocalizedText("Red"),
            getLocalizedText("Blue"),
            getLocalizedText("Green"),
            getLocalizedText("Yellow")
        });
        colorSelector.setEnabled(false); 
//        bottomPanel.add(new JLabel("Choose color:"));
//        bottomPanel.add(colorSelector);
        add(bottomPanel, BorderLayout.SOUTH);      
        
        settingsButton = new JButton(getLocalizedText("Settings"));
        bottomPanel.add(settingsButton);
       
        settingsButton.addActionListener(e -> {
            String[] languages = {"English", "한국어", "中文"};
            String selectedLanguage = (String) JOptionPane.showInputDialog(
                    this,
                    getLocalizedText("Select your language:"),
                    getLocalizedText("Settings"),
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    languages,
                    languages[0]
            );

            if (selectedLanguage != null) {
                if (selectedLanguage.equals("한국어")) {
                    selectedLocale = Locale.KOREAN;
                } else if (selectedLanguage.equals("中文")) {
                    selectedLocale = Locale.CHINESE;
                } else {
                    selectedLocale = Locale.ENGLISH;
                }
                updateTexts();
            }
        });
    }
    
    //display player's handCards on GUI
    public void loadCardImages(ArrayList<Card> hand) {
        clearHandPanel();
        buttonIndexMap.clear();
        currentHandButtons.clear();
        lastReceivedHand = new ArrayList<>(hand);
        
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            String filename = getCardFilename(card);
            ImageIcon icon = loadScaledCardImage(filename); 
            
            if (icon == null) continue;

            JButton button = createCardButton(icon, card.getValue(), card.getSuit(), i);
            handPanel.add(button);
            buttonIndexMap.put(button, i);
            currentHandButtons.add(button);
        }

        handPanel.revalidate();
        handPanel.repaint();
        this.lastReceivedHand = hand; 
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
    
    private String colorWordToCode(String color) {
        return switch (color) {
            case "Red", "빨강", "红色" -> "r";
            case "Blue", "파랑", "蓝色" -> "b";
            case "Green", "초록", "绿色" -> "g";
            case "Yellow", "노랑", "黄色" -> "y";
            default -> "r"; // fallback
        };
    }

    private JButton createCardButton(ImageIcon icon, String value, String suit, int index) {
        JButton button = new JButton(icon);
        button.setPreferredSize(new Dimension(80, 120));
        
        button.addActionListener(e -> {
            int realIndex = -1;
            for (int i = 0; i < lastReceivedHand.size(); i++) {
                Card c = lastReceivedHand.get(i);
                if (c.getValue().equals(value) && c.getSuit().equals(suit)) {
                    realIndex = i;
                    break;
                }
            }

            if (realIndex == -1) {
                JOptionPane.showMessageDialog(this, "Card not found in hand list. Canceling play.");
                return;
            }

            if (value.equals("Wild") || value.equals("Draw 4")) {
                String[] colors = {
                    getLocalizedText("Red"),
                    getLocalizedText("Blue"),
                    getLocalizedText("Green"),
                    getLocalizedText("Yellow")
                };
                String selectedColor = (String) JOptionPane.showInputDialog(
                        this,
                        getLocalizedText("Choose a color:"),
                        getLocalizedText("Wild Card Color Selection"),
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        colors,
                        colors[0]
                );

                if (selectedColor == null) {
                    JOptionPane.showMessageDialog(this, "No color selected. Canceling play.");
                    return;
                }

                String colorCode = colorWordToCode(selectedColor);
                System.out.println("[DEBUG] Sending card: " + suit + " " + value + " (realIndex: " + realIndex + ")");
                client.sendPlayCard(realIndex, colorCode);
            } else {
                client.sendPlayCard(realIndex, null);
            }
//        handPanel.remove(button);
//        currentHandButtons.remove(button);
//        handPanel.revalidate();
//        handPanel.repaint();
        stopCountdown();
        enableCardClicks(false);
        showTurnMessage(false);
    });
        
        return button;
    }
    
    public boolean isMyTurn() {
        return isMyTurn;
    }
    
    public void setMyTurn(boolean myTurn) {
        this.isMyTurn = myTurn;
    }
    
    public void enableCardClicks(boolean enable) {
        for (JButton button : currentHandButtons) {
            button.setEnabled(enable);
        }
        if (enable) {
            turnLabel.setText(getLocalizedText("Your Turn!"));
        } else {
            turnLabel.setText(getLocalizedText("Opponent's Turn"));
        }
    }
    
    public void updateGameState(String topCardInfo, int opponentCardCount, boolean isMyTurn) {
        this.topCardInfo = topCardInfo;
        this.opponentCardCount = opponentCardCount;
        this.isMyTurn = isMyTurn;
        
        topCardLabel.setText(getLocalizedText("Top Card: ") + topCardInfo);
        opponentLabel.setText(getLocalizedText("Opponent's cards: ") + opponentCardCount);
        showTurnMessage(isMyTurn);
    }
    
    public void showTurnMessage(boolean isMyTurn) {
        if (isMyTurn) {
            turnLabel.setText(getLocalizedText("Your turn!"));
        } else {
            turnLabel.setText(getLocalizedText("Waiting for opponent..."));
        }
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
    
    public void startCountdown(int seconds) {
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }

        countdownTimer = new Timer();
        timerLabel.setText(getLocalizedText("Time left: ") + seconds + getLocalizedText("s"));

        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            int timeLeft = seconds;

            @Override
            public void run() {
                timeLeft--;
                SwingUtilities.invokeLater(() -> timerLabel.setText(getLocalizedText("Time left: ") + timeLeft + getLocalizedText("s")));

                if (timeLeft <= 0) {
                    countdownTimer.cancel();
                    
                SwingUtilities.invokeLater(() -> {
                    if (isMyTurn()) {
                        int index = getFirstPlayableCardIndex();
                        if (index != -1 && lastReceivedHand != null && index < lastReceivedHand.size()) {
                            Card card = lastReceivedHand.get(index);
                            String value = card.getValue();

                            if (value.equals("Wild") || value.equals("Draw 4")) {
                                String[] colorCodes = {"R", "G", "B", "Y"};
                                String randColor = colorCodes[new Random().nextInt(4)];
                                client.sendPlayCard(index, randColor);
                            } else {
                                client.sendPlayCard(index, null);
                            }

                            enableCardClicks(false);
                            showTurnMessage(false);
                            }
                        }
                    });   
                }
            }
        }, 1000, 1000);
    }
    
    public void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }
        SwingUtilities.invokeLater(() -> timerLabel.setText("")); 
    }
    
    public int getFirstPlayableCardIndex() {
        return currentHandButtons.isEmpty() ? -1 : 0;
    }

    public Card getCardFromHand(int index) {
        return lastReceivedHand.get(index);
    }
    
    private String getLocalizedText(String text) {
        return switch (selectedLocale.getLanguage()) {
            case "ko" -> switch (text) {
                case "Waiting for the game to start" -> "게임 시작을 기다리는 중";
                case "opponent's current cards: 0" -> "상대방의 현재 카드 수: 0";
                case "Top Card: Waiting..." -> "최상단 카드: 대기 중...";
                case "Waiting for turn info" -> "턴 정보 대기 중";
                case "Waiting for turn..." -> "턴 대기 중...";
                case "Opponent's cards: 0" -> "상대 카드 수: 0";
                case "Top Card: " -> "최상단 카드: ";
                case "Opponent's cards: " -> "상대 카드 수: ";
                case "Your Turn!" -> "당신의 차례입니다!";
                case "Opponent's Turn" -> "상대방 차례입니다";
                case "Your turn!" -> "당신의 차례입니다!";
                case "Waiting for opponent..." -> "상대방을 기다리는 중...";
                case "Game Over" -> "게임 종료";
                case "Choose a color:" -> "색상을 선택하세요:";
                case "Wild Card Color Selection" -> "와일드 카드 색상 선택";
                case "No color selected. Canceling play." -> "색상이 선택되지 않았습니다. 플레이를 취소합니다.";
                case "Red" -> "빨강";
                case "Blue" -> "파랑";
                case "Green" -> "초록";
                case "Yellow" -> "노랑";    
                case "Settings" -> "설정";
                case "Time left: " -> "남은 시간:";
                case "s" -> "초";
                case "New Game" -> "새 게임";
                case "Continue" -> "이어하기";
                case "Start a new game or continue?" -> "새 게임을 시작할까요, 아니면 이어서 할까요?";
                case "Enter game ID:" -> "게임 ID를 입력하세요:";
                case "Which player number are you? (0, 1, ...)" -> "당신은 몇 번 플레이어인가요? (0, 1, ...)";
                case "Your game ID is: " -> "당신의 게임 ID는: ";
                default -> text;
            };
            case "zh" -> switch (text) {
                case "Waiting for the game to start" -> "等待游戏开始";
                case "opponent's current cards: 0" -> "对手当前手牌数：0";
                case "Top Card: Waiting..." -> "顶牌: 等待中...";
                case "Waiting for turn info" -> "等待回合信息";
                case "Waiting for turn..." -> "等待回合...";
                case "Opponent's cards: 0" -> "对手手牌数: 0";
                case "Top Card: " -> "顶牌: ";
                case "Opponent's cards: " -> "对手手牌数: ";
                case "Your Turn!" -> "轮到你了！";
                case "Opponent's Turn" -> "对手回合";
                case "Your turn!" -> "轮到你了！";
                case "Waiting for opponent..." -> "等待对手...";
                case "Game Over" -> "游戏结束";
                case "Choose a color:" -> "选择颜色:";
                case "Wild Card Color Selection" -> "选择万能牌颜色";
                case "No color selected. Canceling play." -> "未选择颜色，取消出牌。";
                case "Red" -> "红色";
                case "Blue" -> "蓝色";
                case "Green" -> "绿色";
                case "Yellow" -> "黄色";   
                case "Settings" -> "设置";
                case "Time left: " -> "剩余时间: ";
                case "s" -> "秒";
                case "New Game" -> "新游戏";
                case "Continue" -> "继续";
                case "Start a new game or continue?" -> "要开始新游戏还是继续?";
                case "Enter game ID:" -> "请输入游戏 ID：";
                case "Which player number are you? (0, 1, ...)" -> "你是几号玩家？（0，1，...）";
                case "Your game ID is: " -> "你的游戏 ID 是：";
                default -> text;
            };
            default -> text;
        };
    }
    
    private void updateTexts() {
        topCardLabel.setText(getLocalizedText("Top Card: ") + topCardInfo);
        opponentLabel.setText(getLocalizedText("Opponent's cards: ") + opponentCardCount);
        showTurnMessage(isMyTurn);

        colorSelector.removeAllItems();
        colorSelector.addItem(getLocalizedText("Red"));
        colorSelector.addItem(getLocalizedText("Blue"));
        colorSelector.addItem(getLocalizedText("Green"));
        colorSelector.addItem(getLocalizedText("Yellow"));

        settingsButton.setText(getLocalizedText("Settings"));
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
