package zpct.GUI;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginRegistrationGUI extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    public boolean logged = false;

    Socket socket; 
    InputStream input;                  
    DataOutputStream output;

    public LoginRegistrationGUI(Socket s, InputStream i,
                      DataOutputStream o) {

        init_GUI(s, i, o);

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // Create panels
        create_panels(loginButton, registerButton);

        init_login_button_listener(loginButton);

        init_register_button_listener(registerButton);

        setVisible(true);
    }

    private void init_GUI(Socket s, InputStream i,
                    DataOutputStream o) {
        socket = s; 
        input = i;                        
        output = o;
        
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 150);
        setLocationRelativeTo(null);
    }

    private void create_panels(JButton loginButton,JButton registerButton) {

        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        JPanel buttonPanel = new JPanel();
                    
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        // Create panels
        loginPanel.add(usernameLabel);
        loginPanel.add(usernameField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        setLayout(new BorderLayout());
        add(loginPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void init_login_button_listener(JButton loginButton) {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement login functionality here
                String username = usernameField.getText();
                char[] password = passwordField.getPassword();

                String pswd = String.valueOf(password);
                try {
                    output.write((username + " " +pswd).getBytes());
                    output.flush();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(LoginRegistrationGUI.this, "Login button clicked");
                logged = true;
                dispose();
            }
        });
    }
    
    private void init_register_button_listener(JButton registerButton) {
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open registration window and close login window
                openRegistrationWindow();
                dispose();
            }
        });
    }

    private void openRegistrationWindow() {
        // Create a new window for registration
        JFrame registrationFrame = new JFrame("Register");
        registrationFrame.setSize(300, 150);
        registrationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registrationFrame.setLocationRelativeTo(null);

        // Create components for registration window
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField nameField = new JTextField(20);
        JTextField surnameField = new JTextField(20);
        JButton registerButton = new JButton("Register");

        // Create panels for registration window
        JPanel registrationPanel = new JPanel(new GridLayout(4, 2));
        registrationPanel.add(new JLabel("Username:"));
        registrationPanel.add(usernameField);
        registrationPanel.add(new JLabel("Password:"));
        registrationPanel.add(passwordField);
        registrationPanel.add(new JLabel("Name:"));
        registrationPanel.add(nameField);
        registrationPanel.add(new JLabel("Surname:"));
        registrationPanel.add(surnameField);

        JPanel registrationButtonPanel = new JPanel();
        registrationButtonPanel.add(registerButton);

        // Add action listener for registration button
        register_button_logic(registerButton, nameField, surnameField, registrationFrame);

        // Set layout for the registration frame
        registrationFrame.setLayout(new BorderLayout());
        registrationFrame.add(registrationPanel, BorderLayout.CENTER);
        registrationFrame.add(registrationButtonPanel, BorderLayout.SOUTH);

        // Make the registration frame visible
        registrationFrame.setVisible(true);
    }

    private void register_button_logic(JButton registerButton, JTextField nameField, 
                                        JTextField surnameField, JFrame registrationFrame) {
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement registration functionality here
                String username = usernameField.getText();
                char[] password = passwordField.getPassword();
                String pswd = String.valueOf(password);
                String name     = nameField.getText();
                String surname  = surnameField.getText();
                try {
                    output.write((username + " " +pswd +" " + name + " " + surname).getBytes());
                    output.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(registrationFrame, "Registration button clicked");
                logged = true;
                registrationFrame.dispose();
            }
        });
    } // TODO: dovyčisti tendle file
}
