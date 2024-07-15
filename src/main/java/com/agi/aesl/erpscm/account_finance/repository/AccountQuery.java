package com.agi.aesl.erpscm.account_finance.repository;

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
            WHERE la.account_status IN ('PENDING')
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
                la.account_status as accountStatus
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
            WHERE la.account_status IN ('REJECTED')
            """;
    String countRejectedAccounts = "SELECT COUNT(*) FROM ("+getRejectedAccountsList+") total";

    interface PendingAccount{
        Long getId();
        String getAccountNo();
        String getStore();
        String getCategory();
        String getSubCategory();
        String getProduct();
        String getGroupAccount();
        String getAccountStatus();
        
    }

    /**
     * PendingAccountDetail
     */
    public interface PendingAccountDetail extends PendingAccount {
        String getMasterAccount();
        String getSubGroupAccount();
        int getOpeningCreditAmount();
        String getOpeningDate();
        int getOpeningDebitAmount();
    }
}
