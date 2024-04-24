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
    private final int USERNAME_INDEX = 0;
    private final int PASSWORD_INDEX = 1;

    private final int REGISTRATION_LENGTH = 4;
    private final int LOGIN_LENGTH = 2;

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
        return isRegisteredOrLogged(tokens, CHECK_FOR_REGISTRATION);
    }

    /**
     * Checks if the provided password is correct for a given username.
     *
     * @param tokens an array of tokens representing user credentials
     * @return true if the password is correct, false otherwise
     * @throws FileNotFoundException if the user data file is not found
     */
    public boolean correctPassword(String[] tokens) throws FileNotFoundException {
        return isRegisteredOrLogged(tokens, CHECK_FOR_LOG);
    }

    private boolean isRegisteredOrLogged(String tokens[], boolean regOrLog) throws FileNotFoundException {
        sc = new Scanner(new File(DATA));
        String line = "";
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            if (tokensInLine(tokens, line, regOrLog)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether are tokens equal to either username from line in data file (in registration case) or
     * whether are tokens equal to password and username.
     *
     * @param tokens    an array of tokens representing user credentials
     * @param line      line from DATA representing some already registered user
     * @param regOrLog  tells whether is user trying to log in or register
     * @return  if user is trying to log in then it returns true if password and usernames from tokens
     *          are identical those in line and false otherwise.
     *          If user is trying to register, then it returns true if usernames in tokens and in line are
     *          identical.
     */
    private boolean tokensInLine(String[] tokens, String line, boolean regOrLog) {
        String[] splLine = line.split(";");
        if (regOrLog == CHECK_FOR_LOG) {
            if (splLine[USERNAME_INDEX].equals(tokens[USERNAME_INDEX]) &&
                splLine[PASSWORD_INDEX].equals(tokens[PASSWORD_INDEX])) {
                return true;
            }
        } else if (regOrLog == CHECK_FOR_REGISTRATION) {
            if (splLine[USERNAME_INDEX].equals(tokens[USERNAME_INDEX])) {
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
    public void registerUser(String[] data) throws IOException {
        for (int i = 0; i < data.length; i++) {
            writer.write(data[i] + ";");
        }
        writer.newLine();
      //  System.out.println("Line added to the file.");
        writer.close();
    }

    /**
     * Logs in a user with the provided tokens.
     *
     * @param tokens    an array of tokens representing user credentials
     * @param output    the output stream to communicate with the client
     * @return true if login is successful, false otherwise
     * @throws IOException if an I/O error occurs while writing to the output stream
     */
    public boolean log_user(String[] tokens, DataOutputStream output) throws IOException {

        if (tokens.length == REGISTRATION_LENGTH) {
            if (is_registered(tokens)) {
       //         System.out.println("Registration unsuccessful");
                output.write("REGISTRATION UNSUCCESSFUL".getBytes());
                output.flush();
                return false;
            }
            registerUser(tokens);
            logged = true;
     //       System.out.println("Client: " + tokens[USERNAME_INDEX] + " registered successfully");
            output.write("LOGIN SUCCESSFUL".getBytes());
            output.flush();
            return true;
        } else if (tokens.length == LOGIN_LENGTH) {

            if (correctPassword(tokens)) {
                logged = true;
      //          System.out.println("Client: " + tokens[USERNAME_INDEX] + " logged in successfully");
                output.write("LOGIN SUCCESSFUL".getBytes());
                output.flush();
                return true;
            } else {
     //           System.out.println("Attempt to log in by client: " + tokens[USERNAME_INDEX] + " was unsuccessful");
                output.write("INCORRECT PASSWORD OR USERNAME".getBytes());
                output.flush();
                return false;
            }
        } else {
     //       System.out.println("Attempt to log in by client: " + tokens[USERNAME_INDEX] + " was unsuccessful");
            output.write("INCORRECT PASSWORD OR USERNAME".getBytes());
            output.flush();
            return false;
        }
    }
}
