package server;
import java.io.IOException;
import java.util.regex.Pattern;

import server.Server.*;

/**
 * The ServerApp class is the entry point for starting the server application.
 * It creates an instance of the {@link server.Server} class to initialize the server.
 */

public class ServerApp {
    
    /**
     * The main method of the ServerApp class.
     * It creates an instance of the Server class to start the server application.
     * 
     * @param args args[0] is port and args[1] is path in which we store data
     * @throws IOException if an I/O error occurs while starting the server
     * @throws InterruptedException if the thread is interrupted while starting the server
     */
    final static int number_of_args = 2;
    final static int port_index = 0;
    final static int data_path_index = 1;
    public static void main(String[] args) throws IOException, InterruptedException {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

        if (args.length == number_of_args && pattern.matcher(args[port_index]).matches()) {
            new Server(Integer.parseInt(args[port_index]),args[data_path_index]); // Start the client application
        } else {
            System.out.println("Invalid input");
        }
    }
}
    