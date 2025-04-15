package cardgame;

/*
* Sean Maloney
* Friday 1:30pm
* @author seanm
*/

public class Card {
    // Define attributes.
    String value, suit;
    
    // Constructors.
    public Card(String value, String suit) {
        this.value = value;
        this.suit = suit;
    }
    
    // Getters.
    public String getValue() {
        return value;
    }
    
    public String getSuit() {
        return suit;
    }
    
    // Setters.
    public void setValue(String input) {
        this.value = input;
    }
    
    public void setSuit(String input) {
        this.suit = input;
    }
}