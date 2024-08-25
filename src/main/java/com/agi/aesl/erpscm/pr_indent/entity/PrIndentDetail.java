package com.agi.aesl.erpscm.pr_indent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pr_indent_details")
public class PrIndentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String attribute;

    private Long brandId;

    @ManyToOne
    private PrIndent prIndent;

    @OneToMany(mappedBy = "prIndentDetail",cascade = CascadeType.ALL)
    List<PrIndentWarehouseDetail> warehouses;
}
