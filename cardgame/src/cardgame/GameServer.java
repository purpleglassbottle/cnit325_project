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

public class GameServer {    
    // Define players.
    ArrayList<Player> players = new ArrayList();        
    //Player currentPlayer;
    Player player0 = new Player(0, "Sean");
    Player player1 = new Player(1, "Dumbass");
    
    // Define game attributes.
    Game game;
    int turn = 0;
    boolean gameOver = false;
    int ruleChange = 0;

    //networking
    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clients = new ArrayList<>();
    
    // emily 
    // running game
    private boolean gameStarted = false;
    //setting play direction for reverses
    private boolean clockwise = true; 

    
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
                Player p = (nextId == 0) ? player0:
                        (nextId == 1) ? player1 :
                               new Player(nextId, "Player" + nextId);
                // emily
                //handle player and track
                handler.setPlayer(p);
                players.add(p);
                
                new Thread(handler).start();
                // emily 
                // notify player has joined
                broadcast("[Server]" + p.getPlayerName() + " joined");
                nextId++;
                
                // launching with 2 players
                if(!gameStarted && players.size() == 2){
                    startGame(new GameUno());
                    //sendTurnPrompt();
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
    
    public void startGame(Game gameChoice) {
        // emily 
        //clear pre-existing players if there are any
        players.clear();        
        // Create player list.
        players.add(player0);
        players.add(player1);
        
        game = gameChoice;        
        // Create and shuffle the deck.
        game.buildDeck();
        game.shuffleCards();
        game.setDealCount();
        // emily
        for (Player p : players){
            game.dealCards(p, game.getDealCount());
        }
        
        // Create a discard pile.
        game.discard(null, 0);

        gameStarted = true;
        System.out.println("Server is going to broadcast the initial GameState");
        broadcastGameState();
    } // End public void startGame.
    
    private void broadcastGameState() {
        for (int i = 0; i < clients.size(); i++) {
            ClientHandler client = clients.get(i);
            Player current = players.get(i);
            Player opponent = players.get((i + 1) % 2);
            
            // Send hand
            client.sendMessage("HAND:" + serializeCards(current.getHand()));
            
            // Send game state
            client.sendMessage("STATE:" + 
                game.getTopCard().getSuit() + " " + 
                game.getTopCard().getValue() + "|" + 
                opponent.getHand().size());
        }
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
        
        Player player = src.getPlayer();
        int result = ((GameUno) game).playCard(player, idx, color);
        
        if (result == -1) {
            src.sendMessage("ERROR:Invalid move");
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
        if (idx >= 0 && idx < clients.size()) {
            clients.remove(idx);                            
            players.remove(idx);                            
            broadcast("[Server] " + players.get(idx).getPlayerName() + " disconnected");  
            if (clients.size() < 2 && gameStarted) {        
                endGame("Not enough players.");            
            }        
        }
    }
    
    // emily
    public void endGame(String reason) {                   
        broadcast("[Server] Game over: " + reason);         
        gameStarted = false;                                
    }
    
    //playGameUno() deleted

    public static void main(String[] args) {
        new GameServer().startNetwork(12345);
    }//End public static void main()
} // End public class Session. 
