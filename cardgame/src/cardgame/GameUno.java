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

            deck.add(new Card("Wild", null));
            deck.add(new Card("Draw 4", null));
        }
    }

    public void discard(Player currentPlayer, int index) {
        if (currentPlayer != null) {
            topCard = currentPlayer.getHand().get(index);
            currentPlayer.getHand().remove(index);
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
    }

    public int playCard(Player currentPlayer, int index, String chosenColor) {
        if (index < 0 || index >= currentPlayer.getHand().size()) return -1;

        Card play = currentPlayer.getHand().get(index);

        if (play.getValue().equals("Wild") || play.getValue().equals("Draw 4") ||
            play.getValue().equals(topCard.getValue()) ||
            (play.getSuit() != null && play.getSuit().equals(topCard.getSuit()))) {

            this.discard(currentPlayer, index);

            switch (topCard.getValue()) {
                case "Skip": return 1;
                case "Draw 2": return 2;
                case "Reverse": return 3;
                case "Wild":
                    if (chosenColor != null) {
                        topCard.setSuit(colorCodeToWord(chosenColor));
                    }
                    return 0;
                case "Draw 4":
                    if (chosenColor != null) {
                        topCard.setSuit(colorCodeToWord(chosenColor));
                    }
                    return 4;
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
