package ro.ubbcluj.map.socialnetworkgui.repository.database;

import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.Validator;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class UserDBRepository extends AbstractDBRepository<Long, User>{

    public UserDBRepository(String url, String sqlUsername, String sqlPassword, Validator<User> userValidator) {
        super(url, sqlUsername, sqlPassword, userValidator);
    }

    @Override
    public void loadData() {
        try (Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
             PreparedStatement statement = connection.prepareStatement("select * from users");
             ResultSet resultSet = statement.executeQuery()
        ){
            while(resultSet.next()){
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String username = resultSet.getString("username");

                User user = new User(firstName, lastName, username);
                user.setId(id);

                // salvare si in memorie
                super.save(user);
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> findOne(Long longID) {
        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement("select * from users "+
                    "where id = ?")
        ){
            statement.setLong(1, longID);

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String username = resultSet.getString("username");

                User user = new User(firstName, lastName, username);

                user.setId(longID);

                return Optional.of(user);
            }
            return Optional.empty();
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<User> findAll() {
//        Set<User> allUsers = new HashSet<>();
//
//        try (Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
//             PreparedStatement statement = connection.prepareStatement("select * from users");
//             ResultSet resultSet = statement.executeQuery()
//        ){
//            while(resultSet.next()){
//                Long id = resultSet.getLong("id");
//                String firstName = resultSet.getString("first_name");
//                String lastName = resultSet.getString("last_name");
//                String username = resultSet.getString("username");
//
//                User user = new User(firstName, lastName, username);
//                user.setId(id);
//
//                allUsers.add(user);
//            }
//            return allUsers;
//        }catch(SQLException e){
//            throw new RuntimeException(e);
//        }
        return super.findAll();
    }

    /**
     * Returneaza id-ul entitatii din baza de date.
     * @param entity: entitatea pentru care dorim sa aflam id-ul
     * @return id-ul entitatii/ Optional.empty() daca aceasta nu exista in baza de date
     */
    private Optional<Long> getIDFromDB(User entity){
        String getSQL = "select id from users where first_name=? and last_name=? and username=?";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
        PreparedStatement statement = connection.prepareStatement(getSQL)){
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setString(3, entity.getUserName());

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                Long id = resultSet.getLong("id");

                return Optional.of(id);
            }
            return Optional.empty();
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> save(User entity) {
        Optional<User> u = findOneByUsername(entity.getUserName());

        validator.validate(entity);

        if(u.isPresent()){
            throw new ValidationException("Acest user exista deja!");
        }

        String insertSQL = "insert into users (first_name, last_name, username) values(?,?,?)";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername,sqlPassword);
        PreparedStatement statement = connection.prepareStatement(insertSQL)){
            statement.setString(1, entity.getFirstName());
            statement.setString(2,entity.getLastName());
            statement.setString(3,entity.getUserName());

            int response = statement.executeUpdate();

            // de scos user-ul din DB + setId inainte de salvare in memory
            if(response != 0){
                Optional<Long> id = getIDFromDB(entity);

                if(id.isPresent()){
                    entity.setId(id.get());
                    // salvare si in memorie
                    super.save(entity);
                }
                else{
                    return Optional.empty();
                }
            }

            return Optional.empty();
            //return response == 0 ? Optional.empty() : Optional.of(entity);
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> delete(Long longID) {
        if(longID == null){
            throw new IllegalArgumentException("ID cannot be null!");
        }

        String deleteSQL="delete from users where id=?";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement(deleteSQL)){
            statement.setLong(1,longID);

            Optional<User> foundUser = findOne(longID);

            int response = 0;
            if(foundUser.isPresent()){

                response = statement.executeUpdate();

                // stergere si din memorie pentru lista de prieteni a fiecarui user
                Iterable<User> users = super.findAll();
                for(User u:users){
                    if(!u.getId().equals(longID)){
                        if(u.getFriends().contains(foundUser.get())){
                            u.deleteFriend(foundUser.get());
                            super.update(u);
                        }
                    }
                }
                super.delete(longID);
            }

            return response == 0 ? Optional.empty() : foundUser;
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> update(User entity) {
        if(entity == null){
            throw new IllegalArgumentException("Entity can't be null!");
        }

        Optional<User> beforeUpdate = findOne(entity.getId());

        if(beforeUpdate.isEmpty()){
            throw new ValidationException("Nu exista acest user!");
        }

        String updateSQL = "update users set first_name=?, last_name=?, username=? where id=?";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement(updateSQL)){
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setString(3, entity.getUserName());
            statement.setLong(4, entity.getId());

            int response = statement.executeUpdate();

            // + in memorie!!
            if(response != 0){
                Optional<User> updated = super.update(entity);

                if(updated.isEmpty()){
                    Iterable<User> users = super.findAll();
                    for(User u:users){
                        if(!u.getId().equals(entity.getId())){
                            if(u.getFriends().contains(beforeUpdate.get())){
                                u.deleteFriend(beforeUpdate.get());
                                u.addFriend(entity);
                                super.update(u);
                            }
                        }
                    }
                }

            }


            return response == 0 ?  Optional.of(entity) : Optional.empty();
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Returneaza user-ul cu username-ul dat.
     * @param username: string ce reprezinta username-ul unui user
     * @return userul cu username-ul dat sau Optional.empty() daca nu exista
     */

    public Optional<User> findOneByUsername(String username) {
        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement("select * from users "+
                    "where username = ?")
        ){
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                Long id = resultSet.getLong("id");

                User user = new User(firstName, lastName, username);

                user.setId(id);

                return Optional.of(user);
            }
            return Optional.empty();
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Varianta alternativa direct din baza de date - nu este utilizata in proiect.
     * Returneaza prietenii unui user.
     * @param id_friend: id-ul unui user
     * @return un set cu prietenii unui user si cu data in care acestia si-au format prietenia.
     */
    public Set<Tuple<User,Date>> getFriends(Long id_friend){
        Set<Tuple<User,Date>> friends = new HashSet<>();

        String getFriendsSQL =
                "select f.id_friend2, u.*, f.friends_from " +
                "from friendships f left join users u on f.id_friend2 = u.id " +
                "where f.id_friend1 = ? " +
                "union " +
                "select f.id_friend1, u.*, f.friends_from " +
                "from friendships f left join users u on f.id_friend1 = u.id " +
                "where f.id_friend2 = ?";
        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement(getFriendsSQL)){
            statement.setInt(1, id_friend.intValue());
            statement.setInt(2, id_friend.intValue());

            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()){
                Long id = resultSet.getLong("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String username = resultSet.getString("username");

                Date data = resultSet.getDate("friends_from");

                User user = new User(first_name, last_name, username);
                user.setId(id);

                Tuple<User, Date> tuplu = new Tuple<>(user, data);
                friends.add(tuplu);
            }

            return friends;
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }
}
