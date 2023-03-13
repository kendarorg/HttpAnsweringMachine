package org.kendar.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.xml.model.DiffPath;
import org.kendar.xml.model.XmlElement;
import org.kendar.xml.model.XmlException;
import org.kendar.xml.parser.JsonBuilder;
import org.kendar.xml.parser.XmlBuilder;
import org.kendar.xml.parser.XmlMatcher;
import org.kendar.xml.parser.XmlTemplatesMerger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

public class DiffInferrer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final XmlBuilder xmlBuilder = new XmlBuilder();
    private final JsonBuilder jsonBuilder = new JsonBuilder();
    private final XmlTemplatesMerger merger = new XmlTemplatesMerger();
    private final XmlMatcher matcher = new XmlMatcher();
    private final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    private Document toXml(String data) {
        try {
            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(new InputSource(new StringReader(data)));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean diff(String... xmls) throws XmlException {

        var first = xmls[0].trim();
        if (first == null) {
            if (!Arrays.stream(xmls).skip(1).allMatch(a -> a == null)) {
                throw new XmlException("Expected:null Found content instead");
            }
        } else {
            if (Arrays.stream(xmls).skip(1).anyMatch(a -> a == null)) {
                throw new XmlException("Expected not nulls Found nulls instead");
            }
        }
        var diffResult = new DiffPath();
        var templates = new ArrayList<XmlElement>();
        XmlElement toCheck;

        if (first.startsWith("{") || first.startsWith("[")) {
            //IS JSON
            toCheck = jsonBuilder.load(toJson(xmls[xmls.length - 1]), 0, diffResult);
            for (var i = 0; i < xmls.length - 1; i++) {
                templates.add(jsonBuilder.load(toJson(xmls[i]), 0, diffResult));
            }
        } else if (first.startsWith("<")) {
            //IS XML
            toCheck = xmlBuilder.load(toXml(xmls[xmls.length - 1]).getDocumentElement(), 0, diffResult);
            for (var i = 0; i < xmls.length - 1; i++) {
                templates.add(xmlBuilder.load(toXml(xmls[i]).getDocumentElement(), 0, diffResult));
            }
        } else {
            if (!first.equals((xmls[xmls.length - 1].trim()))) {
                throw new XmlException("Expected:" + first + " Found:" + xmls[xmls.length - 1]);
            }
            return true;
        }
        var template = merger.mergeTemplates(templates, diffResult);
        matcher.matches(template, toCheck, diffResult);
        return true;
    }

    private JsonNode toJson(String xml) {
        try {
            return mapper.readTree(xml);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
