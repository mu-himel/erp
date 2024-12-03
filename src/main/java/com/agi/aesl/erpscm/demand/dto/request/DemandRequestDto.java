package com.agi.aesl.erpscm.demand.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.agi.aesl.erpscm.common.EntityConvertable;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.demand.entity.Demand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandRequestDto implements EntityConvertable<Demand>{
    private Long id;

    private String demandNo;

    @NotNull(message = "Demand Initiator Required")
    private DemandInitiator requestedBy;

    @NotNull(message = "Demand Detail is required")
    List<DemandDetailDto> demandDetails;

    @NotNull(message = "Demand Category is required")
    ReferenceObjectDto category;

    ReferenceObjectDto subCategory;
    String categories;

    private String deliveryDate;

    Boolean isVerificationRequired;


    @Override
    public Demand getEntity() {
        Demand demand = Demand.builder()
                .id(id)
//                .demandNo(demandNo)
                .build();

        return demand;
    }
}
