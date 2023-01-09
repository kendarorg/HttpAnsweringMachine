package org.kendar.replayer.storage.db.utils;

import org.kendar.janus.cmd.statement.StatementExecute;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.storage.db.DbReplayer;
import org.kendar.servers.db.HibernateSessionFactory;

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
        super(sessionFactory);
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
