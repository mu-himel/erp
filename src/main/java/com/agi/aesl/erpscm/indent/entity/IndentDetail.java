package com.agi.aesl.erpscm.indent.entity;

import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Entity
@NoArgsConstructor
@Table(name = "indent_details")
public class IndentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ItemCategory subCategory;

    private String productRequirementsIds;
    private String itemAttribute;

    private Long brandId;

    @ManyToOne
    @JsonIgnore
    private Indent indent;

    @OneToMany(mappedBy = "indentDetail", cascade = CascadeType.ALL)
    private List<IndentDeliveryDetail> warehouses;

    public IndentDetail(Long id) {
        this.id = id;
    }
}
