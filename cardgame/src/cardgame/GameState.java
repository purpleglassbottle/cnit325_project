package cardgame;

/*
* Cassie Kim
* Xiaotong Luo
* Sean Maloney
* Emily Zhang
*/

import java.util.*;

public class GameState {
    public List<PlayerState> players;
    public List<Card> deck;
    public List<Card> discardPile;
    public int turn;
    public boolean clockwise;

    // no-arg constructor for Gson
    public GameState() {}

    public GameState(List<PlayerState> players,List<Card> deck, List<Card> discardPile, int turn, boolean clockwise) {
        this.players = players;
        this.deck = deck;
        this.discardPile = discardPile;
        this.turn = turn;
        this.clockwise = clockwise;
    }
}