package com.agi.aesl.erpscm.pr_indent.dto.reqeust;

import com.agi.aesl.erpscm.common.EntityConvertable;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.common.enums.IndentPriority;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.pr_indent.entity.PrIndent;
import com.agi.aesl.erpscm.pr_indent.entity.PrIndentDetail;
import com.agi.aesl.erpscm.pr_indent.entity.PrIndentPartialDelivery;
import com.agi.aesl.erpscm.pr_indent.entity.PrIndentWarehouseDetail;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PrIndentRequestDto implements EntityConvertable<PrIndent> {

    private Long id;
    @NotNull(message = "Item Category Missing")
    private ReferenceObjectDto category;

    @NotNull(message = "Item Sub Category Missing")
    private ReferenceObjectDto subCategory;

    @NotNull(message = "PR Indent Priority Missing")
    private IndentPriority priority;
    @NotNull(message = "PR Indent Priority Missing")
    private LocalDateTime priorityDate;

    @NotNull(message = "PR Indent Detail is required")
    private List<PrIndentDetailRequestDto> prIndentDetails;

    private String productRequirementsIds;

    public PrIndentRequestDto(Long id) {
        this.id = id;
    }


    public void setPriorityDate(String priorityDate){
        this.priorityDate = LocalDateTime.parse(priorityDate);
    }

    @Override
    public PrIndent getEntity() {

        PrIndent prIndent = new PrIndent(id);
        List<PrIndentDetail> prIndentDetails = new ArrayList<>();
        BeanUtils.copyProperties(this, prIndent);
        prIndent.setCategory(new ItemCategory(this.category.getId()));
        prIndent.setSubCategory(new ItemCategory(this.subCategory.getId()));
        this.getPrIndentDetails().stream().forEach(_prDetail -> {
            PrIndentDetail prIndentDetail = new PrIndentDetail();
            BeanUtils.copyProperties(_prDetail, prIndentDetail);

            prIndentDetail.setWarehouses(_prDetail.getWarehouses().stream().map(warehouse->{
                PrIndentWarehouseDetail prIndentWarehouseDetail = new PrIndentWarehouseDetail();
                prIndentWarehouseDetail.setWarehouse(new Warehouse(warehouse.getWarehouseId()));
                prIndentWarehouseDetail.setOrderQty(warehouse.getOrderQty());
                prIndentWarehouseDetail.setPrQty(warehouse.getPrQty());
                prIndentWarehouseDetail.setPrIndentDetail(prIndentDetail);
                prIndentWarehouseDetail.setPartialDeliveyTimes(
                        warehouse.getPartialDeliveries().stream().map((PartialDeliveryTimeDto pd)->{
                            PrIndentPartialDelivery pipd = new PrIndentPartialDelivery();
                            pipd.setPdDate(pd.getPdDate());
                            pipd.setQty(pd.getQty());
                            pipd.setPrIndentWarehouseDetail(prIndentWarehouseDetail);
                            return pipd;
                        }).collect(Collectors.toList())
                );
                return prIndentWarehouseDetail;
            }).collect(Collectors.toList()));

            prIndentDetail.setPrIndent(prIndent);
            prIndentDetails.add(prIndentDetail);
        });
        prIndent.setPrIndentDetails(prIndentDetails);

        return prIndent;
    }
}
