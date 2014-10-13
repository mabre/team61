package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.artificialIntelligence.AIType;
import javafx.scene.control.ChoiceBox;

/**
 * Created by markus on 04.10.14. TODO IMPORTANT
 */
public class AIChoiceBox extends ChoiceBox<String> {

    public AIChoiceBox() {
        getItems().add("Human");
        getItems().add("Bot (Simple)");
        getSelectionModel().select(0);
    }

    public AIType getAIValue() {
        return AIType.fromInteger(getValue().equals("Human") ? 0 : 1);
    }

}
