
package zpct.GUI;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * A window for individual chat sessions between users.
 * Extends the ChatAppGUI class to inherit GUI components and functionality.
 */

public class ChatWindow extends ChatAppGUI {

    String from;
    String to;

    Socket socket;
    InputStream input;
    DataOutputStream output;

    /**
     * Constructs a ChatWindow object for a specific chat session.
     *
     * @param s  The socket for communication with the server.
     * @param i  The input stream to receive messages from the server.
     * @param o  The output stream to send messages to the server.
     * @param fr The sender of the messages.
     * @param t  The receiver of the messages.
     */
    public ChatWindow(Socket s, InputStream i,
                      DataOutputStream o, String fr, String t) {
        socket = s;
        input = i;
        output = o;
        from = fr;
        to = t;

        // Set up the JFrame
        setupJFrame();
        // Create components
        createComponents();
        // Set up layout
        setupLayout();
        // Display the GUI
        setVisible(true);
    }

    /**
     * Set up JFrame properties for the chat window.
     */
    @Override
    protected void setupJFrame() {
        setTitle("Chat with: " + to);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Send a message to the receiver. This also displays message sent by user on window.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    void sendMessage() throws IOException {
        String message = messageField.getText();
    //    System.out.println(message);
        if (!message.isEmpty()) {
            chatArea.append("You: " + message + "\n"); // Display sent message in the chatArea
            output.write((to + " " + message).getBytes());
            output.flush();
            // Include logic here to send the message to the other user or chat server
            // For simplicity, just displaying the sent message in the chatArea
            messageField.setText(""); // Clear the message field after sending
        }
    }

    // idea .. one will read and another will write!
    // TODO: Implement the functionality for reading and writing messages simultaneously.
}
