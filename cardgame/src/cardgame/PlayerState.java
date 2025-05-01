/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cardgame;

/**
 *
 * @author emilyzhang
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