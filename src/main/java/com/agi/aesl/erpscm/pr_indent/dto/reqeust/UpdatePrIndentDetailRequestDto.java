package com.agi.aesl.erpscm.pr_indent.dto.reqeust;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePrIndentDetailRequestDto {
    @NotNull(message = "Pr Indent Details should not empty")
    private List<IndentDetailInfo> prIndentDetails;
}

