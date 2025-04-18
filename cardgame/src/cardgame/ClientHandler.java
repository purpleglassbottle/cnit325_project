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
1.  maintains stable connections
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

    //constructor
    public ClientHandler(Socket s, GameServer server) 
    {
        this.clientSocket = s;
        this.server = server;
    }

    //Override
    public void run() 
    {
        try 
        {
            //read messages from the player and send messages to the player (client)
            in = new Scanner(clientSocket.getInputStream());
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            while (in.hasNextLine()) 
            {
                String clientMessage = in.nextLine();
                System.out.println("Received from client: " + clientMessage);
                // handle client message
                if (clientMessage.equals("END_GAME")) 
                {
                    server.broadcast("Game over!");
                    break;
                } else if (clientMessage.startsWith("PLAY_CARD")) 
                {
                    // player plays card
                    server.broadcast("Player played a card!");
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
        }
    }

    // send messages to the client
    public void sendMessage(String message) 
    {
        out.println(message);
    }
}
