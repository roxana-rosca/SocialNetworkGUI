package ro.ubbcluj.map.socialnetworkgui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.service.NetworkService;
import ro.ubbcluj.map.socialnetworkgui.service.UserService;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UserChangeEvent;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observer;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class UserController implements Observer<UserChangeEvent> {
    NetworkService networkService;
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

    public void setUserService(NetworkService networkService) {
        this.networkService = networkService;
        networkService.addObserver(this);
        initModel();
    }

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

        userTableView.setItems(userModel);
    }

    private void initModel() {
        Iterable<User> userIterable = networkService.getAllUsers();

        List<User> userList = StreamSupport.stream(userIterable.spliterator(), false)
                .toList();
        userModel.setAll(userList);
    }

    private void saveUser(User user) {
        try {
            this.networkService.addUser(user.getFirstName(), user.getLastName(), user.getUserName());
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Add User", "User added successfully!");
            clearFields();
        } catch (ValidationException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    public void handleSaveUser(ActionEvent event) {
        String firstName = textFieldFirstName.getText();
        String lastName = textFieldLastName.getText();
        String username = textFieldUsername.getText();

        User user = new User(firstName, lastName, username);
        saveUser(user);
    }

    private void clearFields() {
        textFieldFirstName.setText("");
        textFieldLastName.setText("");
        textFieldUsername.setText("");
    }

    private void setFields(User user){
        textFieldUsername.setText(user.getUserName());
        textFieldFirstName.setText(user.getFirstName());
        textFieldLastName.setText(user.getLastName());
    }

    @FXML
    public void handleCancel() {
        clearFields();
    }


    private void deleteUser(User user){
        Optional<User> deletedUser = networkService.deleteUser(user.getUserName());
        if(deletedUser.isPresent()){
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Delete User","User deleted successfully!");
        }
        else{
            MessageAlert.showErrorMessage(null,"Can't delete this user!");
        }
    }

    @FXML
    public void handleDeleteUser(ActionEvent actionEvent){
        User selected = (User) userTableView.getSelectionModel().getSelectedItem();
        if(selected != null){
            deleteUser(selected);
        }
        else{
            MessageAlert.showErrorMessage(null,"You haven't selected a user!");
        }
    }

    private void updateUser(User currentUser, String newFirstName, String newLastName, String newUsername){
        try{
            networkService.updateUser(currentUser.getUserName(), newUsername,newFirstName,newLastName);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Update User","User updated successfully!");
        }
        catch(ValidationException e){
            MessageAlert.showErrorMessage(null, e.getMessage());
        }

    }

    @FXML
    public void handleUpdateUser(ActionEvent actionEvent){
        User selected = (User) userTableView.getSelectionModel().getSelectedItem();
        if(selected!=null){
            String newFirstName = textFieldFirstName.getText();
            String newLastName = textFieldLastName.getText();
            String newUsername = textFieldUsername.getText();

            updateUser(selected,newFirstName,newLastName,newUsername);
            clearFields();
        }
        else{
            MessageAlert.showErrorMessage(null,"You haven't selected a user!");
        }
    }


    @Override
    public void update(UserChangeEvent userChangeEvent) {
        initModel();
    }
}
