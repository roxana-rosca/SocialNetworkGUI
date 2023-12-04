package ro.ubbcluj.map.socialnetworkgui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkgui.UserPageController;
import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.service.NetworkService;
import ro.ubbcluj.map.socialnetworkgui.utils.MessageAlert;

import java.util.List;

public class NewChatController {
    UserPageController userPageController;
    Stage newChatStage;
    NetworkService networkService;
    @FXML
    ListView<String> newChatListView;
    User user;
    ObservableList<String> newChatModel = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
//        newChatListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                ObservableList<String> selected = newChatListView.getSelectionModel().getSelectedItems();
//
//                for(String s:selected){
//                    System.out.println(s);
//                }
//            }
//        });

        newChatListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void initModel(){
        List<String> newChatList = networkService.getAllUsernamesExceptOfUser(user);

        newChatModel.setAll(newChatList);

        newChatListView.setItems(newChatModel);
    }

    public void setService(Stage stage, NetworkService networkService, User user, UserPageController userPageController){
        this.newChatStage = stage;
        this.networkService = networkService;
        this.user = user;
        this.userPageController = userPageController;

        initModel();
    }


    @FXML
    public void handleSendCreateNewConvo(ActionEvent actionEvent){
        List<String> selected = newChatListView.getSelectionModel().getSelectedItems();

        if(selected.isEmpty()){
            MessageAlert.showErrorMessage(null, "You haven't selected a user!");
            return;
        }

        if(selected.size() == 1){
            userPageController.initMessages(userPageController.getUser().getUserName(), selected.get(0));
            userPageController.getUserMessageModel().add(selected.get(0));

            userPageController.getChatListView().getSelectionModel().selectLast();
        }
        else{
            userPageController.clearMessageListView();
            userPageController.initMultipleMessages(user.getUserName(), selected);
        }

        newChatStage.close();
    }

}
