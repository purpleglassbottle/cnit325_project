package cardgame;

/*
* Cassie Kim
* Xiaotong Luo
* Sean Maloney
* Emily Zhang
*/

import java.util.*;

public class Player {
    // Define attributes.
    ArrayList<Card> hand = new ArrayList<Card>(); 
    
    int playerId;
    String playerName;
    
    // Constructors.
    public Player(int playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }
    
    // Getters.
    public ArrayList<Card> getHand() {
        return hand;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    // Setters.
    public void setPlayerId(int input) {
        this.playerId = input;
    }
    
    public void setPlayerName(String input) {
        this.playerName = input;
    }
    
    // Add a card to the hand.
    public void draw(Card newCard) {
        hand.add(newCard);
    } // End public void draw.
    
    // Print all cards in the hand.
    public void showHand() {
        for(int i = 0; i < hand.size(); i++) {
            System.out.println(i + ". " + hand.get(i).getSuit() + " " + hand.get(i).getValue());
        }
    } // End public void showHand.
} // End public class Player.