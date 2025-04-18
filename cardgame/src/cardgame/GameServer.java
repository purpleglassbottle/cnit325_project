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
    // Define players..
    ArrayList<Player> players = new ArrayList();
        
    Player currentPlayer;
    Player player0 = new Player(0, "Sean");
    Player player1 = new Player(1, "Dumbass");
    
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

    public void startNetwork(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                new Thread(handler).start();
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
    
    public void startGame(Game gameChoice) {
        // Create player list.
        players.add(player0);
        players.add(player1);
        
        game = gameChoice;
        
        // Create and shuffle the deck.
        game.buildDeck();
        game.shuffleCards();
        
        // Deal cards to players.
        game.setDealCount();
        game.dealCards(player0, game.getDealCount());
        game.dealCards(player1, game.getDealCount());
        
        // Create a discard pile.
        game.discard(null, 0);
    } // End public void startGame.
    
    public void changeTurn() {
        // Move to the next turn.
        if(isReversed == false) {
            // Move up the queue.
            if(turn == players.size() - 1) {
                turn = 0;
            } else {
                turn++;
            }
        } else {
            // Move down the queue.
            if(turn == 0) {
                turn = players.size() - 1;
            } else {
                turn--;
            }
        }
    }// End public void changeTurn.
    
    public void playGameUno() {
        while(gameOver == false) {
            // Set the Turn to the corresponding player in order.
            currentPlayer = (Player)players.get(turn);
            System.out.println("\nCurrent Player: " + currentPlayer.getPlayerName() + "\n");
            
            // Temporary hardcoded input values for testing
            int selectedIndex = 0;
            String chosenColor = "r";
            
            // Check for rule changes.
            if(isSkipped == true) {
                System.out.println("SKIPPED");
                
                this.changeTurn();
                
                isSkipped = false;
                
                continue;
            } else if(draw2 == true) {
                System.out.println("DRAW 2");
                
                game.dealCards(currentPlayer, 2);
                this.changeTurn();
                
                draw2 = false;
                
                continue;
            } else if(draw4 == true) {
                System.out.println("DRAW 4");
                
                game.dealCards(currentPlayer, 4);
                this.changeTurn();
                
                draw4 = false;
                
                continue;
            }
            
            // Have the player make a play.
            ruleChange = ((GameUno) game).playCard(currentPlayer, selectedIndex, chosenColor);
            
            // Check if the player has won.
            if(currentPlayer.getHand().isEmpty()) {
                System.out.println(currentPlayer.getPlayerName() + " has won!");
                
                gameOver = true;
            }
            
            // Check for a rule change.
            switch(ruleChange) {
                case 0: // No change. 
                    break;
                case 1: // Skip the next player.
                    isSkipped = true;
                    
                    break;
                case 2: // The next player must draw 2 cards.
                    draw2 = true;
                    
                    break;
                case 3: // Reverse the turn order.
                    isReversed = true;
                    
                    break;
                case 4: // The next player must draw 4 cards.
                    draw4 = true;
                    
                    break;
            } // End switch case.
            
            this.changeTurn();
        } // End while loop.
        
        // Reset gameOver.
        gameOver = false;
    } // End public void playGameUno.
    
    public static void main(String[] args) {
        // Define attributes.
        boolean sessionOver = false;
        
        // Scanner for reading input (replaces System.console().readLine())
        Scanner scanner = new Scanner(System.in);
        
        // Create a new session and start a game.
        GameServer server = new GameServer();
        
        while(sessionOver == false) {
            // Get the player's input from the console.
            System.out.println("Which game would you like to play?\n0. UNO\n1. Exit");
            String rawInput = scanner.nextLine();

            // Try to convert the player's input into an integer.
            try {
                int input = Integer.parseInt(rawInput);

                // Play the chosen game.
                switch(input) {
                    case 0: 
                        server.startGame(new GameUno());
                        server.playGameUno();

                        break;
                    case 1:
                        sessionOver = true;

                        break;
                } // End switch case.
            } catch(NumberFormatException e) {
                    System.out.println("INVALID INPUT");
            }
        } // End while loop.
    } // End public static void main.
} // End public class Session. 