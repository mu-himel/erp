package com.agi.aesl.erpscm.product_requirements.dto.request;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import com.agi.aesl.erpscm.common.EntityConvertable;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.demand.entity.Demand;
import com.agi.aesl.erpscm.demand.entity.DemandDetail;
import com.agi.aesl.erpscm.demand.enums.DemandPriority;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.product_requirements.entity.ProductRequirement;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequirementRequestDto implements EntityConvertable<ProductRequirement>{

    private Long id;

    @NotNull(message = "Item main category missing")
    private ItemCategory category;

    @NotNull(message = "Item sub category missing")
    private ItemCategory subCategory;

    @NotNull(message = "Demand missing")
    private Demand demand;

    @NotNull(message = "Demand date missing")
    private LocalDateTime demandDate;

    @NotNull(message = "Demand priority missing")
    private DemandPriority demandPriority;

    @NotNull(message = "Demand details missing")
    private DemandDetail demandDetail;

    @NotNull(message = "warehouse id missing")
    private ReferenceObjectDto warehouse;

    public ProductRequirementRequestDto(Long id) {
        this.id = id;
    }

    @Override
    public ProductRequirement getEntity() {
        ProductRequirement productRequirement = new ProductRequirement(id);
        BeanUtils.copyProperties(this, productRequirement);
        productRequirement.setWarehouse(new Warehouse(this.warehouse.getId()));
        return productRequirement;
    }
    
}
