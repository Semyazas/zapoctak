package zpct;

import static org.junit.Assert.assertTrue;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    static Socket socket;
    static InputStream input;
    static DataOutputStream output;

    static Socket socket2;
    static InputStream input2;
    static DataOutputStream output2;

    @Test
    public void RegistrationAndLoginTest() throws UnknownHostException, IOException {

        socket = new Socket("localhost", 12345);

        input = socket.getInputStream();
        output = new DataOutputStream(socket.getOutputStream());
        boolean first = true;

        System.out.println("funguju");
        output.write(("Pepe2" + " " + "123" + " " + "Karel" + " " + "Novak").getBytes());
        output.flush();
        while (true) {
            // Read messages from the client
            byte[] buffer = new byte[1024];
            int bytesRead = input.read(buffer);
            if (bytesRead == -1) {
                break; // End of stream, client has disconnected
            }
            output.write(("Pepe2" + " " + "123").getBytes());
            output.flush();
            
            String message = new String(buffer, 0, bytesRead);
            System.out.println("Client: " + message);
            if (message.equals("LOGIN SUCCESSFULL")) {
                assertTrue( true );
                break;
            }
            if (message.equals("LOGIN UNSUCCESSFULL")) {
                assertTrue(false);
                break;
            }
        }
    }

    @Test
    synchronized public void RegistrationAndLoginTest_INCORRECT_PASSWORD() throws UnknownHostException, IOException, InterruptedException {

        socket = new Socket("localhost", 12345);

        input = socket.getInputStream();
        output = new DataOutputStream(socket.getOutputStream());
        
        boolean first = true;

        System.out.println("funguju");
        output.write(("Pepe2" + " " + "13").getBytes());
        output.flush();
        while (true) {
            // Read messages from the client
            byte[] buffer = new byte[1024];
            int bytesRead = input.read(buffer);
            if (bytesRead == -1) {
                break; // End of stream, client has disconnected
            }
            
            String message = new String(buffer, 0, bytesRead);
            System.out.println("Client: " + message);
            if (message.equals("INCORRECT PASSWORD OR USERNAME")) {
                assertTrue( true );
                break;
            }
            if (message.equals("LOGIN SUCCESSFULL")) {
                assertTrue(false);
                break;
            }
        }
    }

    @Test
    synchronized public void RequestHandling_INCORRECT_USAGE() throws UnknownHostException, IOException, InterruptedException {
        socket = new Socket("localhost", 12345);

        input = socket.getInputStream();
        output = new DataOutputStream(socket.getOutputStream());

        output.write(("Pepe3" + " " + "123" ).getBytes());
        output.flush();

        wait(500);

        while (true) {
            // Read messages from the client
            output.write("req 5".getBytes());
            output.flush();

            byte[] buffer = new byte[1024];
            int bytesRead = input.read(buffer);
            if (bytesRead == -1) {
                break; // End of stream, client has disconnected
            }
            
            String message = new String(buffer, 0, bytesRead);
            System.out.println("Client: |" +  message + "|");
            if (message.equals("User you are trying to reach does not exist")) {
                assertTrue( true );
                break;
            }
        }
    }
}
