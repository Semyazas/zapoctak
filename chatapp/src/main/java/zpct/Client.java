package zpct;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.SwingUtilities;

import zpct.GUI.ChatAppGUI;
import zpct.GUI.LoginRegistrationGUI;

/**
 * This class represents the client-side of a socket-based chat application.
 * It connects to a server, handles login functionality, and initializes the GUI.
 */
public class Client {

    static Socket socket;
    static InputStream input;
    static DataOutputStream output;

    /**
     * Constructor for the Client class.
     */
    public Client() {

        try {
            socket = new Socket("localhost", 12345);

            input = socket.getInputStream();
            output = new DataOutputStream(socket.getOutputStream());

            String username = logIn();

            // Initialize the GUI on the Event Dispatch Thread
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new ChatAppGUI(socket, input, output,username);
                }
            });

        } catch (UnknownHostException u) {
            System.out.println(u);
            
        } catch (IOException i) {
            System.out.println(i);
            System.out.println("server je vypnutý");
            return;
        }
        // TODO: oprav registraci ... bug: pokud se zaregistruju, tak registrace nezmizí
    }

    /**
     * Handles the login process with the server.
     *
     * @throws IOException if an I/O error occurs while communicating with the server.
     */
    public static String logIn() throws IOException {
        while (true) {
            // Read messages from the client
            LoginRegistrationGUI l = new LoginRegistrationGUI(socket, input, output);

            byte[] buffer = new byte[1024];
            int bytesRead = input.read(buffer);
            if (bytesRead == -1) {
                break; // End of stream, client has disconnected
            }
            String message = new String(buffer, 0, bytesRead);
            System.out.println("Client: " + message);
            if (message.equals("LOGIN SUCCESSFULL")) {
                System.out.println("todle je username:" + l.username);
                return l.username;
            }
        }
        return "";
    }
}
