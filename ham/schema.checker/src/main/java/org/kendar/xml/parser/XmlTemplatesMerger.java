package org.kendar.xml.parser;

import org.kendar.xml.model.*;

import java.util.ArrayList;

public class XmlTemplatesMerger {

    public XmlElement mergeTemplates(ArrayList<XmlElement> templates, DiffPath diffResult) {
        var result = new XmlElement();
        var first = true;
        for(var i = 0;i<templates.size();i++){
            diffResult.push("template"+i);
            var template = templates.get(i);
            apply(result,template,first,diffResult);
            first = false;
            diffResult.pop();
        }
        return result;
    }


    private void apply(XmlElement result, XmlElement template, boolean first, DiffPath diffResult) {
        result.setTag(template.getTag());
        if(result.getConstraint().matches(XmlConstraint.NONE)){
            result.setConstraint(XmlConstraint.MANDATORY_VALUE);
        }
        setupValueConstraints(result, template,first,diffResult);
        setupAttributesConstraints(result, template, first,diffResult);
        setupChildrenConstraint(result, template, first,diffResult);
    }


    private void setupValueConstraints(XmlElement result, XmlElement template, boolean first, DiffPath diffResult) {
        var valueNotSet = Utils.stringIsEmptyOrNull(template.getValue());

        if(valueNotSet){
            result.setValueConstraint(XmlConstraint.NULLABLE_VALUE);
        }else{
            if(first){
                result.setValueConstraint(XmlConstraint.MANDATORY_VALUE);
            }
        }

    }

    private void setupChildrenConstraint(XmlElement result, XmlElement template, boolean first, DiffPath diffResult) {
        if(first){
            for (var child: template.getChildren().values()) {
                //If does not contains a group for the children, add it
                //The group must contain only ONE ITEM
                if(!result.getChildren().containsKey(child.getTag())){
                    result.getChildren().put(child.getTag(),new XmlElementGroup());
                }
                //Retrieve the group
                var newChild = result.getChildren().get(child.getTag());
                newChild.setTag(child.getTag());
                //It's the first template building, what it finds IS MANDATORY
                newChild.setConstraint(XmlConstraint.MANDATORY_VALUE);
                //Now should merge all the otjer elements
                var subFirst = true;
                var newItem = new XmlElement();
                for (var item : child.getItems()) {
                    apply(newItem, item, subFirst,diffResult);
                    subFirst = false;
                }
                newChild.getItems().add(newItem);
            }
        }else {
            for (var child : result.getChildren().values()) {
                if (!template.getChildren().containsKey(child.getTag())) {
                    child.setConstraint(XmlConstraint.NULLABLE_VALUE);
                }
            }
            for (var child : template.getChildren().values()) {
                if (!result.getChildren().containsKey(child.getTag())) {
                    result.getChildren().put(child.getTag(), new XmlElementGroup());
                    result.getChildren().get(child.getTag()).setConstraint(XmlConstraint.NULLABLE_VALUE);
                }
                var elGroup = result.getChildren().get(child.getTag());
                elGroup.setTag(child.getTag());
                var subFirst = false;
                if (elGroup.getItems().size() == 0) {
                    subFirst = true;
                    var newx = new XmlElement();
                    newx.setConstraint(XmlConstraint.NULLABLE_VALUE);
                    newx.setTag(child.getTag());
                    elGroup.getItems().add(newx);
                }
                var newItem = elGroup.getItems().get(0);

                for (var item : child.getItems()) {
                    apply(newItem, item, subFirst, diffResult);
                    subFirst = false;
                }
            }
        }
    }

    private void setupAttributesConstraints(XmlElement result, XmlElement template, boolean first, DiffPath diffResult) {
        for(var attribute : template.getAttributes().values()){
            if(first){
                var attr = new XmlAttribute();
                attr.setName(attribute.getName());
                var valueNotSet = Utils.stringIsEmptyOrNull(attribute.getValue());
                attr.setConstraint(XmlConstraint.MANDATORY_VALUE);
                if(valueNotSet){
                    attr.setValueConstraint(XmlConstraint.NULLABLE_VALUE);
                }else{
                    attr.setValueConstraint(XmlConstraint.MANDATORY_VALUE);
                }
                result.getAttributes().put(attr.getName(),attr);
            }else{
                var alreadyPresentAttribute = result.getAttributes().get(attribute.getName());
                if(alreadyPresentAttribute==null){
                    attribute.setConstraint(XmlConstraint.NULLABLE_VALUE);
                    result.getAttributes().put(attribute.getName(),attribute);
                    alreadyPresentAttribute = attribute;
                }
                var alreadyPresentValue = !Utils.stringIsEmptyOrNull(result.getValue());
                if(!alreadyPresentValue){
                    alreadyPresentAttribute.setValueConstraint(XmlConstraint.NULLABLE_VALUE);
                }
            }
        }
        if(!first){
            for(var attribute : result.getAttributes().values()){
                var alreadyPresentAttribute = template.getAttributes().get(attribute.getName());
                if(alreadyPresentAttribute==null){
                    attribute.setConstraint(XmlConstraint.NULLABLE_VALUE);
                }
            }
        }
    }
}
