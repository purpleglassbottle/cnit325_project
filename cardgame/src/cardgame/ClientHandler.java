package cardgame;

import java.io.*;
import java.net.*;
import java.util.*;
import cardgame.*;


/**
 *
 * @author reign
 */

/* main functions
1. maintains stable connections
2. each time one players connects, the server will create a clientHandler
3. p2p communicate with player, which includes:
1) receive input from the player
2) relay updates to the player
*/

public class ClientHandler implements Runnable 
{
    private Socket clientSocket;
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
        this.clientSocket = s;
        this.server = server;
        
        try {
        // Initialize InputStream and OutputStream
        this.inStream = clientSocket.getInputStream();
        this.outStream = clientSocket.getOutputStream();

        // Initialize Scanner and PrintWriter
        this.in = new Scanner(inStream);
        this.out = new PrintWriter(outStream, true);  // Ensure PrintWriter is initialized

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setPlayer(Player player){
        this.player = player;
    }
    
    public Player getPlayer(){
        return player;
    }
    
        // send messages to the client
    public void sendMessage(String message) 
    {
        out.println(message);
    }
    
    //Override
    public void run() 
    {
        try 
        {
            //read messages from the player and send messages to the player (client)
            in = new Scanner(clientSocket.getInputStream());
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // will display if connection is successful
            out.println("Welcome：" + player.getPlayerName());
            
            while (in.hasNextLine()) 
            {
                // emily
                // added trim
                String clientMessage = in.nextLine().trim();
                System.out.println("Received from client: " + clientMessage);
                // handle client message
                if(clientMessage.isEmpty())
                    continue;
                
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
        } catch (IOException e) 
        {
            e.printStackTrace();
        } finally 
        {
            try 
            {
                clientSocket.close();
            } catch (IOException e) 
            {
                e.printStackTrace();
            }
            // tell server of disconnect
            server.removeClient(this);
        } 
    }
}

