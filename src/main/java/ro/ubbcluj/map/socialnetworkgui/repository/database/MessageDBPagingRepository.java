package ro.ubbcluj.map.socialnetworkgui.repository.database;

import ro.ubbcluj.map.socialnetworkgui.domain.Message;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

public class MessageDBPagingRepository extends MessageDBRepository implements PagingRepository<Long , Message> {
    Page<Message> messagePage;
    public MessageDBPagingRepository(String url, String sqlUsername, String sqlPassword) {
        super(url, sqlUsername, sqlPassword);

        Pageable pageable = new PageableImplementation(1,10);
        messagePage = new PageImplementation<>(pageable, findAll(pageable).getContent());
    }

    /**
     * Returneaza o pagina de mesaje.
     * @param pageable
     * @return
     */
    @Override
    public Page<Message> findAll(Pageable pageable) {
        String getAllSQL = "select * from messages\n" +
                "limit ?\n" +
                "offset ? * (?-1)";

        Set<Message> allMessages = new HashSet<>();


        try (Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
             PreparedStatement statement = connection.prepareStatement(getAllSQL)){
            statement.setInt(1, pageable.getPageSize());
            statement.setInt(2, pageable.getPageSize());
            statement.setInt(3, pageable.getPageNumber());

            ResultSet resultSet = statement.executeQuery();

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

            Stream<Message> messageStream = allMessages.stream();

            return new PageImplementation<>(pageable, messageStream);


        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * @param pageSize: cate mesaje/pagina
     * @return numarul de pagini
     */
    public int getNoPages(Integer pageSize){
        messagePage.getPageable().setPageSize(pageSize);

        String getNoSQL = "select (\n" +
                "\t(Count(*) + ? - 1)/?\n" +
                "\t) as no_pages from messages";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement(getNoSQL)){
            int pageSize1 = messagePage.getPageable().getPageSize();

            statement.setInt(1, pageSize1);
            statement.setInt(2, pageSize1);

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                return resultSet.getInt("no_pages");
            }

            return -1;

        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * @param pageNo: numarul paginii
     * @param pageSize: cate mesaje/pagina
     * @return o pagina specifica
     */
    public Iterable<Tuple<Long, Long>> getPage(Long idUser, int pageNo, int pageSize){
        messagePage.getPageable().setPageNumber(pageNo);
        messagePage.getPageable().setPageSize(pageSize);

        return findUsernameChatsForUser(messagePage.getPageable(), idUser);
    }

    public Iterable<Tuple<Long, Long>> findUsernameChatsForUser(Pageable pageable, Long id1){
        List<Tuple<Long, Long>> allUsernames = new ArrayList<>();

        String selectSQL = "select distinct id_sender, id_receiver from\n" +
                "(\n" +
                "select m.id, m.id_sender, m.id_receiver, m.date, m.description from messages m\n" +
                "inner join users u on u.id=m.id_sender\n" +
                "where m.id_receiver=? or m.id_sender=?\n" +
                "order by date asc\n" +
                ")\n" +
                "limit ?\n" +
                "offset ?*(?-1)";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
            PreparedStatement statement = connection.prepareStatement(selectSQL)){

            statement.setInt(1, id1.intValue());
            statement.setInt(2, id1.intValue());

            statement.setInt(3, pageable.getPageSize());
            statement.setInt(4, pageable.getPageSize());
            statement.setInt(5, pageable.getPageNumber());

            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()){
                Long id_sender = resultSet.getLong("id_sender");
                Long id_receiver = resultSet.getLong("id_receiver");

                Tuple<Long, Long> tuple = new Tuple<>(id_sender, id_receiver);

                allUsernames.add(tuple);

            }
            return allUsernames;
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }
}
