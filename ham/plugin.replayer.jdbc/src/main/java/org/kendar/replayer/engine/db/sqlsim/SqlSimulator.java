package org.kendar.replayer.engine.db.sqlsim;

import org.kendar.janus.JdbcCallableStatement;
import org.kendar.janus.JdbcConnection;
import org.kendar.janus.JdbcPreparedStatement;
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
import java.util.function.Function;

/**
 * Statement/execute
 * PreparedStatement/executeQuery
 * PreparedStatement/executeUpdate
 */
public class SqlSimulator {
    private static HashMap<Class<?>, Function<Object, Object>> fakes;
    private static HashMap<String, Function<Exec, Object>> fakeExecs;
    private static AtomicLong indexes = new AtomicLong();
    private static Engine engine;

    static {
        engine = new SimEngine();
        fakes = new HashMap<>();
        fakeExecs = new HashMap<>();
        fakes.put(ConnectionConnect.class, (a) -> new ObjectResult(indexes.incrementAndGet()));
        fakes.put(Close.class, (a) -> new ObjectResult());
        fakes.put(Exec.class, SqlSimulator::exec);
        addFakeExecs("Connection/getNetworkTimeout", (a) -> new ObjectResult(0));
        addFakeExecs("Connection/isReadOnly", (a) -> new ObjectResult(false));
        addFakeExecs("Connection/isValid", (a) -> new ObjectResult(true));
        addFakeExecs("Connection/getTransactionIsolation", (a) -> new ObjectResult(2));
        addFakeExecs("DatabaseMetaData/supportsResultSetType", (a) -> new ObjectResult(true));
        addFakeExecs("DatabaseMetaData/supportsResultSetType", (a) -> new ObjectResult(true));
        addFakeExecs("commit", (a) -> new ObjectResult());
        addFakeExecs("rollback", (a) -> new ObjectResult());
        addFakeExecs("setSavepoint", SqlSimulator::setSavepoint);
        fakes.put(ConnectionCreateStatement.class, (a) -> new StatementResult(indexes.incrementAndGet(), 100, 0));
        fakes.put(RetrieveRemainingResultSet.class, (a) -> new RemainingResultSetResult(true, new ArrayList<>()));
        fakes.put(ConnectionPrepareStatement.class, SqlSimulator::prepareStatement);
        fakes.put(ConnectionReleaseSavepoint.class, (a) -> new ObjectResult());
        fakes.put(ConnectionRollbackSavepoint.class, (a) -> new ObjectResult());
        fakes.put(StatementSetQueryTimeout.class, (a) -> new ObjectResult());
        fakes.put(StatementSetMaxRows.class, (a) -> new ObjectResult());
        fakes.put(UpdateSpecialObject.class, (a) -> new ObjectResult());
        fakes.put(ConnectionPrepareCall.class, SqlSimulator::prepareCall);


    }

    private static Object prepareCall(Object o) {
        var ps = (ConnectionPrepareCall) o;
        return new JdbcCallableStatement(newConnection(), engine, indexes.incrementAndGet(),
                100, 0, ps.getType(), ps.getConcurrency(),
                ps.getHoldability()).
                withSql(ps.getSql());
    }

    private static JdbcConnection newConnection() {
        return new JdbcConnection(indexes.incrementAndGet(), engine, true);
    }

    private static Object prepareStatement(Object o) {
        var ps = (ConnectionPrepareStatement) o;
        return new JdbcPreparedStatement(newConnection(), engine, indexes.incrementAndGet(),
                100, 0, ps.getType(), ps.getConcurrency(),
                ps.getHoldability()).withSql(ps.getSql());
    }

    private static Object setSavepoint(Exec exec) {
        var s = new JdbcSavepoint();
        s.setTraceId(indexes.incrementAndGet());
        s.setSavePointId(1);
        s.setSavePointName(UUID.randomUUID().toString());
        return s;
    }


    private static void addFakeExecs(String key, Function<Exec, Object> function) {
        key = key.toLowerCase(Locale.ROOT);
        fakeExecs.put(key, function);
    }

    private static Object exec(Object request) {
        var exec = (Exec) request;
        var key = (exec.getInitiator() + "/" + exec.getName()).toLowerCase(Locale.ROOT);
        Function<Exec, Object> founded;
        if (fakeExecs.containsKey(key)) {
            founded = fakeExecs.get(key);
        } else if (fakeExecs.containsKey(exec.getName().toLowerCase(Locale.ROOT))) {
            key = exec.getName().toLowerCase(Locale.ROOT);
            founded = fakeExecs.get(key);
        } else {
            return new SqlSimResponse();
        }
        var resultCall = founded.apply(exec);
        var result = new SqlSimResponse();
        result.setResponse(resultCall);
        result.setHasResponse(true);
        return result;
    }

    public SqlSimResponse handle(Object request) {
        if (request == null || !fakes.containsKey(request.getClass())) return new SqlSimResponse();
        var resultCall = fakes.get(request.getClass()).apply(request);
        var result = new SqlSimResponse();
        result.setResponse(resultCall);
        result.setHasResponse(true);
        return result;
    }

    static Object handleInternal(Object request){
        return fakes.get(request.getClass()).apply(request);
    }
}
