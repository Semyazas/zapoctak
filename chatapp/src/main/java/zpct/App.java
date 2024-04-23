
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
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

        if (args.length == 2 && pattern.matcher(args[0]).matches()) {
            new Client(Integer.parseInt(args[0]),args[1]); // Start the client application
        } else {
            new Client(Integer.parseInt("12345"),"localhost"); // Start the client application
            System.out.println("Invalid input");
        }
    }
}
