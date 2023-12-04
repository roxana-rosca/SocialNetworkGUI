package ro.ubbcluj.map.socialnetworkgui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.controller.NewChatController;
import ro.ubbcluj.map.socialnetworkgui.domain.Message;
import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.service.NetworkService;
import ro.ubbcluj.map.socialnetworkgui.utils.MessageAlert;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UserChangeEvent;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observer;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

public class UserPageController implements Observer<UserChangeEvent> {
    Stage accountStage;
    User user;
    NetworkService networkService;

    // tab chat
    @FXML
    Label receiverLabel = new Label();
    @FXML
    ListView<String> chatListView;
    @FXML
    ListView<Message> messageListView;
    @FXML
    TextField sendMessage;
    ObservableList<Message> messageModel = FXCollections.observableArrayList();
    ObservableList<String> userMessageModel = FXCollections.observableArrayList();


    // tab friend requests
    @FXML
    TableView<String> friendRequestsTableView;
    @FXML
    TableColumn<String, String> tableColumnUsernameFriendRequestsTableView;
    ObservableList<String> usernameFriendRequestModel = FXCollections.observableArrayList();

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

    // context menu
    private ContextMenu contextMenu = new ContextMenu();
    private MenuItem makeFriendsMenuItem;


    @FXML
    private void initialize(){
        // context menu
        makeFriendsMenuItem = new MenuItem("Send friend request");

        makeFriendsMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String receiver = discoverTableView.getSelectionModel().getSelectedItem();
                String sender = user.getUserName();

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

        initializeChatListView();


    }

    /**
     * Seteaza detalii legate de stage, service, profilul user-ului etc.
     */
    public void setService(Stage stage, User user, NetworkService networkService){
        this.accountStage = stage;
        this.user = user;
        this.networkService = networkService;

        networkService.addObserver(this);

        // tab friend requests
        initUsernames(user.getUserName());
        //tab make friends
        initFriends(user.getUserName());
        initDiscover(user.getUserName());
        // tab chat
        initUsernameModel();
    }

    /**
     * Reload Friend Request Table.
     */
    private void clearAndReloadFriendRequestTableView(){
        friendRequestsTableView.getItems().clear();
        initializeFriendRequestsTableView();
    }

    /**
     * Initializare Friend Request Table.
     */
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

    /**
     * Initializare Friend Request Table.
     */
    private void initUsernames(String username) {
        List<String> usernames = networkService.getFriendRequestsForUsername(username);

        if (usernames == null) {
            MessageAlert.showErrorMessage(null, "This user doesn't exist!");
            return;
        }

        if (usernames.isEmpty()) {
            clearAndReloadFriendRequestTableView();
            Label noFriends = new Label("No friend requests for\n@" + username + "!");
            friendRequestsTableView.setPlaceholder(noFriends);
        } else {
            usernameFriendRequestModel.setAll(usernames);
            initializeFriendRequestsTableView();
        }
    }

    /**
     * Accepta cererea de prietenie.
     */
    @FXML
    public void handleAcceptFriendRequest(ActionEvent actionEvent) {
        String selected = friendRequestsTableView.getSelectionModel().getSelectedItem();

        if (selected != null){
            String cine = user.getUserName();
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

    /**
     * Refuza o cerere de prietenie.
     */
    @FXML
    public void handleRejectFriendRequest(ActionEvent actionEvent){
        String selected = friendRequestsTableView.getSelectionModel().getSelectedItem();

        if (selected != null){
            String cine = user.getUserName();
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


    /**
     * Initializeaza Friends Table
     */
    private void initFriends(String username){
        List<String> usernamesOfFriends = networkService.getUsernameForFriends(username);

        if (usernamesOfFriends == null) {
            MessageAlert.showErrorMessage(null, "This user doesn't exist!");
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

    /**
     * Initializeaza Friends Table
     */
    public void initializeFriendsTableView(){
        tableColumnUsernameFriendsTableView.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()));
        friendsTableView.getColumns().setAll(tableColumnUsernameFriendsTableView);

        friendsTableView.setItems(friendsModel);
    }

    /**
     * Initializeaza Discover Table
     */
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

    /**
     * Initializeaza Discover Table
     */
    public void initializeDiscoverTableView(){
        tableColumnUsernameDiscoverTableView.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()));
        discoverTableView.getColumns().setAll(tableColumnUsernameDiscoverTableView);

        discoverTableView.setItems(discoverModel);
    }

    /**
     * Reload Friends Table
     */
    private void clearAndReloadFriendsTableView(){
        friendsTableView.getItems().clear();
        initializeFriendsTableView();
    }

    /**
     * Reload la Discover Table
     */
    private void clearAndReloadDiscoverTableView(){
        discoverTableView.getItems().clear();
        initializeDiscoverTableView();
    }

    public void clearMessageListView(){
        messageListView.getItems().clear();
    }


    /**
     * Initializare Chat List
     */
    @FXML
    public void initializeChatListView() {

        chatListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String selected= chatListView.getSelectionModel().getSelectedItem();
                if(selected != null){
                    initMessages(user.getUserName(), selected);
                }
            }
        });

        chatListView.setItems(userMessageModel);

    }

    /**
     * Initializeaza Chat List
     */
    private void initUsernameModel() {
        Set<String> usernames = networkService.getUsernamesForMessagesOfUser(user.getUserName());

        userMessageModel.setAll(usernames);
    }

    /**
     * Initializeaza Message List
     */
    public void initMessages(String username1, String username2) {
        Iterable<Message> messageIterable = networkService.getChatForUsers(username1, username2);

        List<Message> messageList = StreamSupport.stream(messageIterable.spliterator(), false).toList();

        if(username1.equals(user.getUserName())){
            receiverLabel.setText("@"+username2);
        }
        else{
            receiverLabel.setText("@"+username1);
        }

        messageModel.setAll(messageList);

        initializeMessageListView();
    }

    public void initMultipleMessages(String sender, List<String> receivers){
        String title = "";
        for(String s:receivers){
            title += "@" + s + ",";
        }
        title = title.substring(0, title.length()-1);

        receiverLabel.setText(title);
    }

    /**
     * Initializeaza Message List
     */
    private void initializeMessageListView() {
        messageListView.setItems(messageModel);
    }

    /**
     * Handler pentru trimitere de mesaje
     */
    @FXML
    public void handleSendMessage(ActionEvent actionEvent){
        if(receiverLabel.getText().contains(",")){

            List<String> users = List.of(receiverLabel.getText().split(","));
            String message = sendMessage.getText();

            if(message.isEmpty()){
                MessageAlert.showErrorMessage(null, "You can't send an empty message!");
                return;
            }

            // scot @
            users.forEach(u -> networkService.sendMessage(user.getUserName(), u.substring(1), message));
            clearSendMessageTextField();

            initUsernameModel();

        }
        else{
            String selected= chatListView.getSelectionModel().getSelectedItem();
            String message = sendMessage.getText();

            if(selected == null){
                MessageAlert.showErrorMessage(null, "You haven't selected a conversation!");
                return;
            }

            try{
                networkService.sendMessage(user.getUserName(), selected, message);
                clearSendMessageTextField();
            }
            catch(ValidationException e){
                MessageAlert.showErrorMessage(null, "You can't send an empty message!");
            }
        }

    }

    /**
     * Clears Message Text Field
     */
    private void clearSendMessageTextField(){
        sendMessage.setText("");
    }

    @FXML
    public void handleNewConversation(MouseEvent actionEvent){
        showNewChat();
    }

    public void showNewChat(){
        try{
            FXMLLoader newChatLoader = new FXMLLoader();

            newChatLoader.setLocation(getClass().getResource("views/new-chat-view.fxml"));

            AnchorPane newChatLayout = newChatLoader.load();

            Stage newChatStage = new Stage();
            newChatStage.setScene(new Scene(newChatLayout));


            newChatStage.setWidth(400);
            String title = "New chat for @"+user.getUserName();
            newChatStage.setTitle(title);
            newChatStage.getIcons().add(new Image("C:\\Users\\roxan\\IdeaProjects\\map\\SocialNetworkGUI\\src\\main\\resources\\ro\\ubbcluj\\map\\socialnetworkgui\\images\\new_chat.png"));

            NewChatController newChatController = newChatLoader.getController();
            newChatController.setService(newChatStage, networkService, user, this);

            newChatStage.show();

        }
        catch(IOException e){
            e.printStackTrace();
            Throwable cause = e.getCause();
            if (cause != null) {
                cause.printStackTrace();
            }
        }
    }


    /**
     * Observer
     * @param userChangeEvent
     */
    @Override
    public void update(UserChangeEvent userChangeEvent) {
        initUsernames(user.getUserName());
        initFriends(user.getUserName());
        initDiscover(user.getUserName());

        String selected= chatListView.getSelectionModel().getSelectedItem();
        if(selected!=null){
            initMessages(user.getUserName(), selected);
        }
    }

    public User getUser() {
        return user;
    }

    public ListView<String> getChatListView() {
        return chatListView;
    }

    public ObservableList<String> getUserMessageModel() {
        return userMessageModel;
    }
}
