package cardgame;

/*
* Cassie Kim
* Xiaotong Luo
* Sean Maloney
* Emily Zhang
*/

public class GameUno extends Game {
    public void setDealCount() {
        this.dealCount = 7;
    }
    
    public void buildDeck() {
        // Define attributes.
        String color = "color";
        
        // Create all cards of each color.
        for(int i = 0; i <= 3; i++) {
            // Switch on the card color created.
            switch(i) {
                case 0:
                    color = "Red";
                    
                    break;
                case 1:
                    color = "Blue";
                    
                    break;
                case 2:
                    color = "Green";
                    
                    break;
                case 3:
                    color = "Yellow";
                    
                    break;
            } // End switch.
            
            // Create two of each card per color.
            for(int j = 0; j <= 1; j++) {
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
            
            // Create additional cards.
            deck.add(new Card("Wild", null));
            deck.add(new Card("Draw 4", null));
        }
    } // End public void buildDeck.
    
    public void discard(Player currentPlayer, int index) {
        if(currentPlayer != null) {
            topCard = (Card)currentPlayer.getHand().get(index);
            currentPlayer.getHand().remove(index);
        } else {
            topCard = deck.get(index);
            
            // Check is the topCard is a Skip, Draw 2, Reverse, Wild, or Draw 4.
            if(topCard.getValue().equals("Skip") || topCard.getValue().equals("Draw 2") || topCard.getValue().equals("Reverse") || topCard.getValue().equals("Wild") || topCard.getValue().equals("Draw 4")) {
                // Reshuffle and draw a new topCard.
                this.shuffleCards();
                this.discard(null, 0);
            } else {
                deck.remove(index);
            }
        }
    } // End public void discard.
    
    public int playCard(Player currentPlayer) {
        // Define attributes.
        Card play;
        boolean hasPlay = false;
        
        // Check if the deck has less than 4 cards.
        if(deck.size() < 4) {
            // Add another deck to accomodate more cards.
            this.buildDeck();
            this.shuffleCards();
        }
        
        // Display the player's Hand.
        currentPlayer.showHand();
        System.out.println("\nTop Card: " + topCard.getSuit() + " " + topCard.getValue() + "\n");
        
        // Check if the player has a valid play.
        for(int i = 0; i < currentPlayer.getHand().size(); i++) {
            play = (Card)currentPlayer.getHand().get(i);
            
            if(play.getValue().equals("Wild") || play.getValue().equals("Draw 4")) {
                hasPlay = true;
                
                break;
            } else if(play.getValue().equals(topCard.getValue()) || play.getSuit().equals(topCard.getSuit())) {
                hasPlay = true;
                
                break;
            }
        }
        
        // Ask the player for a play.
        if(hasPlay == true) {
            while(true) {
                // Get the player's input from the console.
                System.out.println("Enter the index of the card you would like to play.");
                String rawInput = System.console().readLine();

                // Try to convert the player's input into an integer.
                try {
                    int input = Integer.parseInt(rawInput);

                    // Try to get the Card from the chosen index.
                    try {
                        play = (Card)currentPlayer.getHand().get(input);

                        // Check if the chosen play is valid.
                        if(play.getValue().equals("Wild") || play.getValue().equals("Draw 4")) {
                            // Add the card to the discard pile.
                            this.discard(currentPlayer, input);
                            
                            break;
                        } else if(play.getValue().equals(topCard.getValue()) || play.getSuit().equals(topCard.getSuit())) {
                            // Add the card to the discard pile.
                            this.discard(currentPlayer, input);

                            break;
                        } else {
                            System.out.println("INVALID PLAY");
                            
                            continue;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("INVALID INPUT");
                    } // End try.
                } catch(NumberFormatException e) {
                    System.out.println("INVALID INPUT");
                } // End try.
            } // End while loop.
        } else {
            System.out.println("NO PLAY, DRAW CARD");
            
            // Add a new Card to the player's Hand.
            currentPlayer.draw(deck.get(0));
            deck.remove(0);
            
            this.playCard(currentPlayer);
        }
        
        // Check for a rule change.
        switch(topCard.getValue()) {
            case "Skip":
                return 1;
            case "Draw 2":
                return 2;
            case "Reverse":
                return 3;
            case "Wild":
                while(true) {
                    // Get the player's input from the console.
                    System.out.println("Enter the new color (r/b/g/y).");
                    String rawInput = System.console().readLine();

                    switch(rawInput) {
                        case "r":
                            topCard.setValue("Wild");
                            topCard.setSuit("Red");

                            return 0;
                        case "b":
                            topCard.setValue("Wild");
                            topCard.setSuit("Blue");

                            return 0;
                        case "g":
                            topCard.setValue("Wild");
                            topCard.setSuit("Green");

                            return 0;
                        case "y":
                            topCard.setValue("Wild");
                            topCard.setSuit("Yellow");

                            return 0;
                        default:
                            System.out.println("INVALID INPUT");

                            continue;
                    } // End switch.
                } // End while loop.
            case "Draw 4":
                while(true) {
                    // Get the player's input from the console.
                    System.out.println("Enter the new color (r/b/g/y).");
                    String rawInput = System.console().readLine();

                    switch(rawInput) {
                        case "r":
                            topCard.setValue("Draw 4");
                            topCard.setSuit("Red");

                            return 4;
                        case "b":
                            topCard.setValue("Draw 4");
                            topCard.setSuit("Blue");

                            return 4;
                        case "g":
                            topCard.setValue("Draw 4");
                            topCard.setSuit("Green");

                            return 4;
                        case "y":
                            topCard.setValue("Draw 4");
                            topCard.setSuit("Yellow");

                            return 4;
                        default:
                            System.out.println("INVALID INPUT");

                            continue;
                    } // End switch.
                } // End while loop.
            default:
                return 0;
        } // End switch case.
    } // End public int playCard.
} // End public class GameUno.