package org.kendar.replayer.storage.db.utils;

import org.kendar.janus.cmd.RetrieveRemainingResultSet;
import org.kendar.janus.cmd.interfaces.JdbcCommand;
import org.kendar.janus.engine.Engine;
import org.kendar.janus.results.JdbcResult;
import org.kendar.janus.results.RemainingResultSetResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class NullEngine implements Engine {
    @Override
    public Engine create() {
        return null;
    }

    @Override
    public JdbcResult execute(JdbcCommand jdbcCommand, Long aLong, Long aLong1) throws SQLException {
        if(jdbcCommand instanceof RetrieveRemainingResultSet){
            var res = new RemainingResultSetResult();
            res.setRows(new ArrayList<>());
            res.setLastRow(true);
            return res;
        }
        return null;
    }

    @Override
    public int getMaxRows() {
        return 0;
    }

    @Override
    public boolean getPrefetchMetadata() {
        return false;
    }

    @Override
    public String getCharset() {
        return null;
    }

    @Override
    public UUID startRecording() {
        return null;
    }

    @Override
    public void cleanRecordings() {

    }

    @Override
    public void stopRecording(UUID uuid) {

    }

    @Override
    public void startReplaying(UUID uuid) {

    }

    @Override
    public void stopReplaying(UUID uuid) {

    }
}
