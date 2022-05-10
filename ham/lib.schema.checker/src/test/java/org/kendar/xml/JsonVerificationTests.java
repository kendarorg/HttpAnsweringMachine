package org.kendar.xml;

import org.junit.jupiter.api.Test;
import org.kendar.xml.model.XmlException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonVerificationTests extends BaseUtils {


    @Test
    public void missingChild()  {
        var xml1 = read("/json/template.json");
        var xml2 = read("/json/missingChild.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#object#.stuffs.#object# MISSING CHILD extra");
    }

    @Test
    public void missingValue()  {
        var xml1 = read("/json/template.json");
        var xml2 = read("/json/missingValue.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#object#.stuffs.#object#.extra MISSING CONTENT");
    }

    @Test
    public void missingValueEmpty()  {
        var xml1 = read("/json/template.json");
        var xml2 = read("/json/missingValueEmpty.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#object#.stuffs.#object#.extra MISSING CONTENT");
    }

    @Test
    public void correct() throws  XmlException {
        var xml1 = read("/json/template.json");
        var xml2 = read("/json/correct.json");

        var target = new DiffInferrer();
        assertTrue(target.diff(xml1,xml2));
    }
    @Test
    public void correctEmptyArray() throws  XmlException {
        var xml1 = "[]";
        var xml2 = "[]";

        var target = new DiffInferrer();
        assertTrue(target.diff(xml1,xml2));
    }

    @Test
    public void arrayCorrect() throws XmlException {
        var xml1 = read("/json/templateArray.json");
        var xml2 = read("/json/arrayCorrect.json");

        var target = new DiffInferrer();
        assertTrue(target.diff(xml1,xml2));
    }

    @Test
    public void arrayFailEmpty()  {
        var xml1 = read("/json/templateArray.json");
        var xml2 = read("/json/arrayFailEmpty.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#array# MISSING CHILD #array#");
    }

    @Test
    public void arrayFail()  {
        var xml1 = read("/json/templateArray.json");
        var xml2 = read("/json/arrayFail.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#array#.#object# MISSING CHILD content");
    }

    @Test
    public void missingObject()  {
        var xml1 = read("/json/template.json");
        var xml2 = read("/json/missingObject.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#object#.stuffs MISSING CHILD stuffs");
    }

    @Test
    public void missingObject2()  {
        var xml1 = read("/json/object.json");
        var xml2 = read("/json/objectMissing.json");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "#object#.a MISSING CHILD b");
    }
    @Test
    public void correctWeirdStuffs()  {
       
        final String xml1 = "{\"a\":\"b\",\"c\":\"d\"}";
        final String  xml2 = "{\"a\":\"b\"}";

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
             target.diff(xml1,xml2);
        }, "#object# MISSING CHILD c");
    }
}
