package com.agi.aesl.erpscm.control_panel.inventory_control.service.filter;


import com.agi.aesl.erpscm.common.DataFilterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WarehouseFilterService extends DataFilterService {

    void setName(String name);
}
