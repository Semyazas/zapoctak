package zpct;

import javax.swing.SwingUtilities;

import zpct.GUI.ChatAppGUI;
import zpct.client.Client;


/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        Client c = new Client(null, 0);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatAppGUI();
            }
        });
    }
}
