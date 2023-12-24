package ro.ubbcluj.map.socialnetworkgui.repository.database;

import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.UserValidator;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.repository.Repository;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UserDBRepository implements Repository<Long, User> {
    protected String url;
    protected String sqlUsername;
    protected String sqlPassword;
    UserValidator userValidator;

    public UserDBRepository(String url, String sqlUsername, String sqlPassword, UserValidator userValidator) {
        this.url = url;
        this.sqlUsername = sqlUsername;
        this.sqlPassword = sqlPassword;
        this.userValidator = userValidator;
    }

    @Override
    public Optional<User> findOne(Long aLong) {
        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement("select * from users "+
                    "where id = ?")
        ){
            statement.setLong(1, aLong);

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");

                User user = new User(firstName, lastName, username, password);

                user.setId(aLong);

                int noFriends = getNoFriends(aLong);
                user.setNoFriends(noFriends);

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
        Set<User> allUsers = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
             PreparedStatement statement = connection.prepareStatement("select * from users");
             ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");

                User user = new User(firstName, lastName, username, password);
                user.setId(id);

                int noFriends = getNoFriends(id);
                user.setNoFriends(noFriends);

                allUsers.add(user);
            }
            return allUsers;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> save(User entity) {
        Optional<User> u = findOneByUsername(entity.getUserName());

        userValidator.validate(entity);

        if(u.isPresent()){
            throw new ValidationException("Acest user exista deja!");
        }

        String insertSQL = "insert into users (first_name, last_name, username, password) values(?,?,?,?)";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername,sqlPassword);
            PreparedStatement statement = connection.prepareStatement(insertSQL)){
            statement.setString(1, entity.getFirstName());
            statement.setString(2,entity.getLastName());
            statement.setString(3,entity.getUserName());
            statement.setString(4,entity.getPassword());

            int response = statement.executeUpdate();

            return response == 0 ? Optional.empty() : Optional.of(entity);
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> delete(Long aLong) {
        if(aLong == null){
            throw new IllegalArgumentException("ID cannot be null!");
        }

        String deleteSQL="delete from users where id=?";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement(deleteSQL)){
            statement.setLong(1,aLong);

            Optional<User> foundUser = findOne(aLong);

            int response = 0;
            if(foundUser.isPresent()){
                response = statement.executeUpdate();
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

            return response == 0 ?  Optional.of(entity) : Optional.empty();
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }
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
                        "where f.id_friend1 = ? and f.status='accepted'" +
                        "union " +
                        "select f.id_friend1, u.*, f.friends_from " +
                        "from friendships f left join users u on f.id_friend1 = u.id " +
                        "where f.id_friend2 = ? and f.status='accepted'";
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

    public int getNoFriends(Long id_friend){
        int noFriends = 0;

        String getNoFriendsSQL = "select count(*) from(\n" +
                                 "select f.id_friend2, u.*, f.friends_from\n" +
                                 "from friendships f left join users u on f.id_friend2 = u.id\n" +
                                 "where f.id_friend1 = ?\n" +
                                 "union\n" +
                                 "select f.id_friend1, u.*, f.friends_from\n" +
                                 "from friendships f left join users u on f.id_friend1 = u.id\n" +
                                 "where f.id_friend2 = ?\n" +
                                 ")";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement(getNoFriendsSQL)){
            statement.setInt(1, id_friend.intValue());
            statement.setInt(2, id_friend.intValue());

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                noFriends = resultSet.getInt("count");
            }

            return noFriends;
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }
}
