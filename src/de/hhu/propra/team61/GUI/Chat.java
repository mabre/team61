package de.hhu.propra.team61.GUI;

import de.hhu.propra.team61.Network.Client;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Created by markus on 31.05.14.
 */
public class Chat extends VBox {

    TextField input;
    TextArea output;

    Client client;

    public Chat(Client client) {
        this.setId("chatBox");

        this.client = client;

        output = new TextArea("SECURITY ADVICE: No unicorn will ask for your password in this chat.\n--\n");
        output.setEditable(false);
        output.setPrefSize(300, 200);
        output.setMinHeight(200);
        output.setWrapText(true);
        this.getChildren().add(output);

        input = new TextField("/help");
        input.setOnAction((e) -> {
            if(input.getText().startsWith(("/help"))) {
                printHelp();
            } else {
                client.sendChatMessage(input.getText());
            }
            input.clear();
        });
        this.getChildren().add(input);
    }

    public void appendMessage(String name, String msg) {
        output.appendText(name + "ː " + msg + "\n");
    }

    private void printHelp() {
        appendMessage("ːCHATHELP", "Type in your message and press enter. When you see your own message, delivery was successful.\n" +
                "\n" +
                "Available commands:\n" +
                "/help\n" +
                "    Shows this help\n" +
                "/kick <team name>\n" +
                "/kick <team number>\n" +
                "    Removes the given team (sudden death)\n" +
                "    Only a member of that team and the host are allowed to kick a team.");
    }

}
