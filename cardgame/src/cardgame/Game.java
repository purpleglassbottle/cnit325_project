package cardgame;

/*
* Cassie Kim
* Xiaotong Luo
* Sean Maloney
* Emily Zhang
*/

import java.util.*;

public abstract class Game implements Deck {
    // Define attributes.
    protected ArrayList<Card> deck = new ArrayList();
    protected ArrayList<Card> discardPile = new ArrayList<>();
    
    protected Card topCard;
    protected int dealCount;
    
    // Getters.
    public Card getTopCard() {
        return topCard;
    }
    
    public int getDealCount() {
        return dealCount;
    }
    
    // Setters.
    public void setTopCard(Card input) {
        this.topCard = input;
    }
    
    // Overwritten by children.
    public void setDealCount() {
        
    }
    
    // Build the deck. Overwritten by children.
    public void buildDeck() {
        
    } // End public void buildDeck.
    
    // Add to the discard pile. Overwritten by children.
    public abstract void discard(Player currentPlayer, Card playedCard); 
//    {
//        if (currentPlayer == null) {
//            do {
//                topCard = deck.remove(0);
//            } while (topCard.getValue().equals("Wild") || topCard.getValue().equals("Draw 4"));
//            discardPile.add(topCard);
//            return;
//        }
//
//        topCard = currentPlayer.getHand().remove(index);
//        discardPile.add(topCard);        
//    } // End public void discard.
    
    // Play a card. Overwritten by children.
    public int playCard(Player currentPlayer) {
        return 0;
    } // End public void playCard.
    
    // Shuffle the deck. From the Deck interface.
    public void shuffleCards() {
        Collections.shuffle(deck);
    } // End public void shuffleCards.
    
    // Deal from the deck. From the Deck interface.
    public void dealCards(Player player, int dealCount) {
        //xiaotong 4/24
        //fix: change the i to 0
        for(int i = 0; i < dealCount; i++) {
            player.draw(deck.get(0));//used to be i
            deck.remove(0);//used to be i
        }
    } // End publuc void dealCards.  
} // End public class Game.
