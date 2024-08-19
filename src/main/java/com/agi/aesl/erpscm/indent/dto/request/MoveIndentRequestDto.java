package com.agi.aesl.erpscm.indent.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class MoveIndentRequestDto {
    private List<Long> ids;
}
