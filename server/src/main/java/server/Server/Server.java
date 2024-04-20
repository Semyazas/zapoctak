
package server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import server.Server.history.history;

public class Server {

    ServerSocket server;
    DataInputStream input;
    static DataOutputStream output;
    HashMap<String, ClientHandler> username_clientHandler;

    List<String> unread_messages;

    /**
     * Constructs a Server object and initializes necessary components.
     *
     * @throws IOException            If an I/O error occurs when creating the server socket.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    public Server() throws IOException, InterruptedException {
        input = null;
        username_clientHandler = new HashMap<>();
        unread_messages = Collections.synchronizedList(new ArrayList<>());

        try {
            // Server is listening on port 12345
            server = new ServerSocket(12345);
            server.setReuseAddress(true);

            System.out.println("listening ...");
            // Create a thread to handle messages from the client
            while (true) { // Loop indefinitely to accept client connections
                Socket client = server.accept();

                System.out.println("New client connected" + client.getInetAddress().getHostAddress());

                // Create a new ClientHandler thread for each connected client
                ClientHandler clientSock = new ClientHandler(client, username_clientHandler, unread_messages);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inner class to handle client connections and messages.
     */
    public class ClientHandler implements Runnable {

        String userName;
        String passWord;
        registrator registration_handler;
        HashMap<String, ClientHandler> chatters;
        List<String> unread_messages;

        private final DataOutputStream output;
        private final DataInputStream input;
        private final Socket clientSocket;

        private final int MSG_REQUEST_CORRECT = 0;
        private final int MSG_REQUEST_INCORRECT = 1;
        private final int MSG = 2;

        /**
         * Constructs a ClientHandler object for a specific client connection.
         *
         * @param socket          The socket for communication with the client.
         * @param ch              The map of usernames to their respective ClientHandlers.
         * @param unread_messages The list of unread messages for the client. It stores messages that client gets while he is offline.
         * @throws IOException          If an I/O error occurs when creating the input and output streams.
         * @throws InterruptedException If the current thread is interrupted while waiting.
         */
        ClientHandler(Socket socket, HashMap<String, ClientHandler> ch, List<String> unread_messages) throws IOException, InterruptedException {
            this.clientSocket = socket;
            registration_handler = new registrator("C:\\Users\\marti\\OneDrive\\Plocha\\bin_tree.java\\zapoctak\\server\\data\\data.txt");
            chatters = ch;
            input = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());
            this.unread_messages = unread_messages;
            System.out.println(unread_messages);
        }

        /**
         * Gets the socket associated with this client handler.
         *
         * @return The socket associated with this client handler.
         */
        public Socket getSocket() {
            return clientSocket;
        }

        /**
         * Runs the client handler thread, handling incoming messages from the client.
         */
        public void run() {
            try {
                // Output the connected client
                System.out.println("Client connected: " + clientSocket);
                try {
                    handle_receiving_messages(input, output);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                // Remove the client from the active users list
                chatters.remove(userName);
                System.out.println("Connection lost");
                return;
            }
        }

        /**
         * Handles receiving messages from the client.
         *
         * @param input  The input stream for receiving messages from the client.
         * @param output The output stream for sending messages to the client.
         * @throws IOException            If an I/O error occurs when reading from the input stream.
         * @throws InterruptedException If the current thread is interrupted while waiting.
         */
        public void handle_receiving_messages(DataInputStream input, DataOutputStream output) throws IOException, InterruptedException {
            while (true) {
                // Read messages from the client
                byte[] buffer = new byte[1024];
                int bytesRead = input.read(buffer);
                if (bytesRead == -1) {
                    break; // End of stream, client has disconnected
                }
                String message = new String(buffer, 0, bytesRead);
                System.out.println("Client: " + message);

                if (!registration_handler.logged) { // Expecting correct login details
                    System.out.println("funguju a nemám");
                    handle_log_or_registration(message, registration_handler);
                    handle_unread_messages();
                } else {
                    int podm = handle_chatWindow_request(message);
                    if (podm == MSG) {
                        System.out.println("Running");
                        handle_sending_messages(message);
                    }
                    // TODO: Handle messages
                }
            }
        }

        /**
         * Handles a request to open a chat window with another user.
         *
         * @param message The message containing the request.
         * @return An integer representing the type of request.
         * @throws IOException If an I/O error occurs when sending a message to the other user.
         */
        public int handle_chatWindow_request(String message) throws IOException {
            System.out.println("spustil jsem se");
            String[] tokens = message.split("\\s+");
            if (tokens[0].equals("req") ) {
       //         System.out.println("spustil jsem se + req " + registration_handler.is_registered(tokens[1].split(" ")) + " už je acc: " +
       //        history.can_send_request(userName, message, registration_handler));

                if (registration_handler.is_registered(tokens[1].split(" ")) &&
                     history.can_send_request(userName, message, registration_handler)) {

                    history.write_history(userName, tokens[1], message);
                    send_message(tokens[1], message);
                    System.out.println("koretní");
                    return MSG_REQUEST_CORRECT;
                
                }else  if (!registration_handler.is_registered(tokens[1].split(" "))) {
                    output.write("User you are trying to reach does not exist".getBytes());
                    output.flush();
                    System.out.println("cilový user neexistuje"); 

                }
                else if (!history.can_send_request(userName, message, registration_handler)) {
                    System.out.println("už si píšete");

                    output.write("You and user are already friends :)".getBytes());
                    output.flush();
                }
                System.out.println("what the fuck");
                return MSG_REQUEST_INCORRECT;
            }
            return MSG;
        }
        
         /**
         * Handles sending messages between clients and other related operations.
         *
         * @param message The message to be processed and sent.
         * @throws IOException            If an I/O error occurs when sending a message to the target client.
         * @throws InterruptedException If the current thread is interrupted while waiting.
         */
        public void handle_sending_messages(String message) throws IOException, InterruptedException {
    
            String[] tokens = message.split("\\s+");
            
            if ((tokens[0].equals("acc") || tokens[0].equals("window")
                    || tokens[0].equals("hist")) && tokens.length != 2) { // tady by ještě mohlo nastat, že píšeme někomu kdo neexistuje
                output.write("Incorrect message/command format".getBytes());
                output.flush();
                return;
            }

            String target_client_username = tokens[1];
            String mess_string = "";

            if (tokens[0].equals("acc")) { // zjisti jestli poslední zpráva byl request
                if (!history.can_accept_request(userName, target_client_username)) {
                    output.write("Incorrect usage of acc".getBytes());
                    output.flush();
                    return;
                } 
                else {
                    mess_string = "acc " + target_client_username;
                    accept_request(tokens, true);
                }

            } else if (tokens[0].equals("hist")) { // historie je ve formátu "hist target"
                history.get_user_to_user_history(userName, target_client_username, output);

            } else if (tokens[0].equals("window")) { // jestli je req a pak acc
                if (history.can_open_window(userName, target_client_username)) {
                    accept_request(tokens, false);
                    return;
                } else {
                    return;
                }

            } 
            else if (history.can_open_window(userName,tokens[0])) {
                target_client_username = tokens[0];
                mess_string = "";     
                System.out.println("tuto je klient: " + target_client_username);

                for (int i = 1; i < tokens.length; i++) {
                    mess_string += tokens[i] + " ";
                }
                send_message(target_client_username, mess_string);
            }
            System.out.println("toto je target: " + tokens[0]);

            history.write_history(userName, target_client_username, mess_string);
        }


        /**
         * Accepts a request from a client to initiate a chat session.
         *
         * @param tokens                               The message tokens containing necessary information.
         * @param first_time_chatting_between_2_users Indicates if it's the first time chatting between two users.
         * @throws IOException If an I/O error occurs when sending a message to the other client.
         */
        public void accept_request(String[] tokens,
                                    boolean first_time_chatting_between_2_users) throws IOException {
            String mess_string = "";
            System.out.println("acc funguje ");
            String target_client_username = tokens[1];

            if (first_time_chatting_between_2_users) {
                mess_string = " acc " + target_client_username;

            } else {
                mess_string = " wacc " + target_client_username;
                target_client_username = userName;
            }
            send_message(target_client_username, mess_string);
            /* 
            output.write((userName + " uacc " + target_client_username).getBytes());
            output.flush();
            */
        }

        /**
         * Handles unread messages for the client. If client gets some messages, but isn't
         * currently online
         *
         * @throws IOException            If an I/O error occurs when sending a message to the client.
         * @throws InterruptedException If the current thread is interrupted while waiting.
         */
        public void handle_unread_messages() throws IOException, InterruptedException {
            String[] tokens;
            boolean open = false;
            ArrayList<String> to_delete= new ArrayList<>(); 
            
            /*  we need to delete messages that we have 
                already sent*/

            for (String message : unread_messages) {
                tokens = message.split(";");
                if (tokens[1].equals(userName)) { // chces formát odkoho - komu - cas - message
                    System.out.println(userName + " dostal zprávu: " + tokens[0] + " " + tokens[2] + " " + tokens[3]);

                    if (!open && history.can_open_window(userName, tokens[2])) { 
                        output.write((userName + " wacc " + tokens[0]).getBytes());
                        output.flush();
                        open = true;
                    }
                    TimeUnit.MILLISECONDS.sleep(500);

                    output.write(( tokens[2]+ " "+ tokens[0] + " " + tokens[3]).getBytes());
                    output.flush();
                    to_delete.add(message);
                }
            }
            for (String mes : to_delete) {
                unread_messages.remove(mes);
            }
        }

        /**
         * Sends a message to the target client. If is client offline, than we store messages addressed to him
         * in list of unread messages. We store them in format "username;target_username;date;message".
         *
         * @param target_client_username The username of the target client.
         * @param mess_string            The message to be sent.
         * @throws IOException If an I/O error occurs when sending a message to the target client.
         */
        public void send_message(String target_client_username, String mess_string) throws IOException {
            ClientHandler target_client = chatters.get(target_client_username);
            System.out.println(target_client);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();

            if (target_client == null) {
                System.out.println(target_client_username + " is offline");
                unread_messages.add(userName + ";" + target_client_username +
                        ";" + formatter.format(date) + ";" + mess_string);
                return;
            }
            Socket target_socket = target_client.getSocket();
            // Sending the response back to the client.
            // Note: Ideally you want all these in a try/catch/finally block
            OutputStream os = target_socket.getOutputStream();
            DataOutputStream osw = new DataOutputStream(os);
            osw.write((formatter.format(date) + " " + userName  + " " + mess_string).getBytes());
            osw.flush();
        }

        /**
         * Handles the login or registration process for the client.
         *
         * @param message              The message containing login or registration details.
         * @param registration_handler The registration handler object.
         * @throws IOException If an I/O error occurs when sending a message to the client.
         */
        public void handle_log_or_registration(String message, registrator registration_handler) throws IOException {
            String[] tokens = message.split("\\s+");
            userName = tokens[0];
            passWord = tokens[1];
            chatters.put(userName, this); // todle by mělo být asi v loginu
            registration_handler.log_user(tokens, userName, passWord, output);
        }

    } 
}