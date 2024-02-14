package Server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    ServerSocket ServerSocket;
    DataInputStream input;
    DataOutputStream output;
    ServerSocket server; 


    Server() {
        try { 
            // Server is listening on port 1234 
            Server = new ServerSocket(12345); 
            Server.setReuseAddress(true); 
  
            // running infinite loop for getting 
            // client request 
            System.out.println("listening ...");
            while (true) {  // todle se bude pořád točit ... získá od clienta socket
  
                // socket object to receive incoming client 
                // requests 
                Socket client = Server.accept(); 
  
                // Displaying that new client is connected 
                // to Server 
                System.out.println("New client connected"
                                   + client.getInetAddress() 
                                         .getHostAddress()); 
  
                // create a new thread object 
                ClientHandler clientSock 
                    = new ClientHandler(client); 
  
                // This thread will handle the client 
                // separately 
                new Thread(clientSock).start(); 
            } 
        } 
        catch (IOException e) { 
            e.printStackTrace(); 
        } 
        finally { 
            if (Server != null) { 
                try { 
                    Server.close(); 
                } 
                catch (IOException e) { 
                    e.printStackTrace(); 
                } 
            } 
        } 
    }
    public void handle_recieving_messages() {
        try {
            while (true) {
                // Read messages from the client
                byte[] buffer = new byte[1024];
                int bytesRead = input.read(buffer);
                if (bytesRead == -1) {
                    break; // End of stream, client has disconnected
                } else {
                    String message = new String(buffer, 0, bytesRead);
                    System.out.println("Client: " + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private static class ClientHandler implements Runnable { 
        private final Socket clientSocket; 
  
        // Constructor 
        public ClientHandler(Socket socket) { 
            this.clientSocket = socket; 
        } 
        
        /* kdyby jsi chtěl udělat toto pro jednoho clienta, tak stačí nad 
         * hlavní smyčnku v mainu zandat proměnné,co inicializuješ v run .. 
         * basically ctrl-v, ctrl-c 
         */
        public void run()    { 
            final DataOutputStream output ;
            final DataInputStream input;

            try {      
                  // get the outputstream of client 
                System.out.println("Client connected: " + clientSocket);
      
                input = new DataInputStream(clientSocket.getInputStream());
                output = new DataOutputStream(clientSocket.getOutputStream());
      

                        while (true) {
                            // Read messages from the client
                            byte[] buffer = new byte[1024];
                            int bytesRead = input.read(buffer);
                            if (bytesRead == -1) {
                                break; // End of stream, client has disconnected
                            }
                            String message = new String(buffer, 0, bytesRead);
                            System.out.println("Client: " + message);
                            if (message.equals("quit")) {
                                input.close();
                                output.close();
                                System.exit(0);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                
            

        } 
    } 
}