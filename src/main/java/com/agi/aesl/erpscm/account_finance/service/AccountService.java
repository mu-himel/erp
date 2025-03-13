package com.agi.aesl.erpscm.account_finance.service;

import com.agi.aesl.erpscm.account_finance.dto.request.LedgerAccountRequestDto;
import com.agi.aesl.erpscm.account_finance.repository.AccountQuery;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.Optional;

public interface AccountService extends VerificationDomainService {
    String getNextAccountNo();

    Page<AccountQuery.PendingAccount> getAllPendingAccounts(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                            Optional<Long> warehouseId);
    Page<AccountQuery.PendingAccount> getClosedAccounts(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<Long> warehouseId);

    Page<AccountQuery.PendingAccount> getAllPendingVerifications(Jwt token,
                                             Optional<Integer> page, Optional<Integer> size,
                                             Optional<String> fromDate, Optional<String> toDate
    );
    Page<AccountQuery.PendingAccount> getAllPendingApprovals(Jwt token,
                                         Optional<Integer> page, Optional<Integer> size,
                                         Optional<String> fromDate, Optional<String> toDate

    );

    Optional<Map<String,Object>> getLedgerDetailById(Long id);

    Page<AccountQuery.PendingAccount> getAllApprovedAccounts(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                   Optional<Long> warehouseId);

    Page<AccountQuery.PendingAccount> getAllRejectedAccounts(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                   Optional<Long> warehouseId);

    void updateAccount(Jwt token, String uri, Long id, LedgerAccountRequestDto ledgerAccountRequestDto);

    void createItemLedger(Item item, Warehouse warehouse, WarehouseStore warehouseStore);
    void createItemLedger(ClaimResolver claimResolver, Item item, Warehouse warehouse, WarehouseStore warehouseStore);

    void review(Jwt token, Long id, ReviewDto reviewDto);

    void setItemService(ItemService itemService);
}
