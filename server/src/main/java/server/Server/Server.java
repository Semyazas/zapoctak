package server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Server {

    ServerSocket server;
    DataInputStream input;
    static DataOutputStream output;
    HashMap<String,ClientHandler> username_clientHandler;

    public Server() throws IOException {
        this.input = null;
        username_clientHandler = new HashMap<>();
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
                    = new ClientHandler(client,username_clientHandler); 

                new Thread(clientSock).start(); 
            }  

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public class ClientHandler implements Runnable { 

        String userName;
        String passWord;
        registrator registration_handler;
        HashMap<String,ClientHandler> chatters;

        private final DataOutputStream output;
        private final DataInputStream input;
        private final Socket clientSocket; 

        private final int MSG_REQUEST_CORRECT   = 0;
        private final int MSG_REQUEST_INCORRECT = 1;
        private final int MSG                   = 2;
  
        // Constructor 
        ClientHandler(Socket socket, HashMap<String,ClientHandler> ch) throws IOException { 
            this.clientSocket = socket;
            registration_handler = new registrator("C:\\Users\\marti\\OneDrive\\Plocha\\bin_tree.java\\zapoctak\\server\\data\\data.txt");
            chatters = ch;
            input  = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());
        } 
        public Socket getSocket() {
            return clientSocket;
        }

        public void run()    { 
            try {      
                  // get the outputstream of client 
                System.out.println("Client connected: " + clientSocket);
                handle_recieving_messages(input,output);
            } catch (IOException e) {
                chatters.remove(userName);
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
                    chatters.put(userName, this); // todle by mělo být asi v loginu
                    registration_handler.log_user(tokens, userName,passWord,output);
                } else {
                    int podm = handle_chatWindow_request(message);
                    if (podm==MSG) {
                        System.out.println("funguju");
                        handle_sending_messages(message);
                    }
                    // TODO: handle messages ... basic logika .. 
                }
            }
        }
        /*If client with whom this client wants to communicate is up,
         *than send signal to open new chat window.
        */
        public int handle_chatWindow_request(String message) throws IOException { 
            String[] tokens = message.split("\\s+");
            if (tokens[0].equals("req")) {
                if (chatters.containsKey(tokens[1])) {
                    ClientHandler c = chatters.get(tokens[1]);

                    Socket socket = c.getSocket();
                    // Sending the response back to the client.
                    // Note: Ideally you want all these in a try/catch/finally block
                    OutputStream os = socket.getOutputStream();
                    DataOutputStream osw = new DataOutputStream(os);
                    osw.write((userName + " " + message).getBytes());
                    osw.flush();

                    return MSG_REQUEST_CORRECT;
                } else {
                    return MSG_REQUEST_INCORRECT;
                }
            } 
            return MSG;
        }   

        public void handle_sending_messages(String message) throws IOException {
            String[] tokens = message.split("\\s+");
            String target_client_username = "";
            String mess_string;

            if (tokens.length >1 && tokens[0].equals("acc")) {
                System.out.println("acc funguje ");
                target_client_username = tokens[1];
                mess_string =" acc " + tokens[1];
                output.write((userName + " uacc " + tokens[1]).getBytes());
                output.flush();
            } 
            else {
                target_client_username = tokens[0];
                mess_string =""; // tady jsem to možná rozbil, tak se kdyžtak koukni do gitu 
            
                // we will re-build message
                for (int i = 1; i < tokens.length; i ++) {
                    mess_string+= tokens[i] + " ";
                }
            }

            ClientHandler c = chatters.get(target_client_username);

            Socket socket = c.getSocket();
            // Sending the response back to the client.
            // Note: Ideally you want all these in a try/catch/finally block
            OutputStream os = socket.getOutputStream();
            DataOutputStream osw = new DataOutputStream(os);
            osw.write((userName + " " + mess_string).getBytes());
            osw.flush();
            
        }
    } 
}