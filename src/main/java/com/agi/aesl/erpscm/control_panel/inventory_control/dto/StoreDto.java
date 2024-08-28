package com.agi.aesl.erpscm.control_panel.inventory_control.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StoreDto {

    @NotNull(message = "Name is Required")
    @NotBlank(message = "Name is Required")
    private String name;
    private Long warehouseId;
}
