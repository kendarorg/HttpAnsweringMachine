package org.kendar.cucumber;

import io.cucumber.java.en.Given;
import org.kendar.ham.ExtraParam;
import org.kendar.ham.HamException;
import org.kendar.janus.JdbcDriver;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DbReplayerStates extends BaseStates {
    private ResultSet resultSet;
    private int recordsCount;

    @Given("I add a db proxy from {string} to localH2Databse")
    public void i_add_a_db_proxy_from_to_local_h2databse(String dbName) throws HamException {
        var proxy = hamBuilder.proxies().retrieveDbProxies().stream().filter(a->
                        a.getExposed().getConnectionString().equalsIgnoreCase(dbName))
                .findFirst();
        if(proxy.isPresent()) {
            hamBuilder.proxies().removeDbProxy(proxy.get().getId());
        }

        var connectionString = hamBuilder
                .proxies()
                .addRemoteDbProxy("jdbc:h2:tcp://localhost/ham;MODE=MYSQL;",
                        "sa","sa","org.h2.Driver")
                .asLocal(dbName,"login","password");

        assertEquals("jdbc:janus:http://www.local.test/api/db/"+dbName,connectionString);

    }
    @Given("user set parameter {string} to {string}")
    public void user_set_parameter_to(String id, String value) {
        // Write code here that turns the phrase above into concrete actions
        recordingId.withParameter(ExtraParam.fromString(id),value);
    }
    @Given("user execute query on {string} with {string}")
    public void user_execute_query_on_with(String dbName, String query) throws SQLException {
        DriverManager.registerDriver(new JdbcDriver());
        var connection = DriverManager.getConnection("jdbc:janus:http://localhost/api/db/" + dbName);
        connection.setAutoCommit(true);
        var statement = connection.prepareStatement(query);
        resultSet = statement.executeQuery();
        recordsCount = 0;
        while(resultSet.next()){
            recordsCount++;
        }
        resultSet.close();
        statement.close();
        connection.close();
    }
    @Given("user execute update query on {string} with {string}")
    public void user_execute_update_query_on_with(String dbName, String query) throws SQLException {
        DriverManager.registerDriver(new JdbcDriver());
        var connection = DriverManager.getConnection("jdbc:janus:http://localhost/api/db/" + dbName);
        var statement = connection.prepareStatement(query);
        assertFalse(statement.execute());
        connection.close();
    }
    @Given("should find {string} record")
    public void should_find_record(String count) {
        // Write code here that turns the phrase above into concrete actions
        assertEquals(recordsCount,Integer.parseInt(count));
    }


}
