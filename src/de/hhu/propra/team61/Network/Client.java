package de.hhu.propra.team61.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by markus on 15.05.14.
 */
public class Client implements Runnable {
    BufferedReader in;
    PrintWriter out;

    public void run() {
        try {
            String serverAddress = "127.0.0.1"; // TODO
            Socket socket = new Socket(serverAddress, 9042);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String line = in.readLine();
                System.out.println("CLIENT RECEIVED: " + line);
                if(line.startsWith("SUBMITNAME")) {
                    out.println("MUSTERMANN");
                } else if(line.startsWith("NAMEACCEPTED")) {
                    System.out.println("CLIENT: connected");
                } else if(line.startsWith("EXIT")) {
                    System.out.println("CLIENT: exit");
                    break;
                }
                System.out.println("e");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        System.out.println("CLIENT stopping");
        try {
            if(in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
