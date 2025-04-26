package cardgame;

/*
* Sean Maloney
* Friday 1:30pm
* @author seanm
*/

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return value.equals(card.value) && suit.equals(card.suit);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(value, suit);
    }
}