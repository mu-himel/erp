package com.agi.aesl.erpscm.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceObjectDto {
    private Long id;
    private String name;

    public ReferenceObjectDto(Long id) {
        this.id = id;
    }

    
}
