package ro.ubbcluj.map.socialnetworkgui.repository.database;

import ro.ubbcluj.map.socialnetworkgui.domain.Entity;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.Validator;
import ro.ubbcluj.map.socialnetworkgui.repository.InMemoryRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractDBRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> {
    protected String url;
    protected String sqlUsername;
    protected String sqlPassword;
    public AbstractDBRepository(String url, String sqlUsername, String sqlPassword, Validator<E> validator) {
        super(validator);

        this.url = url;
        this.sqlUsername = sqlUsername;
        this.sqlPassword = sqlPassword;

        loadData();
    }

    /**
     * Incarca datele din baza de date in memorie.
     */
    public abstract void loadData();

}
