package com.agi.aesl.erpscm.demand.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.agi.aesl.erpscm.demand.enums.DemandStatus;
import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandDetailResDto {
    String demandNo;
        Long demandId;
        LocalDateTime demandDate;
        DemandStatus demandStatus;
        Long warehouseId;
        String warehouseName;
        String warehouseLocation;
        List<DemandDetailItemResDto> details;
        List<CategoryAttribute> attributes;
        Long nextApproverId;
        Long nextVerifierId;
        Long reviewerId;
        Long empId;
        String employeeId;
        String employeeName;
        String reportingManager;
        String department;
        String designation;
    
        List<?> verifiers;
        List<?> approvers;
        List<?> comments;
    
        public void addDetail(DemandDetailItemResDto demandDetailItemResDto){
            if(this.details!=null){
                this.details.add(demandDetailItemResDto);
            }else{
                this.details = new ArrayList<>();
                this.details.add(demandDetailItemResDto);
            }
        }
    
        public void setComments(List<?> comments) {
            this.comments = comments;
        }
}
