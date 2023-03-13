package org.kendar;

import org.junit.jupiter.api.Test;
import org.kendar.janus.serialization.JsonTypedSerializer;
import org.kendar.servers.dbproxy.HamResultSet;
import org.kendar.servers.dbproxy.ResultSetConverter;
import org.kendar.servers.dbproxy.utils.ResultSetConverterImpl;

import java.io.IOException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HamConverterTest {
    private static JsonTypedSerializer serializer = new JsonTypedSerializer();

    public ArrayList<Object> fill(Object... pars) {
        var result = new ArrayList<Object>();
        for (var par : pars) {
            result.add(par);
        }
        return result;
    }

    private ResultSet getJdbcResultset() throws IOException {
        var data = new String(getClass()
                .getClassLoader()
                .getResourceAsStream("./org/kendar/server/utils/json/janusresultset.json").readAllBytes());
        var deser = serializer.newInstance();
        deser.deserialize(data);
        return deser.read("rs");
    }

    private ResultSet getJdbcFlawedResultset() throws IOException {
        var data = new String(getClass()
                .getClassLoader()
                .getResourceAsStream("./org/kendar/server/utils/json/nulledjanusresultset.json").readAllBytes());
        var deser = serializer.newInstance();
        deser.deserialize(data);
        return deser.read("rs");
    }

    @Test
    public void test() throws Exception {
        ResultSet result = getJdbcResultset();
        ResultSetConverter target = new ResultSetConverterImpl();

        HamResultSet hamResultSet = target.toHam(result);
        var newData = new ArrayList<List<Object>>();
        newData.add(fill(1L, "first", LocalDateTime.of(2010, 1, 1, 1, 1, 1, 1)));
        newData.add(fill(2L, "second", LocalDateTime.of(2011, 1, 1, 1, 1, 1, 1)));
        hamResultSet.fill(newData);
        ResultSet newRs = target.fromHam(hamResultSet);

        assertTrue(newRs.next());
        assertEquals("first", newRs.getString(2));
        assertEquals(1L, newRs.getLong("id"));
        var tst = newRs.getTimestamp("tst");
        assertEquals("2010-01-01 01:01:01.000000001", tst.toString());

        assertTrue(newRs.next());
        assertEquals("second", newRs.getString(2));
        assertEquals(2L, newRs.getLong("id"));
        assertEquals("2011-01-01 01:01:01.000000001", newRs.getTimestamp("tst").toString());


        assertFalse(newRs.next());
    }


    @Test
    void testGetResultSetDate() throws Exception {
        ResultSet result = getJdbcResultset();
        ResultSetConverter target = new ResultSetConverterImpl();


        HamResultSet hamResultSet = target.toHam(result);
        var newData = new ArrayList<List<Object>>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        newData.add(fill(1L, "first", format.parse("2010-01-01")));
        newData.add(fill(2L, "second", format.parse("2011-01-01")));
        hamResultSet.fill(newData);
        ResultSet newRs = target.fromHam(hamResultSet);

        assertTrue(newRs.next());
        assertEquals("first", newRs.getString(2));
        assertEquals(1L, newRs.getLong("id"));
        assertEquals("2010-01-01 00:00:00.0", newRs.getTimestamp("tst").toString());

        assertTrue(newRs.next());
        assertEquals("second", newRs.getString(2));
        assertEquals(2L, newRs.getLong("id"));
        assertEquals("2011-01-01 00:00:00.0", newRs.getTimestamp("tst").toString());


        assertFalse(newRs.next());
    }

    @Test
    public void testNullContent() throws Exception {
        ResultSet result = getJdbcFlawedResultset();
        ResultSetConverter target = new ResultSetConverterImpl();

        HamResultSet hamResultSet = target.toHam(result);

        ResultSet newRs = target.fromHam(hamResultSet);
    }
}
