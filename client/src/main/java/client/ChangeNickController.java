package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChangeNickController {

    @FXML
    public TextArea textArea;

    @FXML
    private TextField nicknameField;

    private Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @FXML
    public void tryToChange(ActionEvent actionEvent) {
        String nickname = nicknameField.getText().trim();
        controller.tryToChange(nickname);

    }

    public void addMessage(String msg) {
        textArea.appendText(msg + "\n");
    }
}
