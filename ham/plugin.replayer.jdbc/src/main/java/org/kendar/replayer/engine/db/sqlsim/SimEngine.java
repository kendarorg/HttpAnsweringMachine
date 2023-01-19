package org.kendar.replayer.engine.db.sqlsim;

import org.kendar.janus.cmd.interfaces.JdbcCommand;
import org.kendar.janus.engine.Engine;
import org.kendar.janus.results.JdbcResult;

import java.sql.SQLException;
import java.util.UUID;

public class SimEngine implements Engine {
    @Override
    public Engine create() {
        return this;
    }

    @Override
    public JdbcResult execute(JdbcCommand jdbcCommand, Long aLong, Long aLong1) throws SQLException {
        return (JdbcResult) SqlSimulator.handleInternal(jdbcCommand);
    }

    @Override
    public int getMaxRows() {
        return 100;
    }

    @Override
    public boolean getPrefetchMetadata() {
        return false;
    }

    @Override
    public String getCharset() {
        return "UTF-8";
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
