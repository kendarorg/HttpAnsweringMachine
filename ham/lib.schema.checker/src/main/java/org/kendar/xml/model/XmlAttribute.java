package org.kendar.xml.model;

public class XmlAttribute {
    private String name;
    private String value;

    @Override
    public String toString() {
        return "{ \"type\":\"XmlAttribute\"" +
                ", \"name\":\"" + name + "\"" +
                ", \"constraint\":\"" + constraint+ "\"" +
                ", \"value\":\"" + value + "\""  +
                ", \"valueConstraint\":\"" + valueConstraint + "\""+
                "}";
    }

    private XmlConstraint constraint = XmlConstraint.NONE;
    private XmlConstraint valueConstraint = XmlConstraint.NONE;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
