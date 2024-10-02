package com.agi.aesl.erpscm.pr_indent.dto.reqeust;

import com.agi.aesl.erpscm.common.EntityConvertable;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.common.enums.IndentPriority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PrIndentDetailRequestDto {
    private String attribute;
    private Long brandId;
    private List<PrWarehouseQtyDto> warehouses;
    private String productRequirementsIds;


}
