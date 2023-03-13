package org.kendar.replayer.engine.db.sqlsim;

import org.kendar.janus.JdbcCallableStatement;
import org.kendar.janus.JdbcConnection;
import org.kendar.janus.JdbcSavepoint;
import org.kendar.janus.cmd.Close;
import org.kendar.janus.cmd.Exec;
import org.kendar.janus.cmd.RetrieveRemainingResultSet;
import org.kendar.janus.cmd.connection.*;
import org.kendar.janus.cmd.resultset.UpdateSpecialObject;
import org.kendar.janus.cmd.statement.StatementSetMaxRows;
import org.kendar.janus.cmd.statement.StatementSetQueryTimeout;
import org.kendar.janus.engine.Engine;
import org.kendar.janus.results.ObjectResult;
import org.kendar.janus.results.RemainingResultSetResult;
import org.kendar.janus.results.StatementResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

/**
 * Statement/execute
 * PreparedStatement/executeQuery
 * PreparedStatement/executeUpdate
 */
public class SqlSimulator {
    private static HashMap<Class<?>, BiFunction<Object, Long, Object>> fakes;
    private static HashMap<String, BiFunction<Exec, Long, Object>> fakeExecs;
    private static AtomicLong indexes = new AtomicLong();
    private static Engine engine;

    static {
        engine = new SimEngine();
        fakes = new HashMap<>();
        fakeExecs = new HashMap<>();
        fakes.put(ConnectionConnect.class, (a, c) -> new ObjectResult(indexes.incrementAndGet()));
        fakes.put(Close.class, (a, c) -> new ObjectResult());
        fakes.put(Exec.class, SqlSimulator::exec);
        addFakeExecs("Connection/getNetworkTimeout", (a, c) -> new ObjectResult(0));
        addFakeExecs("Connection/isReadOnly", (a, c) -> new ObjectResult(false));
        addFakeExecs("Connection/isValid", (a, c) -> new ObjectResult(true));
        addFakeExecs("Connection/getTransactionIsolation", (a, c) -> new ObjectResult(2));
        addFakeExecs("DatabaseMetaData/supportsResultSetType", (a, c) -> new ObjectResult(true));
        addFakeExecs("DatabaseMetaData/supportsResultSetType", (a, c) -> new ObjectResult(true));
        addFakeExecs("commit", (a, c) -> new ObjectResult());
        addFakeExecs("rollback", (a, c) -> new ObjectResult());
        addFakeExecs("setSavepoint", SqlSimulator::setSavepoint);
        //addFakeExecs("setAutoCommit", (a,c) -> new ObjectResult());
        //addFakeExecs("getAutoCommit", (a,c) -> new ObjectResult(true));
        addFakeExecs("setReadOnly", (a, c) -> new ObjectResult());
        fakes.put(ConnectionCreateStatement.class, (a, c) -> new StatementResult(indexes.incrementAndGet(), 100, 0));
        fakes.put(RetrieveRemainingResultSet.class, (a, c) -> new RemainingResultSetResult(true, new ArrayList<>()));
        fakes.put(ConnectionPrepareStatement.class, (a, c) -> new StatementResult(indexes.incrementAndGet(), 100, 0));
        fakes.put(ConnectionReleaseSavepoint.class, (a, c) -> new ObjectResult());
        fakes.put(ConnectionRollbackSavepoint.class, (a, c) -> new ObjectResult());
        fakes.put(StatementSetQueryTimeout.class, (a, c) -> new ObjectResult());
        fakes.put(StatementSetMaxRows.class, (a, c) -> new ObjectResult());
        fakes.put(UpdateSpecialObject.class, (a, c) -> new ObjectResult());
        fakes.put(ConnectionPrepareCall.class, (a, c) -> new StatementResult(indexes.incrementAndGet(), 100, 0));
    }

    private static Object prepareCall(Object o, Long connectionId) {
        var ps = (ConnectionPrepareCall) o;
        return new JdbcCallableStatement(newConnection(connectionId), engine, indexes.incrementAndGet(),
                100, 0, ps.getType(), ps.getConcurrency(),
                ps.getHoldability()).
                withSql(ps.getSql());
    }

    private static JdbcConnection newConnection(Long connectionId) {
        return new JdbcConnection(connectionId, engine, true);
    }

    private static Object prepareStatement(Object o, Long connectionId) {
        var ps = (ConnectionPrepareStatement) o;
        return new StatementResult(indexes.incrementAndGet(), 100, 0);
    }

    private static Object setSavepoint(Exec exec, Long connectionId) {
        var s = new JdbcSavepoint();
        s.setTraceId(indexes.incrementAndGet());
        s.setSavePointId(1);
        s.setSavePointName(UUID.randomUUID().toString());
        return s;
    }


    private static void addFakeExecs(String key, BiFunction<Exec, Long, Object> function) {
        key = key.toLowerCase(Locale.ROOT);
        fakeExecs.put(key, function);
    }

    private static Object exec(Object request, Long connectionId) {
        var exec = (Exec) request;
        var name = exec.getName().toLowerCase(Locale.ROOT);
        var key = (exec.getInitiator() + "/" + name).toLowerCase(Locale.ROOT);
        BiFunction<Exec, Long, Object> founded;
        if (fakeExecs.containsKey(key)) {
            founded = fakeExecs.get(key);
        } else if (fakeExecs.containsKey(name)) {
            key = exec.getName().toLowerCase(Locale.ROOT);
            founded = fakeExecs.get(name);
        } else {
            return new SqlSimResponse();
        }
        var resultCall = founded.apply(exec, connectionId);
        var result = new SqlSimResponse();
        result.setResponse(resultCall);
        result.setHasResponse(true);
        return result;
    }

    public SqlSimResponse handle(Object request, long connectionId) {
        if (request == null || !fakes.containsKey(request.getClass())) return new SqlSimResponse();
        var resultCall = fakes.get(request.getClass()).apply(request, connectionId);
        if (resultCall.getClass() == SqlSimResponse.class) {
            return (SqlSimResponse) resultCall;
        }
        var result = new SqlSimResponse();
        result.setResponse(resultCall);
        result.setHasResponse(true);
        return result;
    }

    static Object handleInternal(Object request, Long connectionId) {
        return fakes.get(request.getClass()).apply(request, connectionId);
    }
}
