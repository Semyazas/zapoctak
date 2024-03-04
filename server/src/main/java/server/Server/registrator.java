package server.Server;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class registrator {
    Scanner sc;
    BufferedWriter writer;
    boolean logged;
    final String DATA;
    final Boolean CHECK_FOR_LOG = true;
    final Boolean CHECK_FOR_REGISTRATION = false;

    registrator(String data) throws IOException {
        sc = new Scanner( new File(data));
        writer = new BufferedWriter (new FileWriter(data, true));
        logged = false;
        DATA = data;
    }

    public boolean is_registered(String[] tokens) throws FileNotFoundException {
        return is_registered_or_logged(tokens, CHECK_FOR_REGISTRATION);
    }

    public boolean correct_password(String[] tokens) throws FileNotFoundException { 
        return is_registered_or_logged(tokens, CHECK_FOR_LOG);
    }

    public boolean is_registered_or_logged(String tokens[], boolean reg_or_log) throws FileNotFoundException {
        String line = "";
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            if (tokens_in_line(tokens, line,reg_or_log)) {
                return true;
            }
        }
        sc = new Scanner( new File(DATA));
        return false;
    }

    public boolean tokens_in_line(String[] tokens,String line,boolean reg_or_log) {
        String[] splLine = line.split(";");
        if (reg_or_log == CHECK_FOR_LOG) {
            if (splLine[0].equals(tokens[0]) && splLine[1].equals(tokens[1])) {
                return true;
            }
        } 
        else if(reg_or_log == CHECK_FOR_REGISTRATION) {
            if (splLine[0].equals(tokens[0])) {
                return true;
            }
        } 
        return false;
    }

    public void register_user(String[] data) throws IOException { // první v registraci username, pak password, pak name ... např: user1;123;Karel Novak;
       // Append a new line to the file
        for(int i = 0; i < data.length; i++) {
            writer.write(data[i] + ";");
        }
        writer.newLine();
        System.out.println("Line added to the file.");
        writer.close();
    }

    public void log_user(String[] tokens,String userName, String passWord
                        ,DataOutputStream output) throws IOException {

        System.out.println("Client: his username is "+ userName + " and his password is: " + passWord);
        if (tokens.length == 4) {
            register_user(tokens);
            logged = true;
            System.out.println("Client: " + userName + " registered succesfully");
            output.write("LOGIN SUCCESSFULL".getBytes());
            output.flush();
        } 
        else if (tokens.length == 2) {
            
            if (correct_password(tokens)) {
                logged = true;
                System.out.println("Client: " + userName + " logged in succesfully");
                output.write("LOGIN SUCCESSFULL".getBytes());
                output.flush();
            } else {
                System.out.println("Attempt to log in  by client: " + userName + " was unsuccsesfull");
                output.write("INCORRECT PASSWORD OR USERNAME".getBytes());
                output.flush();
            }
        }   
    }
}
