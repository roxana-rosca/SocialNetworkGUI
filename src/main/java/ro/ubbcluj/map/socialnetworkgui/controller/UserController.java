package ro.ubbcluj.map.socialnetworkgui.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
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
import ro.ubbcluj.map.socialnetworkgui.UserPageController;
import ro.ubbcluj.map.socialnetworkgui.domain.Message;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.service.NetworkService;
import ro.ubbcluj.map.socialnetworkgui.utils.MessageAlert;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UserChangeEvent;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;


public class UserController implements Observer<UserChangeEvent> {
    NetworkService networkService;

    // context menu
//    private ContextMenu contextMenuUserAccount = new ContextMenu();
//    private MenuItem openUserAccountMenuItem;

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

    // tab chat
    @FXML
    TableView<Tuple<String, String>> chatTableView;
    @FXML
    ListView<Message> messageListView;

    @FXML
    TableColumn<String, String> tableColumnUser1ChatTableView;
    @FXML
    TableColumn<String, String> tableColumnUser2ChatTableView;
    ObservableList<Tuple<String, String>> userMessageModel = FXCollections.observableArrayList();
    ObservableList<Message> messageModel = FXCollections.observableArrayList();

    // tab friend requests
    @FXML
    TableView<String> friendRequestsTableView;
    @FXML
    TableColumn<String, String> tableColumnUsernameFriendRequestsTableView;
    ObservableList<String> usernameFriendRequestModel = FXCollections.observableArrayList();
    @FXML
    private TextField searchBarFriendRequest;

    // tab make friends
    @FXML
    TableView<String> friendsTableView;
    @FXML
    TableColumn<String, String> tableColumnUsernameFriendsTableView;
    @FXML
    TableView<String> discoverTableView;
    @FXML
    TableColumn<String, String> tableColumnUsernameDiscoverTableView;
    ObservableList<String> friendsModel = FXCollections.observableArrayList();
    ObservableList<String> discoverModel = FXCollections.observableArrayList();
    @FXML
    private TextField searchBarMakeFriends;

    // context menu
    private ContextMenu contextMenu = new ContextMenu();
    private MenuItem makeFriendsMenuItem;

    public void setUserService(NetworkService networkService) {
        this.networkService = networkService;
        networkService.addObserver(this);
        initModel();

        initUsernameModel();
    }

    public void showUserAccount(User user){
        try{
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

        }
        catch (IOException e){
            e.printStackTrace();
        }


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

        // context menu principal
//        openUserAccountMenuItem = new MenuItem("Open account");
//
//        openUserAccountMenuItem.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                User selectedUser = userTableView.getSelectionModel().getSelectedItem();
//
//                if(selectedUser != null){
//                    showUserAccount(selectedUser);
//                }
//            }
//        });
//
//        contextMenuUserAccount.getItems().add(openUserAccountMenuItem);
//        userTableView.setContextMenu(contextMenuUserAccount);

        // context menu
        makeFriendsMenuItem = new MenuItem("Send friend request");

        makeFriendsMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String receiver = discoverTableView.getSelectionModel().getSelectedItem();
                String sender = searchBarMakeFriends.getText();

                if(sender == null){
                    MessageAlert.showErrorMessage(null, "You haven't typed anything!");
                    return;
                }

                boolean flag = networkService.sendFriendRequest(sender, receiver);
//                if(flag){
//                    System.out.println("gata!");
//                }
            }
        });
        contextMenu.getItems().add(makeFriendsMenuItem);
        discoverTableView.setContextMenu(contextMenu);

        userTableView.setItems(userModel);

        // tab chat
        initializeChatTableView();

    }

    @FXML
    public void initializeChatTableView() {
        tableColumnUser1ChatTableView.setCellValueFactory(new PropertyValueFactory<String, String>("e1"));
        tableColumnUser2ChatTableView.setCellValueFactory(new PropertyValueFactory<String, String>("e2"));

        chatTableView.setRowFactory(tv -> {
            TableRow<Tuple<String, String>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    Tuple<String, String> usernames = chatTableView.getSelectionModel().getSelectedItem();
                    initMessages(usernames.getE1(), usernames.getE2());
                }
            });
            return row;
        });

        chatTableView.setItems(userMessageModel);

    }

    private void initUsernameModel() {
        Set<Tuple<String, String>> usernames = networkService.getUsernamesForMessages();

        userMessageModel.setAll(usernames);
    }

    private void initMessages(String username1, String username2) {
        Iterable<Message> messageIterable = networkService.getChatForUsers(username1, username2);

        List<Message> messageList = StreamSupport.stream(messageIterable.spliterator(), false).toList();

        messageModel.setAll(messageList);

        initializeMessageListView();
    }

    private void initializeMessageListView() {
        messageListView.setItems(messageModel);
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

    private void setFields(User user) {
        textFieldUsername.setText(user.getUserName());
        textFieldFirstName.setText(user.getFirstName());
        textFieldLastName.setText(user.getLastName());
    }

    @FXML
    public void handleCancel() {
        clearFields();
    }


    private void deleteUser(User user) {
        Optional<User> deletedUser = networkService.deleteUser(user.getUserName());
        if (deletedUser.isPresent()) {
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Delete User", "User deleted successfully!");
        } else {
            MessageAlert.showErrorMessage(null, "Can't delete this user!");
        }
    }

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

    private void updateUser(User currentUser, String newFirstName, String newLastName, String newUsername) {
        try {
            networkService.updateUser(currentUser.getUserName(), newUsername, newFirstName, newLastName);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Update User", "User updated successfully!");
        } catch (ValidationException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }

    }

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


    @Override
    public void update(UserChangeEvent userChangeEvent) {
        initModel();

        initUsernameModel();
    }

    private void clearSearchField() {
        searchBarFriendRequest.setText("");
        searchBarMakeFriends.setText("");
    }

    @FXML
    public void handleSearchFriendRequestsForUsername(ActionEvent actionEvent) {
        String username = searchBarFriendRequest.getText();

        if (!username.isEmpty()) {
            initUsernames(username);
        } else {
            MessageAlert.showErrorMessage(null, "You haven't typed anything!");

        }
        //clearSearchField();
    }

    private void initUsernames(String username) {
        List<String> usernames = networkService.getFriendRequestsForUsername(username);

        if (usernames == null) {
            MessageAlert.showErrorMessage(null, "This user doesn't exist!");
            clearSearchField();
            return;
        }

        if (usernames.isEmpty()) {
            clearAndReloadFriendRequestTableView();
            Label noFriends = new Label("No friend requests for\n@" + username + "!");
            friendRequestsTableView.setPlaceholder(noFriends);
            //String message = "No friend requests for @" + username + "!";
            //MessageAlert.showErrorMessage(null, message);
        } else {
            usernameFriendRequestModel.setAll(usernames);
            initializeFriendRequestsTableView();
        }

    }

    public void initializeFriendRequestsTableView() {
        tableColumnUsernameFriendRequestsTableView.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()));
        friendRequestsTableView.getColumns().setAll(tableColumnUsernameFriendRequestsTableView);

        // pentru selectare coloana
        friendRequestsTableView.setRowFactory(tv -> {
            TableRow<String> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    String username = friendRequestsTableView.getSelectionModel().getSelectedItem();
                    //System.out.println(username);
                }
            });
            return row;
        });

        friendRequestsTableView.setItems(usernameFriendRequestModel);
    }
    @FXML
    public void handleSearchFriendsForUsername(){
        String username = searchBarMakeFriends.getText();

        if (!username.isEmpty()) {
            initFriends(username);
            initDiscover(username);
        } else {
            MessageAlert.showErrorMessage(null, "You haven't typed anything!");

        }
    }

    private void initFriends(String username){
        List<String> usernamesOfFriends = networkService.getUsernameForFriends(username);

        if (usernamesOfFriends == null) {
            MessageAlert.showErrorMessage(null, "This user doesn't exist!");
            clearSearchField();
            return;
        }

        if(usernamesOfFriends.isEmpty()){
            // label
            clearAndReloadFriendsTableView();
            Label noFriends = new Label("@" + username + " has no friends!");
            friendsTableView.setPlaceholder(noFriends);
        }
        else{
            friendsModel.setAll(usernamesOfFriends);
            initializeFriendsTableView();
        }
    }

    public void initializeFriendsTableView(){
        tableColumnUsernameFriendsTableView.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()));
        friendsTableView.getColumns().setAll(tableColumnUsernameFriendsTableView);

        friendsTableView.setItems(friendsModel);
    }

    private void initDiscover(String username){
        List<String> usernamesOfNewPeople = networkService.getUsernameForNewPeople(username);

        if (usernamesOfNewPeople == null) {
            return;
        }

        if(usernamesOfNewPeople.isEmpty()){
            clearAndReloadDiscoverTableView();
            Label noFriends = new Label("@" + username + " is friends \nwith everyone!");
            discoverTableView.setPlaceholder(noFriends);
        }
        else{
            discoverModel.setAll(usernamesOfNewPeople);
            initializeDiscoverTableView();
        }
    }

    public void initializeDiscoverTableView(){
        tableColumnUsernameDiscoverTableView.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()));
        discoverTableView.getColumns().setAll(tableColumnUsernameDiscoverTableView);

        discoverTableView.setItems(discoverModel);
    }

    @FXML
    public void handleAcceptFriendRequest(ActionEvent actionEvent) {
        String selected = friendRequestsTableView.getSelectionModel().getSelectedItem();

        if (selected != null){
            String cine = searchBarFriendRequest.getText();
            if(!cine.isEmpty()){
                networkService.acceptFriendRequest(cine, selected);
                clearAndReloadFriendRequestTableView();
                initUsernames(cine);
            }
            else{
                MessageAlert.showErrorMessage(null, "You need to type a user in the search bar!");
            }

        }
        else {
            MessageAlert.showErrorMessage(null, "You haven't selected a user!");
        }
    }

    @FXML
    public void handleRejectFriendRequest(ActionEvent actionEvent){
        String selected = friendRequestsTableView.getSelectionModel().getSelectedItem();

        if (selected != null){
            String cine = searchBarFriendRequest.getText();
            if(!cine.isEmpty()){
                networkService.rejectFriendRequest(cine, selected);
                clearAndReloadFriendRequestTableView();
                initUsernames(cine);

                //initModel();
            }
            else{
                MessageAlert.showErrorMessage(null, "You need to type a user in the search bar!");
            }
        }
        else{
            MessageAlert.showErrorMessage(null, "You haven't selected a user!");
        }
    }

    private void clearAndReloadFriendRequestTableView(){
        friendRequestsTableView.getItems().clear();
        initializeFriendRequestsTableView();
    }

    private void clearAndReloadFriendsTableView(){
        friendsTableView.getItems().clear();
        initializeFriendsTableView();
    }

    private void clearAndReloadDiscoverTableView(){
        discoverTableView.getItems().clear();
        initializeDiscoverTableView();
    }
}
