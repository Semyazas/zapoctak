package server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    ServerSocket server;
    DataInputStream input;
    DataOutputStream output;

    public Server() {
        try {
            // server is listening on port 1234 
            server = new ServerSocket(12345); 
            server.setReuseAddress(true); 
  
            // running infinite loop for getting 
            // client request 
            System.out.println("listening ...");

            // Create a thread to handle messages from the client
            while (true) {  // todle se bude pořád točit ... získá od clienta socket
  

                Socket client = server.accept(); 
  
                System.out.println("New client connected"
                                   + client.getInetAddress() 
                                         .getHostAddress()); 
  
                ClientHandler clientSock 
                    = new ClientHandler(client); 

                new Thread(clientSock).start(); 
            }  

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static class ClientHandler implements Runnable { 
        private final Socket clientSocket; 
        boolean logged;
  
        // Constructor 
        public ClientHandler(Socket socket) { 
            this.clientSocket = socket;
            logged =false; 
        } 
        
        public void run()    { 
            final DataOutputStream output ;
            final DataInputStream input;

            try {      
                  // get the outputstream of client 
                System.out.println("Client connected: " + clientSocket);
      
                input = new DataInputStream(clientSocket.getInputStream());
                output = new DataOutputStream(clientSocket.getOutputStream());
                handle_recieving_messages(input);
            } finally {
                System.out.println("Connection lost");
                return;
            }
        }
        public void handle_recieving_messages(DataInputStream input) throws IOException {
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
        }
    } 
}