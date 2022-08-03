package org.kendar.servers.http.api;

import org.junit.jupiter.api.Test;
import org.kendar.remote.RestClientParser;
import org.kendar.servers.http.Request;
import org.kendar.utils.ConstantsHeader;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class RestClientParserTest {
    @Test
    public void testParse() throws URISyntaxException {
        var target = new RestClientParser();
        var req = new Request();
        req.setRequestText(
                "POST https://www.google.com/test?a=1&b=%202\n"+
                "Content-Type:text/plain\n"+
                "\n"+
                "This is a multiline\n"+
                "Text\n");
        var result = target.parse(req.getRequestText());
        assertNotNull(result);
        assertEquals(2,result.getQuery().size());
        assertEquals(1,result.getHeaders().size());
        var header = result.getHeader(ConstantsHeader.CONTENT_TYPE);
        assertEquals("text/plain",header);
    }
}
