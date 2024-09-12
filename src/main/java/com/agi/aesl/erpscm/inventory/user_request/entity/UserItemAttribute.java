package com.agi.aesl.erpscm.inventory.user_request.entity;

import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_item_attributes")
public class UserItemAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String attributeType;


    private String attributeUnit;

    @Column(length = 500)
    private String attributeValue;

    @ManyToOne
    @JsonIgnore
    private UserItem userItem;

    public UserItemAttribute(ItemAttribute attr, UserItem userItem) {
        this.attributeType = attr.getAttributeType();
        this.attributeUnit = attr.getAttributeUnit();
        this.attributeValue = attr.getAttributeValue();
        this.setUserItem(userItem);
    }
}
