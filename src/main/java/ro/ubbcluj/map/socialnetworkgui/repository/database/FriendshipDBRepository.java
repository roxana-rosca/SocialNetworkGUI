package ro.ubbcluj.map.socialnetworkgui.repository.database;

import ro.ubbcluj.map.socialnetworkgui.domain.Friendship;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.Validator;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class FriendshipDBRepository extends AbstractDBRepository<Tuple<Long, Long>, Friendship> {
    public FriendshipDBRepository(String url, String sqlUsername, String sqlPassword, Validator<Friendship> friendshipValidator) {
        super(url, sqlUsername, sqlPassword, friendshipValidator);
    }

    @Override
    public void loadData() {
        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement("select * from friendships");
            ResultSet resultSet = statement.executeQuery()
        ){
            while(resultSet.next()){
                Long id = resultSet.getLong("id");
                Long id_friend1 = resultSet.getLong("id_friend1");
                Long id_friend2 = resultSet.getLong("id_friend2");
                LocalDate friends_from = resultSet.getDate("friends_from").toLocalDate();

                Friendship friendship = new Friendship(friends_from);
                friendship.setId(new Tuple<>(id_friend1, id_friend2));

                super.save(friendship);
            }
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Friendship> findOne(Tuple<Long, Long> longLongTuple) {
        try(Connection connection = DriverManager.getConnection(url,sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement("select * from friendships "+
                    "where id_friend1=? and id_friend2=? " + "union " + "select * from friendships "+
                    "where id_friend1=? and id_friend2=?")){
            String idS1 = String.valueOf(longLongTuple.getLeft());
            String idS2 = String.valueOf(longLongTuple.getRight());

            long id_friend1Long = Long.parseLong(idS1);
            long id_friend2Long = Long.parseLong(idS2);

            statement.setLong(1, id_friend1Long);
            statement.setLong(2, id_friend2Long);

            statement.setLong(3, id_friend2Long);
            statement.setLong(4, id_friend1Long);

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                String id_friend1 = resultSet.getString("id_friend1");
                String id_friend2 = resultSet.getString("id_friend2");
                LocalDate date = resultSet.getDate("friends_from").toLocalDate();

                Friendship friendship = new Friendship(date);
                Tuple<Long, Long> id = new Tuple(id_friend1,id_friend2);
                friendship.setId(id);

                return Optional.of(friendship);
            }
            return Optional.empty();
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Friendship> findAll() {
        /*Set<Friendship> friendships = new HashSet<>();

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement("select * from friendships");
            ResultSet resultSet = statement.executeQuery()
        ){
            while(resultSet.next()){
                Long id = resultSet.getLong("id");
                Long id_friend1 = resultSet.getLong("id_friend1");
                Long id_friend2 = resultSet.getLong("id_friend2");
                LocalDate friends_from = resultSet.getDate("friends_from").toLocalDate();

                Friendship friendship = new Friendship(friends_from);
                friendship.setId(new Tuple<>(id_friend1, id_friend2));
                friendships.add(friendship);
            }

            return friendships;
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }*/

        return super.findAll();
    }

    @Override
    public Optional<Friendship> save(Friendship entity) {
        Tuple<Long, Long> tuple = new Tuple<>(entity.getId().getLeft(), entity.getId().getRight());
        Optional<Friendship> f = findOne(tuple);

        if(f.isPresent()){
            throw new ValidationException("Aceasta prietenie exista deja!");
        }

        String insertSQL = "insert into friendships(id_friend1,id_friend2,friends_from) values (?,?,?)";
        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
        PreparedStatement statement = connection.prepareStatement(insertSQL)){
            statement.setInt(1, entity.getId().getLeft().intValue());
            statement.setInt(2, entity.getId().getRight().intValue());
            statement.setDate(3, Date.valueOf(entity.getDate()));

            int response = statement.executeUpdate();

            // salvare prietenie in memorie
            if(response!=0){
                super.save(entity);
            }

            return Optional.empty();
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Friendship> delete(Tuple<Long, Long> longLongTuple) {
        if(longLongTuple == null){
            throw new ValidationException("ID-ul nu poate sa fie null!");
        }
        Optional<Friendship> f = findOne(longLongTuple);
//
//        if(f.isEmpty()){
//            throw new ValidationException("Aceasta prietenie nu exista!");
//        }

        String deleteSQL = "delete from friendships where id_friend1 = ? and id_friend2 = ?";
        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
        PreparedStatement statement = connection.prepareStatement(deleteSQL)
        ){
            String idS1 = String.valueOf(longLongTuple.getLeft());
            String idS2 = String.valueOf(longLongTuple.getRight());

            long id_friend1 = Long.parseLong(idS1);
            long id_friend2 = Long.parseLong(idS2);

            statement.setLong(1, id_friend1);
            statement.setLong(2, id_friend2);

            int response = statement.executeUpdate();

            if(response != 0){
                Tuple<Long, Long> deSters = new Tuple<>(id_friend1,id_friend2);
                super.delete(deSters);
                return f;
            }

            return Optional.empty();
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        return Optional.empty();
    }
}
