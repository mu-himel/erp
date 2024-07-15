package com.agi.aesl.erpscm.account_finance.entity;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ledger_accounts")
public class LedgerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNo;

    @ManyToOne
    private Item item;

    private String masterAccount;
    private String groupAccount;
    private String subGroupAccount;
    private BigDecimal openingCreditAmount;
    private BigDecimal openingDebitAmount;

    @ManyToOne
    private Employee requestedBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate openingDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;


}
