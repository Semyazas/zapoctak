/**
 * The server package contains classes related to the server functionality.
 * It includes the main class {@link server.Server} for starting the server,
 * server class which handles most of logic regarding sending messages/requests
 * to clients. Registrator class handles registratons and logins. History class
 * handles chat history for users.
 */

package server;
import java.io.IOException;

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
     * @param args command line arguments (not used)
     * @throws IOException if an I/O error occurs while starting the server
     * @throws InterruptedException if the thread is interrupted while starting the server
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        new Server();
    }
}
    