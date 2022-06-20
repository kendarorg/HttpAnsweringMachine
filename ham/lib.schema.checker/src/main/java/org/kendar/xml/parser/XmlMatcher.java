package org.kendar.xml.parser;

import org.kendar.xml.model.DiffPath;
import org.kendar.xml.model.XmlConstraint;
import org.kendar.xml.model.XmlElement;
import org.kendar.xml.model.XmlException;

public class XmlMatcher {



    private void matchesChildren(XmlElement template, XmlElement toCheck, DiffPath diffResult) throws XmlException {
        for (var childId: template.getChildren().keySet()) {
            var childToCheck = toCheck.getChildren().get(childId);
            var child = template.getChildren().get(childId);
            var childConstraint = child.getConstraint();

            if(childConstraint.matches(XmlConstraint.MANDATORY_VALUE) && ( childToCheck==null || childToCheck.getItems().size()==0)){
                throw new XmlException(diffResult.getPath()+" MISSING CHILD "+childId);
            }
            var childGroupRepresentative = child.getItems().get(0);
            if(childToCheck==null && childConstraint.matches(XmlConstraint.NULLABLE_VALUE))continue;
            for(var itemChildToCheck:childToCheck.getItems()){
                matches(childGroupRepresentative,itemChildToCheck,diffResult);
            }

        }
    }

    private void matchesAttributes(XmlElement template, XmlElement toCheck, DiffPath diffResult) throws XmlException {
        for(var attribute : template.getAttributes().values()){
            var toCheckAttribute = toCheck.getAttributes().get(attribute.getName());

            var attributeConstraint = attribute.getConstraint();
            if(attributeConstraint.matches(XmlConstraint.MANDATORY_VALUE) && toCheckAttribute==null) {
                throw new XmlException(diffResult.getPath()+" MISSING ATTRIBUTE "+attribute.getName());
            }
            var isAttributeValueSet = !Utils.stringIsEmptyOrNull(toCheckAttribute.getValue());
            var attributeValueConstraint = attribute.getValueConstraint();
            if(attributeValueConstraint.matches(XmlConstraint.MANDATORY_VALUE) && !isAttributeValueSet) {
                throw new XmlException(diffResult.getPath()+" MISSING ATTRIBUTE VALUE"+attribute.getName());
            }
        }
    }

    private void matchesValue(XmlElement template, XmlElement toCheck, DiffPath diffResult) throws XmlException {
        var templateValueConstraint = template.getValueConstraint();
        var toCheckValueNotSet = Utils.stringIsEmptyOrNull(toCheck.getValue());

        if(toCheckValueNotSet && templateValueConstraint.matches(XmlConstraint.MANDATORY_VALUE)){
            throw new XmlException(diffResult.getPath()+" MISSING CONTENT");
        }
    }

    private void matchesTag(XmlElement template, XmlElement toCheck, DiffPath diffResult) throws XmlException {
        if(template.getTag()!=null && toCheck.getTag()==null){
            throw new XmlException(diffResult.getPath()+" WRONG TAG");
        }
        if(template.getTag()==null && toCheck.getTag()!=null){
            if(template.getConstraint()==XmlConstraint.NONE)return;
            throw new XmlException(diffResult.getPath()+" WRONG TAG");
        }
        if(template.getTag()==null && toCheck.getTag()==null){
            return;
        }
        if(!template.getTag().equalsIgnoreCase(toCheck.getTag())){
            throw new XmlException(diffResult.getPath()+" WRONG TAG");
        }
    }



    public void matches(XmlElement template, XmlElement toCheck, DiffPath diffPath) throws XmlException {
        diffPath.push(template.getTag());
        matchesTag(template, toCheck, diffPath);
        matchesValue(template, toCheck, diffPath);

        matchesAttributes(template, toCheck, diffPath);
        matchesChildren(template, toCheck, diffPath);
        diffPath.pop();
    }
}
