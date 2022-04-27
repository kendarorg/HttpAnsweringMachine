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
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseUtils {
    protected ObjectMapper mapper = new ObjectMapper();

    private final XmlBuilder builder = new XmlBuilder();
    protected XmlElement toXmlElement(String xml){
        return builder.load(toXml(xml).getDocumentElement(),0,new DiffPath());
    }

    protected String read(String path){
        try {
            return IOUtils.toString( this.getClass().getResourceAsStream(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }


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
