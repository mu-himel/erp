package com.agi.aesl.erpscm.demand.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.demand.enums.DemandStatus;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scm_demands")
public class Demand {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String demandNo;
    private LocalDateTime demandDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private DemandStatus status;

    @Enumerated(EnumType.STRING)
    private DemandStatus reviewPrevStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private ItemCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private ItemCategory subCategory;

    private String userId;

    private String reviewerId;

    private LocalDateTime reviewDate;

    private Long nextVerifierId;
    private Long nextApproverId;

    @ManyToOne
    private Warehouse warehouse;
    
    @OneToMany(mappedBy = "demand", cascade = CascadeType.ALL)
    private List<DemandDetail> demandDetails;

    @ManyToOne
    private User requestedBy;
}
