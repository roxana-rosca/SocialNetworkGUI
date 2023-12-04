package ro.ubbcluj.map.socialnetworkgui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.service.NetworkService;
import ro.ubbcluj.map.socialnetworkgui.utils.MessageAlert;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UserChangeEvent;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class NetworkController implements Observer<UserChangeEvent> {
    NetworkService networkService;

    // context menu
    private ContextMenu contextMenuUserAccount = new ContextMenu();
    private MenuItem openUserAccountMenuItem;

    // tab all users
    ObservableList<User> userModel = FXCollections.observableArrayList();
    @FXML
    TableView<User> userTableView;
    @FXML
    TableColumn<User, String> tableColumnFirstName;
    @FXML
    TableColumn<User, String> tableColumnLastName;
    @FXML
    TableColumn<User, String> tableColumnUsername;
    @FXML
    TableColumn<User, String> tableColumnNumberOfFriends;

    @FXML
    private TextField textFieldFirstName;
    @FXML
    private TextField textFieldLastName;
    @FXML
    private TextField textFieldUsername;

    /**
     * Seteaza informatiile generale legate de service, observer etc.
     */
    public void setUserService(NetworkService networkService) {
        this.networkService = networkService;
        networkService.addObserver(this);
        initModel();
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
            String title = "@" + user.getUserName();
            accountStage.setTitle(title);
            accountStage.getIcons().add(new Image("C:\\Users\\roxan\\IdeaProjects\\map\\SocialNetworkGUI\\src\\main\\resources\\ro\\ubbcluj\\map\\socialnetworkgui\\images\\user.jpg"));

            UserPageController userPageController = accountLoader.getController();
            userPageController.setService(accountStage, user, networkService);

            accountStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Functia principala de initializare.
     */
    @FXML
    public void initialize() {
        tableColumnUsername.setCellValueFactory(new PropertyValueFactory<User, String>("userName"));
        tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        tableColumnLastName.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));
        tableColumnNumberOfFriends.setCellValueFactory(new PropertyValueFactory<User, String>("noFriends"));

        userTableView.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    setFields(userTableView.getSelectionModel().getSelectedItem());
                }
            });
            return row;
        });

        // context menu principal
        openUserAccountMenuItem = new MenuItem("Open account");

        openUserAccountMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                User selectedUser = userTableView.getSelectionModel().getSelectedItem();

                if (selectedUser != null) {
                    showUserAccount(selectedUser);
                }
            }
        });

        contextMenuUserAccount.getItems().add(openUserAccountMenuItem);
        userTableView.setContextMenu(contextMenuUserAccount);

        userTableView.setItems(userModel);

    }

    /**
     * Initializeaza tabela cu toti userii.
     */
    private void initModel() {
        Iterable<User> userIterable = networkService.getAllUsers();

        List<User> userList = StreamSupport.stream(userIterable.spliterator(), false)
                .toList();
        userModel.setAll(userList);
    }

    /**
     * Salveaza un user.
     */
    private void saveUser(User user) {
        try {
            this.networkService.addUser(user.getFirstName(), user.getLastName(), user.getUserName());
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Add User", "User added successfully!");
            clearFields();
        } catch (ValidationException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    /**
     * Handler salvare user.
     *
     * @param event
     */
    @FXML
    public void handleSaveUser(ActionEvent event) {
        String firstName = textFieldFirstName.getText();
        String lastName = textFieldLastName.getText();
        String username = textFieldUsername.getText();

        User user = new User(firstName, lastName, username);
        saveUser(user);
    }

    /**
     * Clears text fields.
     */
    private void clearFields() {
        textFieldFirstName.setText("");
        textFieldLastName.setText("");
        textFieldUsername.setText("");
    }

    /**
     * Sets text fields.
     *
     * @param user
     */
    private void setFields(User user) {
        textFieldUsername.setText(user.getUserName());
        textFieldFirstName.setText(user.getFirstName());
        textFieldLastName.setText(user.getLastName());
    }


    /**
     * Handler pentru cancel.
     */
    @FXML
    public void handleCancel() {
        clearFields();
    }


    /**
     * Sterge un user.
     */
    private void deleteUser(User user) {
        Optional<User> deletedUser = networkService.deleteUser(user.getUserName());
        if (deletedUser.isPresent()) {
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Delete User", "User deleted successfully!");
        } else {
            MessageAlert.showErrorMessage(null, "Can't delete this user!");
        }
    }

    /**
     * Handler pentru stergere user.
     * @param actionEvent
     */
    @FXML
    public void handleDeleteUser(ActionEvent actionEvent) {
        User selected = (User) userTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            deleteUser(selected);
        } else {
            MessageAlert.showErrorMessage(null, "You haven't selected a user!");
        }
        clearFields();
    }

    /**
     * Modifica un user.
     */
    private void updateUser(User currentUser, String newFirstName, String newLastName, String newUsername) {
        try {
            networkService.updateUser(currentUser.getUserName(), newUsername, newFirstName, newLastName);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Update User", "User updated successfully!");
        } catch (ValidationException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }

    }

    /**
     * Handler pentru modificare user.
     * @param actionEvent
     */
    @FXML
    public void handleUpdateUser(ActionEvent actionEvent) {
        User selected = (User) userTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String newFirstName = textFieldFirstName.getText();
            String newLastName = textFieldLastName.getText();
            String newUsername = textFieldUsername.getText();

            updateUser(selected, newFirstName, newLastName, newUsername);
            clearFields();
        } else {
            MessageAlert.showErrorMessage(null, "You haven't selected a user!");
        }
        clearFields();
    }


    /**
     * Observer
     */
    @Override
    public void update(UserChangeEvent userChangeEvent) {
        initModel();
    }
}
