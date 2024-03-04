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

public class history {
    static Scanner sc;
    static BufferedWriter writer;

    final static String DATA     = "C:\\Users\\marti\\OneDrive\\Plocha\\bin_tree.java\\zapoctak\\server\\src\\main\\java\\server\\Server\\history\\history.txt";
    static final int USER1_INDEX = 0;
    static final int USER2_INDEX = 1;
    static final int TIME_INDEX  = 2;
    static final int MESSAGE_START_INDEX  = 3;



    public static void write_history(String user1, String user2,String msg) throws IOException {
        writer = new BufferedWriter( new FileWriter(DATA,true));

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
        Date date = new Date(); 
         
        writer.write(user1 + ";" + user2 + ";"+ formatter.format(date) + ";" + msg);
        writer.newLine();
        System.out.println("Line added to the file.");
        writer.close();
    } 
    
    /*      zprávy se budou zapisovat ve formátu: 
     *      user1;user2;čas napsání zprávy;samotná zpráva
     *      kde user1 poslal zprávu user2
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

    public static void send_unread_messages(String your_username, String user2_username,
                            DataOutputStream output) throws IOException, InterruptedException {
        String line = "";
        sc = new Scanner(new File(DATA));
        output.write((your_username + " wacc " + user2_username).getBytes());
        output.flush();

        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] splitted_line = line.split(";");
            if (splitted_line[splitted_line.length-1].equals("---unread---")) {
                output.write((splitted_line[TIME_INDEX] + " " + 
                             splitted_line[USER1_INDEX] + ": " + 
                             get_msg(splitted_line)).getBytes());
                output.flush();
                
                TimeUnit.MILLISECONDS.sleep(5);

            }
        }
    }

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

    public static boolean correct_line(String user1, String user2,String[] splitted_line) {
        return  (splitted_line[USER1_INDEX].equals(user1) && 
                    splitted_line[USER2_INDEX].equals(user2)) || 
                (splitted_line[USER2_INDEX].equals(user1) &&
                    splitted_line[USER1_INDEX].equals(user2));
    }

    public static String get_msg(String[] line) {
        String result = "";
        for (int i = MESSAGE_START_INDEX; i < line.length; i++) {
            result += line[i] += "; ";
        }
        System.out.println(result);
        return result;
    }
}
