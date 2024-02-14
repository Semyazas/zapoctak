package zpct;

import javax.swing.SwingUtilities;

import zpct.GUI.ChatAppGUI;


/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatAppGUI();
            }
        });
    }
}
