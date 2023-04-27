package org.kendar.xml;

import org.junit.jupiter.api.Test;
import org.kendar.xml.model.DiffPath;
import org.kendar.xml.model.XmlElement;
import org.kendar.xml.parser.XmlTemplatesMerger;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MergerTests extends BaseUtils {


    private final String SUB_NULLABLE = "{\"type\":\"XmlElement\", \"tag\":\"a\", \"constraint\":\"MANDATORY_VALUE\", \"value\":\"null\", \"valueConstraint\":\"NULLABLE_VALUE\", \"attributes\":[], \"children\":[{\"type\":\"XmlElementGroup\", \"tag\":\"b\", \"constraint\":\"MANDATORY_VALUE\", \"items\":[{\"type\":\"XmlElement\", \"tag\":\"b\", \"constraint\":\"MANDATORY_VALUE\", \"value\":\"null\", \"valueConstraint\":\"NULLABLE_VALUE\", \"attributes\":[], \"children\":[{\"type\":\"XmlElementGroup\", \"tag\":\"c\", \"constraint\":\"NULLABLE_VALUE\", \"items\":[{\"type\":\"XmlElement\", \"tag\":\"c\", \"constraint\":\"MANDATORY_VALUE\", \"value\":\"null\", \"valueConstraint\":\"NULLABLE_VALUE\", \"attributes\":[], \"children\":[]}]}]}]}]}";

    @Test
    public void shouldMergeSimple() {
        var toMerge = new ArrayList<XmlElement>();
        var merger = new XmlTemplatesMerger();
        toMerge.add(toXmlElement("<a><b><c></c></b></a>"));
        var result = merger.mergeTemplates(toMerge, new DiffPath());
        assertEquals(
                "{\"type\":\"XmlElement\", \"tag\":\"a\", \"constraint\":\"MANDATORY_VALUE\", \"value\":\"null\", \"valueConstraint\":\"NULLABLE_VALUE\", \"attributes\":[], \"children\":[{\"type\":\"XmlElementGroup\", \"tag\":\"b\", \"constraint\":\"MANDATORY_VALUE\", \"items\":[{\"type\":\"XmlElement\", \"tag\":\"b\", \"constraint\":\"MANDATORY_VALUE\", \"value\":\"null\", \"valueConstraint\":\"NULLABLE_VALUE\", \"attributes\":[], \"children\":[{\"type\":\"XmlElementGroup\", \"tag\":\"c\", \"constraint\":\"MANDATORY_VALUE\", \"items\":[{\"type\":\"XmlElement\", \"tag\":\"c\", \"constraint\":\"MANDATORY_VALUE\", \"value\":\"null\", \"valueConstraint\":\"NULLABLE_VALUE\", \"attributes\":[], \"children\":[]}]}]}]}]}",
                result.toString());
    }

    @Test
    public void shouldMergeBetween() {
        var toMerge = new ArrayList<XmlElement>();
        var merger = new XmlTemplatesMerger();
        toMerge.add(toXmlElement("<a></a>"));
        toMerge.add(toXmlElement("<a><b></b></a>"));
        var result = merger.mergeTemplates(toMerge, new DiffPath());
        //The b group is nullable
        assertEquals(
                "{\"type\":\"XmlElement\", \"tag\":\"a\", \"constraint\":\"MANDATORY_VALUE\", \"value\":\"null\", \"valueConstraint\":\"NULLABLE_VALUE\", \"attributes\":[], \"children\":[{\"type\":\"XmlElementGroup\", \"tag\":\"b\", \"constraint\":\"NULLABLE_VALUE\", \"items\":[{\"type\":\"XmlElement\", \"tag\":\"b\", \"constraint\":\"MANDATORY_VALUE\", \"value\":\"null\", \"valueConstraint\":\"NULLABLE_VALUE\", \"attributes\":[], \"children\":[]}]}]}",
                result.toString());
    }

    @Test
    public void shouldMergeInternally() {
        var toMerge = new ArrayList<XmlElement>();
        var merger = new XmlTemplatesMerger();
        toMerge.add(toXmlElement("<a><b></b><b><c></c></b></a>"));
        var result = merger.mergeTemplates(toMerge, new DiffPath());
        //The c group should be nullable
        assertEquals(SUB_NULLABLE, result.toString());
    }

    @Test
    public void shouldMergeExternally() {
        var toMerge = new ArrayList<XmlElement>();
        var merger = new XmlTemplatesMerger();
        toMerge.add(toXmlElement("<a><b><c></c></b></a>"));
        toMerge.add(toXmlElement("<a><b></b></a>"));
        var result = merger.mergeTemplates(toMerge, new DiffPath());
        //The c group should be nullable
        assertEquals(SUB_NULLABLE, result.toString());
    }

    @Test
    public void shouldMergeInternallyDespiteOrder() {
        var toMerge = new ArrayList<XmlElement>();
        var merger = new XmlTemplatesMerger();
        toMerge.add(toXmlElement("<a><b><c></c></b><b></b></a>"));
        var result = merger.mergeTemplates(toMerge, new DiffPath());
        //The c group should be nullable
        assertEquals(SUB_NULLABLE, result.toString());
    }

    @Test
    public void shouldMergeExternallyDespiteOrder() {
        var toMerge = new ArrayList<XmlElement>();
        var merger = new XmlTemplatesMerger();
        toMerge.add(toXmlElement("<a><b></b></a>"));
        toMerge.add(toXmlElement("<a><b><c></c></b></a>"));
        var result = merger.mergeTemplates(toMerge, new DiffPath());
        //The c group should be nullable
        assertEquals(SUB_NULLABLE, result.toString());
    }

}
