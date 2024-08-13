package com.agi.aesl.erpscm.control_panel.inventory_control.service;

import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.common.DataFilterService;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseFilterServiceImpl implements WarehouseFilterService, DataFilterService {

    private String name;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    public Page<?> getFilteredData(List<Long> ids, Pageable pageable) {
        return warehouseRepository.findAllByNameAndId(ids,name,pageable);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
