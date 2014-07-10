package de.hhu.propra.team61.network;

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
 * Game Client which handles all events received from the connected game server.
 * <p>
 * Only one client should run at the same time. To create a new thread running a game client, use
 * {@code clientThread = new Thread(client = new client(() -> {callbackFunction};} and start the thread using
 * {@code clientThread.start();}. When the client is ready to accept connection, the given callback function is called.
 * After creating a new client object, {@link #registerCurrentNetworkable(Networkable)} should be called with
 * {@code this} being the argument. Call {@link #stop()} to shut down the client thread properly.
 * </p>
 * See {@link de.hhu.propra.team61.network.Server} for a list of commands that should be interpreted by the client.
 */
public class Client implements Runnable {
    /** reader which receives the command from the server */
    private BufferedReader in;
    /** socket of the connection with the client */
    private Socket socket;
    /** writer for sending messages to the server */
    private PrintWriter out;
    /** ip address of the server, port {@link de.hhu.propra.team61.network.Server#PORT} is used */
    private String serverAddress;
    /** id of this client, which is used by the server to uniquely identify this client; must not contain spaces */
    static String id;
    /** the name of the player sitting in front of this client */
    private String name;

    /** the number of the team which is controlled by this client, counting starts from 0 = host, -1 = spectator */
    private int associatedTeam;
    /** true if the client is connected to a local game (ie. only one client is running) */
    private boolean isLocalGame = false;

    /** method being called when the client has successfully connected with the server (ie. id and name are accepted) */
    private Runnable readyListener;

    /** the currently shown view, which can handle received commands */
    private Networkable currentNetworkable;

    /**
     * Creates a new client object; does not connect with a server, {@see run()}
     * @param ipAddress the ip address of the server
     * @param name the name of the player creating sitting in front of this client
     * @param listener function which is called when the client successfully established a connection with the server
     */
    public Client(String ipAddress, String name, Runnable listener) {
        serverAddress = ipAddress;
        readyListener = listener;
        id = "uninitializedClient";
        this.name = name;
    }

    /**
     * Convenience constructor for a client connecting with localhost.
     * This constructor is typically used for the client running on the host. Equivalent to calling
     * {@code Client("127.0.0.1", name, listener);}.
     * @param name the name of the player creating sitting in front of this client
     * @param listener function which is called when the client successfully established a connection with the server
     * @see Client(String, String, Runnable)
     */
    public Client(String name, Runnable listener) {
        this("127.0.0.1", name, listener);
    }

    /**
     * Convenience constructor for a client connecting with localhost in local mode, sets {@link #isLocalGame} to {@code true}.
     * This constructor should be used for the client running in a local game. This is NOT equivalent to calling
     * {@code Client("127.0.0.1", HOST, listener);} since this constructor does not change {@link #isLocalGame}.
     * @param listener function which is called when the client successfully established a connection with the server
     * @see Client(String, String, Runnable)
     */
    public Client(Runnable listener) {
        this("127.0.0.1", "HOST", listener);
        isLocalGame = true;
    }

    /**
     * Tries to establish a connection with a game server with ip {@link #serverAddress}.
     * Sets up {@link #socket}, {@link #in}, and {@link #out}, adn can negotiates a unique id with the server; all other
     * commands are passed to {@link de.hhu.propra.team61.network.Networkable#handleOnClient(String)} of
     * {@link #currentNetworkable}.
     */
    public void run() {
        try {
            socket = new Socket(serverAddress, Server.PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String line = in.readLine();
                if(line == null) {
                    System.err.println("CLIENT RECEIVED NULL!?");
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
                    Platform.runLater(() -> currentNetworkable.handleOnClient(line));
                }
            }
        } catch (SocketException e) {
            System.err.println("CLIENT readLine() interrupted by SocketException: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to the server, starting with "KEYEVENT ", followed by the string represantation of a key code.
     * @param code the keycode to be sent
     */
    public void sendKeyEvent(KeyCode code) {
        send("KEYEVENT " + code.getName());
    }

    /**
     * Used to send a chat message command to the server.
     * The messages is proceeded with "CHAT ".
     * @param msg the chat massage to be sent
     */
    public void sendChatMessage(String msg) {
        send("CHAT " + msg);
    }

    /**
     * Sends the given message to the client.
     * @param message the message being sent to the client
     */
    public void send(String message) {
        System.out.println("CLIENT " + id + " send: " + message);
        out.println(message);
    }

    /**
     * Stops this client by closing the {@link #socket}.
     */
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

    /**
     * Sets {@link #currentNetworkable}.
     * The given object should have a sensible implementation of {@link de.hhu.propra.team61.network.Networkable#handleOnClient(String)},
     * ie. must understand commands relevant to the view.
     * @param networkable the view which will receive the commands from the server
     * @see #run()
     */
    public void registerCurrentNetworkable(Networkable networkable) {
        this.currentNetworkable = networkable;
    }

    /**
     * Checks if the client is connected with a local game.
     * @return true if the client is connected with a local game
     */
    public boolean isLocalGame() {
        return isLocalGame;
    }

    /**
     * Gets the team which is controlled by this client.
     * Counting starts from 0 = host, -1 = spectator.
     * @return the team associated with this client
     */
    public int getAssociatedTeam() {
        return associatedTeam;
    }

    /**
     * Sets the number of the team which is controlled by this client.
     * Counting starts from 0 = host, -1 = spectator.
     */
    public void setAssociatedTeam(int associatedTeam) {
        this.associatedTeam = associatedTeam;
    }
}
