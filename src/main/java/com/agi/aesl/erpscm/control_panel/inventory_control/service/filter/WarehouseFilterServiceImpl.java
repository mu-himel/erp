package com.agi.aesl.erpscm.control_panel.inventory_control.service.filter;

import com.agi.aesl.erpscm.common.DataFilterService;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseQuery;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseFilterServiceImpl implements WarehouseFilterService, DataFilterService {

    private String name;

    private final WarehouseRepository warehouseRepository;

    @Override
    public Page<WarehouseQuery.WarehouseInfo> getFilteredData(List<Long> ids, Pageable pageable) {
        return warehouseRepository.findAllByNameAndId(ids,name,pageable);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
