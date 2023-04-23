package org.kendar.replayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kendar.janus.JdbcResultSet;
import org.kendar.janus.JdbcStatement;
import org.kendar.janus.cmd.connection.ConnectionConnect;
import org.kendar.janus.cmd.connection.ConnectionCreateStatement;
import org.kendar.janus.enums.ResultSetConcurrency;
import org.kendar.janus.enums.ResultSetHoldability;
import org.kendar.janus.enums.ResultSetType;
import org.kendar.janus.results.ObjectResult;
import org.kendar.janus.results.RemainingResultSetResult;
import org.kendar.janus.results.StatementResult;
import org.kendar.janus.serialization.JsonTypedSerializer;
import org.kendar.replayer.engine.ReplayerEngine;
import org.kendar.replayer.engine.ReplayerResult;
import org.kendar.replayer.engine.db.DbReplayer;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerRow;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DbReplayerTest {
    protected ObjectResult newObjectResult(Object val) {
        var result = new ObjectResult();
        result.setResult(val);
        return result;
    }

    ObjectMapper mapper = new ObjectMapper();

    private JsonTypedSerializer serializer = new JsonTypedSerializer();

    private ReplayerResult extractRecordingFromFile(FakeDbReplayer target, String path) throws IOException {
        var data = new String(getClass()
                .getClassLoader()
                .getResourceAsStream(path).readAllBytes());
        var replayerResult = mapper.readValue(data, ReplayerResult.class);

        var sortedIndexes = replayerResult.getIndexes().stream().sorted(
                Comparator.comparingLong(CallIndex::getId)).collect(Collectors.toList());
        replayerResult.setIndexes(sortedIndexes);
        var sortedReplayerRow = replayerResult.getStaticRequests().stream().sorted(
                Comparator.comparingLong(ReplayerRow::getId)).collect(Collectors.toList());
        replayerResult.setStaticRequests(sortedReplayerRow);
        sortedReplayerRow = replayerResult.getDynamicRequests().stream().sorted(
                Comparator.comparingLong(ReplayerRow::getId)).collect(Collectors.toList());
        replayerResult.setDynamicRequests(sortedReplayerRow);

        for (var callIndex : replayerResult.getIndexes()) {
            target.callIndexMap.put(callIndex.getId(), callIndex);
        }

        for (var replayerRow : replayerResult.getStaticRequests()) {
            target.replayerRowMap.put(replayerRow.getId(), replayerRow);
        }
        for (var replayerRow : replayerResult.getDynamicRequests()) {
            target.replayerRowMap.put(replayerRow.getId(), replayerRow);
        }

        return replayerResult;
    }

    private JdbcStatement newJdbcStatement() {
        return new JdbcStatement(null, null, 1L, 0, 0,
                ResultSetType.FORWARD_ONLY, ResultSetConcurrency.CONCUR_READ_ONLY, ResultSetHoldability.DEFAULT);
    }

    private <T> T verifyStep(DbReplayer target, ReplayerResult result, int index, Class<T> resultClass,
                             String connectionId) throws Exception {
        var req = result.getDynamicRequests().get(index).getRequest();
        if (connectionId != null) {
            req.getHeaders().put("x-connection-id", connectionId);
        }
        var requestMatch = target.findRequestMatch(req, "XXX", new HashMap<>());
        var res = requestMatch.getFoundedRes();
        Object item = new ObjectResult();
        if (res.getResponseText() != null) {
            var dser = serializer.newInstance();
            dser.deserialize(res.getResponseText());
            item = dser.read("result");
        }
        Assertions.assertEquals(resultClass, item.getClass());
        return (T) item;
    }

    private void assertResEquals(String expectedPath, Object actual) throws IOException {
        var expectedData = new String(getClass()
                .getClassLoader()
                .getResourceAsStream(expectedPath).readAllBytes());
        var actualData = actual.toString();
        var eds = String.join("\n", Arrays.stream(expectedData.split("\n"))
                .sequential()
                .filter(d -> d.trim().length() > 0)
                .map(d -> d.stripTrailing()).collect(Collectors.toList()));
        var ads = String.join("\n", Arrays.stream(actualData.split("\n"))
                .filter(d -> d.trim().length() > 0)
                .sequential().map(d -> d.stripTrailing()).collect(Collectors.toList()));
        Assertions.assertEquals(eds, ads);
    }


    void test2() throws Exception {
        var sf = new FakeSessionFactory();
        var target = new FakeDbReplayer(sf);
        var connect = new ConnectionConnect();
        connect.setDatabase("test");
        ReqRespBuilder.create("test")
                .withCommand(connect)
                .withResult(newObjectResult(1L))
                .build(1L, target);

        var createStatement = new ConnectionCreateStatement();
        var statement = newJdbcStatement();
        ReqRespBuilder.create("test")
                .withCommand(createStatement)
                .withResult(newObjectResult(1L))
                .build(1L, target);
        ((ReplayerEngine) target).loadDb(1L);
    }

    @Test
    void testSimpleQuery() throws Exception {
        var sf = new FakeSessionFactory();
        var target = new FakeDbReplayer(sf);

        var result = extractRecordingFromFile(target, "./org/kendar/replayer/simplequery.json");
        ((ReplayerEngine) target).loadDb(1L);
//        var tree = target.getTree("local");
//        assertResEquals("./org/kendar/replayer/simplequery.tree",tree);

        var connectionResult = verifyStep(target, result, 0, ObjectResult.class, null);
        var connectionId = connectionResult.getResult().toString();
        var statementResult = verifyStep(target, result, 1, StatementResult.class, connectionId);
        Assertions.assertNotNull(statementResult);
        var jdbcResultSet = verifyStep(target, result, 2, JdbcResultSet.class, connectionId);
        Assertions.assertNotNull(jdbcResultSet);
        var retrieveRemaining = verifyStep(target, result, 3, RemainingResultSetResult.class, connectionId);
        Assertions.assertNotNull(retrieveRemaining);
        var close = verifyStep(target, result, 4, ObjectResult.class, connectionId);
        Assertions.assertNotNull(close);
    }

    @Test
    void testMixedQueryStandardFlow() throws Exception {
        var sf = new FakeSessionFactory();
        var target = new FakeDbReplayer(sf);

        var result = extractRecordingFromFile(target, "./org/kendar/replayer/mixedquery.json");
        ((ReplayerEngine) target).loadDb(1L);
//        var tree = target.getTree("local");
//        assertResEquals("./org/kendar/replayer/mixedquery.tree",tree);

        var connectionResult = verifyStep(target, result, 0, ObjectResult.class, null);
        var connectionId = connectionResult.getResult().toString();
        var statementResult = verifyStep(target, result, 1, StatementResult.class, connectionId);
        Assertions.assertNotNull(statementResult);
        var jdbcResultSet = verifyStep(target, result, 2, JdbcResultSet.class, connectionId);
        validateResultSetSize(jdbcResultSet, 3);
        var remainingResult = verifyStep(target, result, 3, RemainingResultSetResult.class, connectionId);
        assertTrue(remainingResult.isLastRow());
        verifyStep(target, result, 4, ObjectResult.class, connectionId);

        connectionResult = verifyStep(target, result, 5, ObjectResult.class, null);
        connectionId = connectionResult.getResult().toString();
        statementResult = verifyStep(target, result, 6, StatementResult.class, connectionId);
        Assertions.assertNotNull(statementResult);
        jdbcResultSet = verifyStep(target, result, 7, JdbcResultSet.class, connectionId);
        validateResultSetSize(jdbcResultSet, 1);
        remainingResult = verifyStep(target, result, 8, RemainingResultSetResult.class, connectionId);
        assertTrue(remainingResult.isLastRow());
        verifyStep(target, result, 9, ObjectResult.class, connectionId);


        connectionResult = verifyStep(target, result, 10, ObjectResult.class, null);
        connectionId = connectionResult.getResult().toString();
        statementResult = verifyStep(target, result, 11, StatementResult.class, connectionId);
        Assertions.assertNotNull(statementResult);
        jdbcResultSet = verifyStep(target, result, 12, JdbcResultSet.class, connectionId);
        validateResultSetSize(jdbcResultSet, 3);
        remainingResult = verifyStep(target, result, 13, RemainingResultSetResult.class, connectionId);
        assertTrue(remainingResult.isLastRow());
        verifyStep(target, result, 14, ObjectResult.class, connectionId);

        //assertThrows(Exception.class,()->verifyStep(target,result,0,ObjectResult.class,null));
    }

    @Test
    void testMixedQueryInverted() throws Exception {
        var sf = new FakeSessionFactory();
        var target = new FakeDbReplayer(sf);

        var result = extractRecordingFromFile(target, "./org/kendar/replayer/mixedquery.json");
        ((ReplayerEngine) target).loadDb(1L);
//        var tree = target.getTree("local");
//        assertResEquals("./org/kendar/replayer/mixedquery.tree",tree);

        var connectionResult = verifyStep(target, result, 0, ObjectResult.class, null);
        var connectionId = connectionResult.getResult().toString();
        var statementResult = verifyStep(target, result, 1, StatementResult.class, connectionId);
        Assertions.assertNotNull(statementResult);
        var jdbcResultSet = verifyStep(target, result, 2, JdbcResultSet.class, connectionId);
        validateResultSetSize(jdbcResultSet, 3);
        var remainingResult = verifyStep(target, result, 3, RemainingResultSetResult.class, connectionId);
        assertTrue(remainingResult.isLastRow());
        verifyStep(target, result, 4, ObjectResult.class, connectionId);


        connectionResult = verifyStep(target, result, 10, ObjectResult.class, null);
        connectionId = connectionResult.getResult().toString();
        statementResult = verifyStep(target, result, 11, StatementResult.class, connectionId);
        Assertions.assertNotNull(statementResult);
        jdbcResultSet = verifyStep(target, result, 12, JdbcResultSet.class, connectionId);
        validateResultSetSize(jdbcResultSet, 3);
        remainingResult = verifyStep(target, result, 13, RemainingResultSetResult.class, connectionId);
        assertTrue(remainingResult.isLastRow());
        verifyStep(target, result, 14, ObjectResult.class, connectionId);


        connectionResult = verifyStep(target, result, 5, ObjectResult.class, null);
        connectionId = connectionResult.getResult().toString();
        statementResult = verifyStep(target, result, 6, StatementResult.class, connectionId);
        Assertions.assertNotNull(statementResult);
        jdbcResultSet = verifyStep(target, result, 7, JdbcResultSet.class, connectionId);
        validateResultSetSize(jdbcResultSet, 1);
        remainingResult = verifyStep(target, result, 8, RemainingResultSetResult.class, connectionId);
        assertTrue(remainingResult.isLastRow());
        verifyStep(target, result, 9, ObjectResult.class, connectionId);

        //assertThrows(Exception.class,()->verifyStep(target,result,14,ObjectResult.class,null));
    }

    private void validateResultSetSize(JdbcResultSet jdbcResultSet, int expected) throws SQLException {
        Assertions.assertNotNull(jdbcResultSet);
        jdbcResultSet.setEngine(new NullEngine());
        jdbcResultSet.setConnection(new NullConnection());
        while (jdbcResultSet.next()) {
            expected--;
        }
        Assertions.assertEquals(0, expected);
    }
}
