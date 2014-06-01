package de.hhu.propra.team61.Network;

import de.hhu.propra.team61.IO.JSON.JSONArray;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import static de.hhu.propra.team61.JavaFxUtils.extractPart;

/**
 * Created by markus on 15.05.14.
 */
public class Server implements Runnable {
    static final int PORT = 9042;

    private static ArrayList<Connection> connections = new ArrayList<>();

    Runnable readyListener;

    ServerSocket listener;
    private static Networkable currentNetworkable;

    /**
     * @param listener function which is called when server is set up, thus ready to accept connections
     */
    public Server(Runnable listener) {
        this.readyListener = listener;
    }

    public void run() {
        try {
            listener = new ServerSocket(PORT);
            readyListener.run();
            try {
                while (true) {
                    new Thread(new ConnectionHandler(listener.accept())).start();
                }
            } catch (SocketException e) {
                listener.close();
            }
        } catch(BindException e) {
            System.out.println("ERROR " + e.getLocalizedMessage());
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
        synchronized (connections) {
            for (int i=0; i<connections.size(); i++) {
                String message = "COMMAND " + command;
                connections.get(i).out.println(message);
                System.out.println("SERVER sent command to " + connections.get(i).id+"/"+connections.get(i).name + ": " + message);
            }
        }
    }

    private static boolean clientIdExists(String id) {
        boolean found = false;
        synchronized (connections) {
            for (int i = 0; i < connections.size(); i++) {
                if (connections.get(i).id.equals(id)) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    private static boolean clientNameExists(String name) {
        boolean found = false;
        synchronized (connections) {
            for (int i = 0; i < connections.size(); i++) {
                if (connections.get(i).name.equals(name)) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    private static String getIdFromName(String name) {
        String id = "";
        synchronized (connections) {
            for (int i = 0; i < connections.size(); i++) {
                if (connections.get(i).name.equals(name)) {
                    id = connections.get(i).id;
                    break;
                }
            }
        }
        return id;
    }

    private static String getNameFromId(String id) {
        String name = "";
        synchronized (connections) {
            for (int i = 0; i < connections.size(); i++) {
                if (connections.get(i).id.equals(id)) {
                    name = connections.get(i).name;
                    break;
                }
            }
        }
        return name;
    }

    private static void disconnect(String name) {
        synchronized (connections) {
            try {
                for (int i = 0; i < connections.size(); i++) {
                    if (connections.get(i).name.equals(name)) {
                        connections.get(i).out.close();
                        connections.get(i).in.close();
                        System.out.println("SERVER: removed connection to " + connections.get(i).id + "/" + connections.get(i).name);
                        connections.remove(i);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getSpectatorsAsJson() {
        JSONObject json = new JSONObject();
        JSONArray spectatorsArray = new JSONArray();
        synchronized (connections) {
            for (int i = 0; i < connections.size(); i++) {
                if(!connections.get(i).id.equals(Client.id)) { // TODO hardcoded spectator mode
                    spectatorsArray.put(connections.get(i).name);
                }
            }
        }
        return json.put("spectators", spectatorsArray).toString();
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
        private String id;
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
            if(name != null) {
                synchronized (connections) {
                    connections.add(new Connection(in, out, id, name));
                }
                sendCommand("SPECTATOR_LIST " + getSpectatorsAsJson());
                broadcast();
            }
        }

        private void getName() throws IOException {
            while(true) {
                out.println("SUBMITNAME");
                System.out.println("SERVER: asked for id and name");
                String identifier = in.readLine();
                if(identifier == null) {
                    System.out.println("SERVER: read null identifier");
                    return;
                }
                if(!clientIdExists(id)) {
                    id = identifier.split(" ")[0];
                    name = identifier.split(" ")[1];
                    break;
                }
            }
        }

        private void broadcast() throws IOException {
            try {
                while(true) {
                    String line = in.readLine();
                    if (line == null) {
                        return;
                    }
                    String clientId = line.split(" ", 2)[0]; // TODO equals this.id?
                    if (line.contains("CHAT ")) {
                        String msg = line.split(" ", 3)[2];
                        System.out.println(">");
                        if (msg.startsWith("/kick ")) {
                            System.out.println(">>");
                            String userToKick = msg.split(" ", 2)[1];
                            if(clientNameExists(userToKick)) {
                                System.out.println(">>>");
                                if(clientId.equals(Client.id) || clientId.equals(getIdFromName(userToKick))) {
                                    System.out.println(">>>>");
                                    disconnect(userToKick);
                                    sendCommand("SERVER CHAT command executed. cmd: " + getNameFromId(clientId) + ": " + msg);
                                    sendCommand("SPECTATOR_LIST " + getSpectatorsAsJson());
                                } else {
                                    sendCommand("SERVER CHAT command failed: Operation not allowed. cmd: " + getNameFromId(clientId) + " " + msg);
                                }
                            } else {
                                sendCommand("SERVER CHAT command failed: No such user. cmd: " + getNameFromId(clientId) + " " + msg);
                            }
                        } else {
                            sendCommand(getNameFromId(clientId) + " CHAT " + msg);
                        }
                    } else if (line.contains("GET_STATUS")) {
                        out.println(currentNetworkable.getStateForNewClient());
                    } else if (line.contains("KEYEVENT ")) {
                        if (clientId.equals(Client.id)) { // TODO hardcoded spectator mode (remember the first client connecting as host)
                            Platform.runLater(() -> currentNetworkable.handleKeyEventOnServer(extractPart(line, "KEYEVENT ")));
                        } else {
                            System.out.println("SERVER: operation not allowed for " + clientId + ": " + line);
                            System.out.println("    only allowed for " + Client.id);
                        }
                    } else if (line.contains("STATUS ")) {
                        if (clientId.equals(Client.id)) {
                            sendCommand(extractPart(line, clientId+" "));
                        } else {
                            System.out.println("SERVER: operation not allowed for " + clientId + ": " + line);
                            System.out.println("    only allowed for " + Client.id);
                        }
                    } else {
                        System.out.println("SERVER: unhandled message: " + line);
                    }
                }
            } catch(SocketException e) {
                System.out.println("SERVER: SocketException at " + id+"/"+name + " " + e.getMessage());
            } catch(IOException e) {
                System.out.println("SERVER: IOException at " + id+"/"+name + " " + e.getMessage());
            }
        }

        @Override
        public void close() throws Exception {
            synchronized (connections) {
                if (connections != null) {
                    for (int i = 0; i < connections.size(); i++) {
                        if (connections.get(i).id.equals(id)) {
                            connections.remove(i);
                            break;
                        }
                    }
                }
                if (out != null) {
                    for (int i = 0; i < connections.size(); i++) {
                        if (connections.get(i).out == out) {
                            connections.remove(i);
                            break;
                        }
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }


    private static class Connection {
        public BufferedReader in;
        public PrintWriter out;
        public String id;
        public String name;

        private Connection(BufferedReader in, PrintWriter out, String id, String name) {
            this.in = in;
            this.out = out;
            this.id = id;
            this.name = name;
        }
    }
}
