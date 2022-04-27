package org.kendar.xml;

import org.junit.jupiter.api.Test;
import org.kendar.xml.model.XmlException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonVerificationTests extends BaseUtils {


    @Test
    public void missingChild() throws IOException, XmlException {
        var xml1 = read("/json/template.json");
        var xml2 = read("/json/missingChild.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#object#.stuffs.#object# MISSING CHILD extra");
    }

    @Test
    public void missingValue() throws IOException, XmlException {
        var xml1 = read("/json/template.json");
        var xml2 = read("/json/missingValue.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#object#.stuffs.#object#.extra MISSING CONTENT");
    }

    @Test
    public void missingValueEmpty() throws IOException, XmlException {
        var xml1 = read("/json/template.json");
        var xml2 = read("/json/missingValueEmpty.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#object#.stuffs.#object#.extra MISSING CONTENT");
    }

    @Test
    public void correct() throws IOException, XmlException {
        var xml1 = read("/json/template.json");
        var xml2 = read("/json/correct.json");

        var target = new DiffInferrer();
        assertTrue(target.diff(xml1,xml2));
    }
    @Test
    public void correctEmptyArray() throws IOException, XmlException {
        var xml1 = "[]";
        var xml2 = "[]";

        var target = new DiffInferrer();
        assertTrue(target.diff(xml1,xml2));
    }

    @Test
    public void arrayCorrect() throws IOException, XmlException {
        var xml1 = read("/json/templateArray.json");
        var xml2 = read("/json/arrayCorrect.json");

        var target = new DiffInferrer();
        assertTrue(target.diff(xml1,xml2));
    }

    @Test
    public void arrayFailEmpty() throws IOException, XmlException {
        var xml1 = read("/json/templateArray.json");
        var xml2 = read("/json/arrayFailEmpty.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#array# MISSING CHILD #array#");
    }

    @Test
    public void arrayFail() throws IOException, XmlException {
        var xml1 = read("/json/templateArray.json");
        var xml2 = read("/json/arrayFail.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#array#.#object# MISSING CHILD content");
    }

    @Test
    public void missingObject() throws IOException, XmlException {
        var xml1 = read("/json/template.json");
        var xml2 = read("/json/missingObject.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#object#.stuffs MISSING CHILD stuffs");
    }

    @Test
    public void missingObject2() throws IOException, XmlException {
        var xml1 = read("/json/object.json");
        var xml2 = read("/json/objectMissing.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#object#.a MISSING CHILD b");
    }
    @Test
    public void correctWeirdStuffs() throws IOException, XmlException {
       
        final String xml1 = "{\"a\":\"b\",\"c\":\"d\"}";
        final String  xml2 = "{\"a\":\"b\"}";

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
             target.diff(xml1,xml2);
        }, "#object# MISSING CHILD c");
    }
}
