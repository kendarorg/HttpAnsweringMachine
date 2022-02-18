package org.kendar.xml;

import org.junit.jupiter.api.Test;
import org.kendar.xml.model.XmlException;

import java.io.IOException;

public class XmlVerificationTests extends BaseUtils {

    @Test
    public void missingAttribute() throws IOException {
        var xml1 = read("/org/kendar/xml/template.xml");
        var xml2 = read("/org/kendar/xml/missingAttribute.xml");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "a.b MISSING ATTRIBUTE id");
    }

    @Test
    public void missingChild() throws IOException {
        var xml1 = read("/org/kendar/xml/templateMissingChild.xml");
        var xml2 = read("/org/kendar/xml/missingChild.xml");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "a.b MISSING CHILD e");
    }

    @Test
    public void missingValue() throws IOException {
        var xml1 = read("/org/kendar/xml/missingValueTemplate.xml");
        var xml2 = read("/org/kendar/xml/missingValue.xml");

        var target = new DiffInferrer();
        assertException(XmlException.class, () -> {
            target.diff(xml1,xml2);
        }, "a.e MISSING CONTENT");
    }

    @Test
    public void correct() throws IOException, XmlException {
        var xml1 = read("/org/kendar/xml/template.xml");
        var xml2 = read("/org/kendar/xml/correct.xml");

        var target = new DiffInferrer();
        target.diff(xml1,xml2);
    }


}
