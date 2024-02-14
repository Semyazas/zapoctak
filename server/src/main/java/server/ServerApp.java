package server;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import server.Server.*;
/**
 * Hello world!
 *
 */
public class ServerApp 
{
public static void main(String[] args) {
    Server s = new Server();
      /*   try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Server is waiting for a client to connect...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket);

            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            // Create a thread to handle messages from the client
            Thread clientThread = new Thread(() -> {
                try {
                    while (true) {
                        // Read messages from the client
                        byte[] buffer = new byte[1024];
                        int bytesRead = input.read(buffer);
                        if (bytesRead == -1) {
                            break; // End of stream, client has disconnected
                        }

                        // Check if the message is a file
                        if (new String(buffer, 0, bytesRead).equals("FILE")) {
                            receiveFile("kokosacel.txt",input);
                        } else {
                            String message = new String(buffer, 0, bytesRead);
                            System.out.println("Client: " + message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });

            clientThread.start(); // Start the client thread

            // Allow the server to send messages and files to the client
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                // Read a message from the console
                String serverMessage = consoleReader.readLine();
                
                // Check if the message is a file
                    // Send the message to the client
                output.write(serverMessage.getBytes());
                output.flush();   
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFile2(DataInputStream input) throws IOException {
        System.out.println("Receiving file...");
        try (FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\marti\\OneDrive\\Plocha\\bin_tree.java\\trenink\\src\\cvicneTesty\\zkouska_podprogramky\\chat2\\kok.txt")) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            long size = input.readLong();
            while (size > 0 &&  (bytesRead = input.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            System.out.println("File received successfully!");
        }
    }
    private static void receiveFile(String fileName,DataInputStream dataInputStream) throws Exception{
        System.out.println("funguju");
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\marti\\OneDrive\\Plocha\\bin_tree.java\\trenink\\src\\cvicneTesty\\zkouska_podprogramky\\chat2\\kok.txt");
        
        long size = dataInputStream.readLong();     // read file size
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      // read upto file size
        }
        System.out.println("ja ne ");
        fileOutputStream.close();*/
    }
}
