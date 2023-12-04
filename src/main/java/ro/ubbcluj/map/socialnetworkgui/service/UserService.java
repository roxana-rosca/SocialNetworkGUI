package ro.ubbcluj.map.socialnetworkgui.service;

import ro.ubbcluj.map.socialnetworkgui.domain.Friendship;
import ro.ubbcluj.map.socialnetworkgui.domain.Message;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.repository.Repository;
import ro.ubbcluj.map.socialnetworkgui.repository.database.MessageDBRepository;
import ro.ubbcluj.map.socialnetworkgui.repository.database.UserDBRepository;
import ro.ubbcluj.map.socialnetworkgui.utils.Utils;

import java.sql.Date;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class UserService implements ServiceInterface<Long, User> {
    private final Repository<Long, User> userRepo;

    private final Repository<Long, Message> messageRepo;

    public UserService(Repository<Long, User> userRepo, Repository<Long, Message> messageRepo) {
        this.userRepo = userRepo;

        this.messageRepo = messageRepo;
    }
    @Override
    public boolean addEntity(User entity) {
        Optional<User> addedUser;

        if (entity.getId() == null) {
            entity.setId(Utils.generateUniqueLongID());
        }



        addedUser = userRepo.save(entity);

        return addedUser.isEmpty();
    }

    /**
     * Returneaza un user cu username-ul dat.
     *
     * @param username: un String ce reprezinta un username
     * @return un obiect de tipul User cu username-ul dat/ null in caz ca nu exista acest username
     */
    public Optional<User> getUserByUserName(String username) {
        /*
        Iterable<User> users = getAll();
        for (User u : users) {
            if (u.getUserName().equals(username))
                return u;
        }
        return null;
        */

        // iterables can't stream
        Iterable<User> userIterable = getAll();
        Stream<User> userStream = StreamSupport.stream(userIterable.spliterator(), false);

        return userStream.filter(user -> user.getUserName().equals(username)).findFirst();
    }

    @Override
    public Optional<User> deleteEntity(User entity) {
        return userRepo.delete(entity.getId());
    }

    @Override
    public Iterable<User> getAll() {
        return userRepo.findAll();
    }

    @Override
    public boolean createFriendship(User entity1, User entity2) {
        if (entity1.equals(entity2)) {
            return false;
        }

        entity1.addFriend(entity2);
        entity2.addFriend(entity1);
        return true;
    }

    @Override
    public boolean deleteFriendship(User entity1, User entity2) {
        if (entity1.equals(entity2)) {
            return false;
        }

        entity1.deleteFriend(entity2);
        entity2.deleteFriend(entity1);
        return true;
    }

    @Override
    public Optional<User> updateEntity(User entity) {
        // verific sa nu existe alt user cu acest username
        Optional<User> found = getUserByUserName(entity.getUserName());

        // nu exista/ alege sa isi pastreze username-ul
        if(found.isEmpty() || (found.isPresent() && found.get().getId().equals(entity.getId()))){
            return userRepo.update(entity);
        }
        else {
            throw new ValidationException("Can't use this username!");
        }
    }

    /**
     * Depth First Search.
     *
     * @param user:    un obiect de tipul User
     * @param userSet: un set ce contine useri
     * @return o lista cu userii interconectati, pornind de la user
     */
    private List<User> DFS(User user, Set<User> userSet) {
        List<User> userList = new ArrayList<>();

        userList.add(user);
        userSet.add(user);

        for (User u : user.getFriends()) {
            if (!userSet.contains(u)) {
                List<User> l = DFS(u, userSet);
                userList.addAll(l);
            }
        }

        return userList;
    }

    /**
     * Determina numarul de comunitati (componentele conexe din graf).
     *
     * @return numarul de comunitati
     */
    public int numberOfCommunities() {
        int communities = 0;

        Iterable<User> allUsers = getAll();
        Set<User> userSet = new HashSet<>();

        for (User user : allUsers) {
            if (!userSet.contains(user)) {
                DFS(user, userSet);
                communities++;
            }
        }

        return communities;
    }

    /**
     * Determina toate comunitatile.
     *
     * @return comunitatile prezente
     */
    public List<List<User>> getAllCommunities() {
        Iterable<User> allUsers = getAll();
        Set<User> userSet = new HashSet<>();

        List<List<User>> allCommunities = new ArrayList<>();

        for (User user : allUsers) {
            if (!userSet.contains(user)) {
                allCommunities.add(DFS(user, userSet));
            }
        }

        return allCommunities;
    }

    /**
     * Algoritmul lui Lee.
     *
     * @param userSource: nodul sursa
     * @param userSet:    un set pentru a tine cont de nodurile prin care am trecut deja
     * @return cea mai lunga cale de la nodul sursa
     */
    private int lee(User userSource, Set<User> userSet) {
        int max = -1;
        for (User u : userSource.getFriends()) {
            if (!userSet.contains(u)) {
                userSet.add(u);
                int l = lee(u, userSet);
                if (l > max) {
                    max = l;
                }
                userSet.remove(u);
            }
        }
        return max + 1;
    }

    /**
     * Wrapper pentru algoritmul lui Lee.
     *
     * @param userSource: nodul sursa
     * @return cea mai lunga cale de la nodul sursa, folosind algoritmul lui Lee
     */
    private int longestPathFromSource(User userSource) {
        Set<User> userSet = new HashSet<>();
        return lee(userSource, userSet);
    }

    /**
     * Calculeaza cea mai lunga cale pentru fiecare din nodurile prezente in graf.
     *
     * @param nodes: lista nodurilor din graf
     * @return cel mai lung drum din graf
     */
    private int longestPath(List<User> nodes) {
        int max = 0;
        for (User u : nodes) {
            int l = longestPathFromSource(u);
            if (l > max) {
                max = l;
            }
        }
        return max;
    }

    /**
     * Determina cea mai sociabila comunitate din retea.
     *
     * @return un Iterable cu cele mai sociabile comunitati
     */
    public List<Iterable<User>> mostSociableCommunity() {
        List<Iterable<User>> userList = new ArrayList<>();
        Iterable<User> users = userRepo.findAll();
        Set<User> userSet = new HashSet<>();

        int max = -1;
        for (User u : users) {
            if (!userSet.contains(u)) {
                List<User> aux = DFS(u, userSet); // determin comunitatea~componenta conexa
                int l = longestPath(aux);
                if (l > max) {
                    userList = new ArrayList<>();
                    userList.add(aux);
                    max = l;
                } else if (l == max) {
                    userList.add(aux);
                }
            }
        }

        return userList;
    }

    /**
     * Returneaza user-ul cu id-ul dat.
     *
     * @param id: un id
     * @return un obiect de tipul User cu id-ul identic cu cel dat/ null, daca nu exista
     */
    public Optional<User> getUserByID(Long id) {
        /*
        Iterable<User> allUsers = getAll();

        for (User u : allUsers) {
            if (u.getId().equals(id))
                return u;
        }

        return null;
        */

        Iterable<User> userIterable = getAll();
        Stream<User> userStream = StreamSupport.stream(userIterable.spliterator(), false);

        return userStream.filter(user -> user.getId().equals(id)).findFirst();
    }

    /**
     * Returneaza o lista cu userii care au cel putin n prieteni.
     *
     * @param n: numar intreg pozitiv
     * @return lista cu userii care au cel putin n prieteni
     */
    public List<User> atLeastNFriends(int n) {
        Iterable<User> userIterable = getAll();

        Predicate<User> areCelPutinNPrieteni = user -> user.getFriends().size() >= n;

        return StreamSupport.stream(userIterable.spliterator(), false)
                .filter(areCelPutinNPrieteni)
                .toList();
    }

    /**
     * Returneaza o lista cu userii ce contin un string dat in numele sau in prenumele lor.
     * @param string un string dat
     * @return o lista de useri ce indeplinesc conditia data
     */
    public List<User> usersWithContainedString(String string){
        Iterable<User> userIterable = getAll();

        Predicate<User> contineString = user -> user.getFirstName().toLowerCase().contains(string) || user.getLastName().toLowerCase().contains(string);

        return StreamSupport.stream(userIterable.spliterator(), false)
                            .filter(contineString)
                            .toList();
    }

    /**
     * @return toate mesajele existente
     */
    public Iterable<Message> getAllMessages(){
        return messageRepo.findAll();
    }

    /**
     * Adauga un mesaj
     * @param message: mesajul de adaugat
     * @return Optional.of(messaje) daca s-a adaugat cu succes, Optional.empty() altfel
     */
    public Optional<Message> addMessage(Message message){
        return messageRepo.save(message);
    }

    /**
     * Returneaza un Iterable cu mesajele dintre 2 useri.
     * @param user1: un User
     * @param user2: un User
     * @return un Iterable cu mesajele dintre 2 useri
     */
    public Iterable<Message> getChatForUsers(User user1, User user2) {
        // down cast
        MessageDBRepository messageDBRepository =  (MessageDBRepository) messageRepo;
        return messageDBRepository.findChatForUsers(user1.getId(), user2.getId());
    }

    /**
     *
     * @param username: username-ul unui user
     * @return o lista cu username-urile prietenilor Userului cu username-ul dat
     */
    public List<String> getUsernameForFriends(String username){
        Optional<User> user = getUserByUserName(username);
        List<String> friendsUsernames = null;

        if(user.isPresent()){
            UserDBRepository userDBRepository = (UserDBRepository) userRepo;
            Set<Tuple<User, Date>> friends = userDBRepository.getFriends(user.get().getId());

            friendsUsernames = new ArrayList<>();

            for(var f:friends){
                friendsUsernames.add(f.getE1().getUserName());
            }
        }

        return friendsUsernames;
    }

    /**
     * @param username: username-ul unui User
     * @return o lista cu persoanele care nu sunt prieteni cu Userul cu username-ul dat
     */
    public List<String> getUsernameForNewPeople(String username){
        List<String> friends = getUsernameForFriends(username);
        List<String> newPeople = new ArrayList<>();

        if(friends!=null){
            Iterable<User> allUsers = getAll();
            newPeople = StreamSupport.stream(allUsers.spliterator(), false)
                    .filter(user -> !user.getUserName().equals(username) && !friends.contains(user.getUserName()))
                    .map(User::getUserName)
                    .toList();
            return newPeople;
        }
        else{
            return null;
        }
    }

    public Optional<Message> sendMessage(User sender, User receiver, String description){
        Message message = new Message(sender.getId(), receiver.getId(), description);

        return messageRepo.save(message);
    }
}
