package de.hhu.propra.team61.Network;

import de.hhu.propra.team61.MapWindow;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;

import static de.hhu.propra.team61.JavaFxUtils.extractPart;

/**
 * Created by markus on 15.05.14.
 */
public class Server implements Runnable {
    private static final int PORT = 9042;

    private static HashSet<String> names = new HashSet<>();
    private static HashSet<PrintWriter> writers = new HashSet<>();

    ServerSocket listener;
    private static Networkable currentNetworkable;

    public void run() {
        try {
            listener = new ServerSocket(PORT);
            try {
                while (true) {
                    new Thread(new ConnectionHandler(listener.accept())).start();
                }
            } catch (SocketException e) {
                listener.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("SERVER shut down");
    }

    public void stop() {
        System.out.println("SERVER stopping");
        try {
            if(listener != null) {
                listener.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerCurrentNetworkable(Networkable networkable) {
        this.currentNetworkable = networkable;
    }

    public static void sendCommand(String command) {
        synchronized (writers) {
            for (PrintWriter writer : writers) {
                String message = "COMMAND " + command;
                writer.println(message);
                System.out.println("SERVER sent command: " + message);
            }
        }
    }


    private static class ConnectionHandler implements Runnable {
        private Socket socket;

        public ConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try(ClientConnection connection = new ClientConnection(socket)) {
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static class ClientConnection implements AutoCloseable {
        private final BufferedReader in;
        private final PrintWriter out;
        private final Socket socket;
        private String name;

        public ClientConnection(Socket socket) throws IOException {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        public void connect() throws IOException {
            getName();
            out.println("NAMEACCEPTED");
            System.out.println("SERVER: connection accepted");
            writers.add(out);
            broadcast();
        }

        private void getName() throws IOException {
            while(true) {
                out.println("SUBMITNAME");
                System.out.println("SERVER: asked for name");
                name = in.readLine();
                if(name == null) {
                    return;
                }
                synchronized (names) {
                    if(!names.contains(name)) {
                        names.add(name);
                        break;
                    }
                }
            }
        }

        private void broadcast() throws IOException {
            while(true) {
                String line = in.readLine();
                if (line == null) {
                    return;
                }
                String clientName = line.split(" ", 2)[0];
                if (line.contains("CHAT ")) {
                    sendCommand(line);
                } if (line.contains("GET_STATUS")) {
                    out.println(currentNetworkable.getStateForNewClient());
                } else if (line.contains("KEYEVENT ")) {
                    if (clientName.equals(Client.name)) { // TODO hardcoded spectator mode
                        Platform.runLater(() -> currentNetworkable.handleKeyEventOnServer(extractPart(line, "KEYEVENT ")));
                    } else {
                        System.out.println("SERVER: operation not allowed for " + clientName + ": " + line);
                        System.out.println("    only allowed for " + Client.name);
                    }
                } else {
                    System.out.println("SERVER: unhandled message: " + line);
                }
//                synchronized (writers) {
//                    for (PrintWriter writer : writers) {
//                        String message = "MESSAGE " + name + ": " + line;
//                        System.out.println("SERVER send received message: " + message);
//                        writer.println(message);
//                    }
//                }
            }
        }

        @Override
        public void close() throws Exception {
            if(name != null) {
                names.remove(name);
            }
            if(out != null) {
                writers.remove(out);
            }
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
