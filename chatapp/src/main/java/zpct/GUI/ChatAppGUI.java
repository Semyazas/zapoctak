/**
 * A GUI application for a simple chat client.
 * Allows users to send and receive messages through a server.
 */
package zpct.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ChatAppGUI extends JFrame {

    private final int COMMAND_INDEX         = 3;
    private final int MESSAGE_TARGET_INDEX  = 2;

    protected JTextArea chatArea;
    protected JTextField messageField;

    ArrayList<String> requested;            // List to store requested users
    ArrayList<String> this_was_requested;   // List to store users who requested this client
    ArrayList<ChatWindow> opened_Windows;   // List to store opened chat windows

    Socket socket;          // Socket for communication with the server
    InputStream input;      // Input stream to receive messages from the server
    DataOutputStream output;    // Output stream to send messages to the server
    String username;        // Username of the client

    /**
     * Constructs a ChatAppGUI object.
     * Initializes lists and sets up the GUI components.
     */
    public ChatAppGUI() {
        opened_Windows = new ArrayList<>();
        requested = new ArrayList<>();
        this_was_requested = new ArrayList<>();
    }

    /**
     * Constructs a ChatAppGUI object with provided socket, input stream, and output stream.
     * Initializes lists, sets up the GUI components, and starts the GUI.
     *
     * @param s The socket for communication with the server.
     * @param i The input stream to receive messages from the server.
     * @param o The output stream to send messages to the server.
     */
    public ChatAppGUI(Socket s, InputStream i, DataOutputStream o,String u_name) {
        socket = s;
        input = i;
        output = o;
        username = u_name;

        opened_Windows = new ArrayList<>();
        requested = new ArrayList<>();
        this_was_requested = new ArrayList<>();

        init_GUI(); // Initialize and display the GUI
    }

    /**
     * Initializes the GUI components, sets up layout, and starts the server thread.
     */
    protected void init_GUI() {
        setupJFrame();
        createComponents();
        setupLayout();
        setVisible(true); // Display the GUI

        // Create a thread to handle messages from the server
        Thread serverThread = new Thread(() -> {
            try {
                handle_recieving_messages();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        serverThread.start(); // Start the server thread
    }

    /**
     * Set up the main JFrame window properties.
     */
    protected void setupJFrame() {
        setTitle("Chat App terminal");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Create GUI components.
     */
    protected void createComponents() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        messageField = new JTextField();
    }

    /**
     * Set up the layout of GUI components.
     */
    protected void setupLayout() {
        JButton sendButton = new JButton("Send");
        setupListener(sendButton);

        JScrollPane scrollPane = new JScrollPane(chatArea);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Set up ActionListener for the sendButton.
     *
     * @param sendButton The JButton for sending messages.
     */
    protected void setupListener(JButton sendButton) {
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage(); // Send message when the button is clicked
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    /**
     * Send message to the server.
     *
     * @throws IOException If an I/O error occurs.
     */
    void sendMessage() throws IOException {
        String message = messageField.getText();
        String[] tokens = message.split("\\s+");

        // If the message is a request, add the user to the requested list
        if (tokens[0].equals("req")) {
            requested.add(tokens[1]);
            System.out.println("requested: " + tokens[1]);
        }

        if (!message.isEmpty()) {
            chatArea.append("You: " + message + "\n"); // Display sent message in the chat area
            output.write(message.getBytes());
            output.flush();
            messageField.setText(""); // Clear the message field after sending
        }
    }

    /**
     * Write a message to the chat area.
     *
     * @param message The message to be displayed.
     */
    protected void writeMessage(String message,boolean window_msg) {
        if (!message.isEmpty()) {
            if (!window_msg) {
                chatArea.append("Server: " + message + "\n"); // Display received message in the chat area
            }
            else {
                chatArea.append(message + "\n"); // Display received message in the chat area
            }
            messageField.setText(""); // Clear the message field after displaying the message
        }
    }

    /**
     * Handle receiving messages from the server.
     * @throws InterruptedException 
     */
    protected void handle_recieving_messages() throws InterruptedException {
        try {
            while (true) {
                // Read messages from the server
                byte[] buffer = new byte[1024];
                int bytesRead = input.read(buffer);
                if (bytesRead == -1) {
                    break; // End of stream, server has disconnected
                } else {
                    recieve(buffer, bytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Open a new user-to-user chat window.
     *
     * @param from The sender of the message.
     * @param to   The receiver of the message.
     */
    protected void new_user_to_user_window(String from, String to) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChatWindow t = new ChatWindow(socket, input, output, from, to);
                opened_Windows.add(t); // Add the new chat window to the list
            }
        });
    }

    /**
     * Write a message to appropriate chat windows.
     *
     * @param message The message to be sent.
     * @param tokens  Tokens parsed from the message.
     * @throws InterruptedException 
     */
    protected synchronized void write_to_windows(String message, String[] tokens) throws InterruptedException {
        String remove_dots = "";
        for (ChatWindow window : opened_Windows) {
            System.out.println("to: " + window.to);
            remove_dots = tokens[MESSAGE_TARGET_INDEX].replace(":", "");
            if (window.to.equals(remove_dots)) {
                window.writeMessage(message,true); // Write message to the appropriate chat window
                return;
            }
        }
        System.out.println(username + " " + tokens[MESSAGE_TARGET_INDEX]);
        new_user_to_user_window(username, tokens[MESSAGE_TARGET_INDEX]);
        wait(500); // nešlo by tady zavolat tu samou funkci ?

        for (ChatWindow window : opened_Windows) {
            System.out.println("to: " + window.to);
            remove_dots = tokens[MESSAGE_TARGET_INDEX].replace(":", "");

            if (window.to.equals(remove_dots)) {
                System.out.println("funguju");
                window.writeMessage(message,true); // Write message to the appropriate chat window
                return;
            }
        }
        
    }

    /**
     * Handle incoming requests from the server.
     *
     * @param message The message received from the server.
     * @param tokens  Tokens parsed from the message.
     */
    protected void handle_requests(String message, String[] tokens) {
        System.out.println(message);
        if (tokens.length >= COMMAND_INDEX + 1) {
            if (tokens[COMMAND_INDEX+1].equals("acc") && requested.contains(tokens[0])) {
                new_user_to_user_window(tokens[2], tokens[0]); // Accept request and open a new chat window
            } else if (tokens[1].equals("req")) {
                System.out.println(tokens[0] + " mě chce");
                this_was_requested.add(tokens[0]); // Add the user to the request list
            } else if (tokens[1].equals("uacc") && this_was_requested.contains(tokens[2])) {
                new_user_to_user_window(tokens[0], tokens[2]); // Accept request and open a new chat window
            } else if (tokens[COMMAND_INDEX].equals("wacc")) {
                new_user_to_user_window(tokens[COMMAND_INDEX - 1], tokens[COMMAND_INDEX+1]); // Accept request and open a new chat window
            }
        }
    }

    /**
     * Receive a message from the server and handle it.
     *
     * @param buffer    The buffer containing the received message.
     * @param bytesRead The number of bytes read.
     * @throws InterruptedException 
     */
    protected void recieve(byte[] buffer, int bytesRead) throws InterruptedException {
        String message = new String(buffer, 0, bytesRead);
        String[] tokens = message.split("\\s+");

        System.out.println("todle jsem dostal: " + message);
        if (is_command(message)) {
            System.out.println("je command:");
            handle_requests(message, tokens); // Handle requests from the server
            writeMessage(message,false); // Write the received message to the terminal window
        } else {
            System.out.println("je zprava:");

            write_to_windows(message, tokens); // Write the received message to appropriate chat windows
        }
    }

    protected boolean is_command(String msg) {
        String[] splitted_msg = msg.split(" ");
        System.out.println("command: |"  + splitted_msg[COMMAND_INDEX] + "| " + splitted_msg[COMMAND_INDEX +1]);
        return (splitted_msg[COMMAND_INDEX].equals("req") || splitted_msg[COMMAND_INDEX].equals("acc") ||
                splitted_msg[COMMAND_INDEX].equals("uacc") || splitted_msg[COMMAND_INDEX + 1].equals("wacc") ||
                splitted_msg[COMMAND_INDEX].equals("window")|| splitted_msg[COMMAND_INDEX + 1].equals("req")
                || splitted_msg[COMMAND_INDEX + 1].equals("acc"));
    }
    // TODO: na todle koukni
}
