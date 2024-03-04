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

public class ChatWindow extends ChatAppGUI {

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
    }

    @Override 
    protected void setupJFrame() {
        setTitle("Chat with: " + to );
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    @Override
    void sendMessage() throws IOException {
        String message = messageField.getText();
        System.out.println(message);
        if (!message.isEmpty()) {
            chatArea.append("You: " + message + "\n");
            output.write((to + " " + message).getBytes());
			output.flush();
            // Include logic here to send the message to the other user or chat server
            // For simplicity, just displaying the sent message in the chatArea
            messageField.setText("");
        }
    }

    // idea .. jeden bude prostě číst a další bude psát !
    // TODO: implement this briliant idea
}