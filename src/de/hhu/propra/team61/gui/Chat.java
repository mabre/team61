package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.network.Client;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

/**
 * A class for a chat box.
 */
public class Chat extends VBox {

    /** line for entering new message */
    private TextField input;
    /** area displaying sent and received messages */
    private TextArea output;

    /** client object used for sending messages */
    private Client client;

    /** if true, the chat is transparent when unfocused */
    private boolean unobtrusive = false;

    /** helper variable for removing character entered to open the chat */
    private boolean recentlyOpened = false;

    private static final double OPACITY_UNOBTRUSIVE = .4;
    private static final double OPACITY_UNOBTRUSIVE_HOVER = .8;

    /**
     * Creates a new chat box.
     * @param client the client used to send messages
     */
    public Chat(Client client) {
        this.setId("chatBox");

        this.client = client;

        output = new TextArea("SECURITY ADVICE: No unicorn will ask for your password in this chat.\n--\n");
        output.setEditable(false);
        output.setPrefSize(300, 200);
        output.setMinHeight(200);
        output.setWrapText(true);
        output.setOnKeyPressed((keyEvent) -> {
            if(keyEvent.getCode() == KeyCode.C) {
                setVisible(false);
            }
        });
        this.getChildren().add(output);

        input = new TextField("/help");
        input.setOnAction((e) -> {
            if(input.getText().startsWith(("/help"))) {
                printHelp();
            } else if(input.getText().equals(("c")) && unobtrusive) {
                this.setVisible(false);
            } else if(!input.getText().equals((""))) {
                client.sendChatMessage(input.getText());
            }
            input.clear();
        });
        input.textProperty().addListener((observedValue, oldValue, newValue) -> {
            if (recentlyOpened) {
                if (input.getText().equals("c")) { // remove the c entered to open chat
                    input.clear();
                } else {
                    input.selectAll();
                }
                recentlyOpened = false;
            }
        });
        this.getChildren().add(input);

        this.visibleProperty().addListener((observedValue, oldValue, newValue) -> {
            if (newValue) {
                recentlyOpened = true;
                input.requestFocus();
            }
        });

        setOnMouseEntered((e) -> {
            this.setOpacity(unobtrusive ? OPACITY_UNOBTRUSIVE_HOVER : 1);
        });
        setOnMouseExited((e) -> {
            this.setOpacity(unobtrusive ? OPACITY_UNOBTRUSIVE : 1);
        });
    }

    /**
     * Appends a message to the output area.
     * @param name the name of the user who sent the message
     * @param msg the message to be displayed
     */
    public void appendMessage(String name, String msg) {
        output.appendText(name + "ː " + msg + "\n");
        if(!isVisible()) {
            setVisible(true);
            setOpacity(OPACITY_UNOBTRUSIVE);
        }
    }

    /**
     * Interprets a chat command
     * @param command the chat command, format "CLIENTNAME MESSAGE"
     */
    public void processChatCommand(String command) {
        String name = command.split(" CHAT ", 2)[0];
        String msg = command.split(" CHAT ", 2)[1];
        appendMessage(name, msg);
    }

    /**
     * Prints available chat commands to output area.
     */
    private void printHelp() {
        appendMessage("ːCHATHELP", "Type in your message and press enter. When you see your own message, delivery was successful.\n" +
                "\n" +
                "Available commands:\n" +
                "/help\n" +
                "    Shows this help\n" +
                "/kick <user name>\n" +
                "    Disconnects the given user\n" +
                "    Only the user themselves and the host are allowed to kick a user.\n" +
//                "/kickteam <team name>\n" + // TODO
                "/kickteam <team number>\n" +
                "    Removes the given team (sudden death)\n" +
                "    Only a member of that team and the host are allowed to kick a team.\n" +
                "/rename <new name>\n" +
                "    Changes your name\n" +
                "/rename <old name> <new name>\n" +
                "    Changes a name\n" +
                "    Only the user themselves and the host are allowed to rename a user.\n" +
                "c\n" +
                "    Toggle chat (in game)\n");
    }

    /**
     * Sets {@link #unobtrusive}, and updates opacity if necessary.
     * @param unobtrusive if true, the chat becomes transparent when unfocused.
     */
    public void setUnobtrusive(boolean unobtrusive) {
        this.unobtrusive = unobtrusive;
        this.setOpacity(unobtrusive ? OPACITY_UNOBTRUSIVE : 1);
    }

}
