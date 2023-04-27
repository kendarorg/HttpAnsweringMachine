package org.kendar.xml.parser;

import org.kendar.xml.model.DiffPath;
import org.kendar.xml.model.XmlAttribute;
import org.kendar.xml.model.XmlElement;
import org.kendar.xml.model.XmlElementGroup;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

public class XmlBuilder {
    public XmlElement load(Element node, int depth, DiffPath diffResult) {
        var result = new XmlElement();
        result.setTag(node.getTagName());
        //result.setConstraint(XmlConstraint.MANDATORY_VALUE);
        result.setAttributes(loadAttributes(node.getAttributes(), diffResult));
        for (var i = 0; i < node.getChildNodes().getLength(); i++) {
            var childNode = node.getChildNodes().item(i);

            if (childNode instanceof Element) {
                var el = (Element) childNode;
                var elTag = el.getTagName();
                if (!result.getChildren().containsKey(elTag)) {
                    var group = new XmlElementGroup();
                    group.setTag(elTag);
                    result.getChildren().put(elTag, group);
                }
                //result.getChildren().get(elTag).setConstraint(XmlConstraint.MANDATORY_VALUE);
                result.getChildren().get(elTag).getItems().add(load(el, depth + 1, diffResult));
            } else if (childNode instanceof Node) {
                var val = ((Node) childNode).getNodeValue();
                //result.setConstraint(XmlConstraint.MANDATORY_VALUE);
                if (!Utils.stringIsEmptyOrNull(val)) {
                    result.setValue(val);
                }
            }
        }
        return result;
    }


    private Map<String, XmlAttribute> loadAttributes(NamedNodeMap attributes, DiffPath diffResult) {
        var result = new HashMap<String, XmlAttribute>();
        for (var i = 0; i < attributes.getLength(); i++) {
            var att = attributes.item(i);
            var val = new XmlAttribute();
            //val.setConstraint(XmlConstraint.MANDATORY_VALUE);
            val.setName(att.getNodeName());
            if (!Utils.stringIsEmptyOrNull(att.getNodeValue())) {
                val.setValue(att.getNodeValue());
            }
            result.put(val.getName(), val);
        }
        return result;
    }
}
