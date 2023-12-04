package ro.ubbcluj.map.socialnetworkgui.repository.database;

import ro.ubbcluj.map.socialnetworkgui.domain.Message;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.MessageValidator;
import ro.ubbcluj.map.socialnetworkgui.repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

public class MessageDBRepository implements Repository<Long, Message> {
    private String url;
    private String sqlUsername;
    private String sqlPassword;
    private MessageValidator messageValidator;

    public MessageDBRepository(String url, String sqlUsername, String sqlPassword) {
        this.url = url;
        this.sqlUsername = sqlUsername;
        this.sqlPassword = sqlPassword;

        messageValidator = new MessageValidator();
    }

    @Override
    public Optional<Message> findOne(Long aLong) {
        return Optional.empty();
    }

    /**
     * Returneaza username-ul userului cu id-ul dat.
     * @param id_sender: un id
     * @return string
     */
    public Optional<String> getUsernameForSender(Long id_sender){
        String username;

        String selectSQL = "select distinct u.username from messages m\n" +
                           "inner join users u on u.id=m.id_sender\n" +
                           "where m.id_sender=?";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement(selectSQL)){
            statement.setLong(1, id_sender);

            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                username = resultSet.getString("username");
                return Optional.of(username);
            }
            return Optional.empty();
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Message> findAll() {
        Set<Message> allMessages = new HashSet<>();

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement("select * from messages");
            ResultSet resultSet = statement.executeQuery()){
            while(resultSet.next()){
                Long id = resultSet.getLong("id");
                Long id_sender = resultSet.getLong("id_sender");
                Long id_receiver = resultSet.getLong("id_receiver");
                String description = resultSet.getString("description");

                Timestamp timestamp = resultSet.getTimestamp("date");
                LocalDateTime localDateTime = null;
                if(timestamp != null){
                    ZoneId zoneId = ZoneId.of("Europe/Bucharest");
                    ZonedDateTime zonedDateTime = timestamp.toInstant().atZone(zoneId);

                    localDateTime = zonedDateTime.toLocalDateTime();
                }

                Message message = new Message(id_sender, id_receiver, description);

                message.setId(id);
                message.setDateTime(localDateTime);

                allMessages.add(message);
            }
            return allMessages;
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> save(Message entity) {
        messageValidator.validate(entity);

        String insertSQL = "insert into messages(id_sender, id_receiver, date, description) values (?,?,?,?)";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement(insertSQL)){
            statement.setInt(1, entity.getIDSender().intValue());
            statement.setInt(2, entity.getIDReceiver().intValue());

            Timestamp timestamp = null;
            if(entity.getDateTime() != null){
                timestamp = new Timestamp(entity.getDateTime().toInstant(ZoneOffset.UTC).toEpochMilli());
            }
            statement.setTimestamp(3, timestamp);
            statement.setString(4, entity.getMessage());

            int response = statement.executeUpdate();


            return response == 0 ? Optional.empty() : Optional.of(entity);
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> delete(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<Message> update(Message entity) {
        return Optional.empty();
    }

    /**
     * Returneaza conversatiile dintre 2 useri in ordinea trimiterii mesajelor.
     * @param id1: id user 1
     * @param id2: id user 2
     * @return un iterable cu aceste mesaje
     */
    public Iterable<Message> findChatForUsers(Long id1, Long id2){
//        Set<Message> allMessages = new HashSet<>();
        List<Message> allMessages = new ArrayList<>();

        String selectSQL = "select id, id_sender, id_receiver, date, description from\n" +
                "(\n" +
                "\tselect m.id, m.id_sender, m.id_receiver, m.date, m.description from messages m\n" +
                "\tinner join users u on u.id=m.id_sender\n" +
                "\twhere m.id_receiver=? and m.id_sender=?\n" +
                "\tunion\n" +
                "\tselect m.id, m.id_sender, m.id_receiver, m.date, m.description from messages m\n" +
                "\tinner join users u on u.id=m.id_receiver\n" +
                "\twhere m.id_sender=? and m.id_receiver=?\n" +
                ")\n" +
                "order by date asc";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement(selectSQL)){

            statement.setInt(1, id1.intValue());
            statement.setInt(2, id2.intValue());
            statement.setInt(3, id1.intValue());
            statement.setInt(4, id2.intValue());

            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()){
                Long id_sender = resultSet.getLong("id_sender");
                Long id_receiver = resultSet.getLong("id_receiver");
                String description = resultSet.getString("description");
                Long id = resultSet.getLong("id");

                Timestamp timestamp = resultSet.getTimestamp("date");
                LocalDateTime localDateTime = null;
                if(timestamp != null){
                    ZoneId zoneId = ZoneId.of("Europe/Bucharest");
                    ZonedDateTime zonedDateTime = timestamp.toInstant().atZone(zoneId);

                    localDateTime = zonedDateTime.toLocalDateTime();
                }

                Message message = new Message(id_sender, id_receiver, description);
                message.setId(id);
                message.setDateTime(localDateTime);

                // setare username sender
                Optional<String> username_sender = getUsernameForSender(id_sender);
                username_sender.ifPresent(message::setUsernameSender);

                allMessages.add(message);

            }
            return allMessages;
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }
}

