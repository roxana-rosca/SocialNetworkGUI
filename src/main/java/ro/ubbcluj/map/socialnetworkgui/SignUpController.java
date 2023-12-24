package ro.ubbcluj.map.socialnetworkgui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.service.NetworkService;
import ro.ubbcluj.map.socialnetworkgui.utils.MessageAlert;

import java.io.IOException;

public class SignUpController {
    @FXML
    TextField firstNameTextField;
    @FXML
    TextField lastNameTextField;
    @FXML
    TextField usernameTextField;
    @FXML
    PasswordField passwordTextField;
    @FXML
    PasswordField confirmPasswordTextField;


    NetworkService networkService;

    public void setSignUpService(NetworkService networkService) {
        this.networkService = networkService;
    }

    private void initLogInStage() {
        try {
            FXMLLoader logInLoader = new FXMLLoader();

            logInLoader.setLocation(getClass().getResource("views/log-in-view.fxml"));

            AnchorPane logInLayout = logInLoader.load();

            Stage logInStage = new Stage();
            logInStage.setScene(new Scene(logInLayout));

            logInStage.setWidth(600);

            logInStage.setTitle("Social Network ~ Log in");
            logInStage.getIcons().add(new Image("C:\\Users\\roxan\\IdeaProjects\\map\\SocialNetworkGUI\\src\\main\\resources\\ro\\ubbcluj\\map\\socialnetworkgui\\images\\butterfly.png"));

            LogInController logInController = logInLoader.getController();
            logInController.setUserService(networkService);

            logInStage.show();

            // Close the current stage
            ((Stage) firstNameTextField.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogIn(ActionEvent event) {
        initLogInStage();
    }

    @FXML
    public void handleCreateAccount(ActionEvent event) {
        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        String confirmedPassword = confirmPasswordTextField.getText();

        try {
            networkService.addUserWithPassword(firstName, lastName, username, password, confirmedPassword);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Create an account", "Account created successfully!");
            initLogInStage();
        } catch (ValidationException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
            clearFields();
        }

    }

    private void clearFields() {
        firstNameTextField.clear();
        lastNameTextField.clear();
        usernameTextField.clear();
        passwordTextField.clear();
        confirmPasswordTextField.clear();
    }
}
