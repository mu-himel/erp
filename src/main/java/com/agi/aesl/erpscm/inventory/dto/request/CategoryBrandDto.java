package com.agi.aesl.erpscm.inventory.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryBrandDto {
    private ReferenceObjectDto category;
    @NotNull(message = "Brand Name Required")
    private String name;
}
