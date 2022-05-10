package org.kendar.xml.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kendar.xml.model.*;
import org.w3c.dom.NamedNodeMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonBuilder {
    public XmlElement load(JsonNode node, int depth, DiffPath diffResult) {
        return load(node,depth,diffResult,null);
    }

    private XmlElement load(JsonNode node, int depth, DiffPath diffResult,String fieldName) {

        var result = new XmlElement();
        if(fieldName!=null){
            result.setTag(fieldName);
        }
        if(node.isArray()){
            if(result.getTag()==null)result.setTag("#array#");
            var array = (ArrayNode)node;
            result.setChildren(new HashMap<>());
            result.getChildren().put(result.getTag(), new XmlElementGroup());
            result.getChildren().get(result.getTag()).setTag(result.getTag());
            for(var i=0;i<array.size();i++){
                var item = array.get(i);
                result.getChildren().get(result.getTag()).getItems().add(load(item,depth+1,diffResult));
            }
        }else if(node.isObject()){
            if(result.getTag()==null)result.setTag("#object#");
            var ob = (ObjectNode)node;
            var fieldNames = getFieldNames(node);
            result.setChildren(new HashMap<>());
            /*result.getChildren().put(result.getTag(),new XmlElementGroup());
            result.getChildren().get(result.getTag()).setTag(result.getTag());

            var rootGroup = result.getChildren().get(result.getTag());
            var rootElement*/
            for(var field :fieldNames){
                var item = ob.get(field);

                var val = item.asText();
                var group = new XmlElementGroup();
                group.setTag(field);

                if(!result.getChildren().containsKey(field)){
                    result.getChildren().put(field,group);
                }
                if(item.isArray()||item.isObject()){
                    result.getChildren().get(field).getItems().add(load(item,depth+1,diffResult,field));
                    continue;
                }else{
                    var el = new XmlElement();
                    el.setTag(field);
                    if(!val.equalsIgnoreCase("null")) {
                        el.setValue(val);
                    }
                    group.getItems().add(el);
                }


            }
        }else{
            var val = node.asText();
            result.setTag("#value");
            if(!val.equalsIgnoreCase("null")) {
                result.setValue(val);
            }
        }
        return result;
    }

    private List<String> getFieldNames(JsonNode node) {
        var result = new ArrayList<String>();
        var iterator = node.fieldNames();
        while(iterator.hasNext()){
            result.add(iterator.next());
        }
        return result;
    }

    private List<JsonNode> getChildren(JsonNode node) {
        var result = new ArrayList<JsonNode>();
        var iterator = node.iterator();
        while(iterator.hasNext()){
            result.add(iterator.next());
        }
        return result;
    }


    private Map<String, XmlAttribute> loadAttributes(NamedNodeMap attributes, DiffPath diffResult) {
        var result = new HashMap<String,XmlAttribute>();
        for(var i =0;i<attributes.getLength();i++){
            var att = attributes.item(i);
            var val = new XmlAttribute();
            //val.setConstraint(XmlConstraint.MANDATORY_VALUE);
            val.setName(att.getNodeName());
            if(Utils.stringIsEmptyOrNull(att.getNodeValue())) {
                //val.setValueConstraint(XmlConstraint.NULLABLE_VALUE);
            }else{
                //val.setValueConstraint(XmlConstraint.MANDATORY_VALUE);
                val.setValue(att.getNodeValue());
            }
            result.put(val.getName(),val);
        }
        return result;
    }
}
