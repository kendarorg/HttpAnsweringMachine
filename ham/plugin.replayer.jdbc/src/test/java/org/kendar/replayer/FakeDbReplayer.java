package org.kendar.replayer;

import ch.qos.logback.classic.Level;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.engine.db.DbReplayer;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FakeDbReplayer extends DbReplayer {

    public Map<Long,ReplayerRow> replayerRowMap = new HashMap<>();
    public Map<Long,CallIndex> callIndexMap = new HashMap<>();

    public FakeDbReplayer(HibernateSessionFactory sessionFactory) {
        super(sessionFactory, new LoggerBuilder() {
            @Override
            public void setLevel(String loggerName, Level level) {

            }

            @Override
            public Level getLevel(String loggerName) {
                return null;
            }

            @Override
            public Logger build(Class<?> toLogClass) {
                return new FakeLogger();
            }
        },null);
    }

    protected boolean hasDbRows(Long recordingId) throws Exception{
        hasRows = true;
        return true;
    }

    protected ReplayerRow getReplayerRow(Long recordingId, CallIndex index, EntityManager e) {
        return replayerRowMap.get(index.getReference());
    }

    protected void addAllIndexes(Long recordingId, ArrayList<CallIndex> indexes, EntityManager e) {
        for(var index:callIndexMap.values().stream().sorted(Comparator.comparingLong(CallIndex::getId)).collect(Collectors.toList())){
            if(replayerRowMap.containsKey(index.getReference()) && !index.isStimulatorTest()){
                indexes.add(index);
            }
        }
    }
}
