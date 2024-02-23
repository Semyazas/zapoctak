package zpct.GUI;

import javax.swing.*;

import javafx.scene.control.SplitMenuButton;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    String from;
    String to;

    
	Socket socket;
	InputStream input;
	DataOutputStream output;

    public ChatWindow(Socket s, InputStream i,
                      DataOutputStream o, String fr, String t) {
        socket = s;
        input  = i;
        output = o;
        from   = fr;
        to     = t;

        // Set up the JFrame
        setupJFrame();
        // Create components
        createComponents();

        // Set up layout
        setupLayout();

        // Display the GUI
        setVisible(true);
         
	    // Create a thread to handle messages from the server
   
        // Allow the client to send messages and files to the server
        
    }
    private void setupJFrame() {
        setTitle("chat " + from + " " + to);
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
        String message = to + " " + messageField.getText();
        System.out.println(message);
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

    // idea .. jeden bude prostě číst a další bude psát !
    // TODO: implement this briliant idea
}