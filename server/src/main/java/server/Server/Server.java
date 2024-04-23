package server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import server.Server.history.history;

/**
 * The Server class provides functionality for communication between server and user,
 * chatting between users. It also uses functions from history and registrator class for keeping track
 * of chat history and logging in/registration.
 */
public class Server {

    ServerSocket server;
    DataInputStream input;
    static DataOutputStream output;
    HashMap<String, ClientHandler> username_clientHandler;
    static String PATH_FOR_DATA;

    List<String> unread_messages;

    /**
     * Constructs a Server object and initializes necessary components.
     * @param port                  Port number
     * @param path_for_data         Path to data.
     * @throws IOException          If an I/O error occurs when creating the server socket.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    public Server(int port, String path_for_data) throws IOException, InterruptedException {
        input = null;
        username_clientHandler = new HashMap<>();
        unread_messages = Collections.synchronizedList(new ArrayList<>());
        PATH_FOR_DATA = path_for_data;

        try {
            // Server is listening on the specified port
            server = new ServerSocket(port);
            server.setReuseAddress(true);

            System.out.println("listening ...");
            Path path_to_HISTORY = Paths.get(PATH_FOR_DATA + "\\history.txt");
            Path path_to_DATA = Paths.get(PATH_FOR_DATA + "\\data.txt");

            File dir = new File(PATH_FOR_DATA);
            // Create necessary files if they don't exist
            if (!Files.exists(path_to_DATA)) {
           //     System.out.println("Creating new file: " + path_to_DATA);
                File act = new File(dir, "data.txt");
                act.createNewFile();
            }
            if (!Files.exists(path_to_HISTORY)) {
                File act2 = new File(dir, "history.txt");
                act2.createNewFile();
            }
            // Initialize history data
            history.init_Data(path_for_data + "\\history.txt");

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
            registration_handler = new registrator(PATH_FOR_DATA + "\\data.txt");
            chatters = ch;
            input = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());
            this.unread_messages = unread_messages;
    //        System.out.println(unread_messages);
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
      //          System.out.println("Client: " + message);

                if (!registration_handler.logged) { // Expecting correct login details
                    handle_log_or_registration(message, registration_handler);
                    if (registration_handler.logged)
                        handle_unread_messages();
                } else {
                    int condition = handle_chatWindow_request(message);
                    if (condition == MSG) {
      //                  System.out.println("Running");
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
   //         System.out.println("I started");
            String[] tokens = message.split("\\s+");
            if (tokens[0].equals("req") && tokens.length == 2) {
                if (registration_handler.is_registered(tokens[1].split(" ")) &&
                        history.can_send_request(userName, message, registration_handler)) {

                    history.write_history(userName, tokens[1], message);
                    send_message(tokens[1], message);
                    System.out.println("Correct");
                    return MSG_REQUEST_CORRECT;

                } else if (!registration_handler.is_registered(tokens[1].split(" "))) {
                    output.write("User you are trying to reach does not exist".getBytes());
                    output.flush();
                    System.out.println("Target user does not exist");

                } else if (!history.can_send_request(userName, message, registration_handler)) {
                    System.out.println("You are already chatting");

                    output.write("You and user are already friends :)".getBytes());
                    output.flush();
                }
                System.out.println("What happened");
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

            if (((tokens[0].equals("acc") || tokens[0].equals("window")
                    || tokens[0].equals("hist")) && tokens.length != 2) || message.isEmpty()) { // Here, it could also occur that we are writing to someone who does not exist
                output.write("Incorrect message/command format".getBytes());
                output.flush();
                return;
            }
            String target_client_username = tokens[1];
            String mess_string = "";

            handle_sending_messages_inner(tokens, mess_string, target_client_username);
        }

        /**
         * Handles the process of sending messages internally based on the provided tokens and message string.
         *
         * @param tokens                The tokens containing splitted message
         * @param mess_string           The message string to be sent
         * @param target_client_username The username of the target client
         * @throws IOException if an I/O error occurs while sending or receiving data
         * @throws InterruptedException if the thread is interrupted while waiting
         */
        public void handle_sending_messages_inner(String[] tokens, String mess_string, String target_client_username) throws IOException, InterruptedException {
            if (tokens[0].equals("acc")) { // check if the last message was a request
                if (!history.can_accept_request(userName, target_client_username)) {
                    output.write("Incorrect usage of acc".getBytes());
                    output.flush();
                    return;
                } else {
                    mess_string = "acc " + target_client_username;
                    accept_request(tokens, true);
                }
            } else if (tokens[0].equals("hist")) { // history is in the format "hist target"
                history.get_user_to_user_history(userName, target_client_username, output);
                return;
            } else if (tokens[0].equals("window")) { // if it's a req and then an acc
                handle_window_request(target_client_username, tokens);
                return;
            } else if (history.can_open_window(userName, tokens[0])) {
                send_message_to_target(tokens);
                return;
            }
            history.write_history(userName, target_client_username, mess_string);
        }

        /**
         * Sends a message to the target client based on the provided tokens.
         *
         * @param tokens The tokens containing information about the message
         * @throws IOException if an I/O error occurs while sending or receiving data
         */
        public void send_message_to_target(String[] tokens) throws IOException {
            String target_client_username = tokens[0];
            String mess_string = "";

            for (int i = 1; i < tokens.length; i++) {
                mess_string += tokens[i] + " ";
            }
            send_message(target_client_username, mess_string);
            history.write_history(userName, target_client_username, mess_string);
        }

        /**
         * Handles the window request for the target client.
         *
         * @param target_client_username The username of the target client
         * @param tokens                 The tokens containing information about the request
         * @throws IOException if an I/O error occurs while sending or receiving data
         */
        public void handle_window_request(String target_client_username, String[] tokens) throws IOException {
            if (history.can_open_window(userName, target_client_username)) {
                accept_request(tokens, false);
                return;
            } else {
                return;
            }
        }

        /**
         * Accepts a request from a client to initiate a chat session.
         *
         * @param tokens                                The message tokens containing necessary information.
         * @param first_time_chatting_between_2_users  Indicates if it's the first time chatting between two users.
         * @throws IOException If an I/O error occurs when sending a message to the other client.
         */
        public void accept_request(String[] tokens,
                                   boolean first_time_chatting_between_2_users) throws IOException {
            String mess_string = "";
            System.out.println("Accept is working ");
            String target_client_username = tokens[1];

            if (first_time_chatting_between_2_users) {
                mess_string = " acc " + target_client_username;

            } else {
                mess_string = " wacc " + target_client_username;
                target_client_username = userName;
            }
            send_message(target_client_username, mess_string);
        }

        /**
         * Handles unread messages for the client. If client gets some messages, but isn't
         * currently online, we will send them to him/her when he/she gets online.
         *
         * @throws IOException If an I/O error occurs when sending a message to the client.
         * @throws InterruptedException If the current thread is interrupted while waiting.
         */
        public void handle_unread_messages() throws IOException, InterruptedException {
            String[] tokens;
            boolean open = false;
            ArrayList<String> to_delete = new ArrayList<>();

            /*  we need to delete messages that we have
                already sent*/

            for (String message : unread_messages) {
                tokens = message.split(";");
                if (tokens[1].equals(userName)) {
                    System.out.println(userName + " received a message: " + tokens[0] + " " + tokens[2] + " " + tokens[3]);

                    if (!open && history.can_open_window(userName, tokens[2])) {
                        output.write((userName + " wacc " + tokens[0]).getBytes());
                        output.flush();
                        open = true;
                    }
                    TimeUnit.MILLISECONDS.sleep(500);

                    output.write((tokens[2] + " " + tokens[0] + " " + tokens[3]).getBytes());
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
            osw.write((formatter.format(date) + " " + userName + " " + mess_string).getBytes());
            osw.flush();
        }

        /**
         * Handles the login or registration process for the client.
         *
         * @param message              The message containing login or registration details.
         * @param registration_handler The registration handler object.
         * @throws IOException If an I/O error occurs when sending a message to the client.
         * @return {@code true} If user was logged in sucessfully, {@code false} otherwise.
         */
        public boolean handle_log_or_registration(String message, registrator registration_handler) throws IOException {
            String[] tokens = message.split("\\s+");
            userName = tokens[0];
            chatters.put(userName, this);
            return registration_handler.log_user(tokens, output);
        }

    }
}
