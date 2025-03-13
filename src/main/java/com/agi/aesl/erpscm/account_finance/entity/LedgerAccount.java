package com.agi.aesl.erpscm.account_finance.entity;

import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "ledger_accounts")
public class LedgerAccount extends VerifyableEntity {



    private String accountNo;

    @ManyToOne
    private Item item;

    private String masterAccount;
    private String groupAccount;
    private String subGroupAccount;
    private String store;
    private BigDecimal openingCreditAmount;
    private BigDecimal openingDebitAmount;

    @Enumerated(EnumType.STRING)
    private AccountType accountStatus;

    @Enumerated(EnumType.STRING)
    private AccountType reviewPrevStatus;

    @ManyToOne
    private Warehouse warehouse;

    @ManyToOne
    private WarehouseStore warehouseStore;

    private BigDecimal openingStock;
    private String unit;

    @ManyToOne
    private Employee requestedBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate openingDate;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Override
    public void setStatus(String status) {
        this.accountStatus = AccountType.valueOf(status);
    }


}
