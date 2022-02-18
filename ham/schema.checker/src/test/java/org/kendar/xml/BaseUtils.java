package org.kendar.xml;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.function.Executable;
import org.kendar.xml.model.DiffPath;
import org.kendar.xml.model.XmlElement;
import org.kendar.xml.parser.XmlBuilder;
import org.opentest4j.AssertionFailedError;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseUtils {
    protected ObjectMapper mapper = new ObjectMapper();

    private XmlBuilder builder = new XmlBuilder();
    protected XmlElement toXmlElement(String xml){
        return builder.load(toXml(xml).getDocumentElement(),0,new DiffPath());
    }

    protected String read(String path){
        try {
            return IOUtils.toString( this.getClass().getResourceAsStream(path), "UTF-8");
        } catch (IOException e) {
            return null;
        }
    }

    /*
    protected Document readJson(String path){
        try {
            var str = read(path);
            JsonNode node = mapper.readValue(str, JsonNode.class);
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
            xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
            ObjectWriter ow = xmlMapper.writer().withRootName("root");
            StringWriter w = new StringWriter();
            ow.writeValue(w, node);
            var xml = w.toString();
            return toXml(xml);
        }catch(Exception ex){
            return null;
        }

    }*/

    protected Document readXml(String path){
        return toXml(read(path));
    }

    protected Document toXml(String data){
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            return db.parse(new InputSource(new StringReader(data)));

        }catch(Exception e){
            return null;
        }
    }

    protected void assertException(Class<?> expected, Executable executable){
        assertException(expected,executable,null);
    }

    protected void assertException(Class<?> expected, Executable executable, String message){

        try{
            executable.execute();
        } catch (Throwable e) {
            if(e.getClass().equals(expected)){
                if(message!=null) {
                    if (!message.equals(e.getMessage())) {
                        throw new AssertionFailedError(
                                "Message was " + e.getMessage() + " instead of " + message, e);
                    }
                    assertEquals(message, e.getMessage());
                    return;
                }
            }else{
                throw new AssertionFailedError(
                        "Exception was "+e.getClass().getName()+" instead of "+expected.getName(), e);
            }
        }
        throw new AssertionFailedError(
                "Exception "+expected.getName()+" was expected", null);
    }
}
