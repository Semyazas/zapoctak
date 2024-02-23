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
    private JTextArea chatArea;
    private JTextField messageField;

    ArrayList<String> requested;
    ArrayList<String> this_was_requested;
    ArrayList<ChatWindow> opened_Windows;

	Socket socket;
	InputStream input;
	DataOutputStream output;
    String username;

    public ChatAppGUI(Socket s, InputStream i,
                      DataOutputStream o) {
        socket = s;
        input  = i;
        output = o;

        opened_Windows = new ArrayList<>();
        requested = new ArrayList<>();
        this_was_requested = new ArrayList<>();

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
        setTitle("Chat App terminal");
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

    void sendMessage() throws IOException { // žádosti jsou ve formát od: od_koho req komu
        String message   = messageField.getText();
        String[] tokens = message.split("\\s+");
        if (tokens[0].equals("req")) {
            requested.add(tokens[1]);
            System.out.println("requested: " + tokens[1]);
        }
       /*  if (tokens[0].equals("acc") && this_was_requested.contains(tokens[1])) { 
            new_user_to_user_window(username, tokens[1]);
        } */
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
				// recieve message
				else {
					String message = new String(buffer, 0, bytesRead);
                    String[] tokens  = message.split("\\s+");
                    System.out.println(message);
                    if (tokens[1].equals("acc") && requested.contains(tokens[0])) { // paralelně spust nové okno
                        new_user_to_user_window(tokens[2], tokens[0]);
                    } else if (tokens[1].equals("req")) {
                        System.out.println(tokens[0] + " mě chce");
                        this_was_requested.add(tokens[0]);
                    } else if (tokens[1].equals("uacc") && this_was_requested.contains(tokens[2])) {
                        new_user_to_user_window(tokens[0], tokens[2]);
                    }
					writeMessage(message);
                    write_to_windows(message, tokens);
                     
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    public void new_user_to_user_window(String from, String to) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChatWindow t = new ChatWindow(socket, input, output, from, to);
                opened_Windows.add(t);
            }
        });
    }
    public void write_to_windows(String message, String[] tokens) {
        for (ChatWindow window : opened_Windows) {
            if (window.to.equals(tokens[0])) {
                window.writeMessage(message);
            }
        }
    }
}