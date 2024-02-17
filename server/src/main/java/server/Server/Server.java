package server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {

    ServerSocket server;
    DataInputStream input;
    static DataOutputStream output;

    public Server() throws IOException {

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
        String userName;
        String passWord;
        registrator registration_handler;
  
        // Constructor 
        public ClientHandler(Socket socket) throws IOException { 
            this.clientSocket = socket;
            registration_handler = new registrator("C:\\Users\\marti\\OneDrive\\Plocha\\bin_tree.java\\zapoctak\\server\\data\\data.txt");
        } 
        
        public void run()    { 
            final DataOutputStream output;
            final DataInputStream input;

            try {      
                  // get the outputstream of client 
                System.out.println("Client connected: " + clientSocket);
      
                input = new DataInputStream(clientSocket.getInputStream());
                output = new DataOutputStream(clientSocket.getOutputStream());
                handle_recieving_messages(input,output);
            } catch (IOException e) {
                System.out.println("Connection lost");
                return;
            }
        }
        public void handle_recieving_messages(DataInputStream input,DataOutputStream output) throws IOException {
            while (true) {
                    // Read messages from the client
                byte[] buffer = new byte[1024];
                int bytesRead = input.read(buffer);
                if (bytesRead == -1) {
                    break; // End of stream, client has disconnected
                } 
                String message = new String(buffer, 0, bytesRead);
                System.out.println("Client: " + message);

                if (!registration_handler.logged) { // At first I expect correct output
                    String[] tokens = message.split("\\s+");
                    userName = tokens[0];
                    passWord = tokens[1];
                    registration_handler.log_user(tokens, userName,passWord,output);
                } else {

                }
            }
        }
    } 
}