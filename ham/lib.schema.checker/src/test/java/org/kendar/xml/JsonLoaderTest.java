package org.kendar.xml;

import org.junit.jupiter.api.Test;
import org.kendar.xml.model.DiffPath;
import org.kendar.xml.model.XmlException;
import org.kendar.xml.parser.JsonBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JsonLoaderTest extends BaseUtils {


    @Test
    public void simple() throws IOException, XmlException {
        var json = "[{\"id\":\"a\"}]";
        var target = new JsonBuilder();
        var data = mapper.readTree(json);
        var result = target.load(data, 0, new DiffPath());
        assertNotNull(result);
    }

    @Test
    public void simpleObject() throws IOException, XmlException {
        var json = "[{\"id\":{\"part\":\"a\"}}]";
        var target = new JsonBuilder();
        var data = mapper.readTree(json);
        var result = target.load(data, 0, new DiffPath());
        assertNotNull(result);
    }

    @Test
    public void simpleArray() throws IOException, XmlException {
        var json = "[\"a\",\"b\"]";
        var target = new JsonBuilder();
        var data = mapper.readTree(json);
        var result = target.load(data, 0, new DiffPath());
        assertNotNull(result);
    }
}
