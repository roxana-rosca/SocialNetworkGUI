package ro.ubbcluj.map.socialnetworkgui.repository.database;

import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.UserValidator;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.*;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class UserDBPagingRepository extends UserDBRepository implements PagingRepository<Long, User> {
    Page<User> userPage;


    public UserDBPagingRepository(String url, String sqlUsername, String sqlPassword, UserValidator userValidator) {
        super(url, sqlUsername, sqlPassword, userValidator);

        Pageable pageable = new PageableImplementation(1,5);
        userPage = new PageImplementation<>(pageable,findAll(pageable).getContent());
    }

    /**
     * Returneaza o pagina de useri.
     * @param pageable: Pageable
     * @return o pagina de useri
     */
    @Override
    public Page<User> findAll(Pageable pageable) {
        String getAllSQL = "select * from users\n" +
                           "limit ?\n" +
                           "offset ? * (?-1)";

        Set<User> allUsers = new HashSet<>();


        try (Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
        PreparedStatement statement = connection.prepareStatement(getAllSQL)){
            statement.setInt(1, pageable.getPageSize());
            statement.setInt(2, pageable.getPageSize());
            statement.setInt(3, pageable.getPageNumber());

            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()){
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

            Stream<User> userStream = allUsers.stream();

            return new PageImplementation<>(pageable, userStream);


        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * @param pageSize: cati useri/pagina
     * @return numarul de pagini
     */
    public int getNoPages(Integer pageSize){
        userPage.getPageable().setPageSize(pageSize);

        String getNoSQL = "select (\n" +
                "\t(Count(*) + ? - 1)/?\n" +
                "\t) as no_pages from users";

        try(Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
        PreparedStatement statement = connection.prepareStatement(getNoSQL)){
            int pageSize1 = userPage.getPageable().getPageSize();

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
     * @return pagina curenta de useri
     */
    public Page<User> getCurrentPage(){

        userPage.setContent(findAll(userPage.getPageable()).getContent());

        return this.userPage;
    }

    /**
     * @param pageNo: numarul paginii
     * @param pageSize: cati useri/pagina
     * @return o pagina specifica
     */
    public Page<User> getPage(int pageNo, int pageSize){
        userPage.getPageable().setPageNumber(pageNo);
        userPage.getPageable().setPageSize(pageSize);

        return findAll(userPage.getPageable());
    }

    /**
     * @return urmatoarea pagina
     */
    public Page<User> getNextPage(){
        userPage.setPageable(userPage.nextPageable());

        return findAll(userPage.getPageable());
    }



}
