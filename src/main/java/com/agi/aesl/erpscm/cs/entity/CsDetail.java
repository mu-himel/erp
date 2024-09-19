package com.agi.aesl.erpscm.cs.entity;

import com.agi.aesl.erpscm.indent.entity.IndentDetail;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "cs_details")
public class CsDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private IndentDetail indentDetail;

    @ManyToOne
    private Cs cs;

    @OneToMany(mappedBy = "csDetail", cascade = CascadeType.ALL)
    private List<CsVendorDetail> vendorDetails;
}
