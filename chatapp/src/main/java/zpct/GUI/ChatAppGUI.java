package zpct.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatAppGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;

    
	Socket socket;
	InputStream input;
	DataOutputStream output;

    public ChatAppGUI(Socket s, InputStream i,
                      DataOutputStream o) {
        socket = s;
        input  = i;
        output = o;

        // Set up the JFrame
        setupJFrame();
        // Create components
        createComponents();

        // Set up layout
        setupLayout();

        // Display the GUI
        setVisible(true);
         
	    // Create a thread to handle messages from the server
 
        Thread serverThread = new Thread(() -> {
				handle_recieving_messages();
        });
        serverThread.start(); // Start the server thread

            // Allow the client to send messages and files to the server
        
    }
    private void setupJFrame() {
        setTitle("Chat App");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void createComponents() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        messageField = new JTextField();
    }

    private void setupLayout() {
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(chatArea);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    void sendMessage() throws IOException {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            chatArea.append("You: " + message + "\n");
            output.write(message.getBytes());
			output.flush();
            // Include logic here to send the message to the other user or chat server
            // For simplicity, just displaying the sent message in the chatArea
            messageField.setText("");
        }
    }

    void writeMessage(String message) {
        if (!message.isEmpty()) {
            chatArea.append("Server: " + message + "\n");
            // Include logic here to send the message to the other user or chat server
            // For simplicity, just displaying the sent message in the chatArea
            messageField.setText("");
        }
    }

    public void handle_recieving_messages() {
		try {
			while (true) {
				// Read messages from the server
				byte[] buffer = new byte[1024];
				int bytesRead = input.read(buffer);
				if (bytesRead == -1) {
					break; // End of stream, server has disconnected
				}
				// Check if the message is a file
				else {
					String message = new String(buffer, 0, bytesRead);
					writeMessage(message);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}