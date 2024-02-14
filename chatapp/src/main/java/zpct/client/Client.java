package zpct.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client { 
	// We initialize our socket( tunnel )
	// and our input reader and output stream
	// we will take the input from the user
	// and send it to the socket using output stream

	// constructor that takes the IP Address and the Port

	Socket socket;
	InputStream input;
	DataOutputStream output;

	public Client(String address, int port) { 
		// we try to establish a connection 

		try	{ 
			 // Create a thread to handle messages from the server
			socket = new Socket("localhost", 12345);
			 System.out.println("Connected to server: " + socket);
 
			input = socket.getInputStream();
			output = new DataOutputStream(socket.getOutputStream());
 
            Thread serverThread = new Thread(() -> {
				handle_recieving_messages();
            });

            serverThread.start(); // Start the server thread

            // Allow the client to send messages and files to the server
			handle_recieving_messages();
	    }
		catch(UnknownHostException u) { 
			System.out.println(u); 
		} 
		catch(IOException i) { 
			System.out.println(i); 
            System.out.println("server je vypnutý");
            return;
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
					System.out.println("Server: " + message);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void handle_sending_messages() throws IOException {
		BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			// Read a message from the console
			String clientMessage = consoleReader.readLine();
			
			// Check if the message is a file

				// Send the message to the server
			output.write(clientMessage.getBytes());
			output.flush();
			
		}
	}
}
