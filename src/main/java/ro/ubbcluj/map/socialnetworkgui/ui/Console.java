package ro.ubbcluj.map.socialnetworkgui.ui;

import ro.ubbcluj.map.socialnetworkgui.domain.Friendship;
import ro.ubbcluj.map.socialnetworkgui.domain.Message;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.service.NetworkService;

import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class Console {
    private final Scanner cin;
    private final NetworkService networkService;

    public Console(NetworkService networkService) {
        this.networkService = networkService;
        cin = new Scanner(System.in);
    }

    /**
     * Printeaza meniul consolei.
     */
    private void printMenu() {
        System.out.println("-Meniu-");
        System.out.println("1. See all users.");
        System.out.println("2. Add user.");
        System.out.println("3. Delete user.");
        System.out.println("4. See all friendships.");
        System.out.println("5. Add friendship.");
        System.out.println("6. Remove friendship.");
        System.out.println("7. Show the number of communities.");
        System.out.println("8. Show the most sociable community.");
        System.out.println("9. Print list of users with at least n friends.");
        System.out.println("10. Print all the friendships of a user, from a specific month.");
        System.out.println("11. Print all the users that contain a given string in their name.");
        System.out.println("12. Update user.");
        System.out.println("13. See all messages.");
        System.out.println("14. See all messages of 2 users.");
        System.out.println("15. Send a message.");
        System.out.println("16. Testare friend request.");
        System.out.println("0. EXIT");
    }

    /**
     * Printeaza toti userii.
     */
    private void printAllUsers() {
        Iterable<User> users = networkService.getAllUsers();
        users.forEach(System.out::println);
    }

    /**
     * Printeaza toate prieteniile.
     */
    private void printAllFriendships() {
        Iterable<Friendship> friendships = networkService.getAllFriendships();
        friendships.forEach(System.out::println);
    }

    /**
     * Colecteaza informatiile pentru adaugarea unui user.
     */
    private void addUser() {
        System.out.println("First name:");
        String firstName = cin.next();

        System.out.println("Last name:");
        String lastName = cin.next();

        System.out.println("Username:");
        String userame = cin.next();

        try {
            networkService.addUser(firstName, lastName, userame);
            System.out.println("User adaugat cu succes!");
        } catch (ValidationException | IllegalArgumentException e) {
            System.out.println(e);
        }

    }

    /**
     * Colecteaza informatiile pentru stergerea unui user.
     */
    private void deleteUser() {
        System.out.println("Dati username-ul pe care doriti sa il stergeti:");
        String username = cin.next();

        try {
            Optional<User> deleted = networkService.deleteUser(username);
            if (deleted.isEmpty()) {
                System.out.println("User-ul nu a fost sters!");
            } else {
                System.out.println("User-ul @" + deleted.get().getUserName() + " a fost sters cu succes!");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }
    }

    /**
     * Colecteaza informatiile pentru crearea unei prietenii.
     */
    private void createFriendship() {
        System.out.println("User1:");
        String username1 = cin.next();
        System.out.println("User2:");
        String username2 = cin.next();

        try {
            networkService.createFriendship(username1, username2);
            System.out.println("Prietenie creata cu succes!");
        } catch (ValidationException e) {
            System.out.println(e);
        }
    }

    /**
     * Colecteaza informatii pentru stergerea unei prietenii.
     */
    private void removeFriendship() {
        System.out.println("User1:");
        String username1 = cin.next();
        System.out.println("User2:");
        String username2 = cin.next();

        try {
            networkService.deleteFriendship(username1, username2);
            System.out.println("Prietenie stearsa cu succes!");
        } catch (ValidationException e) {
            System.out.println(e);
        }
    }

    /**
     * Afiseaza numarul de comunitati si care sunt acestea.
     */
    private void numberOfCommunities() {
        System.out.println(networkService.numberOfCommunities() + " comunitati. Acestea sunt:");
        List<List<User>> allCommunities = networkService.getAllCommunities();

        allCommunities.forEach(comm -> {
            System.out.println("Comunitate:");
            comm.forEach(System.out::println);
        });
    }

    /**
     * Afiseaza cea mai sociabila comunitate.
     */
    private void mostSociableCommunity() {
        System.out.println("Cea mai sociabila comunitate este:");
        // pot avea mai multe comunitati cu aceasta proprietate
        List<Iterable<User>> community = networkService.getMostSociableCommunity();

        community.forEach(comm -> {
            System.out.println("Comunitate:");
            comm.forEach(System.out::println);
        });
    }

    /**
     * Afiseaza userii cu cel putin n prieteni.
     */
    private void atLeastNFriends() {
        System.out.println("Dati un numar minim de prieteni:");
        int n = cin.nextInt();

        List<User> userList = networkService.atLeastNFriends(n);

        if (userList.isEmpty()) {
            System.out.println("Nu exista useri cu acest numar de prieteni.");
        } else {
            System.out.println("Userii care indeplinesc conditia sunt:");
            userList.forEach(System.out::println);
        }
    }

    /**
     * Afiseaza prietenii unui user dintr-o anumita luna.
     */
    private void printFriendsFromMonth() {
        System.out.println("Username:");
        String username = cin.next();
        System.out.println("Luna anului:");
        int luna = cin.nextInt();

        if (luna <= 0 || luna > 12) {
            System.out.println("Luna invalida!!");
            return;
        }

        try {
            Set<Tuple<User, LocalDate>> userList = networkService.friendsFromMonth(username, luna);

            if (userList.isEmpty()) {
                System.out.println("Acest user nu are prietenii facute in luna data!");
            } else {
                System.out.println("Prietenii lui @" + username + " din luna " + luna);
                userList.forEach((tuple) -> System.out.println(tuple.getLeft().getFirstName() + " | " + tuple.getLeft().getLastName() + " | " + tuple.getLeft().getUserName() + " | " + tuple.getRight()));
            }
        } catch (ValidationException e) {
            System.out.println(e);
        }
    }

    private void updateUser(){
        // username duplicat

        String currentUsername = "abc";

        String newUsername = "abc";
        String newFirstName = "aaa";
        String newLastName = "aaa";

        try{
            networkService.updateUser(currentUsername, newUsername, newFirstName, newLastName);
        }
        catch(ValidationException e){
            System.out.println(e);
        }
    }

    /**
     * Afiseaza userii care contin un string dat in numele lor.
     */
    private void printUsersWithContainedString() {
        System.out.println("Dati un string:");
        String string = cin.next();

        string = string.toLowerCase();

        List<User> userList = networkService.usersWithContainedString(string);

        if (userList.isEmpty()) {
            System.out.println("Nu exista useri care sa aiba in componenta numelor stringul dat.");
        } else {
            userList.forEach(System.out::println);
        }
    }

    /**
     * Afiseaza toate mesajele existente.
     */
    private void printAllMessages(){
        Iterable<Message> allMessages = networkService.getAllMessages();

        allMessages.forEach(System.out::println);
    }

    /**
     * Afiseaza mesajele in ordine cronologica intre 2 useri.
     */
    private void printAllMessagesOf2Users() {
        String username1 = "lee.zoe";
        String username2 = "jrr.123";

        Iterable<Message> allMessages = networkService.getChatForUsers(username1, username2);
        allMessages.forEach(System.out::println);
    }

    private void addMessage(){
        String username1 = "lee.zoe";
        String username2 = "jrr.123";
        String description = "heyy.. sorry for bothering you... can you help me with something?";

        try{
            networkService.sendMessage(username1, username2, description);
//            if(message.isPresent()){
//                System.out.println("Mesaj trimis cu succes");
//            }
        }
        catch(ValidationException ve){
            System.out.println(ve);
        }
    }

    private void friendRequest(){
        // pending
        System.out.println("Creare prietenie...PENDING [aaa-bbb]");
        String sender = "aaa";
        String receiver = "bbb";
        networkService.sendFriendRequest(sender, receiver);
        printAllFriendships();

        // accept
        System.out.println("Acceptare prietenie...ACCEPTED [aaa-bbb]");
        String cine = "bbb";
        String peCine = "aaa";
        networkService.acceptFriendRequest(cine, peCine);
        printAllFriendships();

        // pending
        System.out.println("Creare prietenie...PENDING [ccc-bbb]");
        String sender1 = "ccc";
        String receiver1 = "bbb";
        networkService.sendFriendRequest(sender1, receiver1);
        printAllFriendships();

        // reject
        System.out.println("Decline friendship...REJECT [bbb-ccc]");
        String cine1 = "bbb";
        String peCine1 = "ccc";
        networkService.rejectFriendRequest(cine1, peCine1);
        printAllFriendships();
    }

    public void start() {
        while (true) {
            printMenu();
            System.out.println("Dati o comanda:");
            int cmd = cin.nextInt();

            switch (cmd) {
                case 0:
                    cin.close();
                    return;
                case 1:
                    printAllUsers();
                    break;
                case 2:
                    addUser();
                    break;
                case 3:
                    deleteUser();
                    break;
                case 4:
                    printAllFriendships();
                    break;
                case 5:
                    createFriendship();
                    break;
                case 6:
                    removeFriendship();
                    break;
                case 7:
                    numberOfCommunities();
                    break;
                case 8:
                    mostSociableCommunity();
                    break;
                case 9:
                    atLeastNFriends();
                    break;
                case 10:
                    printFriendsFromMonth();
                    break;
                case 11:
                    printUsersWithContainedString();
                    break;
                case 12:
                    updateUser();
                    break;
                case 13:
                    printAllMessages();
                    break;
                case 14:
                    printAllMessagesOf2Users();
                    break;
                case 15:
                    addMessage();
                    break;
                case 16:
                    friendRequest();
                    break;
                default:
                    System.out.println("Wrong command!");
            }
        }
    }
}
