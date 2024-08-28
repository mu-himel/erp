package com.agi.aesl.erpscm.quality_control.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteDto {

    @NotNull(message = "Sorry! note should not be null")
    @NotEmpty(message = "Sorry! note should not be blank")
    private String note;
}
