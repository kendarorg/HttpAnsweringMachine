package org.kendar.xml.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class XmlElementGroup {
    private List<XmlElement> items;
    private String tag;

    public XmlElementGroup(){
        items = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "{\"type\":\"XmlElementGroup\"" +
                ", \"tag\":\"" + tag + "\"" +
                ", \"constraint\":\"" + constraint +"\"" +
                ", \"items\":[" + String.join(",",items.stream()
                .sorted(Comparator.comparing(XmlElement::getTag)).map(a->a.toString()).collect(Collectors.toList())) +"]"+
                '}';
    }

    private XmlConstraint constraint = XmlConstraint.NONE;

    public List<XmlElement> getItems() {
        return items;
    }

    public void setItems(List<XmlElement> items) {
        this.items = items;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public XmlConstraint getConstraint() {
        return constraint;
    }

    public void setConstraint(XmlConstraint constraint) {
        this.constraint = constraint;
    }
}
