package de.hhu.propra.team61.Network;

/**
 * Implement this interface to allow Server and Client to trigger actions in the class.
 * The implementing class is usually responsible for handling user input.
 * Created by markus on 28.05.14.
 */
public interface Networkable {

    /**
     * the implementation must interpret and execute the command string, usually by calling a function resulting in an optical change
     * @param command space separated command with parameters
     */
    public void handleOnClient(String command);

    /**
     * the implementation must interpret the key event, usually by performing calculations and sending back
     * a command string (which is then interpreted by handleOnClient) to the clients
     * @param keyCode the key code to be interpreted
     */
    public void handleKeyEventOnServer(String keyCode);

}
