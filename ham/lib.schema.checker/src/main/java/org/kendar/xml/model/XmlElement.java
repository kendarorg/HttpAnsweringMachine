package org.kendar.xml.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class XmlElement {
    private Map<String, XmlAttribute> attributes = new HashMap<>();
    private String value;
    private String tag;
    private Map<String, XmlElementGroup> children = new HashMap<>();
    private XmlConstraint constraint = XmlConstraint.NONE;
    private XmlConstraint valueConstraint = XmlConstraint.NONE;

    @Override
    public String toString() {
        return "{\"type\":\"XmlElement\"" +
                ", \"tag\":\"" + tag + "\"" +
                ", \"constraint\":\"" + constraint + "\"" +
                ", \"value\":\"" + value + "\"" +
                ", \"valueConstraint\":\"" + valueConstraint + "\"" +
                ", \"attributes\":[" + String.join(",", attributes.values().stream()
                .sorted(Comparator.comparing(XmlAttribute::getName))
                .map(a -> a.toString()).collect(Collectors.toList())) + "]" +
                ", \"children\":[" + String.join(",", children.values().stream()
                .sorted(Comparator.comparing(XmlElementGroup::getTag)).map(a -> a.toString()).collect(Collectors.toList())) + "]" +
                "}";
    }

    public Map<String, XmlAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, XmlAttribute> attributes) {
        this.attributes = attributes;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Map<String, XmlElementGroup> getChildren() {
        return children;
    }

    public void setChildren(Map<String, XmlElementGroup> children) {
        this.children = children;
    }

    public XmlConstraint getConstraint() {
        return constraint;
    }

    public void setConstraint(XmlConstraint constraint) {
        this.constraint = constraint;
    }

    public XmlConstraint getValueConstraint() {
        return valueConstraint;
    }

    public void setValueConstraint(XmlConstraint valueConstraint) {
        this.valueConstraint = valueConstraint;
    }
}
