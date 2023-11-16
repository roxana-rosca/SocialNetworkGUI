package ro.ubbcluj.map.socialnetworkgui.service;

import ro.ubbcluj.map.socialnetworkgui.domain.Friendship;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.utils.events.ChangeEventType;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UserChangeEvent;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observable;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observer;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class NetworkService implements Observable<UserChangeEvent> {
    private final UserService userService;
    private final FriendshipService friendshipService;
    private List<Observer<UserChangeEvent>> observers = new ArrayList<>();

    public NetworkService(UserService userService, FriendshipService friendshipService) {
        this.userService = userService;
        this.friendshipService = friendshipService;

        updateAllUserFriends();
    }

    /**
     * Actualizeaza lista de prieteni a unui user, adaugand un nou user in lista de prieteni.
     *
     * @param id1: id-ul unui user
     * @param id2: id-ul altui user
     */
    public void updateUserFriends(Long id1, Long id2) {
        Optional<User> u1 = userService.getUserByID(id1);
        Optional<User> u2 = userService.getUserByID(id2);

        if (u1.isEmpty() || u2.isEmpty())
            throw new ValidationException("Nu exista acest user!");

        if (!u1.get().getFriends().contains(u2.get())) {
            u1.get().addFriend(u2.get());
        }
        if (!u2.get().getFriends().contains(u1.get())) {
            u2.get().addFriend(u1.get());
        }
    }

    /**
     * Actualizeaza lista de prieteni a tuturor userilor, adaugand prietenii noi.
     */
    public void updateAllUserFriends() {
        Iterable<Friendship> allFriendships = friendshipService.getAll();

        for (Friendship friendship : allFriendships) {
            Long id1 = friendship.getId().getLeft();
            Long id2 = friendship.getId().getRight();

            updateUserFriends(id1, id2);
        }
    }


    /**
     * Creeaza o prietenie intre 2 useri.
     *
     * @param username1: username-ul unui user
     * @param username2: username-ul altui user
     * @throws ValidationException daca cel putin unul dintre useri este invalid/ userii sunt identici
     */
    public void createFriendship(String username1, String username2) {
        Optional<User> user1 = userService.getUserByUserName(username1);
        Optional<User> user2 = userService.getUserByUserName(username2);

        if (user1.isEmpty() || user2.isEmpty()) {
            throw new ValidationException("User invalid!");
        }

        if (!userService.createFriendship(user1.get(), user2.get()))
            throw new ValidationException("Cele 2 entitati trebuie sa fie diferite!");

        if(friendshipService.getFriendship(user1.get().getId(), user2.get().getId()).isPresent()){
            throw new ValidationException("Prietenia exista deja!");
        }

        Friendship friendship = new Friendship();
        Tuple<Long, Long> f = new Tuple<>(user1.get().getId(), user2.get().getId());
        friendship.setId(f);
        friendshipService.createFriendship(friendship, null);
    }

    /**
     * Sterge o prietenie.
     *
     * @param username1: username-ul unui user
     * @param username2: username-ul altui user
     * @throws ValidationException daca cel putin unul dintre useri nu exista/ cei 2 useri sunt identici
     */
    public void deleteFriendship(String username1, String username2) {
        Optional<User> user1 = userService.getUserByUserName(username1);
        Optional<User> user2 = userService.getUserByUserName(username2);

        if (user1.isEmpty() || user2.isEmpty()) {
            throw new ValidationException("User invalid!");
        }

        if (!userService.deleteFriendship(user1.get(), user2.get())) {
            throw new ValidationException("Cele 2 entitati trebuie sa fie diferite!");
        }

        Optional<Friendship> deletedFriendship = friendshipService.getFriendship(user1.get().getId(), user2.get().getId());

        if(deletedFriendship.isEmpty()){
            throw new ValidationException("Nu exista aceasta prietenie!");
        }

        if (!friendshipService.deleteFriendship(deletedFriendship.get(), null)) {
            throw new ValidationException("Nu exista aceasta prietenie!");
        }

    }


    /**
     * @return un iterator cu toti userii
     */
    public Iterable<User> getAllUsers(){
        return userService.getAll();
    }

    /**
     * @return un iterator cu toate prieteniile
     */
    public Iterable<Friendship> getAllFriendships() {
        return friendshipService.getAll();
    }

    /**
     * Adauga un user.
     *
     * @param firstName: prenumele user-ului
     * @param lastName:  numele user-ului
     * @param username:  username-ul user-ului
     */
    public void addUser(String firstName, String lastName, String username) {
        User user = new User(firstName, lastName, username);

        if(userService.getUserByUserName(username).isPresent()){
            throw new ValidationException("This username is already in use!");
        }

        boolean flag = userService.addEntity(user);

        if(flag){
            notifyObservers(new UserChangeEvent(ChangeEventType.ADD, user));
        }
    }

    /**
     * Sterge un user.
     *
     * @param username: username-ul unui user
     * @return entitatea stearsa/ null, daca aceasta nu a fost stearsa
     * @throws IllegalArgumentException daca username-ul este null/ nu exista un user cu acest username
     */
    public Optional<User> deleteUser(String username) {
        if (username == null)
            throw new IllegalArgumentException("Username-ul nu poate sa fie null!");

        Optional<User> deletedUser = userService.getUserByUserName(username);

        if (deletedUser.isEmpty()) {
            throw new IllegalArgumentException("Nu exista un user cu acest username!");
        }

        // sterg si prieteniile cu userul nou sters
        // refactor: cu filter prietenii cu acel user
        /*Iterable<Friendship> allFriendships = friendshipService.getAll();
        for(Friendship f:allFriendships){
            if(f.getId().getLeft().equals(deletedUser.get().getId())){
                Optional<User> user1 = userService.getUserByID(f.getId().getRight());
                deleteFriendship(deletedUser.get().getUserName(), user1.get().getUserName());
            }
            else if(f.getId().getRight().equals(deletedUser.get().getId())){
                Optional<User> user2 = userService.getUserByID(f.getId().getLeft());
                deleteFriendship(user2.get().getUserName(), deletedUser.get().getUserName());
            }
        }*/

        /*Iterable<Friendship> allFriendships = friendshipService.getAll();

        for(Friendship f:allFriendships){
            if(f.getId().getLeft().equals(deletedUser.get().getId())){
                Optional<User> user1 = userService.getUserByID(f.getId().getRight());
                deleteFriendship(deletedUser.get().getUserName(), user1.get().getUserName());
            }
            else if(f.getId().getRight().equals(deletedUser.get().getId())){
                Optional<User> user2 = userService.getUserByID(f.getId().getLeft());
                deleteFriendship(user2.get().getUserName(), deletedUser.get().getUserName());
            }
        }*/

        Optional<User> dUser =  userService.deleteEntity(deletedUser.get());
        dUser.ifPresent(user -> notifyObservers(new UserChangeEvent(ChangeEventType.DELETE, user)));
        return dUser;
    }

    /**
     * @return numarul de comunitati existente
     */
    public int numberOfCommunities() {
        return userService.numberOfCommunities();
    }

    /**
     * @return comunitatile existente
     */
    public List<List<User>> getAllCommunities() {
        return userService.getAllCommunities();
    }

    /**
     * @return cele mai sociabile comunitati
     */
    public List<Iterable<User>> getMostSociableCommunity() {
        return userService.mostSociableCommunity();
    }

    /**
     *
     * @param n: numarul minim de prieteni
     * @return o lista cu userii ce au cel putin n prieteni
     */
    public List<User> atLeastNFriends(int n){
        return userService.atLeastNFriends(n);
    }

    /**
     * Determina prietenii dintr-o anumita luna pentru un user.
     * @param username: username-ul unui user
     * @param month: o luna
     * @return un set cu elemenete de tipul Tuple care contine prietenii user-ului si data in care acestia si-au format prietenia
     */
    public Set<Tuple<User, LocalDate>> friendsFromMonth(String username, int month){
        Set<Tuple<User, LocalDate>> friends = new HashSet<>();

        Optional<User> user = userService.getUserByUserName(username);

        if(user.isEmpty()){
            throw new ValidationException("Nu exista acest user!");
        }

        Iterable<Friendship> friendships= friendshipService.getAll();

        Long idUser = user.get().getId();
        Predicate<Friendship> getFriendsFromMonth = friendship ->
                (friendship.getId().getLeft().equals(idUser) || friendship.getId().getRight().equals(idUser))
                && (friendship.getDate().getMonth().getValue() == month);

        // colectez prieteniile userului din luna respectiva
        List<Friendship> f = StreamSupport.stream(friendships.spliterator(), false)
                                          .filter(getFriendsFromMonth)
                                          .toList();

        // adaug prietenii si data formarii prieteniilor
        f.forEach(friendship -> {
            Long idFriend = (friendship.getId().getLeft().equals(idUser) ? friendship.getId().getRight() : friendship.getId().getLeft());
            Optional<User> fr = userService.getUserByID(idFriend);
            if(fr.isPresent()){
                Tuple<User, LocalDate> tuple = new Tuple<>(fr.get(),friendship.getDate());
                friends.add(tuple);
            }
        });

        return friends;
    }

    /**
     * Returneaza o lista ce contine userii care au in componenta numelor un string dat.
     * @param string: un string dat
     * @return o lista cu obiecte de tip User
     */
    public List<User> usersWithContainedString(String string){
        return userService.usersWithContainedString(string);
    }

    public void updateUser(String currentUsername, String newUsername, String newFirstName, String newLastName){
        Optional<User> found = userService.getUserByUserName(currentUsername);

        if(found.isPresent()){
            User newUser = new User(newFirstName, newLastName, newUsername);
            newUser.setFriends(found.get().getFriends());
            newUser.setId(found.get().getId());
            Optional<User> uUser = userService.updateEntity(newUser);
            if(uUser.isEmpty()){
                notifyObservers(new UserChangeEvent(ChangeEventType.UPDATE, found.get()));
            }

        }
        else{
            throw new ValidationException("This user doesn't exist!");
        }
    }

    @Override
    public void addObserver(Observer<UserChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<UserChangeEvent> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(UserChangeEvent t) {
        observers.stream().forEach(x->x.update(t));
    }
}
