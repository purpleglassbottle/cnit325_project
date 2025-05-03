package cardgame;

import java.io.*;
import java.net.*;
import java.util.*;
import cardgame.*;

/* main functions
1. maintains stable connections
2. each time one players connects, the server will create a clientHandler
3. p2p communicate with player, which includes:
1) receive input from the player
2) relay updates to the player
*/

public class ClientHandler implements Runnable 
{
    private Socket s;
    private GameServer server;
    private PrintWriter out;
    private Scanner in;
    private InputStream inStream;
    private OutputStream outStream;
    
    // emily
    // references player
    private Player player;

    // emily
    // constructor
    public ClientHandler(Socket s, GameServer server) 
    {
        this.s = s;
        this.server = server;
        
        try {
        this.inStream = s.getInputStream();
        this.outStream = s.getOutputStream();
        this.in = new Scanner(inStream);
        this.out = new PrintWriter(outStream, true); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setPlayer(Player player){
        this.player = player;
    }
    
    public Player getPlayer(){
        return this.player;
    }
    
    // send messages to the client
    public void sendMessage(String message) 
    {
        System.out.println("[Server send] " + message);
        out.println(message);
    }
    
    //Override
    public void run() 
    {
//        try 
//        {
            //read messages from the player and send messages to the player (client)
//            in = new Scanner(s.getInputStream());
//            out = new PrintWriter(s.getOutputStream(), true);
//            server.registerPlayer(this);
            // will display if connection is successful
//            out.println("Welcome：" + player.getPlayerName());
            
            while (in.hasNextLine()) 
            {

                // emily
                // added trim
                String clientMessage = in.nextLine().trim();
                System.out.println("Received from client: " + clientMessage);
                // handle client message
                if (clientMessage.startsWith("INIT_GAME")) {            // INIT_GAME NEW|LOAD <id>
                    String[] p = clientMessage.split(" ");
                    server.configureGame(p[2], p[1].equals("LOAD"));
                    continue;
                }

                if (clientMessage.startsWith("HELLO ")) {               // HELLO <playerId>
                    int id = Integer.parseInt(clientMessage.substring(6));
                    if (!server.registerPlayer(this, id))
                        sendMessage("ID_TAKEN");
                    continue;
                }

                
                if(clientMessage.isEmpty())
                    continue;
                
                if (clientMessage.equals("READY") || clientMessage.equals("READY_ACK"))
                {
                    server.playerReady(); // server acknowledges this client is ready
                    continue;
                }    
                
                if (clientMessage.equals("END_GAME")) 
                {
                    // emily
                    // notify the server
                    server.endGame("A player has ended the game");
                    //idk if below is needed
                    server.broadcast("Game over!");
                    break;
                } 
                else if (clientMessage.startsWith("PLAY_CARD")) 
                {
                    // emily
                    // if syntax looks something like: PLAY_CARD <handIndex> [colorCode] 
                    String[] parts = clientMessage.split(" ");
                    if(parts.length < 2){
                        sendMessage("play is rejected: missing index error");
                        continue;
                    }
                    try{
                        int index = Integer.parseInt(parts[1]);
                        String color = (parts.length >= 3) ? parts[2] : null;
                        server.processPlay(this, index, color);
                    }
                    catch (NumberFormatException nfexc){
                        sendMessage("play is rejected: number format exeption");
                        nfexc.printStackTrace();  
                    }
                }
                else {
                    sendMessage("Unrecognized command: " + clientMessage);
                }
            }
        } 
//        catch (IOException e) 
//        {
//            e.printStackTrace();
//        } 
//        finally 
//        {
//            try 
//            {
//              s.close();
//            } catch (IOException e) 
//            {
//                e.printStackTrace();
//            }
//            // tell server of disconnect
////            server.removeClient(this);
//        } 
//    }
}

