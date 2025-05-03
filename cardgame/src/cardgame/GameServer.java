package cardgame;

/*
* Cassie Kim
* Xiaotong Luo
* Sean Maloney
* Emily Zhang
*/

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import com.google.gson.Gson;
import java.util.Optional;


public class GameServer {    
    //added s3
    private final SaveGame storage = new SaveGame("cnit325project", "us-east-1");
    
//    private final String gameId = "room1";
    // emily
    private String gameId; // Dynamic
    
    // Define players.
    ArrayList<Player> players = new ArrayList();        
    //Initiate 2 players
//    Player player0 = new Player(0, "Sean");
//    Player player1 = new Player(1, "Dumbass");
    
    // Define game attributes.
    Game game;
    int turn = 0;
    boolean gameOver = false;
    int ruleChange = 0;
    // emily 
    // running game
    private boolean gameStarted = false;
    //setting play direction for reverses
    private boolean clockwise = true; 
    private int expectedPlayers = 0;
    private int readyPlayers = 0;

    //networking
    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clients = new ArrayList<>(); 
    
    // Localization
    private Locale selectedLocale = Locale.ENGLISH;
    
    // Timer
    private Timer turnTimer;
    
    public void startNetwork(int port) {

//        String[] languages = {"English", "한국어", "中文"};
//        String inputLanguage = (String) JOptionPane.showInputDialog(
//                null,
//                "Select Language / 언어 선택 / 选择语言",
//                "Language",
//                JOptionPane.QUESTION_MESSAGE,
//                null,
//                languages,
//                languages[0]
//        );
//
//        if (inputLanguage == null) {
//            System.exit(0);
//        }
//
//        if (inputLanguage.equals("한국어")) {
//            selectedLocale = Locale.KOREAN;
//        } else if (inputLanguage.equals("中文")) {
//            selectedLocale = Locale.CHINESE;
//        } else {
//            selectedLocale = Locale.ENGLISH;
//        }
        // more s3 - restore a saved game
//        Optional<String> maybeJson = storage.load(gameId);
//        
//        if(maybeJson.isPresent()){
//            GameState restored = new Gson().fromJson(maybeJson.get(), GameState.class);
//            restoreFrom(restored);
//            System.out.println("[SERVER] restored game with s3");
//        }
        
        
        String[] options = {"2", "3", "4"};
        String input = (String) JOptionPane.showInputDialog(
                null,
                "Select Number of Players / 플레이어 수 선택 / 选择玩家数量",
                "Player Setup",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (input == null) {
            System.exit(0);
        }
        
        expectedPlayers = Integer.parseInt(input);
        
        Optional<String> maybeJson = (gameId != null) ? storage.load(gameId) : Optional.empty();
        if (maybeJson.isPresent()) {
            GameState restored = new Gson().fromJson(maybeJson.get(), GameState.class);
            restoreFrom(restored);
            System.out.println("[SERVER] restored game with s3");
        }
            
        try {   
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            // emily
            // player id for tracking
            int nextId = 0;
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                if (gameStarted || clients.size() >= expectedPlayers) {
                    PrintWriter tempOut = new PrintWriter(clientSocket.getOutputStream(), true);
                    tempOut.println("Game already started. You can't join now.");
                    
                    try {
                        Thread.sleep(100); 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    clientSocket.close();
                    continue; 
                }
                
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                // emily
                // player object handling
//                Player p = new Player(nextId, "Player" + nextId);
                // emily
                //handle player and track
//                handler.setPlayer(p);
//                players.add(p);
                
                new Thread(handler).start();
                // emily 
                // notify player has joined
                
//                broadcast("[Server]" + p.getPlayerName() + " joined");
//                nextId++;
//                
//                // launching with the players
//                if (!gameStarted &&f players.size() == expectedPlayers) {
//                    startGame(new GameUno());
//                    sendTurnPrompt();
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    // emily
//    public synchronized void registerPlayer(ClientHandler handler) {
//        Player p = new Player(players.size(), "Player" + players.size());
//        handler.setPlayer(p);
//        players.add(p); 
//
//        broadcast("[Server] " + p.getPlayerName() + " joined");

//        if (!gameStarted && players.size() == expectedPlayers) {
//            startGame(new GameUno());
//            sendTurnPrompt();
//        }
//    }    

    public synchronized boolean registerPlayer(ClientHandler h, int id) {
        if (id < 0) return false;                       // sanity
        for (ClientHandler c : clients)                 // slot taken?
            if (c.getPlayer() != null && c.getPlayer().getPlayerId() == id)
                return false;

        Player p;
        if (id < players.size() && players.get(id) != null) {                     // loaded game → reuse
            p = players.get(id);
        } else {                                       // fresh game → create
            p = new PlayerUno(id, "Player" + id);
            while (players.size() <= id) players.add(null);
            players.set(id, p);
        }

        h.setPlayer(p);
        broadcast("[Server] " + p.getPlayerName() + " joined / re-joined");
        return true;
    }

    
    // Called when the client sends a READY signal
    public synchronized void playerReady() {
        readyPlayers++;
        System.out.println("Ready players: " + readyPlayers + "/" + expectedPlayers);

//        if (!gameStarted && readyPlayers == expectedPlayers) {
//            startGame(new GameUno());
//            sendTurnPrompt();
//        }
        if (!gameStarted && readyPlayers == expectedPlayers) {
            if (game == null) {                  // fresh lobby
                startGame(new GameUno());
            } else {                             // restored game
                gameStarted = true;
                broadcastGameState();
                sendTurnPrompt();
            }
        }

    }
    
    public synchronized boolean configureGame(String id, boolean loadExisting) {
        if (gameId != null) return false;          // already set
        gameId = id;
        if (loadExisting) {
            storage.load(gameId).ifPresent(json -> {
                GameState st = new Gson().fromJson(json, GameState.class);
                restoreFrom(st);
                System.out.println("[SERVER] restored game " + gameId);
            });
        }
        return true;
    }

    public void startGame(Game gameChoice) {
        // emily 
        //clear pre-existing players if there are any
//        players.clear();        
        // Create player list.
//        players.add(player0);
//        players.add(player1);
        
        game = gameChoice;        
        // Create and shuffle the deck.
        game.buildDeck();
        System.out.println("Deck built with size: " + game.deck.size());
        game.shuffleCards();
        game.setDealCount();
        // emily
        for (Player p : players){
            game.dealCards(p, game.getDealCount());
        }
        // Create a discard pile.
        ((GameUno) game).discardRandomTopCardFromDeck();
        
        if (game.getTopCard() == null) {
            System.out.println("[SERVER] topCard is STILL null after discard()!!");
        } else {
            System.out.println("[SERVER] topCard set to: " + game.getTopCard().getSuit() + " " + game.getTopCard().getValue());
        }
        
        sendInitialHands();

        gameStarted = true;
        System.out.println("Server is going to broadcast the initial GameState");
        broadcastGameState();
        
        sendTurnPrompt();
    } // End public void startGame.    
    
    private void sendInitialHands() {
        for (int i = 0; i < clients.size(); i++) {
            ClientHandler handler = clients.get(i);
            Player player = players.get(i);
            System.out.println("[SERVER] Sending HAND to Player" + i);
            handler.sendHand(player.getHand());
        }
    }

    public void broadcast(String msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

//    public ArrayList<ClientHandler> getClients() {
//        return clients;
//    }

    private void broadcastGameState() {
        for (ClientHandler client : clients) {
            Player current = client.getPlayer(); 
            int opponentCardCount = 0;
            
            System.out.println("[SERVER] Sending HAND to " + current.getPlayerName());
            
            Player opponent = null;
            for (Player p : players) {
                if (!p.equals(current)) { 
                    opponent = p;
                    break;
                }
            }
            
            if (opponent == null) {
                opponent = current; 
            }
            
            opponentCardCount = opponent.getHand().size();
            
            System.out.println("[SERVER] Sending HAND to " + current.getPlayerName() + ": " + serializeCards(current.getHand()));
            // Send hand
            client.sendMessage("HAND:" + serializeCards(current.getHand()));

            String topSuit = game.getTopCard().getSuit();
            String topValue = game.getTopCard().getValue();
            String state = "STATE:" + (client == clients.get(turn) ? "*" : "") + topSuit + " " + topValue + "|" + opponentCardCount;

            System.out.println("[SERVER] Sending STATE to " + current.getPlayerName() + ": " + state);
            // Send game state
            client.sendMessage(state);
        }
    }    
    
    private String serializeCards(ArrayList<Card> cards) {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            //testing null
            if(card.getSuit() == null || card.getValue() == null){
                System.out.println("Error: Card has null suit or value");
            }
            sb.append(card.getSuit()).append(",")
              .append(card.getValue()).append(";");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
    }
    
    //  emily play flow control ? 
    public void changeTurn() {
        if(!clockwise){
            turn = (turn - 1 + players.size()) % players.size();
        } else{
            turn = (turn + 1) % players.size();
        }
    }

    // emily
    // telling a player it's their turn
    private void sendTurnPrompt(){
        Player currentPlayer = players.get(turn);
        
        // Cassie
        if (!hasPlayableCard(currentPlayer)) {
            System.out.println("[Server] " + currentPlayer.getPlayerName() + " has no playable card. Drawing...");

            game.dealCards(currentPlayer, 1);
            broadcast("[Server] " + currentPlayer.getPlayerName() + " drew a card (no playable).");
            broadcastGameState();

            if (!hasPlayableCard(currentPlayer)) {
            System.out.println("[Server] " + currentPlayer.getPlayerName() + " still cannot play after draw.");
            changeTurn();
            sendTurnPrompt();
            } else {
            clients.get(turn).sendMessage("Your turn");
            clients.get(turn).sendMessage("TIMER_START:10");
            startTurnTimer(currentPlayer);
            }
            
        } else {
        clients.get(turn).sendMessage("Your turn");
        clients.get(turn).sendMessage("TIMER_START:10");
        startTurnTimer(currentPlayer);
        }

//        for (ClientHandler client : clients) {
//            if (client.getPlayer() == currentPlayer) {
//                client.sendMessage("Your turn");
//                System.out.println("[SERVER] Sent Your Turn to: " + currentPlayer.getPlayerName());
//                break;
//            }
//        }
    }
    
    // Cassie
    private void startTurnTimer(Player player) {
        if (turnTimer != null) {
            turnTimer.cancel(); 
        }

        turnTimer = new Timer();
        turnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("[Server] " + player.getPlayerName() + " ran out of time!");
                
                int playerIdx = players.indexOf(player);

                if (playerIdx == -1) {
                    System.out.println("[Server] Player no longer exists.");
                    return;
                }            
                
                ClientHandler clientHandler = clients.get(playerIdx);

                if (hasPlayableCard(player)) {
                    for (int i = 0; i < player.getHand().size(); i++) {
                        Card c = player.getHand().get(i);
                        Card top = game.getTopCard();
                        if (c.getSuit().equals(top.getSuit()) || c.getValue().equals(top.getValue()) || 
                            c.getValue().equals("Wild") || c.getValue().equals("Draw 4")) {
                            System.out.println("[Server] Auto-playing card for " + player.getPlayerName());
                            processPlay(clients.get(turn), i, null);
                            return;
                        }
                    }
                } else {
                    game.dealCards(player, 1);
                    broadcast("[Server] " + player.getPlayerName() + " drew a card (timeout).");
                    broadcastGameState();
                    changeTurn();
                    sendTurnPrompt();
                }
            }
        }, 10000); 
    }
    
    // Cassie
    private boolean hasPlayableCard(Player player) {
        Card top = game.getTopCard();
        for (Card c : player.getHand()) {
            if (c.getSuit().equals(top.getSuit()) || c.getValue().equals(top.getValue()) ||
                c.getValue().equals("Wild") || c.getValue().equals("Draw 4")) {
                return true;
            }
        }
        return false;
    }
    
    // emily
    public synchronized void processPlay(ClientHandler src, int idx, String color){
        if (!gameStarted) {
            src.sendMessage("play rejected: The game has not started");     
            return;
        }
        if (clients.get(turn) != src) {                     
            src.sendMessage("play rejected: Not your turn");  
            return;
        }
        
        Player player = src.getPlayer();
        int result = ((GameUno) game).playCard(player, idx, color);
        System.out.println("[DEBUG] processPlay() received index: " + idx + ", interpreted as: " + game.getTopCard().getSuit() + " " + game.getTopCard().getValue());
        
        if (result == -1) {
            src.sendMessage("ERROR:Invalid move");
            
            // Cassie
            game.dealCards(player, 1); 
            broadcast("[Server] " + player.getPlayerName() + " drew a card.");
            
            changeTurn();
            broadcastGameState();  
            changeTurn();
            sendTurnPrompt();           
            
            return;
        }
        
        broadcast(player.getPlayerName() + " played " + 
                 game.getTopCard().getSuit() + " " + 
                 game.getTopCard().getValue());
        
        if (player.getHand().isEmpty()) {
            endGame(player.getPlayerName() + " wins!");
            return;
        }
        
        applyRuleEffects(result);
        
        broadcastGameState();   
        sendTurnPrompt();
        
        System.out.println("[DEBUG] TURN is now: " + turn + " / Direction: " + (clockwise ? "→" : "←"));
        
        // save after every move is done
        GameState snapshot = captureCurrentState();
        storage.save(gameId, new Gson().toJson(snapshot));
    }
    
    // emily
    private void applyRuleEffects(int rule) {              
        switch (rule) {
            case 1:  // Skip                            
                broadcast("[Server] Player skipped."); 
                // Cassie
                changeTurn(); 
                changeTurn(); 
//                broadcastGameState();  
//                sendTurnPrompt();    
                break;
            case 2:  // Draw 2
//                int nextTurn = (turn + 1) % players.size();
                Player p2 = players.get(turn);
                
                game.dealCards(p2, 2);                     
                broadcast(p2.getPlayerName() + " draws 2.");
                
//                broadcastGameState(); 
//                sendTurnPrompt();    

//                turn = nextTurn;
                changeTurn(); 
                break;
//                changeTurn();
//                Player p2 = players.get(turn);              
//                game.dealCards(p2, 2);                     
//                broadcast(p2.getPlayerName() + " draws 2.");    
//                changeTurn();
//                break;
            case 3:  // Reverse
                clockwise = !clockwise;                     
                broadcast("[Server] Direction reversed.");  
                // Cassie
                if (players.size() == 2) {
                    changeTurn();
                }                
//                broadcastGameState();   
//                sendTurnPrompt();       
                break;
            case 4:  // Draw 4
//                int nextTurn4 = (turn + 1) % players.size();
                changeTurn();
                Player p4 = players.get(turn);

                game.dealCards(p4, 4);
                broadcast(p4.getPlayerName() + " draws 4.");

//                broadcastGameState();
//                sendTurnPrompt();

//                turn = nextTurn4;
                changeTurn();
                break;
//                changeTurn();
//                Player p4 = players.get(turn);              
//                game.dealCards(p4, 4);                      
//                broadcast(p4.getPlayerName() + " draws 4."); 
//                changeTurn();
//                break;
            // Cassie
            default:
                changeTurn(); 
                break;
        }
        broadcastGameState();  
        sendTurnPrompt();  
    }
    
    // emily
//    public synchronized void removeClient(ClientHandler c) { 
//        int idx = clients.indexOf(c);                     
//        if (idx >= 0 && idx < clients.size()) {
//            clients.remove(idx);                            
//            players.remove(idx);                            
//            broadcast("[Server] " + players.get(idx).getPlayerName() + " disconnected");  
//            if (clients.size() < 2 && gameStarted) {        
//                endGame("Not enough players.");            
//            }        
//        }
//    }
    
    // emily
    public void endGame(String reason) {                   
        broadcast("[Server] Game over: " + reason);        
        // Cassie
        broadcast("END_GAME " + reason + " (ID: " + gameId + ')');
//        broadcast("END_GAME " + reason);
        gameStarted = false;                                
    }
    
    //get the state and restore it 
    private GameState captureCurrentState() {
        List<PlayerState> ps = new ArrayList<>();
        for (Player p : players) {
            // make a copy of the hand
            ps.add(new PlayerState(p.getPlayerId(),new ArrayList<>(p.getHand())));
        }
        return new GameState(
            ps, new ArrayList<>(game.deck), new ArrayList<>(game.discardPile), turn, clockwise);
    }

    private void restoreFrom(GameState st) {
        // restore players
        players.clear();
        for (PlayerState ps : st.players) {
            Player p = new PlayerUno(ps.playerId, "Player" + ps.playerId);
            p.getHand().addAll(ps.hand);
            players.add(p);
        }

        // restore game internals
        game = new GameUno();
        game.deck.clear();
        game.deck.addAll(st.deck);
        game.discardPile.clear();
        game.discardPile.addAll(st.discardPile);
        
        if (!game.discardPile.isEmpty()) {
            game.setTopCard(game.discardPile.get(game.discardPile.size() - 1));
        }


        turn = st.turn;
        clockwise = st.clockwise;
        gameStarted = false;          // let players re-join first
        readyPlayers = 0;             // they’ll send READY again
        expectedPlayers = players.size();

    }
    
    //playGameUno() deleted
    
    private String getLocalizedText(String text) {
        return switch (selectedLocale.getLanguage()) {
            case "ko" -> switch (text) {
                case "How many players?" -> "몇 명이 플레이할까요?";
                case "Player Count Selection" -> "플레이어 수 선택";
                case "Select Language" -> "언어 선택";
                case "Language" -> "언어";
                default -> text;
            };
            case "zh" -> switch (text) {
                case "How many players?" -> "要几个人玩？";
                case "Player Count Selection" -> "玩家数量选择";
                case "Select Language" -> "选择语言";
                case "Language" -> "语言";
                default -> text;
            };
            default -> text;
        };
    }

    public static void main(String[] args) {
        new GameServer().startNetwork(12345);
    }//End public static void main()
} // End public class Session. 
