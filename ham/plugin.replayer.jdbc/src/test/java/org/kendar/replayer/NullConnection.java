package org.kendar.replayer;

import org.kendar.janus.JdbcConnection;
import org.kendar.janus.engine.Engine;

public class NullConnection extends JdbcConnection {
    public NullConnection() {
        super(1, new NullEngine(), false);
    }

    public NullConnection(long traceId, Engine engine) {
        super(traceId, engine, false);
    }
}
