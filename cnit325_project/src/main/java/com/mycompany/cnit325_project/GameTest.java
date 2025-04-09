package cardgame;

/*
* Cassie Kim
* Xiaotong Luo
* Sean Maloney
* Emily Zhang
*/

public class GameTest extends Game {
    public void buildDeck() {
        deck.add(new Card("Ace", "Spades"));
        deck.add(new Card("2", "Spades"));
        deck.add(new Card("3", "Spades"));
        deck.add(new Card("4", "Spades"));
        deck.add(new Card("5", "Spades"));
        deck.add(new Card("6", "Spades"));
        deck.add(new Card("7", "Spades"));
        deck.add(new Card("8", "Spades"));
        deck.add(new Card("9", "Spades"));
        deck.add(new Card("10", "Spades"));
        deck.add(new Card("Jack", "Spades"));
        deck.add(new Card("Queen", "Spades"));
        deck.add(new Card("King", "Spades"));
    }
}