package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.network.Client;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

/**
 * Created by markus on 31.05.14.
 */
public class Chat extends VBox {

    TextField input;
    TextArea output;

    Client client;

    boolean unobtrusive = false;

    private static final double OPACITY_UNOBTRUSIVE = .8;
    private static final double OPACITY_UNOBTRUSIVE_HOVER = .3;

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
        input.focusedProperty().addListener((observedValue, oldValue, newValue) -> {
            if (newValue) {
                if (input.getText().equals("c")) input.clear(); // TODO not working as expected
                input.selectAll();
            }
        });
        this.getChildren().add(input);

        this.visibleProperty().addListener((observedValue, oldValue, newValue) -> {
            if(newValue) {
                input.requestFocus();
                if(input.getText().equals("c")) input.clear(); // TODO not working as expected
                input.selectAll();
            }
        });

        setOnMouseEntered((e) -> {
            this.setOpacity(unobtrusive ? OPACITY_UNOBTRUSIVE_HOVER : 1);
        });
        setOnMouseExited((e) -> {
            this.setOpacity(unobtrusive ? OPACITY_UNOBTRUSIVE : 1);
        });
    }

    public void appendMessage(String name, String msg) {
        output.appendText(name + "ː " + msg + "\n");
        if(!isVisible()) {
            setVisible(true);
            setOpacity(OPACITY_UNOBTRUSIVE_HOVER);
        }
    }

    public void processChatCommand(String command) {
        String name = command.split(" CHAT ", 2)[0];
        String msg = command.split(" CHAT ", 2)[1];
        appendMessage(name, msg);
    }

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
                "/kick <new name>\n" +
                "    Changes your name\n" +
                "/rename <old name> <new name>\n" +
                "    Changes a name\n" +
                "    Only the user themselves and the host are allowed to rename a user.\n" +
                "c\n" +
                "    Toggle chat (in game)\n");
    }

    public void setUnobtrusive(boolean unobtrusive) {
        this.unobtrusive = unobtrusive;
        this.setOpacity(unobtrusive ? OPACITY_UNOBTRUSIVE : 1);
    }

}
