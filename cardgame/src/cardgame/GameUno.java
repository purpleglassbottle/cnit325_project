package cardgame;

/*
* Cassie Kim
* Xiaotong Luo
* Sean Maloney
* Emily Zhang
*/

public class GameUno extends Game {
    
    //xiaotong 4/24
    public void setTopCardColor(String color) {
        topCard.setSuit(color); // Update the top card's suit to the new color
    }

    public void setDealCount() {
        this.dealCount = 7;
    }

    public void buildDeck() {
        String color = "";

        for (int i = 0; i <= 3; i++) {
            switch (i) {
                case 0: color = "Red"; break;
                case 1: color = "Blue"; break;
                case 2: color = "Green"; break;
                case 3: color = "Yellow"; break;
            }

            for (int j = 0; j <= 1; j++) {
                deck.add(new Card("1", color));
                deck.add(new Card("2", color));
                deck.add(new Card("3", color));
                deck.add(new Card("4", color));
                deck.add(new Card("5", color));
                deck.add(new Card("6", color));
                deck.add(new Card("7", color));
                deck.add(new Card("8", color));
                deck.add(new Card("9", color));
                deck.add(new Card("Skip", color));
                deck.add(new Card("Draw 2", color));
                deck.add(new Card("Reverse", color));
            }

            deck.add(new Card("Wild", "Black"));
            deck.add(new Card("Draw 4", "Black"));           
        }
    }

    //xiaotong
    //updated on 4/24
    // Cassie
    // Fixed some logics
    public void discard(Player currentPlayer, Card playedCard) {
        if (currentPlayer != null) {
            currentPlayer.getHand().remove(playedCard);
            topCard = playedCard;
        } else {
            topCard = new Card(deck.get(0).getValue(), deck.get(0).getSuit()); 
            deck.remove(0);
        
            // 
            if (topCard.getValue().equals("Wild") || topCard.getValue().equals("Draw 4")) {
                topCard.setSuit("Black");  
            } else if (topCard.getValue().equals("Skip") || topCard.getValue().equals("Draw 2")) {
                if (!deck.isEmpty()) {
                    discard(null, 0); 
                }
              }
        }
        //to check whether topCard has been updated
        System.out.println("New top card: " + topCard.getValue() + " " + topCard.getSuit());
    }
    
    //deleted by xiaotong for now
    /*public void discard(Player currentPlayer, int index) {
        if (currentPlayer != null) {
            topCard = currentPlayer.getHand().remove(index); 
        } else {
            topCard = deck.get(index);
            if (topCard.getValue().equals("Skip") || topCard.getValue().equals("Draw 2") ||
                topCard.getValue().equals("Reverse") || topCard.getValue().equals("Wild") ||
                topCard.getValue().equals("Draw 4")) {
                this.shuffleCards();
                this.discard(null, 0);
            } else {
                deck.remove(index);
            }
        }
        //to check whether topCard has been updated
        System.out.println("New top card: " + topCard.getValue() + " " + topCard.getSuit());
    }
    */
    public int playCard(Player currentPlayer, int index, String chosenColor) {
        if (index < 0 || index >= currentPlayer.getHand().size()) return -1;

        Card play = currentPlayer.getHand().get(index);
        String playedValue = play.getValue();
        String playedSuit = play.getSuit();   

        if (playedValue.equals(topCard.getValue()) || 
            (playedSuit != null && playedSuit.equals(topCard.getSuit())) ||
            playedValue.equals("Wild") || playedValue.equals("Draw 4")) {

            if (playedValue.equals("Wild") || playedValue.equals("Draw 4")) {
                if (chosenColor != null) {
                    play.setSuit(colorCodeToWord(chosenColor)); 
                }
            }

            this.discard(currentPlayer, play);  

            switch (playedValue) {
                case "Skip": return 1;
                case "Draw 2": return 2;
                case "Reverse": return 3;
                case "Draw 4": return 4;
                default: return 0;
            }
        }

        return -1;
    }

    private String colorCodeToWord(String code) {
        switch (code) {
            case "r": return "Red";
            case "b": return "Blue";
            case "g": return "Green";
            case "y": return "Yellow";
            default: return "Red";
        }
    }
}
