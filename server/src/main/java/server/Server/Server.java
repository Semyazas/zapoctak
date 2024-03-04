package server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import org.omg.CORBA.TIMEOUT;

import java.util.*;
import java.util.concurrent.TimeUnit;

import server.Server.history.history;

public class Server {

    ServerSocket server;
    DataInputStream input;
    static DataOutputStream output;
    HashMap<String,ClientHandler> username_clientHandler;

    List<String> unread_messages;

    public Server() throws IOException, InterruptedException {
        input = null;
        username_clientHandler = new HashMap<>();
        unread_messages =   Collections.synchronizedList(new ArrayList<>());

        try {
            // server is listening on port 1234 
            server = new ServerSocket(12345); 
            server.setReuseAddress(true); 
  
            System.out.println("listening ...");
            // Create a thread to handle messages from the client
            while (true) {  // todle se bude pořád točit ... získá od clienta socket
                Socket client = server.accept(); 

                System.out.println("New client connected" + client.getInetAddress().getHostAddress()); 

                ClientHandler clientSock = new ClientHandler(client,username_clientHandler,unread_messages); 
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
        List<String> unread_messages;

        private final DataOutputStream output;
        private final DataInputStream input;
        private final Socket clientSocket; 

        private final int MSG_REQUEST_CORRECT   = 0;
        private final int MSG_REQUEST_INCORRECT = 1;
        private final int MSG                   = 2;
  
        // Constructor 
        ClientHandler(Socket socket, HashMap<String,ClientHandler> ch, List<String> unread_messages) throws IOException, InterruptedException { 
            this.clientSocket = socket;
            registration_handler = new registrator("C:\\Users\\marti\\OneDrive\\Plocha\\bin_tree.java\\zapoctak\\server\\data\\data.txt");
            chatters = ch;
            input  = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());
            this.unread_messages = unread_messages;
            handle_unread_messages();
        } 
        public Socket getSocket() {
            return clientSocket;
        }

        public void run()    { 
            try {      
                  // get the outputstream of client 
                System.out.println("Client connected: " + clientSocket);
                try {
                    handle_recieving_messages(input,output);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (IOException e) {
                chatters.remove(userName);
                System.out.println("Connection lost");
                return;
            }
        }
        public void handle_recieving_messages(DataInputStream input,DataOutputStream output) throws IOException, InterruptedException {
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
                    handle_log_or_registration(message, registration_handler);
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

        public void handle_sending_messages(String message) throws IOException, InterruptedException {
            String[] tokens = message.split("\\s+");
            String target_client_username = tokens[1];
            String mess_string = "";

            if (tokens.length >1 && tokens[0].equals("acc")) {
                accept_request(tokens,true);

            }  else if (tokens.length >1 && tokens[0].equals("hist")) { // historie je ve formátu "hist target"
                history.get_user_to_user_history(userName, target_client_username, output);
            } 
            else if (tokens.length >1 && tokens[0].equals("window")) {
                if (history.can_open_window(userName, target_client_username)) {
                    accept_request(tokens, false);
                }

            } else {
                target_client_username = tokens[0];
                mess_string ="";     // tady jsem to možná rozbil, tak se kdyžtak koukni do gitu 
            
                for (int i = 1; i < tokens.length; i ++) {
                    mess_string+= tokens[i] + " ";
                }
                send_message(target_client_username, mess_string); 
                history.write_history(userName, target_client_username, mess_string); 
            }
        }

        public void accept_request(String[] tokens,
                                    boolean first_time_chatting_between_2_users) throws IOException {
            String mess_string = "";
            System.out.println("acc funguje ");
            String target_client_username = tokens[1]; 

            if (first_time_chatting_between_2_users) {
                mess_string =" acc " + target_client_username;

            } else {
                mess_string=" wacc " + target_client_username;
                target_client_username = userName;
            }
            send_message(target_client_username, mess_string);

            output.write((userName + " uacc " + target_client_username).getBytes());
            output.flush();

        }

        public void handle_unread_messages() throws IOException, InterruptedException {
            String[] tokens;
            boolean open = false;
            for (String message : unread_messages) {
                tokens = message.split(";");
                if (tokens[0].equals(userName)) { // chces formát odkoho - komu - cas - message
                    if (open) {
                        output.write((userName + " wacc " + tokens[0]).getBytes());
                        output.flush();  
                    }

                    TimeUnit.MILLISECONDS.sleep(5);

                    output.write((tokens[1]).getBytes());
                    output.flush();
                }
            }
        }

        public void send_message(String target_client_username, String mess_string) throws IOException {
            ClientHandler target_client = chatters.get(target_client_username);
            System.out.println(target_client);
            if (target_client == null) {
                System.out.println(target_client_username + " is offline");
                unread_messages.add(userName + " " + mess_string);
                return;
            }
            Socket target_socket = target_client.getSocket();
            // Sending the response back to the client.
            // Note: Ideally you want all these in a try/catch/finally block
            OutputStream os = target_socket.getOutputStream();
            DataOutputStream osw = new DataOutputStream(os);
            osw.write((userName + " " + mess_string).getBytes());
            osw.flush();
        }

        public void handle_log_or_registration(String message, registrator registration_handler) throws IOException {
            String[] tokens = message.split("\\s+");
            userName = tokens[0];
            passWord = tokens[1];
            chatters.put(userName, this); // todle by mělo být asi v loginu
            registration_handler.log_user(tokens, userName,passWord,output);
        }
    } 
}