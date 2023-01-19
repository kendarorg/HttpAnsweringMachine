package org.kendar.replayer;

import org.junit.jupiter.api.Test;
import org.kendar.janus.JdbcCallableStatement;
import org.kendar.janus.JdbcConnection;
import org.kendar.janus.cmd.Exec;
import org.kendar.janus.cmd.connection.ConnectionConnect;
import org.kendar.janus.cmd.preparedstatement.PreparedStatementExecute;
import org.kendar.janus.enums.ResultSetConcurrency;
import org.kendar.janus.enums.ResultSetHoldability;
import org.kendar.janus.enums.ResultSetType;
import org.kendar.janus.results.ObjectResult;
import org.kendar.replayer.engine.db.sqlsim.SqlSimulator;

import static org.junit.jupiter.api.Assertions.*;

public class SqlSimulatorTest {
    @Test
    void test(){
        var target = new SqlSimulator();
        var res = target.handle(new ConnectionConnect(), 0L);
        assertTrue(res.isHasResponse());
    }

    @Test
    void test01(){
        var target = new SqlSimulator();
        var initiator = new JdbcConnection(0L,null,true);
        var exec = new Exec(initiator,"isValid");
        var res = target.handle(exec, 0L);
        assertTrue(res.isHasResponse());
        assertTrue((boolean) ob(res.getResponse()).getResult());
    }


    @Test
    void test02(){
        var target = new SqlSimulator();
        var initiator = new JdbcCallableStatement(null,null,1,1,1,
                ResultSetType.FORWARD_ONLY, ResultSetConcurrency.CONCUR_READ_ONLY, ResultSetHoldability.DEFAULT);
        var exec = new Exec(initiator,"commit");
        var res = target.handle(exec, 0L);
        assertTrue(res.isHasResponse());
        assertNull( ob(res.getResponse()).getResult());
    }


    @Test
    void test03(){
        var target = new SqlSimulator();
        var command = new PreparedStatementExecute();
        var res = target.handle(command, 0L);
        assertFalse(res.isHasResponse());
    }

    private ObjectResult ob(Object response) {
        return (ObjectResult) response;
    }
}
