package de.hhu.propra.team61.Network;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import static de.hhu.propra.team61.JavaFxUtils.extractPart;

/**
 * Created by markus on 15.05.14.
 */
public class Client implements Runnable {
    BufferedReader in;
    Socket socket;
    PrintWriter out;
    String serverAddress;
    static String id; //! must not contain spaces!
    private String name;

    Runnable readyListener;

    Networkable currentNetworkable;

    /**
     * @param ipAddress the ip address of the server
     * @param listener function which is called when the client successfully established a connection with the server
     */
    public Client(String ipAddress, String name, Runnable listener) {
        serverAddress = ipAddress;
        readyListener = listener;
        id = "uninitializedClient";
        this.name = name;
    }

    /**
     * constructor for a client connecting with localhost
     */
    public Client(String name, Runnable listener) {
        this("127.0.0.1", name, listener);
    }

    /**
     * constructor for a client connecting with localhost in local mode
     */
    public Client(Runnable listener) {
        this("127.0.0.1", "HOST", listener);
    }

    public void run() {
        try {
            socket = new Socket(serverAddress, Server.PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String line = in.readLine();
                if(line == null) {
                    System.out.println("CLIENT RECEIVED NULL!?");
                    return;
                }
                System.out.println("CLIENT RECEIVED: " + line);
                if(line.startsWith("SUBMITNAME")) {
                    id = "client"+(int)(Math.random()*1000)+(int)(System.currentTimeMillis()%100000/100);
                    System.out.println("CLIENT id: " + id + " / " + name);
                    out.println(id + " " + name);
                } else if(line.startsWith("NAMEACCEPTED")) {
                    System.out.println("CLIENT: connected");
                    readyListener.run();
                } else if(line.startsWith("EXIT")) {
                    System.out.println("CLIENT: exit");
                    break;
                } else {
                    // use runLater; otherwise, an exception will be thrown:
                    // Exception in thread "Thread-4" java.lang.IllegalStateException: Not on FX application thread; currentThread = Thread-4
                    Platform.runLater(() -> currentNetworkable.handleOnClient(extractPart(line, "COMMAND ")));
                }
            }
        } catch (SocketException e) {
            System.out.println("CLIENT readLine() interrupted by SocketException: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendKeyEvent(KeyCode code) {
        send("KEYEVENT " + code.getName());
    }

    public void sendChatMessage(String msg) {
        send("CHAT " + msg);
    }

    public void send(String message) {
        System.out.println("CLIENT send: " + id + " " + message);
        out.println(id + " " + message);
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

    public void registerCurrentNetworkable(Networkable networkable) {
        this.currentNetworkable = networkable;
    }
}
