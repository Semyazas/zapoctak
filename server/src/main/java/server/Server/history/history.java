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
 * Provides methods for managing chat history.
 */
public class history {
    static Scanner sc;
    static BufferedWriter writer;


    final static String DATA = "C:\\Users\\marti\\OneDrive\\Plocha\\bin_tree.java\\zapoctak\\server\\src\\main\\java\\server\\Server\\history\\history.txt";
    static final int USER1_INDEX = 0;
    static final int USER2_INDEX = 1;
    static final int TIME_INDEX = 2;
    static final int MESSAGE_START_INDEX = 3;

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
        System.out.println("Line added to the file.");
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
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] splitted_line = line.split(";");
            if (correct_line(your_username, user2_username, splitted_line)) {
                output.write((splitted_line[TIME_INDEX] + " " +
                        splitted_line[USER1_INDEX] + ": " +
                        get_msg(splitted_line)).getBytes());
                output.flush();
                TimeUnit.MILLISECONDS.sleep(5);

            }
        }
    }

    /**
     * Checks if there is chat history between two users.
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


    public static boolean can_send_request(String your_username,
                         String target_username, registrator req) throws IOException {
        String line = "";
        sc = new Scanner(new File(DATA));

        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] splitted_line = line.split(";");
            if (correct_line_req(your_username, target_username, splitted_line, req)) {
                return false;
            }
        }
        return true;
    }

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

     public static boolean correct_line_acc(String user1, String user2 ,String[] splitted_line) {
        if (!(splitted_line.length == 4 && splitted_line[MESSAGE_START_INDEX].split(" ").length ==2)) {
            return false;
        }
        System.out.println("aaaaaaaaaaaaaaa");
        System.out.println(splitted_line[USER1_INDEX].equals(user2) + " " +
            splitted_line[USER2_INDEX].equals(user1) + " " +
            splitted_line[MESSAGE_START_INDEX].split(" ")[0].equals("req") + " " +  
            splitted_line[MESSAGE_START_INDEX].split(" ")[1].equals(user1));

        return (splitted_line[USER1_INDEX].equals(user2) &&
                splitted_line[USER2_INDEX].equals(user1) && 
                splitted_line[MESSAGE_START_INDEX].split(" ")[0].equals("req") && 
                splitted_line[MESSAGE_START_INDEX].split(" ")[1].equals(user1));
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

        if (!(splitted_line.length == 4 && splitted_line[MESSAGE_START_INDEX].split(" ").length ==2)) {
            return false;
        }

        return ((splitted_line[USER1_INDEX].equals(user1) &&
                splitted_line[USER2_INDEX].equals(user2)) ||
                (splitted_line[USER2_INDEX].equals(user1) &&
                        splitted_line[USER1_INDEX].equals(user2))) &&  splitted_line[MESSAGE_START_INDEX].split(" ")[0].equals("acc");
    }

    public static boolean correct_line_req(String user1, String user2, String[] splitted_line,registrator reg) throws IOException {

        if (!(splitted_line.length == 4 && splitted_line[MESSAGE_START_INDEX].split(" ").length ==2)) {
            return false;
        }
        boolean b = ((splitted_line[USER1_INDEX].equals(user2) &&
        splitted_line[USER2_INDEX].equals(user1)) || 
        (splitted_line[USER1_INDEX].equals(user1) &&
        splitted_line[USER2_INDEX].equals(user2))) &&
        splitted_line[MESSAGE_START_INDEX].split(" ")[0].equals("acc") && 
        splitted_line[MESSAGE_START_INDEX].split(" ")[1].equals(user1);
        System.out.println("hist podm: " + b);
        return ((splitted_line[USER1_INDEX].equals(user2) &&
                splitted_line[USER2_INDEX].equals(user1)) || 
                (splitted_line[USER1_INDEX].equals(user1) &&
                splitted_line[USER2_INDEX].equals(user2))) &&
                splitted_line[MESSAGE_START_INDEX].split(" ")[0].equals("acc") && 
                splitted_line[MESSAGE_START_INDEX].split(" ")[1].equals(user1);
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
        System.out.println(result);
        return result;
    }
}
