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
    static final int PORT = 61421;

    private static ArrayList<ClientConnection> clients = new ArrayList<>();

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
        synchronized (clients) {
            for (int i=0; i< clients.size(); i++) {
                String message = "COMMAND " + command;
                clients.get(i).out.println(message);
                System.out.println("SERVER sent command to " + clients.get(i).id+"/"+ clients.get(i).name + ": " + message);
            }
        }
    }

    private static boolean clientIdExists(String id) {
        boolean found = false;
        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).id.equals(id)) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    private static boolean clientNameExists(String name) {
        boolean found = false;
        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).name.equals(name)) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    private static String getIdFromName(String name) {
        String id = "";
        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).name.equals(name)) {
                    id = clients.get(i).id;
                    break;
                }
            }
        }
        return id;
    }

    private static String getNameFromId(String id) {
        String name = "";
        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).id.equals(id)) {
                    name = clients.get(i).name;
                    break;
                }
            }
        }
        return name;
    }

    private static void disconnect(String name) {
        synchronized (clients) {
            try {
                for (int i = 0; i < clients.size(); i++) {
                    if (clients.get(i).name.equals(name)) {
                        clients.get(i).out.close();
                        clients.get(i).in.close();
                        System.out.println("SERVER: removed connection to " + clients.get(i).id + "/" + clients.get(i).name);
                        clients.remove(i);
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
        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                if(!clients.get(i).id.equals(Client.id)) { // TODO hardcoded spectator mode
                    spectatorsArray.put(clients.get(i).name);
                }
            }
        }
        return json.put("spectators", spectatorsArray).toString();
    }

    private static void renameByName(String oldName, String newName) {
        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).name.equals(oldName)) {
                    clients.get(i).name = newName;
                }
            }
        }
    }

    /**
     * changes the team number associated with a client
     * @param id the id associated with the client
     * @param newTeam the new team number of the client (counting starts from 0=host, -1 means spectator)
     */
    public void changeTeamById(String id, int newTeam) {
        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).id.equals(id)) {
                    clients.get(i).associatedTeam = newTeam;
                    clients.get(i).out.println("SET_TEAM_NUMBER " + newTeam);
                    System.out.println(clients.get(i).id + "/" + clients.get(i).name + " associated with team " + newTeam);
                    return;
                }
            }
            System.out.println("WARNING Did not find " + id);
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
        private String id;
        private String name;
        private int associatedTeam = -1;
        private boolean isReady = false;

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
                synchronized (clients) {
                    clients.add(this);
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
                    if(Client.id.equals(id)) { // we are host
                        associatedTeam = 0;
                    }
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
                        if (msg.startsWith("/kick ")) {
                            String userToKick = msg.split(" ", 2)[1];
                            if(clientNameExists(userToKick)) {
                                if(clientId.equals(Client.id) || clientId.equals(getIdFromName(userToKick))) {
                                    disconnect(userToKick);
                                    sendCommand("SERVER CHAT command executed. cmd: " + getNameFromId(clientId) + ": " + msg);
                                    sendCommand("SPECTATOR_LIST " + getSpectatorsAsJson());
                                } else {
                                    sendCommand("SERVER CHAT command failed: Operation not allowed. cmd: " + getNameFromId(clientId) + " " + msg);
                                }
                            } else {
                                sendCommand("SERVER CHAT command failed: No such user. cmd: " + getNameFromId(clientId) + " " + msg);
                            }
                        } else if (msg.startsWith("/kickteam ")) {
                            if(clientId.equals(Client.id)) { // TODO hardcoded spectator mode
                                currentNetworkable.handleKeyEventOnServer(msg);
                                sendCommand("SERVER CHAT command executed. cmd: " + getNameFromId(clientId) + ": " + msg);
                            } else {
                                sendCommand("SERVER CHAT command failed: Operation not allowed. cmd: " + getNameFromId(clientId) + " " + msg);
                            }
                        } else if (msg.startsWith("/rename ")) {
                            String names[] = msg.split(" ", 3);
                            if(names.length == 3) {
                                if(clientId.equals(Client.id) || clientId.equals(getIdFromName(names[1]))) {
                                    sendCommand("SERVER CHAT command executed. cmd: " + names[1] + ": " + msg);
                                    renameByName(names[1], names[2]);
                                    sendCommand("SPECTATOR_LIST " + getSpectatorsAsJson());
                                } else {
                                    sendCommand("SERVER CHAT command failed: Operation not allowed. cmd: " + getNameFromId(clientId) + " " + msg);
                                }
                            } else {
                                sendCommand("SERVER CHAT command executed. cmd: " + name + ": " + msg);
                                renameByName(name, names[1]);
                                sendCommand("SPECTATOR_LIST " + getSpectatorsAsJson());
                            }
                        } else {
                            sendCommand(getNameFromId(clientId) + " CHAT " + msg);
                        }
                    } else if (line.contains("GET_STATUS")) {
                        out.println(currentNetworkable.getStateForNewClient());
                    } else if (line.contains("CLIENT_READY")) {
                        isReady = true;
                        Platform.runLater(() -> currentNetworkable.handleKeyEventOnServer("READY " + associatedTeam));
                    } else if (line.contains("SPECTATOR ")) {
                        Platform.runLater(() -> currentNetworkable.handleKeyEventOnServer(line + " " + associatedTeam));
                    } else if (line.contains("KEYEVENT ")) {
                        Platform.runLater(() -> currentNetworkable.handleKeyEventOnServer(associatedTeam + " " + extractPart(line, "KEYEVENT ")));
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
            synchronized (clients) {
                if (clients != null) {
                    for (int i = 0; i < clients.size(); i++) {
                        if (clients.get(i).id.equals(id)) {
                            clients.remove(i);
                            break;
                        }
                    }
                }
                if (out != null) {
                    for (int i = 0; i < clients.size(); i++) {
                        if (clients.get(i).out == out) {
                            clients.remove(i);
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

        public void setName(String name) {
            this.name = name;
        }
    }

}
