package com.agi.aesl.erpscm.erpn_integration.service;

import com.agi.aesl.erpscm.account_finance.dto.request.RemoteLedgerAccountDto;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import org.springframework.security.oauth2.jwt.Jwt;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;

public interface IntegrationWriterService {
    void createWarehouse(Jwt token, Warehouse warehouse);
    void updateWarehouse(Jwt token, String oldName, Warehouse warehouse);
    void deleteWarehouse(Jwt token, String warehouseName);

    void createLedgerItem(Jwt token, LedgerAccount ledgerAccount);
}
