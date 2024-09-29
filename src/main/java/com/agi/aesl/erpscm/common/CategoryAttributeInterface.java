package com.agi.aesl.erpscm.common;

public interface CategoryAttributeInterface {

    Long getId();
    String getAttributeType();
    void setAttributeType(String type);

    String getAttributeUnit();

    void setAttributeUnit(String unit);
    String getAttributeValue();
    void setAttributeValue(String val);
}
