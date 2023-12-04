package ro.ubbcluj.map.socialnetworkgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.controller.UserController;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.FriendshipValidator;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.UserValidator;
import ro.ubbcluj.map.socialnetworkgui.repository.database.FriendshipDBRepository;
import ro.ubbcluj.map.socialnetworkgui.repository.database.MessageDBRepository;
import ro.ubbcluj.map.socialnetworkgui.repository.database.UserDBRepository;
import ro.ubbcluj.map.socialnetworkgui.service.FriendshipService;
import ro.ubbcluj.map.socialnetworkgui.service.NetworkService;
import ro.ubbcluj.map.socialnetworkgui.service.UserService;

import java.io.IOException;


public class StartApplication extends Application{

    UserDBRepository userDBRepository;
    FriendshipDBRepository friendshipDBRepository;
    MessageDBRepository messageDBRepository;
    UserService userService;
    FriendshipService friendshipService;
    NetworkService networkService;

    @Override
    public void start(Stage primaryStage) throws IOException {
        String url = "jdbc:postgresql://localhost:5432/SocialNetwork";
        String sqlUsername = "postgres";
        String sqlPassword = "postgres";

        userDBRepository = new UserDBRepository(url, sqlUsername, sqlPassword, new UserValidator());
        messageDBRepository = new MessageDBRepository(url, sqlUsername, sqlPassword);
        userService = new UserService(userDBRepository, messageDBRepository);

        friendshipDBRepository = new FriendshipDBRepository(url,sqlUsername, sqlPassword, new FriendshipValidator());
        friendshipService = new FriendshipService(friendshipDBRepository);

        networkService = new NetworkService(userService, friendshipService);

        initView(primaryStage);
        primaryStage.setWidth(600);
        primaryStage.setTitle("Social Network");
        primaryStage.getIcons().add(new Image("C:\\Users\\roxan\\IdeaProjects\\map\\SocialNetworkGUI\\src\\main\\resources\\ro\\ubbcluj\\map\\socialnetworkgui\\images\\butterfly.png"));
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }

    private void initView(Stage primaryStage) throws IOException{
        /*FXMLLoader userLoader = new FXMLLoader();
        userLoader.setLocation(getClass().getResource("views/user-view.fxml"));
        AnchorPane userLayout = userLoader.load();
        primaryStage.setScene(new Scene(userLayout));

        UserController userController = userLoader.getController();
        userController.setUserService(networkService);*/

        FXMLLoader networkLoader = new FXMLLoader();
        networkLoader.setLocation(getClass().getResource("views/network-view.fxml"));
        AnchorPane userLayout = networkLoader.load();
        primaryStage.setScene(new Scene(userLayout));

        NetworkController networkController = networkLoader.getController();
        networkController.setUserService(networkService);
    }
}
