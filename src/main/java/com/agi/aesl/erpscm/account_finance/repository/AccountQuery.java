package com.agi.aesl.erpscm.account_finance.repository;

import com.agi.aesl.erpscm.common.IResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public interface AccountQuery {

    String COUNT_INIT="SELECT COUNT (*) FROM (";
    String COUNT_END=") as total ";

    String GET_PENDING_ACCOUNTS = """
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
            WHERE
            (COALESCE(:warehouseId) IS NULL OR ws.warehouse_id IN (:warehouseId)) AND 
            (COALESCE(:warehouseId) IS NULL OR la.warehouse_id IN (:warehouseId)) AND
            la.account_status IN ('PENDING','PENDING_VERIFICATION','PENDING_APPROVAL','REVIEW')
            GROUP BY la.id
            """;

    String COUNT_PENDING_ACCOUNTS = COUNT_INIT+GET_PENDING_ACCOUNTS+COUNT_END;

    String GET_CLOSED_ACCOUNTS = """
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
            AND cws.warehouse_id = la.warehouse_id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            WHERE
            (COALESCE(:warehouseId) IS NULL OR ws.warehouse_id IN (:warehouseId)) AND 
            (COALESCE(:warehouseId) IS NULL OR la.warehouse_id IN (:warehouseId)) AND
            la.account_status IN ('APPROVED','REJECTED','VERIFIED','COMPLETED')
            GROUP BY la.id
            """;

    String COUNT_CLOSE_ACCOUNTS = COUNT_INIT+GET_CLOSED_ACCOUNTS+COUNT_END;

    //accounts query
    String LEDGER_ACC_DETAIL = """
        SELECT
        la.id,
        la.account_no as accountNo,
        la.master_account as masterAccount,
        la.sub_group_account as subGroupAccount,
        la.opening_credit_amount as openingCreditAmount,
        la.opening_date as openingDate,
        la.opening_debit_amount as openingDebitAmount,
        la.opening_stock as openingStock,
        la.unit as unit,
        ws.store_name as store,
        ipc.id as categoryId,
        ipc.name as category,
        ic.id as subCategoryId,
        ic.name as subCategory,
        i.item_attribute_name as product,
        cb.name as brand,
        la.group_account as groupAccount,
        la.account_status as accountStatus,
        la.review_prev_status as prevStatus,
        la.reviewer_id as reviewerId,
        la.review_date as reviewDate,
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
        LEFT JOIN scm_category_brands cb ON cb.id = i.brand_id
        LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
        LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
        LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
                    AND cws.warehouse_id = la.warehouse_id
        LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
                    AND ws.warehouse_id = la.warehouse_id
        LEFT JOIN acl_users au ON au.id = la.requested_by_id
        WHERE la.id=:id
        GROUP BY la.id
        """;

    String GET_APPROVED_ACCOUNTS = """
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
            WHERE
            (COALESCE(:warehouseId) IS NULL OR ws.warehouse_id IN (:warehouseId)) AND 
            (COALESCE(:warehouseId) IS NULL OR la.warehouse_id IN (:warehouseId)) AND 
            la.account_status IN ('APPROVED','COMPLETED')
            GROUP BY la.id
            """;
    String COUNT_APPROVED_ACCOUNTS = COUNT_INIT+GET_APPROVED_ACCOUNTS+") total";
    String GET_REJECTED_ACCOUNTS = """
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
            WHERE
            (COALESCE(:warehouseId) IS NULL OR ws.warehouse_id IN (:warehouseId)) AND 
            (COALESCE(:warehouseId) IS NULL OR la.warehouse_id IN (:warehouseId)) AND
            la.account_status IN ('REJECTED')
            """;
    String COUNT_REJECTED_ACCOUNTS = COUNT_INIT+GET_REJECTED_ACCOUNTS+") total";

    String GET_ALL_FILTERED_PV = """
            SELECT
                la.id,
                la.account_no accountNo,
                ws.store_name as store,
                ipc.name as category,
                ic.name as subCategory,
                i.item_attribute_name as product,
                la.group_account as groupAccount,
                la.account_status as accountStatus,
                (SELECT MAX(account_status) FROM `ledger_accounts_verification_approval_histories` 
                    where `employee_id` = :nextVerifierId AND `ledger_account_id`=la.id AND account_status='VERIFIED') as actionStatus
            FROM ledger_accounts la
            LEFT JOIN scm_items i ON i.id = la.item_id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            LEFT JOIN ledger_accounts_verification_approval_histories lavah ON lavah.ledger_account_id = la.id 
            WHERE ((la.next_verifier_id = :nextVerifierId AND la.account_status IN (:accountStatuses))
            OR (lavah.employee_id=:nextVerifierId AND lavah.account_status='VERIFIED'))
            AND (:fromDate IS NULL OR (la.created_at BETWEEN :fromDate AND :toDate))
            GROUP BY la.id
            ORDER BY CASE WHEN la.account_status IN ('REVIEW','PENDING_VERIFICATION') THEN 1 ELSE 2 END ASC
            """;
    String GET_ALL_FILTERED_PV_WITH_NEXT_VERIFIER = """
            SELECT
                la.id,
                la.account_no accountNo,
                ws.store_name as store,
                ipc.name as category,
                ic.name as subCategory,
                i.item_attribute_name as product,
                la.group_account as groupAccount,
                la.account_status as accountStatus,
                (SELECT MAX(account_status) FROM `ledger_accounts_verification_approval_histories` 
                    where `employee_id` = :nextVerifierId AND `ledger_account_id`=la.id AND account_status='VERIFIED') as actionStatus
            FROM ledger_accounts la
            LEFT JOIN scm_items i ON i.id = la.item_id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            LEFT JOIN ledger_accounts_verification_approval_histories lavah ON lavah.ledger_account_id = la.id 
            WHERE (ic.id IN (:categoryIds) OR pc.id IN (:categoryIds)) 
            AND ((la.next_verifier_id = :nextVerifierId AND la.account_status IN (:accountStatuses))
            OR (lavah.employee_id=:nextVerifierId AND lavah.account_status='VERIFIED'))
            AND (:fromDate IS NULL OR (la.created_at BETWEEN :fromDate AND :toDate))
            GROUP BY la.id
            ORDER BY CASE WHEN la.account_status IN ('REVIEW','PENDING_VERIFICATION') THEN 1 ELSE 2 END ASC
            """;

    String COUNT_ALL_FILTERED_PV = COUNT_INIT+GET_ALL_FILTERED_PV+COUNT_END;
    String COUNT_ALL_FILTERED_PV_WITH_NEXT_VERIFIER = COUNT_INIT+GET_ALL_FILTERED_PV_WITH_NEXT_VERIFIER+COUNT_END;


    String GET_ALL_FILTERED_PA = """
            SELECT
                la.id,
                la.account_no accountNo,
                ws.store_name as store,
                ipc.name as category,
                ic.name as subCategory,
                i.item_attribute_name as product,
                la.group_account as groupAccount,
                la.account_status as accountStatus,
                (SELECT MAX(account_status) FROM `ledger_accounts_verification_approval_histories` 
                    where `employee_id` = :nextApproverId AND `ledger_account_id`=la.id AND account_status='APPROVED') as actionStatus
            FROM ledger_accounts la
            LEFT JOIN scm_items i ON i.id = la.item_id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            LEFT JOIN ledger_accounts_verification_approval_histories lavah ON lavah.ledger_account_id = la.id 
            WHERE ((la.next_approver_id = :nextApproverId AND la.account_status IN (:accountStatuses))
            OR (lavah.employee_id=:nextApproverId AND lavah.account_status='APPROVED'))
            AND (:fromDate IS NULL OR (la.created_at BETWEEN :fromDate AND :toDate))
            GROUP BY la.id
            ORDER BY CASE WHEN la.account_status IN ('REVIEW','PENDING_APPROVAL') THEN 1 ELSE 2 END ASC
            """;
    String GET_ALL_FILTERED_PA_WITH_NEXT_APPROVER = """
            SELECT
                la.id,
                la.account_no accountNo,
                ws.store_name as store,
                ipc.name as category,
                ic.name as subCategory,
                i.item_attribute_name as product,
                la.group_account as groupAccount,
                la.account_status as accountStatus,
                (SELECT MAX(account_status) FROM `ledger_accounts_verification_approval_histories` 
                    where `employee_id` = :nextApproverId AND `ledger_account_id`=la.id AND account_status='APPROVED') as actionStatus
            FROM ledger_accounts la
            LEFT JOIN scm_items i ON i.id = la.item_id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            LEFT JOIN ledger_accounts_verification_approval_histories lavah ON lavah.ledger_account_id = la.id 
            WHERE (ic.id IN (:categoryIds) OR pc.id IN (:categoryIds)) 
            AND ((la.next_approver_id = :nextApproverId AND la.account_status IN (:accountStatuses))
            OR (lavah.employee_id=:nextApproverId AND lavah.account_status='APPROVED'))
            AND (:fromDate IS NULL OR (la.created_at BETWEEN :fromDate AND :toDate))
            GROUP BY la.id
            ORDER BY CASE WHEN la.account_status IN ('REVIEW','PENDING_APPROVAL') THEN 1 ELSE 2 END ASC
            """;

    String COUNT_ALL_FILTERED_PA = COUNT_INIT+GET_ALL_FILTERED_PA+COUNT_END;
    String COUNT_ALL_FILTERED_PA_WITH_NEXT_APPROVER = COUNT_INIT+GET_ALL_FILTERED_PA_WITH_NEXT_APPROVER+COUNT_END;


    interface PendingAccount extends IResponse {
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
        String getActionStatus();
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
        String getPrevStatus();
        String getInitiatorDepartment();
        String getInitiatorDesignation();
        @JsonIgnore
        Long getInitiatorWarehouseId();

        @JsonIgnore
        String getInitiatorWarehouseName();

        String getBrand();

        String getReviewerId();
        String getReviewDate();

        BigDecimal getOpeningStock();
        String getUnit();

        default String setInitiatorWarehouseName(String location){
            return this.getInitiatorWarehouseName() + " " + location;
        }
    }
}
