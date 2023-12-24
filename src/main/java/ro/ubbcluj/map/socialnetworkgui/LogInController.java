package ro.ubbcluj.map.socialnetworkgui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.service.NetworkService;
import ro.ubbcluj.map.socialnetworkgui.utils.MessageAlert;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class LogInController {
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordTextField;
    NetworkService networkService;

    public void setUserService(NetworkService networkService) {
        this.networkService = networkService;
    }

    @FXML
    public void initialize() {
//        keyGenerator = KeyGenerator.getInstance("AES");
//        keyGenerator.init(128);
//        secretKey = keyGenerator.generateKey();
    }


    @FXML
    public void handleLogIn(ActionEvent event) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        if(!username.isEmpty() && !password.isEmpty()){

            Optional<User> user = networkService.verifyIdentity(username, password);

            if(user.isPresent()){
                System.out.println("User-ul exista!");
                clearLogInDetails();
                showUserAccount(user.get());

                // Close the current stage
                ((Stage) usernameTextField.getScene().getWindow()).close();
//                ((Stage) usernameTextField.getScene().getWindow()).hide();
            }
            else{
                MessageAlert.showErrorMessage(null, "Wrong username/password!");
                clearLogInDetails();
            }
        }
        else{
            MessageAlert.showErrorMessage(null, "Wrong username/password!");
            clearLogInDetails();
            return;
        }

    }

    private void clearLogInDetails(){
        usernameTextField.clear();
        passwordTextField.clear();
    }

    /**
     * Deschide un nou Stage cu contul unui utilizator selectat din tabel.
     */
    public void showUserAccount(User user) {
        try {
            FXMLLoader accountLoader = new FXMLLoader();

            accountLoader.setLocation(getClass().getResource("views/user-page-view.fxml"));


            AnchorPane accountLayout = accountLoader.load();

            Stage accountStage = new Stage();
            accountStage.setScene(new Scene(accountLayout));

            accountStage.setWidth(600);
            String title = "Social Network ~ " + "@" + user.getUserName();
            accountStage.setTitle(title);
            accountStage.getIcons().add(new Image("C:\\Users\\roxan\\IdeaProjects\\map\\SocialNetworkGUI\\src\\main\\resources\\ro\\ubbcluj\\map\\socialnetworkgui\\images\\user.jpg"));

            UserPageController userPageController = accountLoader.getController();
            userPageController.setService(accountStage, user, networkService);

            accountStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSignUp(ActionEvent event){
        try{
            FXMLLoader signUpLoader = new FXMLLoader();

            signUpLoader.setLocation(getClass().getResource("views/sign-up-view.fxml"));

            AnchorPane signUpLayout = signUpLoader.load();

            Stage signUpStage = new Stage();
            signUpStage.setScene(new Scene(signUpLayout));

            signUpStage.setWidth(600);

            signUpStage.setTitle("Social Network ~ Sign up");
            signUpStage.getIcons().add(new Image("C:\\Users\\roxan\\IdeaProjects\\map\\SocialNetworkGUI\\src\\main\\resources\\ro\\ubbcluj\\map\\socialnetworkgui\\images\\butterfly.png"));

            SignUpController signUpController = signUpLoader.getController();
            signUpController.setSignUpService(networkService);

            signUpStage.show();

            // Close the current stage
            ((Stage) usernameTextField.getScene().getWindow()).close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
