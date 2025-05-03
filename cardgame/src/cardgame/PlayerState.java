package cardgame;

/*
* Cassie Kim
* Xiaotong Luo
* Sean Maloney
* Emily Zhang
*/

import java.util.*;

public class PlayerState {
    public int playerId;
    public List<Card> hand;

    public PlayerState() {}
    
    public PlayerState(int playerId, List<Card> hand) {
        this.playerId = playerId;
        this.hand = hand;
    }
}