/**
 * A GUI application for user login and registration.
 * Allows users to log in or register for the chat application.
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

public class LoginRegistrationGUI extends JFrame {

    private JTextField usernameField;
    private JTextField nameField;
    private JPasswordField passwordField;
    JTextField surnameField;
    JButton registerButton;

    public boolean logged = false;

    Socket socket;
    InputStream input;
    DataOutputStream output;

    /**
     * Constructs a LoginRegistrationGUI object with provided socket, input stream, and output stream.
     * Initializes GUI components and sets up listeners for login and registration buttons.
     *
     * @param s The socket for communication with the server.
     * @param i The input stream to receive data from the server.
     * @param o The output stream to send data to the server.
     */
    public LoginRegistrationGUI(Socket s, InputStream i, DataOutputStream o) {
        init_GUI(s, i, o);

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // Create panels for login and register buttons
        create_panels(loginButton, registerButton);

        // Set up listeners for login and register buttons
        init_login_button_listener(loginButton);
        init_register_button_listener(registerButton);

        setVisible(true); // Make the GUI visible
    }

    /**
     * Initializes the GUI components and sets up the main JFrame properties.
     *
     * @param s The socket for communication with the server.
     * @param i The input stream to receive data from the server.
     * @param o The output stream to send data to the server.
     */
    private void init_GUI(Socket s, InputStream i, DataOutputStream o) {
        socket = s;
        input = i;
        output = o;

        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 150);
        setLocationRelativeTo(null);
    }

    /**
     * Create panels for login and register buttons.
     *
     * @param loginButton    The button for login.
     * @param registerButton The button for registration.
     */
    private void create_panels(JButton loginButton, JButton registerButton) {
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        JPanel buttonPanel = new JPanel();

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");

        // Add components to loginPanel
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

    /**
     * Set up ActionListener for the loginButton.
     *
     * @param loginButton The JButton for login.
     */
    private void init_login_button_listener(JButton loginButton) {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement login functionality here
                String username = usernameField.getText();
                char[] password = passwordField.getPassword();
                String pswd = String.valueOf(password);
                try {
                    output.write((username + " " + pswd).getBytes());
                    output.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(LoginRegistrationGUI.this, "Login button clicked");
                logged = true;
                dispose();
            }
        });
    }

    /**
     * Set up ActionListener for the registerButton.
     *
     * @param registerButton The JButton for registration.
     */
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

    /**
     * Create and open registration window.
     */
    private void openRegistrationWindow() {
        // Create a new window for registration
        JFrame registrationFrame = new JFrame("Register");
        registrationFrame.setSize(300, 150);
        registrationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registrationFrame.setLocationRelativeTo(null);

        // Create components for registration window
        create_components_for_registration_window();

        // Create panels for registration window
        JPanel registrationPanel = new JPanel(new GridLayout(4, 2));
        setup_registration_panel(registrationPanel, nameField, surnameField);

        JPanel registrationButtonPanel = new JPanel();
        registrationButtonPanel.add(registerButton);

        // Set up logic for registration button
        register_button_logic(registerButton, nameField, surnameField, registrationFrame);

        // Set layout for the registration frame
        registrationFrame.setLayout(new BorderLayout());
        registrationFrame.add(registrationPanel, BorderLayout.CENTER);
        registrationFrame.add(registrationButtonPanel, BorderLayout.SOUTH);

        // Make the registration frame visible
        registrationFrame.setVisible(true);
    }

    /**
     * Set up registration panel components.
     *
     * @param registrationPanel The JPanel for registration.
     * @param nameField         The JTextField for name.
     * @param surnameField      The JTextField for surname.
     */
    private void setup_registration_panel(JPanel registrationPanel, JTextField nameField, JTextField surnameField) {
        registrationPanel.add(new JLabel("Username:"));
        registrationPanel.add(usernameField);
        registrationPanel.add(new JLabel("Password:"));
        registrationPanel.add(passwordField);
        registrationPanel.add(new JLabel("Name:"));
        registrationPanel.add(nameField);
        registrationPanel.add(new JLabel("Surname:"));
        registrationPanel.add(surnameField);
    }

    /**
     * Create components for registration window.
     */
    private void create_components_for_registration_window() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        nameField = new JTextField(20);
        surnameField = new JTextField(20);
        registerButton = new JButton("Register");
    }

    /**
     * Set up logic for the registration button.
     *
     * @param registerButton    The JButton for registration.
     * @param nameField         The JTextField for name.
     * @param surnameField      The JTextField for surname.
     * @param registrationFrame The JFrame for registration.
     */
    private void register_button_logic(JButton registerButton, JTextField nameField,
                                       JTextField surnameField, JFrame registrationFrame) {
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement registration functionality here
                String username = usernameField.getText();
                char[] password = passwordField.getPassword();
                String pswd = String.valueOf(password);
                String name = nameField.getText();
                String surname = surnameField.getText();
                try {
                    output.write((username + " " + pswd + " " + name + " " + surname).getBytes());
                    output.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(registrationFrame, "Registration button clicked");
                logged = true;
                registrationFrame.dispose();
            }
        });
    }
}
