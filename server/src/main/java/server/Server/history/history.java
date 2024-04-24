package server.Server.history;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import server.Server.registrator;

/**
 * Provides methods for managing chat history. It stores messages from all users in one text file,
 * where each message is stored in format: "sender;receiver;date;time;message".
 */
public class history {
    static Scanner sc;
    static BufferedWriter writer;

    static String DATA;
    static final int USER1_INDEX = 0;
    static final int USER2_INDEX = 1;
    static final int TIME_INDEX = 2;
    static final int MESSAGE_START_INDEX = 3;
    static final int COMMAND_LENGTH = 2;
    static final int FULL_LINE_LENGTH = 4;
    static final int COMMAND_INDEX = 0;
    static final int TARGET_INDEX = 1;


    /**
     * Initiates data.
     * 
     * @param path_for_data path to said data
     */
    public static void init_Data(String path_for_data) {
        DATA = path_for_data;
    }

    /**
     * Writes a chat message to the history file.
     *
     * @param user1 The username of the sender.
     * @param user2 The username of the recipient.
     * @param msg   The message content.
     * @throws IOException If an I/O error occurs when writing to the history file.
     */
    public static void write_history(String user1, String user2, String msg) throws IOException {
        writer = new BufferedWriter(new FileWriter(DATA, true));

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        writer.write(user1 + ";" + user2 + ";" + formatter.format(date) + ";" + msg);
        writer.newLine();
     //   System.out.println("Line added to the file.");
        writer.close();
    }

    /**
     * Retrieves the chat history between two users and sends it to the client.
     *
     * @param your_username  Your username.
     * @param user2_username The username of the other user.
     * @param output         The output stream to send the history to the client.
     * @throws IOException            If an I/O error occurs when reading the history file or sending data to the client.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    public static void get_user_to_user_history(String your_username, String user2_username,
                                                DataOutputStream output) throws IOException, InterruptedException {
        String line = "";
        sc = new Scanner(new File(DATA));
        if (!can_open_window(your_username, user2_username)) {
            return;
        }

        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] splitted_line = line.split(";");
            if (correct_line_hist(your_username, user2_username, splitted_line)) {
                output.write((user2_username + " " + splitted_line[TIME_INDEX] + " " +
                        splitted_line[USER1_INDEX] + " " +
                        get_msg(splitted_line)).getBytes());
                output.flush();
                TimeUnit.MILLISECONDS.sleep(300);
            }
        }
    }

    /**
     * Checks if there was sent request by one user and then it was accepted by other user.
     *
     * @param your_username  Your username.
     * @param user2_username The username of the other user.
     * @return True if there is chat history between the two users, false otherwise.
     * @throws FileNotFoundException If the history file is not found.
     */
    public static boolean can_open_window(String your_username, String user2_username) throws FileNotFoundException {
        String line = "";
        sc = new Scanner(new File(DATA));

        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] splitted_line = line.split(";");
            if (correct_line(your_username, user2_username, splitted_line)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a request can be sent from the specified sender to the target user.
     *
     * @param your_username the username of the sender
     * @param target_username the username of the target user
     * @param req the registrator object
     * @return {@code true} if the request can be sent, {@code false} otherwise
     * @throws IOException if an I/O error occurs while reading the data file
     */
    public static boolean can_send_request(String your_username, String target_username, registrator req) throws IOException {
        String line = "";
        sc = new Scanner(new File(DATA));

        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] splitted_line = line.split(";");
            if (correct_line_req(your_username, target_username, splitted_line, req)) {
  //              System.out.println("lajna z history: " + line);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a request from the specified sender to the target user can be accepted. It checks
     * if request to this user was sent from other user.
     *
     * @param your_username the username of the sender
     * @param target_username the username of the target user
     * @return {@code true} if the request can be accepted, {@code false} otherwise
     * @throws IOException if an I/O error occurs while reading the data file
     */
    public static boolean can_accept_request(String your_username, String target_username) throws IOException {
        String line = "";
        sc = new Scanner(new File(DATA));

        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] splitted_line = line.split(";");
            if (correct_line_acc(your_username, target_username, splitted_line)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the specified line from the history file represents request sent from user1 to user2.
     *
     * @param user1 the username of one user involved in the request
     * @param user2 the username of the other user involved in the request
     * @param splitted_line line splitted by ";"
     * @return {@code true} if the request can be accepted, {@code false} otherwise
     */
    public static boolean correct_line_acc(String user1, String user2, String[] splitted_line) {
        if (!(splitted_line.length == FULL_LINE_LENGTH && 
            splitted_line[MESSAGE_START_INDEX].split(" ").length == COMMAND_LENGTH)) {
            return false;
        }

        return (splitted_line[USER1_INDEX].equals(user2) &&
                splitted_line[USER2_INDEX].equals(user1) &&
                splitted_line[MESSAGE_START_INDEX].split(" ")[COMMAND_INDEX].equals("req") &&
                splitted_line[MESSAGE_START_INDEX].split(" ")[TARGET_INDEX].equals(user1));
    }


    /**
     * Checks if a line in the history contains message with "acc". This holds if and only if
     * request for chatting was sent from one user and accepted by other.
     *
     * @param user1         The username of one user.
     * @param user2         The username of the other user.
     * @param splitted_line The split line of chat history.
     * @return True if the line corresponds to a conversation between the two users, false otherwise.
     */
    public static boolean correct_line(String user1, String user2, String[] splitted_line) {

        if (!(splitted_line.length == FULL_LINE_LENGTH && 
            splitted_line[MESSAGE_START_INDEX].split(" ").length ==COMMAND_LENGTH)) {
            return false;
        }

        return ((splitted_line[USER1_INDEX].equals(user1) &&
                splitted_line[USER2_INDEX].equals(user2)) ||
                (splitted_line[USER2_INDEX].equals(user1) &&
                        splitted_line[USER1_INDEX].equals(user2))) &&  splitted_line[MESSAGE_START_INDEX].split(" ")[0].equals("acc");
    }

    /**
     * Checks if the specified line from the history file represents message sent from user1 to user2.
     *
     * @param user1 the username of one user involved in the conversation
     * @param user2 the username of the other user involved in the conversation
     * @param splitted_line the array containing the split components of the line
     * @return {@code true} if the line represents a correct history entry, {@code false} otherwise
     */
    public static boolean correct_line_hist(String user1, String user2, String[] splitted_line) {
        return ((splitted_line[USER1_INDEX].equals(user1) &&
                splitted_line[USER2_INDEX].equals(user2)) ||
                (splitted_line[USER2_INDEX].equals(user1) &&
                splitted_line[USER1_INDEX].equals(user2)));
    }

    /**
     * Checks if the specified line from the history file represents a correct request.
     *
     * @param user1 the username of one user involved in the request
     * @param user2 the username of the other user involved in the request
     * @param splitted_line the array containing the split components of the line
     * @param reg the registrator object
     * @return {@code true} if the line represents a correct request, {@code false} otherwise
     * @throws IOException if an I/O error occurs while reading the data file
     */
    public static boolean correct_line_req(String user1, String user2, String[] splitted_line, registrator reg) throws IOException {
        if (!(splitted_line.length == FULL_LINE_LENGTH
             && splitted_line[MESSAGE_START_INDEX].split(" ").length == COMMAND_LENGTH)) {
            return false;
        }
        return ((splitted_line[USER1_INDEX].equals(user2) &&
                splitted_line[USER2_INDEX].equals(user1)) ||
                (splitted_line[USER1_INDEX].equals(user1) &&
                splitted_line[USER2_INDEX].equals(user2))) &&
                splitted_line[MESSAGE_START_INDEX].split(" ")[COMMAND_INDEX].equals("acc") &&
                splitted_line[MESSAGE_START_INDEX].split(" ")[TARGET_INDEX].equals(user1);
    }

    /**
     * Extracts the message content from a line of chat history.
     *
     * @param line The split line of chat history.
     * @return The message content.
     */
    public static String get_msg(String[] line) {
        String result = "";
        for (int i = MESSAGE_START_INDEX; i < line.length; i++) {
            result += line[i] += "; ";
        }
    //    System.out.println(result);
        return result;
    }
}
