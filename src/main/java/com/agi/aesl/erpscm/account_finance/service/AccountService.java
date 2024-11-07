package com.agi.aesl.erpscm.account_finance.service;

import com.agi.aesl.erpscm.account_finance.dto.request.LedgerAccountRequestDto;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface AccountService extends VerificationDomainService {
    String getNextAccountNo();

    Page<?> getAllPendingAccounts(Jwt token,Optional<Integer> page, Optional<Integer> size,
                                  Optional<Long> warehouseId);
    Page<?> getClosedAccounts(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<Long> warehouseId);

    Page<?> getAllPendingVerifications(Jwt token,
                                             Optional<Integer> page, Optional<Integer> size,
                                             Optional<String> fromDate, Optional<String> toDate
    );
    Page<?> getAllPendingApprovals(Jwt token,
                                         Optional<Integer> page, Optional<Integer> size,
                                         Optional<String> fromDate, Optional<String> toDate

    );

    Optional<?> getLedgerDetailById(Long id);

    Page<?> getAllApprovedAccounts(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                   Optional<Long> warehouseId);

    Page<?> getAllRejectedAccounts(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                   Optional<Long> warehouseId);

    void updateAccount(Jwt token, String uri, Long id, LedgerAccountRequestDto ledgerAccountRequestDto);

    void createItemLedger(Item item, Warehouse warehouse, WarehouseStore warehouseStore);
    void createItemLedger(ClaimResolver claimResolver, Item item, Warehouse warehouse, WarehouseStore warehouseStore);

    void review(Jwt token, Long id, ReviewDto reviewDto);

    void setItemService(ItemService itemService);
}
