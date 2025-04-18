package cardgame;

import java.util.Scanner;

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
        String color = "color";
        
        for(int i = 0; i <= 3; i++) {
            switch(i) {
                case 0: color = "Red"; break;
                case 1: color = "Blue"; break;
                case 2: color = "Green"; break;
                case 3: color = "Yellow"; break;
            }
            
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
            
            deck.add(new Card("Wild", null));
            deck.add(new Card("Draw 4", null));
        }
    }
    
    public void discard(Player currentPlayer, int index) {
        if(currentPlayer != null) {
            topCard = (Card)currentPlayer.getHand().get(index);
            currentPlayer.getHand().remove(index);
        } else {
            topCard = deck.get(index);
            if(topCard.getValue().equals("Skip") || topCard.getValue().equals("Draw 2") ||
               topCard.getValue().equals("Reverse") || topCard.getValue().equals("Wild") ||
               topCard.getValue().equals("Draw 4")) {
                this.shuffleCards();
                this.discard(null, 0);
            } else {
                deck.remove(index);
            }
        }
    }

    public int playCard(Player currentPlayer) {
        Scanner scanner = new Scanner(System.in);
        Card play;
        boolean hasPlay = false;

        if(deck.size() < 4) {
            this.buildDeck();
            this.shuffleCards();
        }

        currentPlayer.showHand();
        System.out.println("\nTop Card: " + topCard.getSuit() + " " + topCard.getValue() + "\n");

        for(int i = 0; i < currentPlayer.getHand().size(); i++) {
            play = (Card)currentPlayer.getHand().get(i);
            if(play.getValue().equals("Wild") || play.getValue().equals("Draw 4") ||
               play.getValue().equals(topCard.getValue()) || play.getSuit().equals(topCard.getSuit())) {
                hasPlay = true;
                break;
            }
        }

        if(hasPlay) {
            while(true) {
                System.out.println("Enter the index of the card you would like to play.");
                String rawInput = scanner.nextLine();

                try {
                    int input = Integer.parseInt(rawInput);
                    play = (Card)currentPlayer.getHand().get(input);

                    if(play.getValue().equals("Wild") || play.getValue().equals("Draw 4") ||
                       play.getValue().equals(topCard.getValue()) || play.getSuit().equals(topCard.getSuit())) {
                        this.discard(currentPlayer, input);
                        break;
                    } else {
                        System.out.println("INVALID PLAY");
                    }
                } catch (Exception e) {
                    System.out.println("INVALID INPUT");
                }
            }
        } else {
            System.out.println("NO PLAY, DRAW CARD");
            currentPlayer.draw(deck.get(0));
            deck.remove(0);
            return this.playCard(currentPlayer);
        }

        switch(topCard.getValue()) {
            case "Skip":
                return 1;
            case "Draw 2":
                return 2;
            case "Reverse":
                return 3;
            case "Wild":
                while(true) {
                    System.out.println("Enter the new color (r/b/g/y).");
                    String rawInput = scanner.nextLine();
                    switch(rawInput) {
                        case "r": topCard.setSuit("Red"); return 0;
                        case "b": topCard.setSuit("Blue"); return 0;
                        case "g": topCard.setSuit("Green"); return 0;
                        case "y": topCard.setSuit("Yellow"); return 0;
                        default: System.out.println("INVALID INPUT");
                    }
                }
            case "Draw 4":
                while(true) {
                    System.out.println("Enter the new color (r/b/g/y).");
                    String rawInput = scanner.nextLine();
                    switch(rawInput) {
                        case "r": topCard.setSuit("Red"); return 4;
                        case "b": topCard.setSuit("Blue"); return 4;
                        case "g": topCard.setSuit("Green"); return 4;
                        case "y": topCard.setSuit("Yellow"); return 4;
                        default: System.out.println("INVALID INPUT");
                    }
                }
            default:
                return 0;
        }
    }
}
