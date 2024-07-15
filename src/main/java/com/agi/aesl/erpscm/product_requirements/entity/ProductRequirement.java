package com.agi.aesl.erpscm.product_requirements.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.demand.entity.Demand;
import com.agi.aesl.erpscm.demand.entity.DemandDetail;
import com.agi.aesl.erpscm.demand.enums.DemandPriority;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.product_requirements.enums.ProductRequirementStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "product_requirements")
public class ProductRequirement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
        private LocalDateTime productRequirementDate;
    
        @ManyToOne(fetch = FetchType.LAZY)
        private ItemCategory category;
    
        @ManyToOne(fetch = FetchType.LAZY)
        private ItemCategory subCategory;
    
        @ManyToOne(fetch = FetchType.LAZY)
        private Demand demand;
    
        private LocalDateTime demandDate;
    
        private LocalDateTime demandDeadline;
    
        @Enumerated(EnumType.STRING)
        private DemandPriority demandPriority;
    
        @ManyToOne(fetch = FetchType.LAZY)
        private DemandDetail demandDetail;
    
        @Enumerated(EnumType.STRING)
        private ProductRequirementStatus status;
    
        @ManyToOne
        private Warehouse warehouse;
    
        @ManyToOne
        private Employee requestedBy;
    
        public ProductRequirement(Long id) {
            this.id = id;
        }

}
