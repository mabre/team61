package de.hhu.propra.team61.Network;

import de.hhu.propra.team61.MapWindow;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;

import javax.xml.bind.SchemaOutputResolver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by markus on 15.05.14.
 */
public class Client implements Runnable {
    BufferedReader in;
    Socket socket;
    PrintWriter out;
    MapWindow mapwindow;

    public Client(MapWindow mapWindow) {
        this.mapwindow = mapWindow;
    }

    public void run() {
        try {
            String serverAddress = "127.0.0.1"; // TODO
            socket = new Socket(serverAddress, 9042);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String line = in.readLine();
                System.out.println("CLIENT RECEIVED: " + line);
                if(line.startsWith("SUBMITNAME")) {
                    out.println("MUSTERMANN");
                } else if(line.startsWith("NAMEACCEPTED")) {
                    System.out.println("CLIENT: connected");
                // use runLater; otherwise, an exception will be thrown:
                // Exception in thread "Thread-4" java.lang.IllegalStateException: Not on FX application thread; currentThread = Thread-4
                } else if(line.contains("KEYEVENT Number Sign")) {
                    Platform.runLater(() -> mapwindow.cheatMode());
                } else if(line.contains("KEYEVENT Space")) {
                    Platform.runLater(() -> mapwindow.endTurn());
                } else if(line.startsWith("EXIT")) {
                    System.out.println("CLIENT: exit");
                    break;
                }
            }
        } catch (SocketException e) {
            System.out.println("CLIENT readLine() interrupted by SocketException");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendKeyEvent(KeyCode code) {
        send("KEYEVENT " + code.getName());
    }

    private void send(String message) {
        System.out.println("CLIENT send: " + message);
        out.println(message);
    }

    public void stop() {
        System.out.println("CLIENT stopping");
        try {
            if(socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
