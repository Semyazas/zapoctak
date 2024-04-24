
package zpct;

import java.util.regex.Pattern;

/**
 * Class that calls Client()
 */

public class App {

    /**
     * Main method to start the client application.
     *
     * @param args args[0] tells which port to use and args[1] which host to use
     */
    static final int number_of_args = 2;
    static final int port_index = 0;
    static final int host_index = 1;
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

        if (args.length == number_of_args && pattern.matcher(args[port_index]).matches()) {
            new Client(Integer.parseInt(args[port_index]),args[host_index]); // Start the client application
        } else {
            System.out.println("Invalid input");
        }
    }
}
