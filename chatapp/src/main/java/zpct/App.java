package zpct;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.SwingUtilities;

import zpct.GUI.ChatAppGUI;
import zpct.GUI.LoginRegistrationGUI;


public class App {

    static Socket socket;
	static InputStream input;
	static DataOutputStream output;
    public static void main( String[] args ) {
        try {
			socket = new Socket("localhost", 12345);
            
			input = socket.getInputStream();
			output = new DataOutputStream(socket.getOutputStream());
  
            logIn();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new ChatAppGUI(socket,input,output);
                }
            });
        }
        catch(UnknownHostException u) { 
			System.out.println(u); 
		} 
		catch(IOException i) { 
			System.out.println(i); 
            System.out.println("server je vypnutý");
            return; 
        }
        // TODO: oprav registraci ... bug: pokud se zaregistruju, tak registrace nezmizí
    }
    public static void logIn() throws IOException {
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
                break;
            }
        }
    }
}
