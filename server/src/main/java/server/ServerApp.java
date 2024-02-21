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

public class ServerApp {
    public static void main(String[] args) throws IOException {
        Server s = new Server();
    }
}
    