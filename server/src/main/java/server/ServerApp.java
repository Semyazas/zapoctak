/**
 * The server package contains classes related to the server functionality.
 * It includes the main class {@link server.Server} for starting the server,
 * server class which handles most of logic regarding sending messages/requests
 * to clients. Registrator class handles registratons and logins. History class
 * handles chat history for users.
 */

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
    public static void main(String[] args) throws IOException, InterruptedException {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

        if (args.length == 2 && pattern.matcher(args[0]).matches()) {
            new Server(Integer.parseInt(args[0]),args[1]); // Start the client application
        } else {
            System.out.println("Invalid input");
        }
    }
}
    