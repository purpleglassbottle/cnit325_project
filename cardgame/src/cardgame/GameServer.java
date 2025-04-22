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
import java.util.Scanner;
import java.util.stream.Collectors;

public class GameServer {
    // Define players.
    ArrayList<Player> players = new ArrayList();
        
    Player currentPlayer;
    
    // Define game attributes.
    Game game;
    int turn = 0;
    boolean gameOver = false;
    int ruleChange = 0;
    
    // Define rule change flags.
    boolean isSkipped = false;
    boolean isReversed = false;
    boolean draw2 = false;
    boolean draw4 = false;
    
    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clients = new ArrayList<>();
    private int expectedPlayers = 2;
    
    // emily 
    // running game
    private boolean gameStarted = false;
    //setting play direction for reverses
    private boolean clockwise = true; 

    public void setExpectedPlayers(int expected) {
        this.expectedPlayers = expected;
    }
    
    public void startNetwork(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            // emily
            // player id for tracking
            int nextId = 0;
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                // emily
                // player object handling
//                Player p = (nextId == 0) ? player0:
//                        (nextId == 1) ? player1 :
//                               new Player(nextId, "Player" + nextId);
                
                // Cassie
                Player p = new Player(nextId, "Player" + nextId);
                // emily
                // handle player and track
                handler.setPlayer(p);
                players.add(p);
                
                handler.initializeStreams();
                
                new Thread(handler).start();
                // emily 
                // notify player has joined
                broadcast("[Server]" + p.getPlayerName() + " joined");
                nextId++;
                
                // launching with the number of players
                if(!gameStarted && players.size() == expectedPlayers){
                    startGame(new GameUno());
                    sendTurnPrompt();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    public ArrayList<ClientHandler> getClients() {
        return clients;
    }
    
    public void sendInitialHandsToClients() {
        for (int i = 0; i < clients.size(); i++) {
            ClientHandler ch = clients.get(i);
            Player p = players.get(i);
            String handStr = p.getHand().stream()
                .map(c -> c.getValue() + "_" + c.getSuit())
                .collect(Collectors.joining(","));
            ch.sendMessage("HAND " + handStr);
        }
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
        game.shuffleCards();
        
        // Deal cards to players.
        game.setDealCount();
        // emily
        for (Player p : players){
            game.dealCards(p, game.getDealCount());
        }
        
//        game.dealCards(player0, game.getDealCount());
//        game.dealCards(player1, game.getDealCount());
        
        // Create a discard pile.
        game.discard(null, 0);
        // emily
        // notifying the server
        broadcast("[Server] Game started! Top card is "
                + game.getTopCard().getSuit() + " "
                + game.getTopCard().getValue());
        gameStarted = true;
        
        // Cassie
        // Send initial hand to each client
        for (int i = 0; i < clients.size(); i++) {
            ClientHandler ch = clients.get(i);
            Player p = players.get(i);
            for (Card card : p.getHand()) {
                ch.sendMessage("CARD " + card.getValue() + " " + card.getSuit());
//                System.out.println("[Server Send] CARD " + card.getValue() + " " + card.getSuit());
            }
            ch.sendMessage("HAND_DONE");
        }
        
        // Send the current top card to all clients after dealing hands
        Card top = game.getTopCard();
        for (ClientHandler ch : clients) {
            ch.sendMessage("TOP_CARD " + top.getValue() + " " + top.getSuit());
        }
    } // End public void startGame.
    
    
    //  emily play flow control ? 
    public void changeTurn() {
        if(!clockwise){
            turn = (turn - 1 + players.size()) % players.size();
        } else{
            turn = (turn + 1) % players.size();
        }
    }
    
    // emily
    // telling a player its their turn
    private void sendTurnPrompt(){
        clients.get(turn).sendMessage("Your turn");
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
        
        int result = ((GameUno) game).playCard(src.getPlayer(), idx, color); 
        if (result == -1) {                                 
            src.sendMessage("playe rejected: Against the rules");    
            return;
        }
        
        broadcast("played " + src.getPlayer().getPlayerName()
                + " played " + game.getTopCard().getSuit() + " "
                + game.getTopCard().getValue());            
        
        if (src.getPlayer().getHand().isEmpty()) {         
            endGame(src.getPlayer().getPlayerName() + " wins!"); 
            return;
        }
        
        applyRuleEffects(result);                          
        changeTurn();                                       
        sendTurnPrompt();
    }
    
    // emily
    private void applyRuleEffects(int rule) {              
        switch (rule) {
            case 1:  // Skip
                changeTurn();                               
                broadcast("[Server] Player skipped.");      
                break;
            case 2:  // Draw 2
                Player p2 = players.get(turn);              
                game.dealCards(p2, 2);                     
                broadcast(p2.getPlayerName() + " draws 2."); 
                break;
            case 3:  // Reverse
                clockwise = !clockwise;                     
                broadcast("[Server] Direction reversed.");  
                break;
            case 4:  // Draw 4
                Player p4 = players.get(turn);              
                game.dealCards(p4, 4);                      
                broadcast(p4.getPlayerName() + " draws 4."); 
                break;
        }
    }
    
    // emily
    public synchronized void removeClient(ClientHandler c) { 
        int idx = clients.indexOf(c);                     
        if (idx >= 0) {
            clients.remove(idx);                            
            players.remove(idx);                            
            broadcast("[Server] A player disconnected.");   
            if (clients.size() < 2 && gameStarted) {        
                endGame("Not enough players.");            
            }
            if (turn >= clients.size()) turn = 0;           
        }
    }
    
    // emily
    public void endGame(String reason) {                   
        broadcast("[Server] Game over: " + reason);         
        gameStarted = false;                                
    }

      // Cassie
      // Old console-based game loop. Not used anymore.
//    public void playGameUno() {
//        while(gameOver == false) {
//            // Set the Turn to the corresponding player in order.
//            currentPlayer = (Player)players.get(turn);
//            System.out.println("\nCurrent Player: " + currentPlayer.getPlayerName() + "\n");
//            
//            // Temporary hardcoded input values for testing
//            int selectedIndex = 0;
//            String chosenColor = "r";
//            
//            // Check for rule changes.
//            if(isSkipped == true) {
//                System.out.println("SKIPPED");
//                
//                this.changeTurn();
//                
//                isSkipped = false;
//                
//                continue;
//            } else if(draw2 == true) {
//                System.out.println("DRAW 2");
//                
//                game.dealCards(currentPlayer, 2);
//                this.changeTurn();
//                
//                draw2 = false;
//                
//                continue;
//            } else if(draw4 == true) {
//                System.out.println("DRAW 4");
//                
//                game.dealCards(currentPlayer, 4);
//                this.changeTurn();
//                
//                draw4 = false;
//                
//                continue;
//            }
//            
//            // Have the player make a play.
//            ruleChange = ((GameUno) game).playCard(currentPlayer, selectedIndex, chosenColor);
//            
//            // Check if the player has won.
//            if(currentPlayer.getHand().isEmpty()) {
//                System.out.println(currentPlayer.getPlayerName() + " has won!");
//                
//                gameOver = true;
//            }
//            
//            // Check for a rule change.
//            switch(ruleChange) {
//                case 0: // No change. 
//                    break;
//                case 1: // Skip the next player.
//                    isSkipped = true;
//                    
//                    break;
//                case 2: // The next player must draw 2 cards.
//                    draw2 = true;
//                    
//                    break;
//                case 3: // Reverse the turn order.
//                    isReversed = true;
//                    
//                    break;
//                case 4: // The next player must draw 4 cards.
//                    draw4 = true;
//                    
//                    break;
//            } // End switch case.
//            
//            this.changeTurn();
//        } // End while loop.
//        
//        // Reset gameOver.
//        gameOver = false;
//    } // End public void playGameUno.
    
    public static void main(String[] args) {
        // Create a new session and start a game.
        GameServer server = new GameServer();
        // Connect to a Server
        server.startNetwork(12345);
        
    } // End public static void main.
} // End public class Session. 