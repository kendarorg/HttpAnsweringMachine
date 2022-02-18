package org.kendar.xml.model;

public enum XmlConstraint {
    NONE(0x00),
    MANDATORY_VALUE(0x01),
    NULLABLE_VALUE(0x02);

    private long flag;

    XmlConstraint(long flag) {
        this.flag = flag;
    }
    
    public long getValue(){
        return flag;
    }
    
    public boolean matches(XmlConstraint toMatch){
        return (toMatch.flag & flag)==toMatch.flag;
    }

}
