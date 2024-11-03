package com.agi.aesl.erpscm.account_finance.dto.request;

import lombok.Data;

@Data
public class LedgerInitiatorDto {
    private String employeeId;
    private String employeeName;
    private String employeeDesignation;
    private String employeeDepartment;
    private String reportingManager;
    private String employeeWarehouse;
    private String warehouseLocation;
}
