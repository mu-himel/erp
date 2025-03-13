package com.agi.aesl.erpscm.common;

import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DataFilterService {

    Page<WarehouseQuery.WarehouseInfo> getFilteredData(List<Long> ids, Pageable pageable);
}
