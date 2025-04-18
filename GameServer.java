package cardgame;

import java.io.*;
import java.net.*;
import java.util.*;

/* main functions 
1. manages central game logic
2. manage session lifecycle
3. synchronizes game state across all clientsn 
*/
public class GameServer 
{
    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clients;

    //constructor
    public GameServer(int port) throws IOException 
    {
        serverSocket = new ServerSocket(port);
        clients = new ArrayList<>();
    }

    // run the srever and listenning the client
    public void startServer() 
    {
        System.out.println("Server started...");
        while (true) 
        {
            try 
            {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
                clients.add(clientHandler); // Add client handler to the list
            } catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
    }

    // broadcast all messages to clients
    public void broadcast(String message) 
    {
        for (ClientHandler client : clients) 
        {
            client.sendMessage(message);
        }
    }

    //getter
    public ArrayList<ClientHandler> getClients() 
    {
        return clients;
    }

    public static void main(String[] args) 
    {
        try 
        {
            GameServer server = new GameServer(12345);
            server.startServer();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
}
