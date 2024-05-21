package com.agi.aesl.erpscm.inventory.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncItemDto {
    List<SyncItemDetail> items;
}
