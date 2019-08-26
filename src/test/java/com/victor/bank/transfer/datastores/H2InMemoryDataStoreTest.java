package com.victor.bank.transfer.datastores;

import com.victor.bank.transfer.configs.H2InMemoryConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

class H2InMemoryDataStoreTest {

    private static final String testTable = "CREATE TABLE test (id INT IDENTITY PRIMARY KEY, str TEXT);";
    private static final String insetValues = "INSERT INTO test(id, str) values (1, 'aa'), (2, 'bb'), (3, 'cc'), (4, 'dd');";
    private static final String connectionUrl = "jdbc:h2:mem:";
    private DataStore datastore;

    @BeforeEach
    void setUp() {
        datastore = new H2InMemoryDataStore(
            H2InMemoryConfig.of(connectionUrl, 20, List.of(testTable, insetValues)));
    }

    @AfterEach
    void tearDown() {
        try {
            datastore.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void withConnection() {
        final Integer queryDB = datastore.withConnection(connection -> {
            try (final Statement stm = connection.createStatement();
                 final ResultSet rs = stm.executeQuery("Select 1 + 1;")) {
                rs.next();
                return rs.getInt(1);
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
            }
            return null;
        }).join();
        assertThat(queryDB, is(2));
    }

    @Test
    void runQuery() {
        final List<String> elements = datastore.runQuery("SELECT id, str FROM test WHERE id != ? ;", preparedStatement -> {
            try {
                preparedStatement.setInt(1, 3);
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
            }
            return preparedStatement;
        }, rs -> {
            try {
                final String str = rs.getString("str");
                return Optional.of(str);
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
                return Optional.empty();
            }
        }).join();
        assertThat(elements, hasSize(3));
        assertThat(elements, contains("aa", "bb", "dd"));
    }

    @Test
    void executeInsert() {
        final List<Integer> ids = datastore.executeInsert("INSERT INTO test(str) VALUES ? ; ", preparedStatement -> {
            try {
                preparedStatement.setString(1, "ee");
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
            }
            return preparedStatement;
        }, rs -> {
            try {
                return Optional.of(rs.getInt("id"));
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
                return Optional.empty();
            }
        }).join();
        assertThat(ids, hasSize(1));
        assertThat(ids, contains(5));
    }

    @Test
    void executeUpdate() {
        final String newStr = "ee";
        final int rowsAffected = datastore.executeUpdate("UPDATE test SET str = ? WHERE id = ? ; ", preparedStatement -> {
            try {
                preparedStatement.setString(1, newStr);
                preparedStatement.setInt(2, 1);
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
            }
            return preparedStatement;
        }).join();
        final Optional<String> strOpt = datastore.runQuery("SELECT str FROM test WHERE id = ? ;", preparedStatement -> {
            try {
                preparedStatement.setInt(1, 1);
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
            }
            return preparedStatement;
        }, rs -> {
            try {
                return Optional.of(rs.getString("str"));
            } catch (SQLException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }).thenApply((List<String> l) -> {
            if (l.size() != 1) {
                // log size is wrong
                return Optional.<String>empty();
            }
            return Optional.of(l.get(0));
        }).join();
        assertThat(rowsAffected, is(1));
        assertThat(strOpt, isPresentAndIs(newStr));
    }

    @Test
    void executeDelete() {
        final int rowsAffected = datastore.executeUpdate("DELETE FROM test WHERE id = ? ; ", preparedStatement -> {
            try {
                preparedStatement.setInt(1, 1);
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
            }
            return preparedStatement;
        }).join();
        final Optional<String> strOpt = datastore.runQuery("SELECT * FROM test WHERE id = ? ;", preparedStatement -> {
            try {
                preparedStatement.setInt(1, 1);
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
            }
            return preparedStatement;
        }, rs -> {
            try {
                return Optional.of(rs.getString("str"));
            } catch (SQLException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }).thenApply((List<String> l) -> {
            if (l.size() != 1) {
                // log size is wrong
                return Optional.<String>empty();
            }
            return Optional.of(l.get(0));
        }).join();
        assertThat(rowsAffected, is(1));
        assertThat(strOpt, isEmpty());
    }
}