package de.hhu.propra.team61.network;

/**
 * Implement this interface to allow game server and client to trigger actions in the class.
 * The implementing class is usually responsible for handling user input. See {@link de.hhu.propra.team61.network.Server}
 * and {@link de.hhu.propra.team61.network.Client} for the list of commands that can be passed to the implementing class.
 * @see de.hhu.propra.team61.network.Server
 * @see de.hhu.propra.team61.network.Client
 */
public interface Networkable {

    /**
     * The implementation must interpret and execute the command string, usually by calling a function resulting in an optical change.
     * @param command space separated command with parameters
     */
    public void handleOnClient(String command);

    /**
     * The implementation must interpret command string, usually by performing calculations and sending back a command string (which is then interpreted by handleOnClient) to the clients.
     * @param command space separated command with parameters to be interpreted, often a key event
     */
    public void handleOnServer(String command);

    /**
     * Returns a json string which represents the overall state of the implementing class.
     * Example: The map window returns the state of all teams including its figures, the terrain, wind, turn counter, etc.
     * @return a json string representing the current state of the class
     */
    public String getStateForNewClient();
}
