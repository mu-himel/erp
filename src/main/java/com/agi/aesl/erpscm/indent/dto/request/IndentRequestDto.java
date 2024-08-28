package com.agi.aesl.erpscm.indent.dto.request;

import com.agi.aesl.erpscm.common.EntityConvertable;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.indent.entity.Indent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class IndentRequestDto implements EntityConvertable<Indent> {
    private Long id;

    private String indentNo;

    @NotNull
    private List<IndentDetailRequestDto> items;

    private List<Long> prIds;

    private Integer duration;

    private String categories;

    private Long categoryId;

    private Long subCategoryId;

    private Boolean isDevliverToSingleWarehouse;

    private ReferenceObjectDto singleWarehouse;

    private String priority;

    private LocalDate priorityDate;

    @Override
    public Indent getEntity() {
        Indent indent = new Indent();
        return indent;
    }
}
