package com.agi.aesl.erpscm.account_finance.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public interface AccountQuery {

    String getPendingAccounts = """
            SELECT 
                la.id,
                la.account_no accountNo,
                ws.store_name as store,
                ipc.name as category,
                ic.name as subCategory,
                i.item_attribute_name as product,
                la.group_account as groupAccount,
                la.account_status as accountStatus
            FROM ledger_accounts la
            LEFT JOIN scm_items i ON i.id = la.item_id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            WHERE la.account_status IN ('PENDING','PENDING_VERIFICATION','PENDING_APPROVAL')
            """;

    String countPendingAccounts = "SELECT COUNT(*) FROM ("+getPendingAccounts+") total";

    //accounts query
    String ledgerAccDetail = """
        SELECT 
        la.id,
        la.account_no as accountNo,
        la.master_account as masterAccount,
        la.sub_group_account as subGroupAccount,
        la.opening_credit_amount as openingCreditAmount,
        la.opening_date as openingDate,
        la.opening_debit_amount as openingDebitAmount,
        ws.store_name as store,
        ipc.id as categoryId,
        ipc.name as category,
        ic.id as subCategoryId,
        ic.name as subCategory,
        i.item_attribute_name as product,
        la.group_account as groupAccount,
        la.account_status as accountStatus,
        la.store as storeInfo,
        au.id as initiatorId,
        au.employee_id as initiatorEmployeeId,
        au.employee_name as initiatorName,
        au.department_name as initiatorDepartment,
        au.designation_name as initiatorDesignation,
        au.warehouse_id as initiatorWarehouseId,
        au.warehouse_name as initiatorWarehouseName
        FROM ledger_accounts la
        LEFT JOIN scm_items i ON i.id = la.item_id
        LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
        LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
        LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
        LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
        LEFT JOIN acl_users au ON au.id = la.requested_by_id
        WHERE la.id=:id
            """;

    String getApprovedAccountsList = """
        SELECT 
                la.id,
                la.account_no accountNo,
                ws.store_name as store,
                ipc.name as category,
                ic.name as subCategory,
                i.item_attribute_name as product,
                la.group_account as groupAccount,
                la.account_status as accountStatus,
                la.store as storeInfo
            FROM ledger_accounts la
            LEFT JOIN scm_items i ON i.id = la.item_id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            WHERE la.account_status IN ('APPROVED')
            """;
    String countApprovedAccounts = "SELECT COUNT(*) FROM ("+getApprovedAccountsList+") total";
    String getRejectedAccountsList = """
        SELECT 
                la.id,
                la.account_no accountNo,
                ws.store_name as store,
                ipc.id as categoryId,
                ipc.name as category,
                ic.name as subCategory,
                ic.id as subCategoryId,
                la.store as storeInfo,
                i.item_attribute_name as product,
                la.group_account as groupAccount,
                la.account_status as accountStatus,
                la.store as storeInfo
            FROM ledger_accounts la
            LEFT JOIN scm_items i ON i.id = la.item_id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            WHERE la.account_status IN ('REJECTED')
            """;
    String countRejectedAccounts = "SELECT COUNT(*) FROM ("+getRejectedAccountsList+") total";

    String getAllFilteredPendingVerifications = """
            SELECT 
                la.id,
                la.account_no accountNo,
                ws.store_name as store,
                ipc.name as category,
                ic.name as subCategory,
                i.item_attribute_name as product,
                la.group_account as groupAccount,
                la.account_status as accountStatus
            FROM ledger_accounts la
            LEFT JOIN scm_items i ON i.id = la.item_id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            WHERE la.next_verifier_id = :nextVerifierId AND la.account_status IN (:accountStatuses)
            AND (:fromDate IS NULL OR (la.created_at BETWEEN :fromDate AND :toDate))
            """;
    String getAllFilteredPendingVerificationsWithNextVerifier = """
            SELECT 
                la.id,
                la.account_no accountNo,
                ws.store_name as store,
                ipc.name as category,
                ic.name as subCategory,
                i.item_attribute_name as product,
                la.group_account as groupAccount,
                la.account_status as accountStatus
            FROM ledger_accounts la
            LEFT JOIN scm_items i ON i.id = la.item_id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            WHERE (ic.id IN (:categoryIds) OR pc.id IN (:categoryIds)) 
            AND la.next_verifier_id = :nextVerifierId AND la.account_status IN (:accountStatuses)
            AND (:fromDate IS NULL OR (la.created_at BETWEEN :fromDate AND :toDate))
            """;

    String countAllFilteredPendingVerifications = "SELECT COUNT(*) FROM ("+getAllFilteredPendingVerifications+") as total";
    String countAllFilteredPendingVerificationsWithNextVerifier = "SELECT COUNT(*) FROM ("+getAllFilteredPendingVerificationsWithNextVerifier+") as total";


    String getAllFilteredPendingApprovals = """
            SELECT 
                la.id,
                la.account_no accountNo,
                ws.store_name as store,
                ipc.name as category,
                ic.name as subCategory,
                i.item_attribute_name as product,
                la.group_account as groupAccount,
                la.account_status as accountStatus
            FROM ledger_accounts la
            LEFT JOIN scm_items i ON i.id = la.item_id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            WHERE la.next_approver_id = :nextApproverId AND la.account_status IN (:accountStatuses)
            AND (:fromDate IS NULL OR (la.created_at BETWEEN :fromDate AND :toDate))
            """;
    String getAllFilteredPendingApprovalsWithNextApprover = """
            SELECT 
                la.id,
                la.account_no accountNo,
                ws.store_name as store,
                ipc.name as category,
                ic.name as subCategory,
                i.item_attribute_name as product,
                la.group_account as groupAccount,
                la.account_status as accountStatus
            FROM ledger_accounts la
            LEFT JOIN scm_items i ON i.id = la.item_id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            WHERE (ic.id IN (:categoryIds) OR pc.id IN (:categoryIds)) 
            AND la.next_approver_id = :nextApproverId AND la.account_status IN (:accountStatuses)
            AND (:fromDate IS NULL OR (la.created_at BETWEEN :fromDate AND :toDate))
            """;

    String countAllFilteredPendingApprovals = "SELECT COUNT(*) FROM ("+getAllFilteredPendingApprovals+") as total";
    String countAllFilteredPendingApprovalsWithNextApprover = "SELECT COUNT(*) FROM ("+getAllFilteredPendingApprovalsWithNextApprover+") as total";


    interface PendingAccount{
        Long getId();
        String getAccountNo();
        String getStore();
        Long getCategoryId();
        String getCategory();
        Long getSubCategoryId();
        String getSubCategory();
        String getProduct();
        String getGroupAccount();
        String getAccountStatus();
        String getStoreInfo();
        
    }

    /**
     * PendingAccountDetail
     */
    public interface PendingAccountDetail extends PendingAccount {

        String getMasterAccount();
        String getSubGroupAccount();
        BigDecimal getOpeningCreditAmount();
        String getOpeningDate();
        BigDecimal getOpeningDebitAmount();
        String getInitiatorId();
        String getInitiatorEmployeeId();
        String getInitiatorName();
        String getInitiatorDepartment();
        String getInitiatorDesignation();
        @JsonIgnore
        Long getInitiatorWarehouseId();

        @JsonIgnore
        String getInitiatorWarehouseName();

        default String setInitiatorWarehouseName(String location){
            return this.getInitiatorWarehouseName() + " " + location;
        }
    }
}
