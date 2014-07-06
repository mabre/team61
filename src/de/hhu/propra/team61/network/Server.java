package de.hhu.propra.team61.network;

import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

import static de.hhu.propra.team61.JavaFxUtils.extractPart;
import static de.hhu.propra.team61.JavaFxUtils.removeExtension;
import static de.hhu.propra.team61.JavaFxUtils.removeLastChar;

/**
 * Game Server which handles all events triggered on the client.
 * <p>
 * Only one server should run at the same time. To create a new thread running a game server, use
 * {@code serverThread = new Thread(server = new Server(() -> {callbackFunkction};} and start the thread using
 * {@code serverThread.start();}. When the server is ready to accept connection, the given callback function is callced.
 * After creating a new server object, {@link #registerCurrentNetworkable(Networkable)} should be called with
 * {@code this} being the argument. Call {@link #stop()} to shut down the server thread properly.
 * </p>
 * The following commands are sent by the server and should be handled appropriately on the client:
 * <ul>
 * <li>{@code ACTIVATE_FIGURE $int_currentTeam}<br>
 * Activate the current figure of the given teem, and scrolls to it (if not disabled).<br>
 * see {@link de.hhu.propra.team61.objects.Figure#setActive(boolean)}
 * <li>{@code $String_clientName CHAT $String_message}<br>
 * Display the given message from the given client.<br>
 * see {@link de.hhu.propra.team61.gui.Chat#processChatCommand(String)}
 * <li>{@code CONDITION POISON $int_team $int_figure}<br>
 * Poisons the given figure of the given team.<br>
 * see {@link de.hhu.propra.team61.objects.Figure#setIsPoisoned(boolean)}
 * <li>{@code CONDITION FIRE $int_team $int_figure}<br>
 * Burns the given figure of the given team.<br>
 * see {@link de.hhu.propra.team61.objects.Figure#setIsBurning(boolean)} // TODO changed?
 * <li>{@code CONDITION STUCK $int_team $int_figure}<br>
 * Stucks the given figure of the given team.<br>
 * see {@link de.hhu.propra.team61.objects.Figure#setIsStuck(boolean)} // TODO do we need this?
 * <li>{@code CURRENT_FIGURE_ANGLE_DOWN}<br>
 * Move the crosshair of the current figure down.<br>
 * see {@link de.hhu.propra.team61.objects.Item#angleDown(boolean)} // TODO include figure id
 * <li>{@code CURRENT_FIGURE_ANGLE_UP}<br>
 * Move the crosshair of the current figure up.<br>
 * see {@link de.hhu.propra.team61.objects.Item#angleUp(boolean)}
 * <li>{@code CURRENT_FIGURE_CHOOSE_WEAPON $int_index}<br>
 * Make the currently active figure choose the given weapon.<br>
 * see {@link de.hhu.propra.team61.objects.Figure#setSelectedItem(de.hhu.propra.team61.objects.Item)} // TODO changed?
 * <li>{@code CURRENT_FIGURE_FACE_LEFT}<br>
 * Make the currently active figure look to the left.<br>
 * see {@link de.hhu.propra.team61.objects.Figure#setFacingRight(boolean)}
 * <li>{@code CURRENT_FIGURE_FACE_RIGHT}<br>
 * Make the currently active figure look to the right.<br>
 * see {@link de.hhu.propra.team61.objects.Figure#setFacingRight(boolean)} // TODO merge these commands
 * <li>{@code CURRENT_FIGURE_SHOOT}<br>
 * Makes the currently active figure shoot, and scrolls to the created projectile.<br>
 * see {@link de.hhu.propra.team61.objects.Figure#shoot()}
 * <li>{@code CURRENT_TEAM_END_ROUND $int_currentTeam} (*)<br>
 * see {@link de.hhu.propra.team61.Team#endRound()} // TODO rename
 * <li>{@code DEACTIVATE_FIGURE $int_currentTeam}<br>
 * Deactivates the currently active figure and allows shooting again.<br>
 * see {@link de.hhu.propra.team61.objects.Figure#setActive(boolean)}
 * <li>{@code DEGITATE $int_team $int_figure} (*)<br>
 * Degitates the given figure of the given team.<br>
 * see {@link de.hhu.propra.team61.objects.Figure#degitate()}
 * <li>{@code DIGITATE $int_team $int_figure} (*)<br>
 * Digitates the given figure of the given team.<br>
 * see {@link de.hhu.propra.team61.objects.Figure#digitate()}
 * <li>{@code FIGURE_SET_POSITION $int_team $int_figure $double_x $double_y [$boolean_scroll=false]}<br>
 * Move the given figure of the given team to the given position.<br>
 * see {@link de.hhu.propra.team61.objects.Figure#setPosition(javafx.geometry.Point2D)}
 * <li>{@code GAME_OVER $int_team}<br>
 * Shows the game over window with the given team being the winner.<br>
 * see {@link de.hhu.propra.team61.gui.GameOverWindow}
 * <li>{@code LOBBY_CHANGE}<br>
 * <li>{@code NAMEACCEPTED} (**)<br>
 * Sent after successful negotiation of id and name.<br>
 * see {@link de.hhu.propra.team61.network.Server.ClientConnection#getName()}
 * <li>{@code REPLACE_BLOCK $int_column $int_row $char_terrainType} (*)<br>
 * Replace the terrain block at the given position with the given type, note that ' ' is encoded as '#'.<br>
 * see {@link de.hhu.propra.team61.objects.Terrain#replaceBlock(int, int, char)}
 * <li>{@code PAUSE $boolean_paused}<br>
 * Pauses the game.<br>
 * see {@link de.hhu.propra.team61.MapWindow#pause}
 * <li>{@code PROJECTILE_SET_POSITION $double_x $double_y} (*)<br>
 * Sets the position of the current projectile to the given position.<br>
 * see {@link de.hhu.propra.team61.objects.Projectile#setPosition(javafx.geometry.Point2D)} // TODO changed?
 * <li>{@code SD BOSS MOVE} (*)<br>
 * Moves the boss.<br>
 * see {@link de.hhu.propra.team61.MapWindow#moveBoss()}
 * <li>{@code SD BOSS SPAWN $String_name $boolean_spawnedlLeft} (*)<br>
 * Spawns the boss with the given name at the given side.<br>
 * see {@link de.hhu.propra.team61.MapWindow#initBoss(String)}
 * <li>{@code SET_CURRRENT_TEAM $int_currentTeam}<br>
 * Sets the current team to the given team, and disabled the figure of the previously active team, // TODO why?
 * see {@link de.hhu.propra.team61.MapWindow#currentTeam}
 * <li>{@code SET_HP $int_team $int_figure $int_hp} (*)<br>
 * Sets the hp of the given figure of the given team to the given values.<br>
 * see {@link de.hhu.propra.team61.objects.Figure#setHealth(int)}
 * <li>{@code SET_TEAM_NUMBER $int_team} (**)<br>
 * Sets the team number for the client.<br>
 * see {@link #changeTeamById(String, int)}, {@link #changeTeamByNumber(int, int)}
 * <li>{@code SET_TURN_COUNT $int_turnCount}<br>
 * Sets turn counter to the given value.<br>
 * see {@link de.hhu.propra.team61.MapWindow#turnCount} // TODO to we need this?
 * <li>{@code SPECTATOR_LIST $JSONObject_clientList}<br>
 * Contains a list with the connected clients.<br>
 * see {@link #getClientListAsJson()} // TODO rename
 * <li>{@code STATUS LOBBY $JSONObject_netLobby}<br>
 * Contains the overall state of the lobby.<br>
 * see {@link de.hhu.propra.team61.gui.NetLobby#getStateForNewClient()}
 * <li>{@code STATUS MAPWINDOW $JSONObject_mapWindow}<br>
 * Contains the overall state of the map window, display map window if it is not shown.<br>
 * see {@link de.hhu.propra.team61.MapWindow#getStateForNewClient()}
 * <li>{@code SUBMITNAME}<br>
 * Requests the client to submit a unique id and a name.<br>
 * see {@link de.hhu.propra.team61.network.Server.ClientConnection#getName()}
 * <li>{@code SUDDEN_DEATH $int_team}<br>
 * Kills the given team.<br>
 * see {@link de.hhu.propra.team61.Team#suddenDeath()}
 * <li>{@code TEAM_LABEL_SET_TEXT $String_text}<br>
 * Sets the text of the team label.<br>
 * see {@link de.hhu.propra.team61.MapWindow#teamLabel}
 * <li>{@code WIND_FORCE $double_windMagnitude}<br>
 * Sets the displayed wind force to the given value.<br>
 * see {@link de.hhu.propra.team61.gui.WindIndicator#setWindForce(double)}
 * </ul>
 * (*) These commands have effects on calculations done on the server and are therefore already executed on the server
 * before the command is sent. Consequently, the client running on the host must NOT interpret these commands again.<br>
 * (**) These commands are always sent to only one client, namely the one which sent the command mentioned in the
 * description.
 * <p>
 * The server understands the following commands:
 * <ul>
 * <li>{@code $String_id $String_name}<br>
 * id and name of the client, sent after {@code SUBMITNAME}.<br>
 * see {@link de.hhu.propra.team61.network.Server.ClientConnection#getName()}
 * <li>{@code CLIENT_READY $JSONObject_netLobby}<br>
 * Sets client ready status to {@code true} and applies the parts of the given lobby status the client is allowed to change.<br>
 * see {@link de.hhu.propra.team61.network.Server.ClientConnection#isReady}
 * <li>{@code CHAT $String_message}<br>
 * Sends the chat message after prepending the client's name to all clients.
 * <li>{@code GET_STATUS}<br>
 * Sends the status of the current view to the client requesting it.<br>
 * see {@link Networkable#getStateForNewClient()}
 * <li>{@code KEYEVENT $String_keyCode}<br>
 * Contains a key which was pressed on the client and must be handled on server.
 * <li>{@code SPECTATOR CHECKED}<br>
 * Makes the client a spectator.<br>
 * see {@link de.hhu.propra.team61.gui.NetLobby#spectatorBoxChanged(boolean)}
 * <li>{@code SPECTATOR UNCHECKED}<br>
 * Makes the client a player, and finds a team for it.<br>
 * see {@link de.hhu.propra.team61.gui.NetLobby#spectatorBoxChanged(boolean)}
 * <li>{@code STATUS LOBBY $JSONObject_netLobby}<br>
 * Applies the parts of the given lobby status the client is allowed to change.<br>
 * see {@link de.hhu.propra.team61.gui.NetLobby#applySettingsFromClient(de.hhu.propra.team61.io.json.JSONObject, int)}
 * </ul>
 */
public class Server implements Runnable {
    /** port on which the server runs */
    static final int PORT = 61421;

    /** list of connected clients */
    private static ArrayList<ClientConnection> clients = new ArrayList<>();

    /** method being called when the server is up and running */
    private Runnable readyListener; // TODO 0.2 can we make everything static and forget about the server objects? (will need another condition for checking who is host, maybe a static boolean here)

    /** listens for new client connections */
    private ServerSocket listener;
    /** the currently shown view, which can handle received commands */
    private static Networkable currentNetworkable;

    /**
     * Creates a new server. Only one server should be running at the same time. Does not start listening for new connections, see {@link #run()}
     * @param listener function which is called when server is set up, thus ready to accept connections
     */
    public Server(Runnable listener) {
        this.readyListener = listener;
    }

    /**
     * Starts the server thread and listens for new client connections.
     */
    @Override
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
            System.err.println("ERROR " + e.getLocalizedMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("SERVER shut down");
    }

    /**
     * Shuts down the server by stopping listening for new connections.
     */
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

    /**
     * Sets {@link #currentNetworkable}.
     * The given object should have a sensible implementation of {@link de.hhu.propra.team61.network.Networkable#handleOnServer(String)},
     * ie. must understand commands relevant to the view.
     * @param networkable the view which will receive processed and filtered server commands
     * @see de.hhu.propra.team61.network.Server.ClientConnection#broadcast()
     */
    public void registerCurrentNetworkable(Networkable networkable) {
        this.currentNetworkable = networkable;
    }

    /**
     * Gets a comma separated list of IPv4 addresses of the network interface of this computer.
     * Loopback addresses (127.*) are not returned. If no address can be found, "No IPs found." is returned. Note that
     * it is not possible to get the "public" ip address (only the router knows it).
     * @return IPs for this machine
     */
    public static String getIps() {
        String ips = "";
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface)e.nextElement();
                Enumeration addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    String address = ((InetAddress)addresses.nextElement()).getHostAddress();
                    if(!address.contains(":") && !address.startsWith("127.")) { // do not return IPv6 or loopback addresses
                        ips += address + ", ";
                        System.err.println("2");
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if(ips.length() < 2) {
            return "No IPs found.";
        } else {
            return removeExtension(ips, 2);
        }
    }

    /**
     * Sends the given command to all connected clients.
     * @param command the command to be sent
     */
    public static void send(String command) {
        synchronized (clients) {
            for (ClientConnection client : clients) {
                client.out.println(command);
                System.out.println("SERVER sent command to " + client.id + "/" + client.name + ": " + command);
            }
        }
    }

    /**
     * Sends the given commands to all connected clients.
     * @param commands the commands to be sent
     * @see #send(String)
     */
    public static void  sendCommands(ArrayList<String> commands) {
        for (String command : commands) {
            send(command);
        }
    }

    /**
     * Checks if a client with the given id exists.
     * @param id the id to be checked
     * @return true if a client with the given id exists
     */
    private static boolean clientIdExists(String id) {
        synchronized (clients) {
            for (ClientConnection client: clients) {
                if (client.id.equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a client with the given name exists.
     * @param name name to be checked
     * @return true if a client with the given name exists
     */
    private static boolean clientNameExists(String name) {
        synchronized (clients) {
            for (ClientConnection client : clients) {
                if (client.name.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the id of the client with the given name.
     * @param name the name of the client whose id is sought
     * @return the id of the client with the given name, "" when no matching client is found
     */
    private static String getIdFromName(String name) {
        synchronized (clients) {
            for (ClientConnection client : clients) {
                if (client.name.equals(name)) {
                    return client.id;
                }
            }
        }
        return "";
    }

    /**
     * Gets the name of the client with the given id.
     * @param id the id of the client whose name is sought
     * @return the name of the client with the given id, "" when no matching client is found
     */
    private static String getNameFromId(String id) {
        synchronized (clients) {
            for (ClientConnection client : clients) {
                if (client.id.equals(id)) {
                    return client.name;
                }
            }
        }
        return "";
    }

    /**
     * Disconnects the client with the given name by closing its writer and reader.
     * @param name the name of the client which shall be disconnected
     */
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

    /**
     * Get a list of connected client with their name and team as json.
     * TODO doc example output
     * @return a JSONObject representing the connected clients, including associated team
     */
    private static String getClientListAsJson() {
        JSONObject json = new JSONObject();
        JSONArray spectatorsArray = new JSONArray();
        synchronized (clients) {
            for (ClientConnection client: clients) {
                JSONObject player = new JSONObject();
                player.put("name", client.name);
                player.put("team", client.getAssociatedTeam());
                spectatorsArray.put(player);
            }
        }
        return json.put("spectators", spectatorsArray).toString(); // TODO spectators still appropriate?
    }

    /**
     * Changes the name of a client.
     * @param oldName the name of the client whose name should be changed
     * @param newName the new name for the client
     */
    private static void renameByName(String oldName, String newName) {
        synchronized (clients) {
            for (ClientConnection client: clients) {
                if (client.name.equals(oldName)) {
                    client.name = newName;
                    return;
                }
            }
        }
    }

    /**
     * Checks if all clients which have a team are ready.
     * @return true when all teams are ready
     */
    public static boolean teamsAreReady() {
        for (ClientConnection client: clients) {
            if (!client.isReady && client.associatedTeam > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Changes the team number associated with a client; informs all clients about the change by sending the current list of connected clients.
     * @param id the id associated with the client
     * @param newTeam the new team number of the client (counting starts from 0=host, -1 means spectator)
     */
    public void changeTeamById(String id, int newTeam) {
        synchronized (clients) {
            for (ClientConnection client: clients) {
                if (client.id.equals(id)) {
                    client.associatedTeam = newTeam;
                    client.out.println("SET_TEAM_NUMBER " + newTeam);
                    System.out.println(client.id + "/" + client.name + " associated with team " + newTeam);
                    send("SPECTATOR_LIST " + getClientListAsJson());
                    return;
                }
            }
            System.err.println("WARNING Did not find " + id);
        }
    }

    /**
     * Changes the team number associated with a client; informs all clients about the change by sending the current list of connected clients.
     * @param team the number of the team, counting starts from 0=host
     * @param newTeam the new team number of the client (counting starts from 0=host, -1 means spectator)
     */
    public void changeTeamByNumber(int team, int newTeam) {
        if(team < 1) {
            System.err.println("ERROR: team " + team + " cannot be changed.");
            return;
        }
        synchronized (clients) {
            for (ClientConnection client: clients) {
                if (client.associatedTeam == team) {
                    client.associatedTeam = newTeam;
                    client.out.println("SET_TEAM_NUMBER " + newTeam);
                    System.out.println(client.id + "/" + client.name + " associated with team " + newTeam);
                    send("SPECTATOR_LIST " + getClientListAsJson());
                    return;
                }
            }
            System.err.println("WARNING Did not find " + team);
        }
    }

    /**
     * Sets up a client connection.
     */
    private static class ConnectionHandler implements Runnable {
        private Socket socket;

        /**
         * @param socket connection socket
         */
        public ConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Establishes connection with client by creating new {@link de.hhu.propra.team61.network.Server.ClientConnection}.
         */
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


    /**
     * This class represents a connected client and holds the input/output reader/writers.
     * Each connection has a unique id and player name. Each player (unless it is spectator) is associated with a team.
     */
    private static class ClientConnection implements AutoCloseable {
        /** reader which receives the messages of the associated client */
        private final BufferedReader in;
        /** writer which allows sending messages to the associated client */
        private final PrintWriter out;
        /** the socket of the connection */
        private final Socket socket;
        /** a random and unique id for identifying this client */
        private String id;
        /** the name of the player sitting in front of the client */
        private String name;
        /** the team associated with this client, counting starts from 0=host, -1=spectator */
        private int associatedTeam = -1;
        /** indicates whether the client clicked on "ready" in the lobby */
        private boolean isReady = false;

        /**
         * Creates a new client connection by setting up the reader and writer using the given socket.
         * @param socket the connection socket which provides reader and writer
         * @throws IOException
         */
        public ClientConnection(Socket socket) throws IOException {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        /**
         * Asks the client for id and name (blocks if id (TODO name) is already used by another client), adds the client
         *  to the list of connected clients, sends the list of connected players, and starts listening for messages from the client
         * @throws IOException
         */
        public void connect() throws IOException {
            getName();
            out.println("NAMEACCEPTED");
            System.out.println("SERVER: connection accepted");
            if(name != null) {
                synchronized (clients) {
                    clients.add(this);
                }
                send("SPECTATOR_LIST " + getClientListAsJson());
                broadcast();
            }
        }

        /**
         * asks the client for id and name, blocks till an id which is not already used is given
         * @throws IOException
         */
        private void getName() throws IOException {
            while(true) {
                out.println("SUBMITNAME");
                System.out.println("SERVER: asked for id and name");
                String identifier = in.readLine();
                if(identifier == null) {
                    System.err.println("SERVER: read null identifier");
                    return;
                }
                if(!clientIdExists(id)) { // TODO ok, here seems to be sth wrong ...
                    id = identifier.split(" ", 2)[0];
                    name = identifier.split(" ", 2)[1];
                    if(Client.id.equals(id)) { // we are host
                        associatedTeam = 0;
                        isReady = true;
                    }
                    break;
                }
            }
        }

        /**
         * Processes messages received from the client, and passes commands to {Networkable#handleOnServer} of {#currentNetworkable}, if necessary.
         * Unknown commands are discarded.
         * @throws IOException
         */
        private void broadcast() throws IOException {
            try {
                while(true) {
                    String line = in.readLine();
                    if (line == null) {
                        return;
                    }
                    System.out.println("SERVER RECEIVED message from " + id + ": " + line);
                    if (line.contains("CHAT ")) {
                        processChatMessage(line.split(" ", 2)[1]);
                    } else if (line.startsWith("GET_STATUS")) {
                        out.println(currentNetworkable.getStateForNewClient());
                    } else if (line.startsWith("CLIENT_READY")) {
                        isReady = true;
                        Platform.runLater(() -> currentNetworkable.handleOnServer("READY " + associatedTeam + " " + extractPart(line, "CLIENT_READY ")));
                    } else if (line.startsWith("SPECTATOR ")) {
                        Platform.runLater(() -> currentNetworkable.handleOnServer(id + " " + line + " " + associatedTeam));
                    } else if (line.startsWith("KEYEVENT ")) {
                        Platform.runLater(() -> currentNetworkable.handleOnServer(associatedTeam + " " + extractPart(line, "KEYEVENT ")));
                    } else if (line.startsWith("LOBBY_CHANGE ")) {
                        Platform.runLater(() -> currentNetworkable.handleOnServer("LOBBY_CHANGE " + associatedTeam + " " + extractPart(line, "LOBBY_CHANGE ")));
                    } else if (line.startsWith("STATUS ")) {
                        System.err.println("WARNING: COMMAND NO LONGER SUPPORTED!"); // TODO remove
                    } else {
                        System.err.println("SERVER: unhandled message: " + line);
                    }
                }
            } catch(SocketException e) {
                System.err.println("SERVER: SocketException at " + id+"/"+name + " " + e.getMessage());
            } catch(IOException e) {
                System.err.println("SERVER: IOException at " + id+"/"+name + " " + e.getMessage());
            }
        }

        /**
         * Processes a chat message.
         * Checks if the message is a cheat command and executes it. If it is a normal message, it is passed to
         * {@link #currentNetworkable}.
         * @param msg the message to be processed
         */
        private void processChatMessage(String msg) {
            if (msg.startsWith("/kick ")) {
                kickUserByChat(msg.split(" ", 1)[0], msg);
            } else if (msg.startsWith("/kickteam ")) {
                kickTeamByChat(msg);
            } else if (msg.startsWith("/rename ")) {
                String names[] = extractPart(msg, "/rename ").split(" "); // TODO renaming players with spaces?
                if(names.length == 1) { // if only one name given -> rename current user
                    renameByChat(name, names[0], msg);
                } else if(names.length == 2) {
                    renameByChat(names[0], names[1], msg);
                } else {
                    System.err.println("renaming users with spaces in name not supported, considering whole string as new name");
                    renameByChat(name, extractPart(msg, "/rename "), msg);
                }
            // cheat codes are enclosed in double low-9 quotation mark [AltGr-v on Linux] and right double quotation mark [AltGr-n]
            // (typographic German opening quotation mark 99 / typographic English closing quotation mark 99 - no-one will type this accidentally)
            } else if (msg.startsWith("/„") && msg.endsWith("”")) {
                currentNetworkable.handleOnServer("CHEAT " + removeLastChar(extractPart(msg, "/„")));
            } else {
                send(getNameFromId(id) + " CHAT " + msg);
            }
        }

        /**
         * Changes the name of the given user.
         * A user can only be renamed by the user themselves or by the host. The original chat command is sent to all
         * clients to inform them of the execution of this command.
         * @param oldName name of the user which is renamed
         * @param newName new name of the user
         * @param msg the original chat message which contains the rename command
         */
        private void renameByChat(String oldName, String newName, String msg) {
            if (id.equals(Client.id) || id.equals(getIdFromName(oldName))) {
                send("SERVER CHAT command executed. cmd: " + oldName + ": " + msg);
                renameByName(oldName, newName);
                send("SPECTATOR_LIST " + getClientListAsJson());
            } else {
                send("SERVER CHAT command failed: Operation not allowed. cmd: " + getNameFromId(id) + " " + msg);
            }
        }

        /**
         * Instructs {@link #currentNetworkable} to kick the team mentioned in the message.
         * "/kickteam " followed by the team number is passed. Only the host can kick a team. The original chat command
         * is sent to all clients to inform them of the execution of this command.
         * @param msg the original chat message which contains the kick command
         */
        private void kickTeamByChat(String msg) {
            if(id.equals(Client.id)) { // TODO team cannot surrender
                currentNetworkable.handleOnServer(msg);
                send("SERVER CHAT command executed. cmd: " + getNameFromId(id) + ": " + msg);
            } else {
                send("SERVER CHAT command failed: Operation not allowed. cmd: " + getNameFromId(id) + " " + msg);
            }
        }

        /**
         * Kicks the given user by disconnecting it.
         * A user can only be kicked by the user themselves or by the host. The associated team is not removed from the
         * current game, use {@link #kickTeamByChat}. The original chat command is sent to all clients to inform them of
         * the execution of this command.
         * @param userToKick the id or name of the client which shall be kicked
         * @param msg the original chat message which contains the kickteam command
         */
        private void kickUserByChat(String userToKick, String msg) {
            if(clientNameExists(userToKick)) {
                // host can kick every client, and the client itself can kick itself
                if(id.equals(Client.id) || id.equals(getIdFromName(userToKick))) { // first condition is true when client running on host sent this command // TODO condidtion to function
                    disconnect(userToKick);
                    send("SERVER CHAT command executed. cmd: " + getNameFromId(id) + ": " + msg);
                    send("SPECTATOR_LIST " + getClientListAsJson());
                } else {
                    send("SERVER CHAT command failed: Operation not allowed. cmd: " + getNameFromId(id) + " " + msg);
                }
            } else {
                send("SERVER CHAT command failed: No such user. cmd: " + getNameFromId(id) + " " + msg);
            }
        }


        /**
         * Removes this connection from the list of {@link #clients} list.
         * @throws Exception
         */
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
                            clients.remove(i); // TODO huh?
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

        /**
         * Sets the name of the player associated with this connection.
         * @param name the name of the player
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the associated team as formatted string.
         *  (eg. "Spectator" instead of -1)
         * @return associated team as formatted string
         */
        public String getAssociatedTeam() {
            if(associatedTeam == -1) return "Spectator";
            if(associatedTeam == 0) return "Team 1 (Host)";
            return "Team " + (associatedTeam+1);
        }
    }

}
