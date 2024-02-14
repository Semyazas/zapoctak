package zpct.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatAppGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;

    public ChatAppGUI() {
        // Set up the JFrame
        setupJFrame();
        // Create components
        createComponents();

        // Set up layout
        setupLayout();

        // Display the GUI
        setVisible(true);
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
                sendMessage();
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

    void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            chatArea.append("You: " + message + "\n");
            // Include logic here to send the message to the other user or chat server
            // For simplicity, just displaying the sent message in the chatArea
            messageField.setText("");
        }
    }
}