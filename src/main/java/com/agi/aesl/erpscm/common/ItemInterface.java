package com.agi.aesl.erpscm.common;

public interface ItemInterface {
    Long getId();
    String getCode();
    String getItemAttributeName();
    CategoryInterface getCategory();
    BrandInterface getBrand();
    String getItemUnit();
}
