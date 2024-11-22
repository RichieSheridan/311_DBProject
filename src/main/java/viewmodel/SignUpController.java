package viewmodel;

import dao.DbConnectivityClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignUpController {
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();

    public void createNewAccount(ActionEvent actionEvent) {

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Username and password cannot be empty.");
            alert.showAndWait();
            return;
        }

        if (cnUtil.emailExists(username)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Username already exists. Please try another.");
            alert.showAndWait();
            return;
        }

        //Insert new user
        cnUtil.insertUserAccount(username, password);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Account created successfully.");
        alert.showAndWait();

        goBack(actionEvent);
    }

    public void goBack(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
