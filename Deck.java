package cardgame;

/*
* Cassie Kim
* Xiaotong Luo
* Sean Maloney
* Emily Zhang
*/

public interface Deck {
    public void shuffleCards();
    
    public void dealCards(Player player, int dealCount);
} // End public interface Deck.