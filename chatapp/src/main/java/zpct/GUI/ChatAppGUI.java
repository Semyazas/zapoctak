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
    protected JTextArea chatArea;
    protected JTextField messageField;

    ArrayList<String> requested;
    ArrayList<String> this_was_requested;
    ArrayList<ChatWindow> opened_Windows;

	Socket socket;
	InputStream input;
	DataOutputStream output;
    String username;

    public ChatAppGUI() {
        opened_Windows = new ArrayList<>();
        requested = new ArrayList<>();
        this_was_requested = new ArrayList<>();
    }

    public ChatAppGUI(Socket s, InputStream i,
                      DataOutputStream o) {
        socket = s;
        input  = i;
        output = o;

        opened_Windows = new ArrayList<>();
        requested = new ArrayList<>();
        this_was_requested = new ArrayList<>();

        init_GUI();
    }
    protected void init_GUI() {
        // Set up the JFrame
        setupJFrame();

        // Create components
        createComponents();
        
        // Set up layout
        setupLayout();
        
        // Display the GUI
        setVisible(true);

        Thread serverThread = new Thread(() -> { // Create a thread to handle messages from the server
            handle_recieving_messages();
        });
        serverThread.start(); // Start the server thread
    }

    protected void setupJFrame() {
        setTitle("Chat App terminal");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    protected void createComponents() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        messageField = new JTextField();
    }

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

    protected void setupListener(JButton sendButton) {
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
    }

    void sendMessage() throws IOException { 
        String message   = messageField.getText();
        String[] tokens = message.split("\\s+");
        
        if (tokens[0].equals("req")) {
            requested.add(tokens[1]);
            System.out.println("requested: " + tokens[1]);
        }
        if (!message.isEmpty()) {
            chatArea.append("You: " + message + "\n");
            output.write(message.getBytes());
			output.flush();
            messageField.setText("");
        }
    }

    protected void writeMessage(String message) {
        if (!message.isEmpty()) {
            chatArea.append("Server: " + message + "\n");
            // Include logic here to send the message to the other user or chat server
            // For simplicity, just displaying the sent message in the chatArea
            messageField.setText("");
        }
    }

    protected void handle_recieving_messages() {
		try {
			while (true) {
				// Read messages from the server
				byte[] buffer = new byte[1024];
				int bytesRead = input.read(buffer);
				if (bytesRead == -1) {
					break; // End of stream, server has disconnected
				}
				else {
                    recieve(buffer, bytesRead);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    protected void new_user_to_user_window(String from, String to) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChatWindow t = new ChatWindow(socket, input, output, from, to);
                opened_Windows.add(t);
            }
        });
    }

    protected void write_to_windows(String message, String[] tokens) {
        for (ChatWindow window : opened_Windows) {
            if (window.to.equals(tokens[0])) {
                window.writeMessage(message);
            }
        }
    }

    protected void handle_requests(String message, String[] tokens) {
        System.out.println(message);
        if (tokens.length >1) {

            if (tokens[1].equals("acc") && requested.contains(tokens[0])) { // paralelně spust nové okno
                new_user_to_user_window(tokens[2], tokens[0]);
            }
            else if (tokens[1].equals("req")) {
                System.out.println(tokens[0] + " mě chce");
                this_was_requested.add(tokens[0]);
            } 
            else if (tokens[1].equals("uacc") && this_was_requested.contains(tokens[2])) {
                new_user_to_user_window(tokens[0], tokens[2]);
            }
        }
    }
    
    protected void recieve(byte[] buffer, int bytesRead) {
        String message = new String(buffer, 0, bytesRead);
        String[] tokens  = message.split("\\s+");

        handle_requests(message,tokens);
        writeMessage(message);          // writes message to terminal window
        write_to_windows(message, tokens); // writes message to apropriate chat window
    }
}