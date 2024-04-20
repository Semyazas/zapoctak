package server.Server;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * The Registrator class provides functionality for user registration and login.
 * It allows users to register, log in, and performs validation of credentials.
 */
public class registrator {

    private Scanner sc;
    private BufferedWriter writer;
    boolean logged;
    private final String DATA;
    private final Boolean CHECK_FOR_LOG = true;
    private final Boolean CHECK_FOR_REGISTRATION = false;

    /**
     * Constructor for the Registrator class.
     * Initializes the Scanner and BufferedWriter objects.
     * 
     * @param data the path to the file containing user data
     * @throws IOException if an I/O error occurs while initializing objects
     */
    public registrator(String data) throws IOException {
        sc = new Scanner(new File(data));
        writer = new BufferedWriter(new FileWriter(data, true));
        logged = false;
        DATA = data;
    }

    /**
     * Checks if a user is registered based on provided tokens.
     * 
     * @param tokens an array of tokens representing user credentials
     * @return true if the user is registered, false otherwise
     * @throws FileNotFoundException if the user data file is not found
     */
    public boolean is_registered(String[] tokens) throws FileNotFoundException {
        return is_registered_or_logged(tokens, CHECK_FOR_REGISTRATION);
    }

    /**
     * Checks if the provided password is correct for a given username.
     * 
     * @param tokens an array of tokens representing user credentials
     * @return true if the password is correct, false otherwise
     * @throws FileNotFoundException if the user data file is not found
     */
    public boolean correct_password(String[] tokens) throws FileNotFoundException {
        return is_registered_or_logged(tokens, CHECK_FOR_LOG);
    }

    private boolean is_registered_or_logged(String tokens[], boolean reg_or_log) throws FileNotFoundException {
        String line = "";
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            if (tokens_in_line(tokens, line, reg_or_log)) {
                return true;
            }
        }
        sc = new Scanner(new File(DATA));
        return false;
    }
    /**
     * Checks whether are tokens equal to either username from line in data file (in registration case) or 
     * whether are tokens equal to password and username.
     * 
     * @param tokens an array of tokens representing user credentials
     * @param line   line from DATA representing some already registered user
     * @param reg_or_log tells whether is user trying to log in or register
     * @return  if user is trying to log in then it returns true if password and usernames from tokens 
     *          are identical those in line and false otherwise. 
     *          If user is trying to register, then it returns true if usernames in tokens and in line are
     *          identical.  
     */

    private boolean tokens_in_line(String[] tokens, String line, boolean reg_or_log) {
        String[] splLine = line.split(";");
        if (reg_or_log == CHECK_FOR_LOG) {
            if (splLine[0].equals(tokens[0]) && splLine[1].equals(tokens[1])) {
                return true;
            }
        } else if (reg_or_log == CHECK_FOR_REGISTRATION) {
            if (splLine[0].equals(tokens[0])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Registers a new user with the provided data.
     * 
     * @param data an array of data representing user information
     * @throws IOException if an I/O error occurs while writing to the file
     */
    public void register_user(String[] data) throws IOException {
        for (int i = 0; i < data.length; i++) {
            writer.write(data[i] + ";");
        }
        writer.newLine();
        System.out.println("Line added to the file.");
        writer.close();
    }

    /**
     * Logs in a user with the provided tokens.
     * 
     * @param tokens an array of tokens representing user credentials
     * @param userName the username of the user
     * @param passWord the password of the user
     * @param output the output stream to communicate with the client
     * @throws IOException if an I/O error occurs while writing to the output stream
     */
    public void log_user(String[] tokens, String userName, String passWord, DataOutputStream output) throws IOException {
        System.out.println("Client: his username is " + userName + " and his password is: " + passWord);
        if (tokens.length == 4) {

            if (is_registered(tokens)) {
                System.out.println("Registration unseccesfull");
                output.write("REGISTRATION UNSUCCESSFULL".getBytes());
                output.flush();
                return;
            }
            register_user(tokens);
            logged = true;
            System.out.println("Client: " + userName + " registered succesfully");
            output.write("LOGIN SUCCESSFULL".getBytes());
            output.flush();
        } else if (tokens.length == 2) {

            if (correct_password(tokens)) {
                logged = true;
                System.out.println("Client: " + userName + " logged in succesfully");
                output.write("LOGIN SUCCESSFULL".getBytes());
                output.flush();
            
            } else {
                System.out.println("Attempt to log in by client: " + userName + " was unsuccsesfull");
                output.write("INCORRECT PASSWORD OR USERNAME".getBytes());
                output.flush();
            }
        }
    }
}
