package org.kendar.xml;

import org.junit.jupiter.api.Test;
import org.kendar.xml.model.XmlException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleXmlTests extends BaseUtils {


    @Test
    public void simple() throws IOException, XmlException {
        var xml1 = ("<a><b></b></a>");
        var xml2 = ("<a><b></b></a>");

        var target = new DiffInferrer();
        var result= target.diff(xml1,xml2);
        assertTrue(result);
    }

    @Test
    public void simpleWrong() throws IOException, XmlException {
        var xml1 = ("<a><b></b></a>");
        var xml2 = ("<a></a>");

        var target = new DiffInferrer();
        assertException(XmlException.class,()-> target.diff(xml1,xml2),"a MISSING CHILD b");
    }

    @Test
    public void childOverwrite() throws IOException, XmlException {
        var xml1 = ("<a><b><c></c></b><b></b></a>");
        var xml2 = ("<a><b></b></a>");

        var target = new DiffInferrer();
        assertTrue(target.diff(xml1,xml2));
    }
}
